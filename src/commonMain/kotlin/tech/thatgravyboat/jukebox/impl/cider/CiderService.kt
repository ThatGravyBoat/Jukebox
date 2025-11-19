package tech.thatgravyboat.jukebox.impl.cider

import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import tech.thatgravyboat.jukebox.api.service.SocketIoService
import tech.thatgravyboat.jukebox.api.state.RepeatState
import tech.thatgravyboat.jukebox.api.state.ShuffleState
import tech.thatgravyboat.jukebox.impl.cider.state.*
import tech.thatgravyboat.jukebox.utils.Http.bodyAsJson
import tech.thatgravyboat.jukebox.utils.Http.get
import tech.thatgravyboat.jukebox.utils.Http.plus
import tech.thatgravyboat.jukebox.utils.Http.post
import tech.thatgravyboat.jukebox.utils.asFloat
import tech.thatgravyboat.jukebox.utils.asInt
import tech.thatgravyboat.jukebox.utils.asObject

private val JSON = Json { ignoreUnknownKeys = true }
private val SOCKET_URL = Url("ws://localhost:10767/socket.io/?EIO=4&transport=websocket")
private val API_URL = Url("http://localhost:10767/api/v1/playback")

class CiderService : SocketIoService(
    url = SOCKET_URL,
    data = mapOf()
) {

    private var repeatState: RepeatState = RepeatState.DISABLED
    private var shuffleState: ShuffleState = ShuffleState.DISABLED
    private var volume: Float = 0f

    private var playback: PlaybackData = PlaybackData(0f, 0f, false)
    private var state: StateData = StateData()

    init {
        (API_URL + "volume").get { volume = it.bodyAsJson().asObject?.get("volume").asFloat ?: 0f }
        (API_URL + "repeat-mode").get { repeatState = it.bodyAsJson().asObject?.get("value").asInt?.toRepeatState() ?: RepeatState.DISABLED }
        (API_URL + "shuffle-mode").get { shuffleState = it.bodyAsJson().asObject?.get("value").asInt?.toShuffleState() ?: ShuffleState.DISABLED }
    }

    override fun onMessage(event: String?, data: List<JsonElement>) {
        if (event == "API:Playback" && data.isNotEmpty()) {
            try {
                JSON.decodeFromJsonElement(CiderStateSerializer, data[0])
            } catch (e: Exception) {
                e.printStackTrace()
                onError("Error parsing Player JSON")
                null
            }?.let { state ->
                when {
                    state is CiderPlayerState -> this.state = state.data
                    state is CiderPlayerAttributeState -> this.state = state.data.attributes
                    state is CiderPlaybackState -> this.playback = state.data
                    state is CiderFloatState && state.type == "playerStatus.volumeDidChange" -> this.volume = state.data
                    state is CiderFloatState && state.type == "playerStatus.repeatModeDidChange" -> this.repeatState = state.data.toInt().toRepeatState()
                    state is CiderFloatState && state.type == "playerStatus.shuffleModeDidChange" -> this.shuffleState = state.data.toInt().toShuffleState()
                    else -> return
                }

                onDataChanged()
            }
        }
    }

    override fun setPaused(paused: Boolean): Boolean {
        (API_URL + if (paused) "pause" else "play").post { }
        return true
    }

    override fun toggleShuffle(): Boolean {
        getState() ?: return false
        (API_URL + "toggle-shuffle").post { }
        return true
    }

    override fun toggleRepeat(): Boolean {
        getState() ?: return false
        (API_URL + "toggle-repeat").post { }
        return true
    }

    override fun setVolume(volume: Int, notify: Boolean): Boolean {
        if (volume in 0..100) {
            (API_URL + "volume").post(
                body = "{\"volume\": ${volume.toDouble()/100.0}}",
                contentType = ContentType.Application.Json,
            ) {}
            onVolumeChange(volume, notify)
            return true
        }
        return false
    }

    override fun move(forward: Boolean): Boolean {
        (API_URL + if (forward) "next" else "previous").post { }
        return true
    }

    private fun onDataChanged() {
        onSuccess(state.getState(volume, repeatState, shuffleState, this.playback))
    }

    private fun Int.toRepeatState(): RepeatState {
        return when (this) {
            0 -> RepeatState.OFF
            1 -> RepeatState.SONG
            2 -> RepeatState.ALL
            else -> RepeatState.DISABLED
        }
    }

    private fun Int.toShuffleState(): ShuffleState {
        return when (this) {
            0 -> ShuffleState.OFF
            1 -> ShuffleState.ON
            else -> ShuffleState.DISABLED
        }
    }
}