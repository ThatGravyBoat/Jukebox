package tech.thatgravyboat.jukebox.impl.spotify

import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Job
import kotlinx.serialization.json.Json
import tech.thatgravyboat.jukebox.api.service.BaseService
import tech.thatgravyboat.jukebox.api.service.ServicePhase
import tech.thatgravyboat.jukebox.api.service.ServiceType
import tech.thatgravyboat.jukebox.api.state.RepeatState
import tech.thatgravyboat.jukebox.api.state.ShuffleState
import tech.thatgravyboat.jukebox.impl.spotify.state.SpotifyError
import tech.thatgravyboat.jukebox.impl.spotify.state.SpotifyErrorState
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
            } else if (response.status == HttpStatusCode.OK) {
                val json = Json { ignoreUnknownKeys = true }

                val body = response.bodyAsText()
                if (body.isNotBlank()) {
                    val data = try {
                        json.decodeFromString(SpotifyStateSerializer, body)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        println("Json Error: $body")
                        SpotifyErrorState(SpotifyError(UShort.MAX_VALUE, "Error parsing Player JSON"))
                    }

                    if (data is SpotifyErrorState) {
                        onError("Polling error: ${data.error}")
                    } else if (data.getState() != null) {
                        onSuccess(data.getState()!!)
                    } else {
                        onError("Unknown player state: ${data::class.simpleName}")
                    }
                }
            }
        }
    }

    //region State Management
    override fun setPaused(paused: Boolean): Boolean {
        val state = getState()
        if (state == null || token == null) return false
        val path = if (!paused && !state.isPlaying) "play" else if (paused && state.isPlaying) "pause" else null
        path?.let {
            putCode(API_URL + path) {
                if (it.status == HttpStatusCode.Unauthorized) onUnauthorized()
            }
        }
        return path != null
    }

    override fun toggleShuffle(): Boolean {
        if (token == null) return false
        val state = when (getState()?.player?.shuffle) {
            ShuffleState.OFF -> true
            ShuffleState.ON -> false
            else -> return false
        }
        putCode( API_URL + "shuffle?state=$state") {
            if (it.status == HttpStatusCode.Unauthorized) onUnauthorized()
        }
        return true
    }

    override fun toggleRepeat(): Boolean {
        if (token == null) return false
        val state = when (getState()?.player?.repeat) {
            RepeatState.OFF -> "track"
            RepeatState.SONG -> "context"
            RepeatState.ALL -> "off"
            else -> return false
        }
        putCode( API_URL + "repeat?state=$state") {
            if (it.status == HttpStatusCode.Unauthorized) onUnauthorized()
        }
        return true
    }

    override fun move(forward: Boolean): Boolean {
        if (token == null) return false
        val path = if (forward) "next" else "previous"
        postCode(API_URL + path) {
            if (it.status  == HttpStatusCode.Unauthorized) onUnauthorized()
        }
        return true
    }

    override fun setVolume(volume: Int, notify: Boolean): Boolean {
        putCode(API_URL + "volume?volume_percent=$volume") {
            if (it.status == HttpStatusCode.Unauthorized) onUnauthorized()
            else onVolumeChange(volume, notify)
        }
        return volume in 0..100
    }
    //endregion

    override fun getServiceType() = ServiceType.HTTP

    //region Utils
    private fun postCode(url: Url, callback: HttpCallback) = url.post(body = "", headers = authHeaders, callback = callback)
    private fun putCode(url: Url, callback: HttpCallback) = url.put(body = "", headers = authHeaders, callback = callback)
    //endregion

}