import java.io.File

/**
 * Created by root on 23.05.17.
 */


/**
 *
 */
class Parser(){

    var issues: ArrayList<Issue> = ArrayList()

    var api: ApiConnector = ApiConnector()

    fun parseFile(file: File) {
        for (line in file.readLines()){
            if(line.contains("Git-Issue")){

                try {

                    val issueContent = line.split(":")[1]
                    val issueTitle = issueContent.split(">>")[0]

                    if(issueContent.split(">>").size > 1){
                        val issueBody = issueContent.split(">>")[1]
                        api.postIssue(Issue(issueTitle, issueBody))
                        return;
                    }

                    api.postIssue(Issue(issueTitle))
                }  catch (e: Exception) {
                    println("Error Parsing Git issue in file "+file.name)
                }
            }
        }
    }


    fun jumpUntil(line: String, search: String, start: Int = 0): Int {
        var i: Int = start
        var found: Int = 0

        while (i < line.length){

            found = 0
            for (j in  search.indices ){
                if(line[i+j] == search[j]){
                    found++
                } else {
                    break
                }
            }

            if(found == search.length){
                return i + found
            }

            i++
        }

        return -1
    }


}