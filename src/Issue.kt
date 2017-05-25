/**
 * Created by root on 23.05.17.
 */

class Issue(val title: String,
            val body: String = "",
            val line: Int = 0,
            val file: String = "",
            val number: String = "") {

    var labels: ArrayList<String> = ArrayList()

    fun addlabel(label: String){
        labels.add(label)
    }
}