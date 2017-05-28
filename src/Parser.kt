import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption


class Parser(){

    var issues: ArrayList<Issue> = ArrayList()
    var api: ApiConnector = ApiConnector()

    var removeFromRemote: Boolean = true

    var newIssues: ArrayList<Issue> = ArrayList()

    /**
     * Git-Issue[156]: Let The ApiConnector run asynchronous and inform parser with replaysubject
     */
    fun parseProject(root: File, userToken: String){
        api.authToken = userToken

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
     * Git-Issue[152]: Only works if there are no ; in text
     */
    fun parseFile(file: File){
        val lines = file.readLines()

        var state = 0
        var parseState = 0
        var currentIssueText = ""
        var issueLine = 0

        for(i in lines.indices){
            if(state == 0 && lines[i].contains("Git-Issue")){

                issueLine = i

                // Multiline
                if(lines[i].contains("{")){
                    state = 1
                    parseState = 0
                    currentIssueText = lines[i]
                    continue
                }

                parseIssue(lines[i], false, issueLine+1, file)
                continue
            }

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
                        parseIssue(currentIssueText, true, issueLine+1, file)
                        break
                    }

                    /**
                     * Git-Issue[153]: body markers have to be in new line currently
                     */
                    // Fallback (Only if not in Body
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

    // Git-Issue[154]: Add Number-Parsing
    fun parseIssue(issueText: String, multiline: Boolean, lineNumber: Int, file: File){

        try {
            val issueContent = issueText.split(":")[1]
            var issueTitle = issueContent
            var issueBody = ""

            // If got body
            if(issueContent.split(">>").size > 1){
                issueTitle = issueContent.split(">>")[0].removeSurrounding(" ")
                issueBody = issueContent.split(">>")[1].split("<<")[0].removeSurrounding(" ")
            }

            issueTitle = removeStart(issueTitle, charArrayOf(' ','*','{'))
            issueTitle = removeEnd(issueTitle, charArrayOf(' ','*','}'))

            issueBody = removeEnd(issueBody, charArrayOf(' ','*','}'))
            issueBody = removeEnd(issueBody, charArrayOf(' ','*','}'))

            val issueFound = Issue(issueTitle, issueBody, lineNumber, file.path)

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

                for(label in labelDef){
                    if(label.removeSurrounding(" ").isNotEmpty())
                    issueFound.addlabel(label.removeSurrounding(" "))
                }
            }

          foundIssue(issueFound)
        } catch (e: Exception) {
            e.printStackTrace()
            println("Skipping "+issueText)
        }
    }

    /**
     * Git-Issue[159]: Update Attributes like line-number online
     */
    fun foundIssue(issue: Issue){

        issues.add(issue)

        val completeIssue = api.getIssue(issue)

        // Add Issue to list if not already online
        if(completeIssue == null){
            newIssues.add(issue)
        }

    }

    /**
     * Writes new Issues to the server and updates numbers in files
     */
    fun updateRemote(){

        var usedFiles: HashMap<String, File> = HashMap()

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
     * Writes an issue number at the right position
     */
    fun writeIssueNumber(issue: Issue, file: File){

        val lines = file.readLines()

        var complete = ""
        var lineContent: String

        for(i in lines.indices){
            if(i == issue.line-1){
                val lineParts = lines[i].split(":")

                lineContent = lineParts[0] + "[" + issue.number + "]:"

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
