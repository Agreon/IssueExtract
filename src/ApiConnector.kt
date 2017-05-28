import khttp.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.*


/**
 * Git-Issue[148]: {
 * When no connection to internet is possible - Abort
 * >>  <<
 * [ development ]
 * }
 */
/**
 * Git-Issue[149]: make origin selectable
 */
class ApiConnector() {

    var authToken: String = ""
    val baseAddress = "https://api.github.com"

    var onlineIssues: ArrayList<Issue> = ArrayList()

    var repoOwner: String = ""
    var repoName: String = ""

    init {
        try {
            val configFile = File(".git/config")
            var urlLine = configFile.readLines().filter { line -> line.contains("url") }[0]
            urlLine = urlLine.split(':')[1]
            val tuple = urlLine.split('/')
            val repoName = tuple[1].split(".")[0]

            this.repoOwner = tuple[0]
            this.repoName = repoName

            println("User: " + tuple[0])
            println("Repository: " + repoName)

            getIssuesFromRepo()
        } catch (e: Exception){
            e.printStackTrace()
            throw Exception("[Error]: Git-Config corrupted!")
        }
    }

    /**
     * Git-Issue[151]: Check differences between local and online-issues >> Best would be to check if the numbers are the same, (if only the title changed), so the old ones don't have to be deleted. <<
     */
    fun getIssuesFromRepo() {
        println("Getting Issues...")

        var issues = get("$baseAddress/repos/$repoOwner/$repoName/issues").jsonArray

        println("Got Issues: " + issues.length())

        for (issue in issues) {
            val issueObj = JSONObject(issue.toString())

            val title = issueObj.getString("title")
            val number = issueObj.getInt("number")

            var body: String
            try {
                body = issueObj.getString("body")
            } catch (e: Exception) {
                body = ""
            }

            val newIssue = Issue(title, body, 0, "", number.toString())

            val labels = issueObj.getJSONArray("labels")

            for (label in labels) {
                val labelObj = JSONObject(label.toString())
                newIssue.addlabel(labelObj.getString("name"))
            }

            onlineIssues.add(newIssue)
        }
    }

    /**
     * Adds an Issue
     */
    fun postIssue(issue: Issue): JSONObject {

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

        body += "\n\n File: [${issue.file}](https://github.com/$repoOwner/$repoName/blob/master/${issue.file}#L${issue.line})"
        params.put("body", body)

        val labels = JSONArray()
        for(label in issue.labels){
                labels.put(label)
        }
        params.put("labels", labels)

        val headers = mapOf<String, String>("Authorization" to authToken)

        val resp = post("$baseAddress/repos/$repoOwner/$repoName/issues", headers, mapOf(), params).text

        val retObj = JSONObject(resp)

        if(retObj.has("message")){
            throw Exception("[Error]: Could not connect! "+retObj.getString("message"))
        } else {
            return retObj
        }
    }


    fun removeUnused(allFound: ArrayList<Issue>){

        val headers = mapOf<String, String>("Authorization" to authToken)

        onlineIssues
                .filter { i -> allFound.find { j ->  j.title == i.title } == null}
                .forEach { i ->
                    println("Removing #"+i.number+": "+i.title)

                    val params = org.json.JSONObject()
                    params.put("state","closed")

                    val resp = patch("$baseAddress/repos/$repoOwner/$repoName/issues/${i.number}", headers, mapOf(), params).text
                }
    }


    fun getIssue(issue: Issue): Issue? {
        if(issue.number != ""){
            return onlineIssues.find({ i -> i.number == issue.number})
        } else {
            return onlineIssues.find({ i -> i.title == issue.title })
        }
    }

}
