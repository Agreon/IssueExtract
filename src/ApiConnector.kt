/**
 * Created by agreon on 30.05.17.
 */

interface ApiConnector {

    var httpManager: HttpManager


    fun getIssue(issue: Issue): Issue?
}