import org.json.JSONObject
import java.util.ArrayList

/**
 * Created by agreon on 30.05.17.
 */

interface ApiConnector {
    fun postIssue(issue: Issue): JSONObject
    fun updateIfNecessary(issue: Issue)
    fun removeUnused(allFound: ArrayList<Issue>)
    fun getIssue(issue: Issue): Issue?
    fun setAuthToken(token: String)
}