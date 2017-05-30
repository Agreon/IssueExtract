import java.io.File
import java.io.FileNotFoundException
import kotlin.concurrent.thread

/**
 * Git-Issue[179]: {
 * Improve the whole Project-Strucutre
 * >>
 * - Add Statemachine-Parsing and decouple the parser from it
 * - Api-Connection through interface, so that the remote gets interchangable
 * - Prepare for mutli-user editing
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


    /**
     * Git-Issue[174]: { More Arguments
     * >>
     * - [ ] If skip or exception on parse-error
     * - [ ] error-level
     * <<
     * [improvement]
     * }
     */
    /**
     * Git-Issue[175]: { More Config
     * >>
     *  - [ ] what name to extract to be used
     *  - [ ] how the update should happen
     *  - [ ] make origin selectable
     * <<
     * [improvement]
     * }
     */
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
