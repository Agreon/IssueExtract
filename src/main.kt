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

    fun traverse(file: File){
        if(file.isDirectory){
            file.listFiles().forEach { f ->
                traverse(f)
            }
        } else {

            parser.parseFile(file)

        }
    }

    fun run(args: Array<String>) {
        println("Hello World")
        args.forEach {
            arg -> println(arg)
        }

        if(args.isEmpty()){
            println("No path specified! Taking root...")
            path = "."
        } else {
            path = args[0]
        }

        var file = File(path)

        val start = System.nanoTime()

        traverse(file)

        val end = System.nanoTime()

        println("Runned "+end.minus(start)/1000)

    }
}