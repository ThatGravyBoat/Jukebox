package tech.thatgravyboat.jukebox.impl.spotify

import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Job
import kotlinx.serialization.json.Json
import tech.thatgravyboat.jukebox.api.service.BaseService
import tech.thatgravyboat.jukebox.api.service.ServicePhase
import tech.thatgravyboat.jukebox.api.state.RepeatState
import tech.thatgravyboat.jukebox.api.state.State
import tech.thatgravyboat.jukebox.impl.spotify.state.SpotifyError
import tech.thatgravyboat.jukebox.impl.spotify.state.SpotifyErrorState
import tech.thatgravyboat.jukebox.impl.spotify.state.SpotifyPlayerState
import tech.thatgravyboat.jukebox.impl.spotify.state.SpotifyStateSerializer
import tech.thatgravyboat.jukebox.utils.Http.get
import tech.thatgravyboat.jukebox.utils.Http.plus
import tech.thatgravyboat.jukebox.utils.Http.post
import tech.thatgravyboat.jukebox.utils.Http.put
import tech.thatgravyboat.jukebox.utils.HttpCallback
import tech.thatgravyboat.jukebox.utils.Scheduler
import tech.thatgravyboat.jukebox.utils.Scheduler.invokeCancel
import kotlin.time.DurationUnit

private val API_URL = Url("https://api.spotify.com/v1/me/player")

class SpotifyService(var token: String?) : BaseService() {

    private var poller: Job? = null

    private val authHeaders: Map<String, String>
        get() = mapOf("Authorization" to "Bearer $token")

    override fun start() {
        this.poller = Scheduler.schedule(0, 2, DurationUnit.SECONDS) { requestPlayer() }
    }

    override fun stop(): Boolean {
        return poller?.invokeCancel() ?: false
    }

    override fun restart() {
        if (!stop()) {
            Scheduler.schedule(2, DurationUnit.SECONDS) { start() }
        } else {
            start()
        }
    }

    override fun getPhase() = when {
        poller != null && getState() == null -> ServicePhase.STARTING
        poller != null && getState() != null -> ServicePhase.RUNNING
        else -> ServicePhase.STOPPED
    }

    private suspend fun requestPlayer() {
        if (token == null) return
        API_URL.get(headers = authHeaders) { response ->
            if (response.status == HttpStatusCode.Unauthorized) {
                onUnauthorized()
            } else {
                val json = Json { ignoreUnknownKeys = true }

                val data = try {
                    json.decodeFromString(SpotifyStateSerializer, response.bodyAsText())
                } catch (e: Exception) {
                    e.printStackTrace()
                    SpotifyErrorState(SpotifyError(UShort.MAX_VALUE, "Error parsing Player JSON"))
                }

                if (data is SpotifyErrorState) {
                    onError("Polling error: ${data.error}")
                } else if (data is SpotifyPlayerState) {
                    onSuccess(data.state)
                }
                response.bodyAsText()
            }
        }
    }

    //region State Management
    override fun setPaused(paused: Boolean) {
        val state = getState()
        if (state == null || token == null) return
        val path = if (!paused && !state.isPlaying) "play" else if (paused && state.isPlaying) "pause" else null
        path?.let {
            putCode(API_URL + path) {
                if (it.status == HttpStatusCode.Unauthorized) onUnauthorized()
            }
        }
    }

    override fun setShuffle(shuffle: Boolean) {
        val state = getState()
        if (state == null || token == null) return
        val shuffleState: Boolean? = if (shuffle && !state.isShuffling) true else if (!shuffle && state.isShuffling) false else null
        shuffleState?.let {
            putCode( API_URL + "shuffle?state=$shuffleState") {
                if (it.status == HttpStatusCode.Unauthorized) onUnauthorized()
            }
        }
    }

    override fun setRepeat(repeat: RepeatState) {
        val state = getState()
        if (state == null || token == null) return
        val repeatState: String? = when {
            checkRepeatState(RepeatState.OFF, repeat, state) -> "off"
            checkRepeatState(RepeatState.SONG, repeat, state) -> "track"
            checkRepeatState(RepeatState.ALL, repeat, state) -> "context"
            else -> null
        }
        repeatState?.let {
            putCode( API_URL + "repeat?state=$repeatState") {
                if (it.status  == HttpStatusCode.Unauthorized) onUnauthorized()
            }
        }
    }

    override fun move(forward: Boolean) {
        if (token == null) return
        val path = if (forward) "next" else "previous"
        postCode(API_URL + path) {
            if (it.status  == HttpStatusCode.Unauthorized) onUnauthorized()
        }
    }

    override fun setVolume(volume: Int, notify: Boolean) {
        putCode(API_URL + "volume?volume_percent=$volume") {
            if (it.status == HttpStatusCode.Unauthorized) onUnauthorized()
            else onVolumeChange(volume, notify  )
        }
    }
    //endregion

    //region Utils
    private fun postCode(url: Url, callback: HttpCallback) = url.post(body = "", headers = authHeaders, callback = callback)
    private fun putCode(url: Url, callback: HttpCallback) = url.put(body = "", headers = authHeaders, callback = callback)

    private fun checkRepeatState(repeat: RepeatState, check: RepeatState, state: State) = check == repeat && state.player.repeat != repeat
    //endregion

}