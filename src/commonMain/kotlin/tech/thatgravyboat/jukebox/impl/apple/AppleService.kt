package tech.thatgravyboat.jukebox.impl.apple

import io.ktor.http.*
import kotlinx.serialization.json.Json
import tech.thatgravyboat.jukebox.api.service.BaseSocketService
import tech.thatgravyboat.jukebox.api.state.RepeatState
import tech.thatgravyboat.jukebox.api.state.ShuffleState
import tech.thatgravyboat.jukebox.impl.apple.state.ApplePlayerState
import tech.thatgravyboat.jukebox.impl.apple.state.AppleStateSerializer
import tech.thatgravyboat.jukebox.utils.CloseableSocket

private val JSON = Json { ignoreUnknownKeys = true }
private val API_URL = Url("ws://localhost:26369")

@Deprecated("It is recommended to use CiderService instead")
class AppleService : BaseSocketService(CloseableSocket(API_URL)) {

    override fun onMessage(message: String) {
        try {
            JSON.decodeFromString(AppleStateSerializer, message)
        } catch (e: Exception) {
            e.printStackTrace()
            onError("Error parsing Player JSON")
            null
        }?.apply {
            if (this is ApplePlayerState) {
                onSuccess(state)
            }
        }
    }

    override fun setPaused(paused: Boolean): Boolean {
        socket.send("{\"action\":\"${if (paused) "pause" else "play"}\"}")
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
            socket.send("{\"action\":\"set-shuffle\", \"shuffle\":$it}")
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
            socket.send("{\"action\":\"set-repeat\", \"repeat\":$it}")
        }
        return repeatState != null
    }

    override fun setVolume(volume: Int, notify: Boolean): Boolean {
        if (volume in 0..100) {
            socket.send("{\"action\":\"volume\", \"volume\":${(volume.toDouble()/100.0)}}")
            onVolumeChange(volume, notify)
            return true
        }
        return false
    }

    override fun move(forward: Boolean): Boolean {
        val state = if (forward) "next" else "previous"
        socket.send("{\"action\":\"$state\"}")
        return true
    }

}