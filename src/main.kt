import java.io.File
import kotlin.concurrent.thread

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
        if(args.isEmpty()){
            println("No path specified! Taking root...")
            path = "."
        } else {
            path = args[0]
        }

        // TODO: Maybe switch later
        if(args.contains("-r=false")){
            parser.removeFromRemote = false
        }

        val config = File("config.ie")
        val token = config.readLines().get(0)
        println("UserToken "+token)

        val start = System.nanoTime()

        parser.parseProject(File(path), token)

        val end = System.nanoTime()

        println("\nRun-Time: "+end.minus(start)/1000)

    }
}
