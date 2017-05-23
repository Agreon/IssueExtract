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

        val start = System.nanoTime()

        parser.parseProject(File(path))

        val end = System.nanoTime()

        println("\nRun-Time: "+end.minus(start)/1000)

    }
}