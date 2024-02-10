package tech.thatgravyboat.jukebox.impl.cider

import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import tech.thatgravyboat.jukebox.api.service.BaseSocketService
import tech.thatgravyboat.jukebox.api.state.RepeatState
import tech.thatgravyboat.jukebox.api.state.ShuffleState
import tech.thatgravyboat.jukebox.impl.cider.state.CiderFloatState
import tech.thatgravyboat.jukebox.impl.cider.state.CiderPlayerState
import tech.thatgravyboat.jukebox.impl.cider.state.CiderStateSerializer
import tech.thatgravyboat.jukebox.utils.CloseableSocket
import tech.thatgravyboat.jukebox.utils.Http.get
import tech.thatgravyboat.jukebox.utils.Http.plus
import tech.thatgravyboat.jukebox.utils.Http.post

private val JSON = Json { ignoreUnknownKeys = true }
private val SOCKET_URL = Url("ws://localhost:10766/ws")
private val API_URL = Url("http://[::1]:10769")

class CiderService : BaseSocketService(CloseableSocket(SOCKET_URL)) {

    private var repeatState: RepeatState = RepeatState.OFF
    private var shuffleState: ShuffleState = ShuffleState.OFF
    private var volume: Float = 0f

    init {
        (API_URL + "audio").get { volume = it.bodyAsText().toFloatOrNull() ?: 0f }
        // Cider doesn't support these features
        // (API_URL + "repeatMode").get { repeatState = it.bodyAsText().toIntOrNull()?.toRepeatState() ?: RepeatState.DISABLED }
        // (API_URL + "shuffleMode").get { shuffleState = it.bodyAsText().toIntOrNull()?.toShuffleState() ?: ShuffleState.DISABLED }
    }

    override fun onMessage(message: String) {
        try {
            JSON.decodeFromString(CiderStateSerializer, message)
        } catch (e: Exception) {
            e.printStackTrace()
            onError("Error parsing Player JSON")
            null
        }?.apply {
            when {
                this is CiderPlayerState -> onSuccess(this.getState(volume, repeatState, shuffleState))
                this is CiderFloatState && type == "playerStatus.volumeDidChange" -> volume = data
                this is CiderFloatState && type == "playerStatus.repeatModeDidChange" -> repeatState = data.toInt().toRepeatState()
                this is CiderFloatState && type == "playerStatus.shuffleModeDidChange" -> shuffleState = data.toInt().toShuffleState()
            }
        }
    }

    override fun setPaused(paused: Boolean): Boolean {
        (API_URL + if (paused) "pause" else "play").get { }
        return true
    }

    override fun setShuffle(shuffle: Boolean): Boolean {
        val state = getState() ?: return false
        val shuffleState: Boolean? = when {
            (shuffle && state.player.shuffle == ShuffleState.OFF) -> true
            (!shuffle && state.player.shuffle == ShuffleState.ON) -> false
            else -> null
        }
        shuffleState?.let {
            (API_URL + "toggleShuffle").post { }
        }
        return shuffleState != null
    }

    override fun setRepeat(repeat: RepeatState): Boolean {
        val state = getState() ?: return false
        val repeatState: Int? = when {
            checkRepeatState(RepeatState.OFF, repeat, state) -> 0
            checkRepeatState(RepeatState.SONG, repeat, state) -> 1
            checkRepeatState(RepeatState.ALL, repeat, state) -> 2
            else -> null
        }
        repeatState?.let {
            (API_URL + "toggleRepeat").post { }
        }
        return repeatState != null
    }

    override fun setVolume(volume: Int, notify: Boolean): Boolean {
        if (volume in 0..100) {
            (API_URL + "audio/${volume.toDouble()/100.0}").get { }
            onVolumeChange(volume, notify)
            return true
        }
        return false
    }

    override fun move(forward: Boolean): Boolean {
        (API_URL + if (forward) "next" else "previous").get { }
        return true
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