package tech.thatgravyboat.jukebox.utils

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.websocket.*

class CloseableSocket(private val url: Url, private val request: (HttpRequestBuilder) -> Unit = {}) {

    private val messages = ArrayDeque<String>()
    private val client: HttpClient = HttpClient { install(WebSockets) }

    private var handler: ((String) -> Unit)? = null
    private var connected = false
    private var closed = false

    fun start() {
        closed = false
        Scheduler.async {
            client.webSocket({
                url(this@CloseableSocket.url)
                request(this@CloseableSocket.request)
            }) {
                while (!closed) {
                    connected = true
                    incoming.tryReceive().getOrNull()?.let { frame ->
                        (frame as? Frame.Text)?.let { text -> handler?.let { it(text.readText()) } }
                    }
                    messages.removeFirstOrNull()?.let {
                        outgoing.trySend(Frame.Text(it))
                    }
                }
                connected = false
                messages.clear()
            }
            client.close()
        }
    }

    fun send(message: String) {
        if (isConnected()) {
            messages.add(message)
        }
    }

    fun close() {
        closed = true
    }

    fun isConnected() = connected && !closed

    fun setHandler(handler: (String) -> Unit) {
        this.handler = handler
    }

}