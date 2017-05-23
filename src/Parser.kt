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

    var removeFromRemote: Boolean = true

    fun parseProject(root: File){
        traverse(root)

        println("Finished scan!")

        if(removeFromRemote){
            println("Removing deleted Issues..")
            api.removeUnused(issues)
        }
    }

    fun traverse(file: File){
        if(file.isDirectory){
            file.listFiles().forEach { f ->
                traverse(f)
            }
        } else {
            parseFile(file)
        }
    }


    // Git-Issue: Enable Multi-Line Parsing
    // Git-Issue: Add Number-Parsing
    // Git-Issue: Parse Label-Definition
    fun parseFile(file: File) {
        for (line in file.readLines()){
            if(line.contains("Git-Issue")){
               try {
                    val issueContent = line.split(":")[1]
                    val issueTitle = issueContent.split(">>")[0].removeSurrounding(" ")

                    if(issueContent.split(">>").size > 1){
                        val issueBody = issueContent.split(">>")[1].split("<<")[0]
                        foundIssue(Issue(issueTitle, issueBody.removeSurrounding(" ")))
                        continue
                    }

                   foundIssue(Issue(issueTitle))
                }  catch (e: Exception) {
                   println("Skipping "+line)
                   continue
               }
            }
        }
    }

    fun foundIssue(issue: Issue){

        issues.add(issue)

        val completeIssue = api.getIssue(issue)

        // Add Issue if not already online
        if(completeIssue == null){
            api.postIssue(issue)
            return
        }

        /**
         * Git-Issue: Write Number after title >> Have to write at specific position at specific file <<
         */

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