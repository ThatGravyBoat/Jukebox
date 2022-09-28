package tech.thatgravyboat.jukebox.impl.apple

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import tech.thatgravyboat.jukebox.api.service.BaseService
import tech.thatgravyboat.jukebox.api.service.ServicePhase
import tech.thatgravyboat.jukebox.api.state.RepeatState
import tech.thatgravyboat.jukebox.api.state.ShuffleState
import tech.thatgravyboat.jukebox.api.state.State
import tech.thatgravyboat.jukebox.impl.apple.state.ApplePlayerState
import tech.thatgravyboat.jukebox.impl.apple.state.AppleStateSerializer
import tech.thatgravyboat.jukebox.utils.Scheduler
import kotlin.time.DurationUnit

private val API_URL = Url("ws://localhost:26369")

class AppleService : BaseService() {

    private val messages = ArrayDeque<String>()

    private var stopped = false
    private var connected = false
    private val client = HttpClient {
        install(WebSockets)
    }

    override fun start() {
        stopped = false
        Scheduler.async {
            client.webSocket(method = HttpMethod.Get, host = API_URL.host, port = API_URL.port, path = API_URL.encodedPath) {
                while (!stopped) {
                    connected = true
                    val text = incoming.receive() as? Frame.Text
                    if (text != null) {
                        val json = Json { ignoreUnknownKeys = true }

                        try {
                            json.decodeFromString(AppleStateSerializer, text.readText())
                        }catch (e: Exception) {
                            e.printStackTrace()
                            onError("Error parsing Player JSON")
                            null
                        }?.apply{
                            if (this is ApplePlayerState) {
                                onSuccess(state)
                            }
                        }
                    }
                    if (!messages.isEmpty()) {
                        send(messages.removeFirst())
                    }
                }
                connected = false
                messages.clear()
            }
            client.close()
        }
    }

    override fun stop(): Boolean {
        stopped = true
        return true
    }

    override fun restart() {
        stop()
        Scheduler.schedule(1000, DurationUnit.MILLISECONDS) {
            start()
        }
    }

    override fun getPhase(): ServicePhase {
        return when {
            connected && getState() == null -> ServicePhase.STARTING
            connected && getState() != null -> ServicePhase.RUNNING
            else -> ServicePhase.STOPPED
        }
    }

    override fun setPaused(paused: Boolean): Boolean {
        messages.add("{\"action\":\"${if (paused) "pause" else "play"}\"}")
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
            messages.add("{\"action\":\"set-shuffle\", \"shuffle\":$it}")
        }
        return shuffleState != null
    }

    override fun setRepeat(repeat: RepeatState): Boolean {
        val state = getState() ?: return false
        val repeatState: String? = when {
            checkRepeatState(RepeatState.OFF, repeat, state) -> "NONE"
            checkRepeatState(RepeatState.SONG, repeat, state) -> "ONE"
            checkRepeatState(RepeatState.ALL, repeat, state) -> "ALL"
            else -> null
        }
        repeatState?.let {
            messages.add("{\"action\":\"repeat\"}")
        }
        return repeatState != null
    }

    override fun setVolume(volume: Int, notify: Boolean): Boolean {
        if (volume in 0..100) {
            messages.add("{\"action\":\"volume\", \"volume\":${(volume.toDouble()/100.0)}}")
            onVolumeChange(volume, notify)
            return true
        }
        return false
    }

    override fun move(forward: Boolean): Boolean {
        val state = if (forward) "next" else "previous"
        messages.add("{\"action\":\"$state\"}")
        return true
    }

    private fun checkRepeatState(repeat: RepeatState, check: RepeatState, state: State) = check == repeat && state.player.repeat != repeat

}