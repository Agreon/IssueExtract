import java.io.File


class Parser(){

    var issues: ArrayList<Issue> = ArrayList()
    var api: ApiConnector = ApiConnector()

    var removeFromRemote: Boolean = true

    var newIssues: ArrayList<Issue> = ArrayList()

    fun parseProject(root: File, userToken: String){
        api.authToken = userToken

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

    /**
     * Git-Issue: Only works if there are no ; in text
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
                     * Git-Issue: body markers have to be in new line currently
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

    // Git-Issue: Add Number-Parsing
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
                labels = issueContent.split(">>")[1].split("<<")
            }

            // TODO: Only select if second one not empty
            // If got labels
            if(labels.size == 2 && labels[1].isNotEmpty()){
                val labelDef = labels[1].split(",") as ArrayList<String>

                // TODO: Why is [ removed from label?

                // Remove trailing stuff from first label
                //labelDef[0] = labelDef[0].split("[")[1]

                // Remove trailing stuff from last label
                labelDef[labelDef.size - 1] = labelDef[labelDef.size - 1].split("]")[0]

                for(label in labelDef){
                    if(label.removeSurrounding(" ").isNotEmpty())
                    issueFound.addlabel(label.removeSurrounding(" "))
                }
            }

          var issueFromRepo = foundIssue(issueFound)
        } catch (e: Exception) {
            e.printStackTrace()
            println("Skipping "+issueText)
        }
    }

    fun foundIssue(issue: Issue){

        issues.add(issue)

        val completeIssue = api.getIssue(issue)

        // Add Issue if not already online
        if(completeIssue == null){

            newIssues.add(issue)

            /**
             * Git-Issue: {
             *  Write number after title after adding
             *  >> Do this after all checking is done => We get the numbers back on adding
             *  # TODO
             *  - [ ] Get Back Issue from Server
             *  - [ ] somehow save issue in array, so that the files can be written later
             *
             *  Maybe Use smth. like 'forEachLine()'
             *
             *  <<
             *  [ development ]
             *  }
             */
            api.postIssue(issue)

            /**
             * Git-Issue: Let The ApiConnector run asynchronous and inform parser with replaysubject
             */
            /**
             * Issues come back from Server
             * wir brauchen die line-numbers und files und die richtige zeile
             *
             */

            return
        }

    }

    /**
     * Writes new Issues to the server and updates numbers in files
     */
    fun updateRemote(){
        for(issue in newIssues){
            val fullIssue = api.postIssue(issue)
            issue.number = fullIssue.getInt("number").toString()
            writeIssueNumber(issue)
        }
    }

    /**
     * Writes an issue number at the right position
     */
    fun writeIssueNumber(issue: Issue){

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