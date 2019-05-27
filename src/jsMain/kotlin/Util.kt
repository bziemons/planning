import org.w3c.fetch.*
import kotlin.js.json

external fun encodeURIComponent(str: String): String

private fun defaultApiFetchOptions(): RequestInit {
    return RequestInit(
        mode = RequestMode.SAME_ORIGIN,
        credentials = RequestCredentials.OMIT,
        headers = json(Pair("Accept", "application/json")),
        redirect = RequestRedirect.ERROR
    )
}

fun getRequestOptions(): RequestInit {
    val fetchOptions = defaultApiFetchOptions()
    fetchOptions.method = "GET"
    return fetchOptions
}

fun postRequestOptions(data: Any?): RequestInit {
    val fetchOptions = defaultApiFetchOptions()
    fetchOptions.method = "POST"
    if (data != null) {
        fetchOptions.body = JSON.stringify(data)
    }
    return fetchOptions
}

fun responseToJson(response: Response): String {
    return try {
        JSON.stringify(response)
    } catch (e: RuntimeException) {
        console.error(response)
        "Could not encode response"
    }
}