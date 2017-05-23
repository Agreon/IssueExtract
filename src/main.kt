import java.io.File

class App {

    var path: String = ""

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
            println("File")
            println(file.name)
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

        traverse(file)

    }
}