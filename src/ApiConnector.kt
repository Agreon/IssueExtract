import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.httpGet

import khttp.*;
import java.io.File
import kotlin.concurrent.thread


/**
 * Created by root on 23.05.17.
 */

/**
 * eufrasia-augentropfen
 * Git-Issue: make origin selectable
 * Git-Issue: Remove Issues from repo, when not in files
 */
class ApiConnector(){

    val baseAddress = "https://api.github.com"

    var onlineIssues: ArrayList<Issue> = ArrayList()

    var repoOwner: String = ""
    var repoName: String = ""

    /**
     * Git-Issue: Add Exception Handling
     */
    init {

        val configFile = File(".git/config")
        var urlLine = configFile.readLines().filter { line -> line.contains("url") }[0]
        urlLine = urlLine.split(':')[1]
        val tuple = urlLine.split('/')
        val repoName = tuple[1].split(".")[0]

        this.repoOwner = tuple[0]
        this.repoName = repoName

        println("User: "+tuple[0])
        println("repo: "+repoName)

        getIssuesForRepo()
    }


    /**
     * Git-Issue: Add Exception handling
     */
    fun getIssuesForRepo(){
        println("Getting Issues...")


            var issues = get(baseAddress+"/repos/"+repoOwner+"/"+repoName+"/issues").jsonArray

            println("Got Issues: " + issues.length())

            for(issue in issues){
                println(issue.toString())



                    val issueObj = org.json.JSONObject(issue.toString())

                    val title = issueObj.getString("title")

                    var body = ""
                    try {
                        body = issueObj.getString("body")
                    } catch (e: Exception){
                        body = ""
                    }

                    val newIssue = Issue(title, body)

                    val labels = issueObj.getJSONArray("labels")

                    for (label in labels) {
                        val labelObj = org.json.JSONObject(label)
                        println(labelObj.toString())
                        //newIssue.addlabel()
                    }

                    onlineIssues.add(newIssue)

            }


    }

    /**
     * Adds an Issue
     */
    fun postIssue(issue: Issue){

        println("Posting Issue "+issue.title)

        if(onlineIssues.find({ i -> i.title == issue.title }) != null){
            println("Got Issue already up")
            return
        }

        val params = org.json.JSONObject()
        params.put("title",issue.title)
        if(issue.body.isNotEmpty()){
            params.put("body",issue.body)
        }

        val headers = mapOf<String,String>("Authorization" to "token ebaf1a3647a4ea78dcd51d942741c7e54eb77c02")
        val paramsMap = mapOf<String,String>()

        val resp = post(baseAddress+"/repos/"+repoOwner+"/"+repoName+"/issues", headers, paramsMap, params).text
        println("Posted "+resp);
    }
}