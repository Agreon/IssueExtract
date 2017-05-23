/**
 * Created by root on 23.05.17.
 */

class Issue(val title: String, val body: String = "") {

    var labels: ArrayList<String> = ArrayList()

    fun addlabel(label: String){
        labels.add(label)
    }
}