/**
 * Created by root on 23.05.17.
 */

/**
 * Git-Issue: Save place of issue
 */
class Issue(val title: String, val body: String = "", val number: String = "") {

    var labels: ArrayList<String> = ArrayList()

    fun addlabel(label: String){
        labels.add(label)
    }
}