package tech.thatgravyboat.jukebox.impl.youtubev2

import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import tech.thatgravyboat.jukebox.api.service.SocketIoService
import tech.thatgravyboat.jukebox.api.state.RepeatState
import tech.thatgravyboat.jukebox.impl.youtubev2.state.YoutubeV2PlayerState
import tech.thatgravyboat.jukebox.utils.Http.post
import tech.thatgravyboat.jukebox.utils.HttpCallback

private val JSON = Json { ignoreUnknownKeys = true }
private val SOCKET_URL = Url("ws://localhost:9863/socket.io/?EIO=4&transport=websocket")
private val API_URL = Url("http://localhost:9863/api/v1/command")

class YoutubeServiceV2(token: String) : SocketIoService(
    url = SOCKET_URL,
    namespace = "/api/v1/realtime",
    data = mapOf("token" to token)
) {

    private val authHeaders = mapOf("Authorization" to token)

    override fun onMessage(event: String?, data: List<JsonElement>) {
        // This is a very crude way of doing this, but it works for now.
        // Should be replaced with a proper implementation of socket.io
        if (event == "state-update" && data.isNotEmpty()) {
            try {
                JSON.decodeFromJsonElement<YoutubeV2PlayerState>(data[0])
            } catch (e: Exception) {
                e.printStackTrace()
                onError("Error parsing Player JSON")
                null
            }?.apply {
                onSuccess(state)
            }
        }
    }

    //region State Management
    override fun setPaused(paused: Boolean): Boolean {
        command(if (paused) "pause" else "play")
        return true
    }

    override fun setShuffle(shuffle: Boolean): Boolean {
        // We don't shuffle 'round these parts!
        return false
    }

    override fun setRepeat(repeat: RepeatState): Boolean {
        val state = getState() ?: return false
        val repeatState: Int? = when {
            checkRepeatState(RepeatState.OFF, repeat, state) -> 0
            checkRepeatState(RepeatState.ALL, repeat, state) -> 1
            checkRepeatState(RepeatState.SONG, repeat, state) -> 2
            else -> null
        }
        repeatState?.let { command("repeatMode", it) }
        return repeatState != null
    }

    override fun move(forward: Boolean): Boolean {
        command(if (forward) "next" else "previous")
        return true
    }

    override fun setVolume(volume: Int, notify: Boolean): Boolean {
        if (volume in 0..100) {
            command("setVolume", volume) {
                onVolumeChange(volume, notify)
            }
            return true
        }
        return false
    }
    //endregion

    //region Utils
    private fun command(command: String, callback: HttpCallback = {}) =
        API_URL.post(
            body = "{\"command\": \"$command\"}",
            contentType = ContentType.Application.Json,
            headers = authHeaders,
            callback = callback
        )

    private fun command(command: String, value: Int, callback: HttpCallback = {}) =
        API_URL.post(
            body = "{\"command\": \"$command\", \"data\": $value}",
            contentType = ContentType.Application.Json,
            headers = authHeaders,
            callback = callback
        )
    //endregion

}