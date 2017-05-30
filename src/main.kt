import java.io.File
import java.io.FileNotFoundException

/**
 * Git-Issue[179]: {
 * Improve the whole Project-Strucutre
 * >>
 * - Prepare for mutli-user editing
 *  - Sync online Issue-Body to local body?
 * - Let The GithubApiConnector run asynchronous and inform parser with replaysubject
 * <<
 * [improvement]
 * }
 */
class App {

    var path: String = ""
    var parser: Parser = Parser()

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            App().run(args)
        }
    }


    fun run(args: Array<String>) {

        /**
         * Git-Issue[180]: Enable Regex-Folder-Spec [improvement]
         */
        if(args.isEmpty()){
            println("No path specified! Taking root...")
            path = "."
        } else {
            path = args[0]
        }

        if(args.contains("-r")){
            parser.removeFromRemote = true
        }

        if(args.contains("-b")){
            parser.skipParseErrors = false
        }

        try {
            val config = File("config.ie")
            val token = config.readLines().get(0)
            println("UserToken "+token)

            val start = System.currentTimeMillis()

            parser.parseProject(File(path), token)

            val end = System.currentTimeMillis()

            println("\nRun-Time: "+end.minus(start)+" ms")

        } catch (e: FileNotFoundException) {
            println("config.ie not found! Please add 'config.ie' with your api-token in it")
        }
    }
}
