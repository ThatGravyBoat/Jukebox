package tech.thatgravyboat.jukebox.utils

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

object Http {

    private val client = HttpClient {
        BrowserUserAgent()
        install(HttpTimeout)
    }

    operator fun Url.plus(path: String): Url {
        val segments = path.split("?")
        return URLBuilder(this).clone().appendPathSegments(segments[0]).apply {
            if (segments.size > 1) {
                segments[1].split("&")
                    .map { it.split("=") }
                    .forEach { parameters.append(it[0], it[1]) }
            }
        }.build()
    }

    fun Url.put(body: String? = null, contentType: ContentType? = null, headers: Map<String, String>? = null, callback: HttpCallback) {
        return call("PUT", body, contentType, headers, callback)
    }

    fun Url.get(body: String? = null, contentType: ContentType? = null, headers: Map<String, String>? = null, callback: HttpCallback) {
        return call("GET", body, contentType, headers, callback)
    }

    fun Url.post(body: String? = null, contentType: ContentType? = null, headers: Map<String, String>? = null, callback: HttpCallback) {
        return call("POST", body, contentType, headers, callback)
    }

    private fun Url.call(method: String, body: String? = null, contentType: ContentType? = null, headers: Map<String, String>? = null, callback: HttpCallback) {
        Scheduler.async {
            val response = client.request(this@call) {
                this.method = HttpMethod.parse(method)
                this.timeout {
                    connectTimeoutMillis = 15000
                    requestTimeoutMillis = 15000
                }
                headers?.forEach { (key, value) ->
                    this.headers.append(key, value)
                }
                contentType?.let(this::contentType)
                body?.let(this::setBody)
            }
            callback(response)
        }
    }
}

typealias HttpCallback = suspend (HttpResponse) -> Unit