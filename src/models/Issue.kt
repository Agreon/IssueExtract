package models

import org.json.JSONArray
import org.json.JSONObject

/**
 * Created by root on 23.05.17.
 */


class Issue(val title: String,
            val body: String = "",
            val line: Int = 0,
            val file: String = "",
            var number: String = "") {

    var labels: ArrayList<String> = ArrayList()

    fun addlabel(label: String){
        labels.add(label)
    }

    fun getDifference(issue: Issue): JSONObject {

        val difference = JSONObject()

        if(this.title != issue.title){
            difference.put("title",issue.title)
        }

        if(this.body != issue.body){
            difference.put("body",issue.body)
        }

        var labelDiff = false
        if(this.labels.size == issue.labels.size){

            for(i in labels.indices) {
                if (this.labels[i] != issue.labels[i]) {
                    labelDiff = true
                    break
                }
            }

        } else {
            labelDiff = true
        }

        if(labelDiff){
            val labels = JSONArray()
            for(label in issue.labels){
                labels.put(label)
            }
            difference.put("labels", labels)
        }

        return difference
    }

}