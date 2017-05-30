import khttp.get
import khttp.patch
import khttp.post
import khttp.responses.Response
import org.json.JSONObject

/**
 * Created by agreon on 30.05.17.
 */

class HttpManager {

    var headers: Map<String, String> = mapOf()

    fun setAuthToken(token: String){
        headers = mapOf<String, String>("Authorization" to token)
    }

    fun httpPost(url: String, parameters: JSONObject): Response {

        val response = post(url, headers, mapOf(), parameters)

        return handleResponse(response)
    }

    fun httpGet(url: String, parameters: Map<String, String> = mapOf()): Response {

        val response = get(url, mapOf(), parameters)

        return handleResponse(response)
    }


    fun httpPatch(url: String, params: JSONObject): Response {
        val response = patch(url, headers, mapOf(), params)
        return handleResponse(response)
    }

    /**
     * Git-Issue[186]: Add More Error-handling [improvement]
     */
    fun handleResponse(response: Response): Response {
        if(response.statusCode >= 400){
            val retObj = response.jsonObject
            if(response.statusCode == 403){
                throw Exception("Sorry, the max request limmit is exceeded..im working on that")
            }
            throw Exception(retObj.getString("message"))
        }

        return response
    }

}
