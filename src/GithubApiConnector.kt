import models.Issue
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.*

class GithubApiConnector() : ApiConnector {

    val httpManager: HttpManager = HttpManager()

    val baseAddress = "https://api.github.com"

    val onlineIssues: ArrayList<Issue> = ArrayList()

    val closedIssues: ArrayList<Issue> = ArrayList()

    var repoOwner: String = ""
    var repoName: String = ""

    init {
        try {
            val configFile = File("config.ie")
            val remoteLines = configFile.readLines().filter{ line -> line.contains("remote") }

            var remote = "origin"
            if(remoteLines.count() != 0){
                remote = remoteLines[0].split(" ")[1]
            }

            val gitConfig = File(".git/config").readLines()

            var urlIndex = -1

            for(i in gitConfig.indices) {
                if(gitConfig[i].contains(remote)) {
                    urlIndex = i + 1
                    break
                }
            }

            var urlLine = gitConfig[urlIndex]
            urlLine = urlLine.split(':')[1]
            val tuple = urlLine.split('/')

            // SSH
            if(tuple.count() < 3){
                this.repoOwner = tuple[0]
                this.repoName = tuple[1].split(".")[0]
            } else {
                this.repoOwner = tuple[3]
                this.repoName = tuple[4].split(".")[0]
            }

            println("Owner: " + this.repoOwner)
            println("Repository: " + this.repoName)

            getIssuesFromRepo()
        } catch (e: Exception){
            e.printStackTrace()
            throw Exception("[Error]: Git-Config corrupted!")
        }
    }

    /**
     * Gets all Issues from the repository
     * Git-Issue[182]: Is it good to get the closed ones?
     */
    private fun getIssuesFromRepo() {
        println("Getting Issues...")

        var currentPage = 1
        var allIssues = 0

        while(true){
            val response = httpManager.httpGet("$baseAddress/repos/$repoOwner/$repoName/issues", mapOf("state" to "all", "page" to currentPage.toString(), "per_page" to "100"))

            if(response.jsonArray.length() == 0){
                break
            }

            allIssues += response.jsonArray.length()
            currentPage++

            addIssues(response.jsonArray)
        }

        println("Got "+allIssues+" Issues")
    }

    private fun addIssues(issues: JSONArray){
        for (issue in issues) {

            val issueObj = JSONObject(issue.toString())

            val title = issueObj.getString("title")
            val number = issueObj.getInt("number")

            var body: String
            try {
                body = ""
                var lines = issueObj.getString("body").split("\n")

                // Remove Body-end with reference to file
                for(i in 0 until lines.indices.count() - 2){
                    body += lines[i]
                    if(i < lines.indices.count() - 3){
                        body += "\n"
                    }
                }

            } catch (e: Exception) {
                body = ""
            }

            val newIssue = Issue(title, body, 0, "", "0")
            newIssue.number = number.toString()

            val labels = issueObj.getJSONArray("labels")

            for (label in labels) {
                val labelObj = JSONObject(label.toString())
                newIssue.addlabel(labelObj.getString("name"))
            }

            if(issueObj.getString("state") == "closed"){
                closedIssues.add(newIssue)
            } else {
                onlineIssues.add(newIssue)
            }
        }
    }


    /**
     * Adds an Issue
     */
    override fun postIssue(issue: Issue): JSONObject {

        println("Found new Issue: " + issue.title)

        if (onlineIssues.find({ i -> i.title == issue.title }) != null) {
            return JSONObject()
        }

        val params = org.json.JSONObject()
        params.put("title", issue.title)

        var body = ""
        if(issue.body.isNotEmpty()){
            body = issue.body
        }

        body += "\n\n File: [${issue.file}:${issue.line}](https://github.com/$repoOwner/$repoName/blob/master/${issue.file}#L${issue.line})"
        params.put("body", body)

        val labels = JSONArray()
        for(label in issue.labels){
                labels.put(label)
        }
        params.put("labels", labels)

        return httpManager.httpPost("$baseAddress/repos/$repoOwner/$repoName/issues", params).jsonObject
    }

    /**
     * Updates Issues online if they were updated
     */
    override fun updateIfNecessary(issue: Issue) {
        var onlineIssue = getIssue(issue)

        if(onlineIssue == null){
            return
        }

        val diff = onlineIssue.getDifference(issue)

        if(diff.length() == 0){
            return
        }

        if(diff.has("body")){
            diff.put("body",diff.getString("body") + "\n\n File: [${issue.file}:${issue.line}](https://github.com/$repoOwner/$repoName/blob/master/${issue.file}#L${issue.line})")
        }

        println("Updating Issue #"+issue.number)

        updateIssue(onlineIssue.number, diff)
    }


    private fun updateIssue(number: String, issue: JSONObject) {
        httpManager.httpPatch("$baseAddress/repos/$repoOwner/$repoName/issues/${number}", issue)
    }

    override fun removeUnused(allFound: ArrayList<Issue>) {
        onlineIssues
                .filter { i -> allFound.find { j ->  j.number == i.number } == null}
                .forEach { i ->
                    println("Removing #"+i.number+": "+i.title)

                    val params = org.json.JSONObject()
                    params.put("state","closed")

                    httpManager.httpPatch("$baseAddress/repos/$repoOwner/$repoName/issues/${i.number}", params)
                }
    }

    override fun getIssue(issue: Issue): Issue? {
        if(issue.number != ""){
            return onlineIssues.find({ i -> i.number == issue.number})
        } else {
            return onlineIssues.find({ i -> i.title == issue.title })
        }
    }

    override fun setAuthToken(token: String){
        httpManager.setAuthToken(token)
    }

}
