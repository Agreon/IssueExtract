import models.Issue
import models.TodoRef
import java.io.File

/**
 * Git-Issue[178]: {
 * Make it possible to reference Issues in TODO-Items
 * >> Just write the number after a TODO and it will be added to the body of the referenced Issue <<
 * [improvement]
 * }
 */
// TODO[#179]: Move Parser to separate class (not connected to api)
class Parser(){

    var api: ApiConnector = GithubApiConnector()

    var issues: ArrayList<Issue> = ArrayList()
    var newIssues: ArrayList<Issue> = ArrayList()
    var todos: HashMap<String, ArrayList<TodoRef>> = HashMap()

    // Parameters
    var removeFromRemote: Boolean = false
    var skipParseErrors: Boolean = true


    fun parseProject(root: File, userToken: String){
        api.setAuthToken(userToken)

        println("Starting Parser")

        traverse(root)

        println("Finished scan!")

        if(removeFromRemote){
            println("Removing deleted Issues..")
            api.removeUnused(issues)
        }

        updateRemote()
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

    /**
     * Parses a file and searchs for Git-Issues
     */
    fun parseFile(file: File){
        val lines = file.readLines()

        var state = 0
        var parseState = 0
        var currentIssueText = ""
        var issueLine = 0

        for(i in lines.indices){

            // Reference-Todos
            if(state == 0 && lines[i].contains("TODO[")){

                val issueRef = lines[i].split("[")[1].split("]")[0].removePrefix("#")
                val todoText = lines[i].split("]")[1].removePrefix(":")

                if(!todos.containsKey(issueRef)) {
                    todos.put(issueRef, ArrayList())
                }
                todos.get(issueRef)!!.add(TodoRef(issueRef, todoText))
            }

            // Parse-Start if Issue found
            if(state == 0 && lines[i].contains("Git-Issue")){

                issueLine = i

                // Multiline
                if(lines[i].contains("{")){
                    state = 1
                    parseState = 0
                    currentIssueText = lines[i]
                    continue
                }

                parseIssue(lines[i], issueLine+1, file)
                continue
            }

            // For multiline parsing: Addes the lines up until '}' or the comment ends
            if(state == 1) {
                for(j in lines[i].indices){
                    if(j < lines[i].length - 1){
                        // For skipping body-'}'
                        if(lines[i][j] == '>' && lines[i][j+1] == '>'){
                            parseState++
                            continue
                        }
                        if(lines[i][j] == '<' && lines[i][j+1] == '<'){
                            parseState--
                            continue
                        }
                    }

                    // Check if git-issue ended
                    if(parseState == 0 && lines[i][j] == '}'){
                        state = 0
                        currentIssueText += lines[i]
                        parseIssue(currentIssueText, issueLine+1, file)
                        break
                    }


                    // Fallback (Only if not in Body)
                    if(j < lines[i].length - 1 && lines[i][j] == '*'  && lines[i][j+1] == '/'){
                        println("Expected } for issue")
                        state = 0
                        parseState = 0
                        currentIssueText = ""
                        continue
                    }

                }
                // If end not found, add current line to whole issue
                if(state == 1){
                    currentIssueText += removeWhiteSpaceUntil(lines[i],'*')+ "\n"
                }
            }

        }

    }


    /**
     * Git-Issue[177]: Make Link-To-File dynamic
     */
    fun parseIssue(issueText: String, lineNumber: Int, file: File){

        try {
            /**
             * Git-Issue[#188]: Only works if there is no ; in text! [bug]
             */
            val issueContent = issueText.split(":")[1]
            var issueTitle = ""
            var issueBody = ""

            // If got body
            if(issueContent.split(">>").size > 1){
                issueTitle = issueContent.split(">>")[0].removeSurrounding(" ")
                issueBody = issueContent.split(">>")[1].split("<<")[0].removeSurrounding(" ")
            }  else {
                issueTitle = issueContent.split("[")[0]
            }

            issueTitle = removeStart(issueTitle, charArrayOf(' ','*','{'))
            issueTitle = removeEnd(issueTitle, charArrayOf(' ','*','}'))

            issueBody = removeStart(issueBody, charArrayOf('*','}','\n'))
            issueBody = removeEnd(issueBody, charArrayOf(' ','*','}','\n'))

            val issueFound = Issue(issueTitle, issueBody, lineNumber, file.path)

            val numberPart = issueText.split(":")[0].split("[")
            // Find Number, if already added
            if(numberPart.size > 1){
                var number = numberPart[1].removeSuffix("]").removePrefix("#")
                issueFound.number = number
            }

            // Find Labels
            val labels : List<String>
            if(issueBody.isEmpty()){
                labels = issueContent.split("[")
            } else {
                labels = issueContent.split(">>")[1].split("<<")[1].split("[")
            }

            // If got labels
            if(labels.size == 2 && labels[1].isNotEmpty()){
                val labelDef = labels[1].split(",") as ArrayList<String>

                // Remove trailing stuff from first label
                labelDef[0] = removeStart(labelDef[0], charArrayOf('\n',' ','['))

                // Remove trailing stuff from last label
                labelDef[labelDef.size - 1] = labelDef[labelDef.size - 1].split("]")[0]
                labelDef[labelDef.size - 1] = removeEnd(labelDef[labelDef.size - 1], charArrayOf(' '))

                for(label in labelDef){
                    if(label.removeSurrounding(" ").isNotEmpty())
                    issueFound.addlabel(label.removeSurrounding(" "))
                }
            }

          foundIssue(issueFound)
        } catch (e: Exception) {
            var errorText = "\u001B[31mError at ${file.path}:$lineNumber for ${issueText} \u001B[0m"

            if(skipParseErrors){
                println(errorText)
            } else {
                throw Exception(errorText)
            }
        }
    }

    /**
     * Checks if Issues is already in repository
     */
    fun foundIssue(issue: Issue){

        // Add Issue to list if not already online
        if(issue.number == ""){
            newIssues.add(issue)
        } else {
            issues.add(issue)
        }

    }

    /**
     * Writes new Issues to the server and updates numbers in files
     */
    fun updateRemote(){

        /**
         * Update existing issues
         */
        for(issue in issues){

            insertTodosInIssueBody(issue)

            api.updateIfNecessary(issue)
        }

        /**
         * Upload new Issues
         */
        val usedFiles: HashMap<String, File> = HashMap()

        for(issue in newIssues){
            val fullIssue = api.postIssue(issue)
            issue.number = fullIssue.getInt("number").toString()

            var file: File
            if (usedFiles.containsKey(issue.file)){
                file = usedFiles.get(issue.file) as File
            } else {
                file = File(issue.file)
            }

            writeIssueNumber(issue, file)
        }


    }

    /**
     * TODO[#178]: Insert referenced TODOS in issue body
     */
    private fun insertTodosInIssueBody(issue: Issue) {

        if(!todos.containsKey(issue.number)){
            return
        }

        var text = "#TODOS"

        for (todo in todos.get(issue.number)!!){
            text += "- ${todo.text}\n"
        }

        val b = 0

    }

    /**
     * Writes an issue number at the right position
     */
    fun writeIssueNumber(issue: Issue, file: File){

        val lines = file.readLines()

        var complete = ""
        var lineContent: String

        for(i in lines.indices){
            if(i == issue.line-1){
                val lineParts = lines[i].split(":")

                lineContent = lineParts[0] + "[#" + issue.number + "]:"

                for(j in 1 until lineParts.indices.count()){
                    lineContent += lineParts[j]
                }

            } else {
                lineContent = lines[i]
            }
            complete += lineContent + "\n"
        }

        file.writeText(complete)
    }

    /**
     * Util
     */

    fun removeStart(content: String, chars: CharArray): String {

        var skipAt = 0

        for(i in content.indices){
            if(!chars.contains(content[i])){
                skipAt = i
                break
            }
        }

        return content.substring(skipAt)
    }

    fun removeEnd(content: String, chars: CharArray): String {

        var skipAt = 0
        var i = content.length - 1

        while(i > 0){
            if(!chars.contains(content[i])){
                skipAt = i+1
                break
            }
            i--
        }

        return content.substring(0,skipAt)
    }

    fun removeWhiteSpaceUntil(content: String, char: Char): String {
        var cutAt = 0
        var i = 0

        while(i < content.length - 1){
            if(content[i] == ' '){
                i++
                continue
            }
            if(content[i] == char){
                cutAt = i+1
                break
            }
            i++
        }

        return content.substring(cutAt)
    }


}
