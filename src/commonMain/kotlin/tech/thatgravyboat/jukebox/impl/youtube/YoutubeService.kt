package tech.thatgravyboat.jukebox.impl.youtube

import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Job
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import tech.thatgravyboat.jukebox.api.service.BaseSocketService
import tech.thatgravyboat.jukebox.api.service.ServiceFunction
import tech.thatgravyboat.jukebox.api.state.RepeatState
import tech.thatgravyboat.jukebox.impl.youtube.state.YoutubePlayerState
import tech.thatgravyboat.jukebox.utils.CloseableSocket
import tech.thatgravyboat.jukebox.utils.Http.post
import tech.thatgravyboat.jukebox.utils.HttpCallback
import tech.thatgravyboat.jukebox.utils.Scheduler
import tech.thatgravyboat.jukebox.utils.SocketIoOpenPacket
import kotlin.time.DurationUnit

private val JSON = Json { ignoreUnknownKeys = true }
private val SOCKET_URL = Url("ws://localhost:9863/socket.io/?EIO=2&transport=websocket")
private val API_URL = Url("http://localhost:9863/query")
private val FUNCTIONS = setOf(ServiceFunction.VOLUME, ServiceFunction.REPEAT, ServiceFunction.MOVE)

class YoutubeService(password: String) : BaseSocketService(CloseableSocket(SOCKET_URL) {
    it.headers {
        append("password", password)
    }
}) {

    private val authHeaders = mapOf("Authorization" to "Bearer $password")

    private var pinger: Job? = null

    override fun stop(): Boolean {
        pinger?.cancel()
        return super.stop()
    }

    override fun onMessage(message: String) {
        // This is a very crude way of doing this, but it works for now.
        // Should be replaced with a proper implementation of socket.io
        if (message.startsWith("42[\"tick\",")) {
            try {
                JSON.decodeFromString<YoutubePlayerState>(message.substring(10, message.length - 1))
            } catch (e: Exception) {
                e.printStackTrace()
                onError("Error parsing Player JSON")
                null
            }?.apply {
                onSuccess(state)
            }
        } else if (message.startsWith("0")) {
            try {
                JSON.decodeFromString<SocketIoOpenPacket>(message.substring(1))
            } catch (e: Exception) {
                e.printStackTrace()
                onError("Error parsing SocketIO Open Packet, expect timeout errors.")
                null
            }?.apply {
                if (pinger != null) pinger?.cancel()
                pinger = Scheduler.schedule(interval.toLong(), interval.toLong(), DurationUnit.MILLISECONDS) {
                    socket.send("2")
                }
            }
        }
    }

    //region State Management
    override fun setPaused(paused: Boolean): Boolean {
        command(if (paused) "track-pause" else "track-play")
        return true
    }

    override fun toggleShuffle(): Boolean {
        // We don't shuffle 'round these parts!
        return false
    }

    override fun toggleRepeat(): Boolean {
        val state: String = when (getState()?.player?.repeat) {
            RepeatState.OFF -> "ONE"
            RepeatState.SONG -> "ALL"
            RepeatState.ALL -> "NONE"
            else -> return false
        }
        command("player-repeat", state)
        return true
    }

    override fun move(forward: Boolean): Boolean {
        command(if (forward) "track-next" else "track-previous")
        return true
    }

    override fun setVolume(volume: Int, notify: Boolean): Boolean {
        if (volume in 0..100) {
            command("player-set-volume", volume.toString()) {
                onVolumeChange(volume, notify)
            }
            return true
        }
        return false
    }

    override fun getFunctions() = FUNCTIONS
    //endregion

    //region Utils
    private fun command(command: String, callback: HttpCallback = {}) =
        API_URL.post(
            body = "{\"command\": \"$command\"}",
            contentType = ContentType.Application.Json,
            headers = authHeaders,
            callback = callback
        )

    private fun command(command: String, value: String, callback: HttpCallback = {}) =
        API_URL.post(
            body = "{\"command\": \"$command\", \"value\": \"$value\"}",
            contentType = ContentType.Application.Json,
            headers = authHeaders,
            callback = callback
        )
    //endregion

}