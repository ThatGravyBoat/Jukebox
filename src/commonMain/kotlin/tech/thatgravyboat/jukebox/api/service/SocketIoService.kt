package tech.thatgravyboat.jukebox.api.service

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import tech.thatgravyboat.jukebox.utils.Scheduler
import kotlin.time.DurationUnit

private val JSON = Json { ignoreUnknownKeys = true }

abstract class SocketIoService(
    private val url: Url,
    private val namespace: String = "/",
    data: Map<String, String> = mapOf()
): BaseService() {

    private val client: HttpClient = HttpClient { install(WebSockets) }
    private val data: String = JsonObject(data.mapValues { (_, value) -> JsonPrimitive(value) }).toString()

    private var connected = false
    private var closed = false

    override fun start() {
        closed = false
        Scheduler.async {
            client.webSocket({
                url(this@SocketIoService.url)
            }) {
                try {
                    outgoing.trySend(Frame.Text("40$namespace,${data}"))
                    while (!closed) {
                        connected = true
                        incoming.tryReceive().getOrNull()?.let { frame ->
                            (frame as? Frame.Text)?.let { text ->
                                when (val value = text.readText()) {
                                    "2" -> outgoing.trySend(Frame.Text("3"))
                                    else -> onMessage(value)
                                }
                            }
                            (frame as? Frame.Close)?.let {
                                this@SocketIoService.stop()
                            }
                        }
                    }
                    connected = false
                } catch (e: ClosedReceiveChannelException) {
                    this@SocketIoService.stop()
                }
            }
            client.close()
        }
    }

    override fun stop(): Boolean {
        closed = true
        return true
    }

    override fun restart() {
        if (!stop()) {
            Scheduler.schedule(2, DurationUnit.SECONDS) { start() }
        } else {
            start()
        }
    }

    override fun getPhase() = when {
        connected && !closed && getState() == null -> ServicePhase.STARTING
        connected && !closed && getState() != null -> ServicePhase.RUNNING
        else -> ServicePhase.STOPPED
    }

    override fun getServiceType() = ServiceType.WEBSOCKET

    private fun onMessage(message: String) {
        val prefix = namespace.takeIf { it != "/" }?.let { "42$it," } ?: "42"
        if (message.startsWith(prefix)) {
            try {
                val rawMessage = JSON.decodeFromString<JsonArray>(message.removePrefix(prefix))
                if (rawMessage.size > 0) {
                    val event = rawMessage[0]
                    if (event is JsonPrimitive && event.isString) {
                        onMessage(event.content, rawMessage.subList(1, rawMessage.size))
                        return
                    }
                }
                return
            } catch (_: Exception) {
                // ignore error
            }
        }
        onMessage(null, listOf(JsonPrimitive(message)))
    }

    /**
     * @param event if event is null that signifies unknown or unhandled message type
     */
    abstract fun onMessage(event: String?, data: List<JsonElement>)
}