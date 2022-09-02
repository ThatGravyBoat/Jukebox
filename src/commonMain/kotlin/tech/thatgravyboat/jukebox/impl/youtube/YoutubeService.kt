package tech.thatgravyboat.jukebox.impl.youtube

import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Job
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import tech.thatgravyboat.jukebox.api.service.BaseService
import tech.thatgravyboat.jukebox.api.service.ServicePhase
import tech.thatgravyboat.jukebox.api.state.RepeatState
import tech.thatgravyboat.jukebox.api.state.State
import tech.thatgravyboat.jukebox.impl.youtube.state.YoutubePlayerState
import tech.thatgravyboat.jukebox.utils.Http.get
import tech.thatgravyboat.jukebox.utils.Http.post
import tech.thatgravyboat.jukebox.utils.HttpCallback
import tech.thatgravyboat.jukebox.utils.Scheduler
import tech.thatgravyboat.jukebox.utils.Scheduler.invokeCancel
import kotlin.time.DurationUnit

private val API_URL = Url("http://localhost:9863/query")

class YoutubeService(password: String) : BaseService() {

    private var poller: Job? = null
    private val authHeaders = mapOf("Authorization" to "Bearer $password")

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
        API_URL.get(headers = authHeaders) { response ->
            if (response.status == HttpStatusCode.Unauthorized) {
                onUnauthorized()
            } else {
                val json = Json { ignoreUnknownKeys = true }

                try {
                    json.decodeFromString<YoutubePlayerState>(response.bodyAsText())
                }catch (e: Exception) {
                    e.printStackTrace()
                    onError("Error parsing Player JSON")
                    null
                }?.apply{
                    onSuccess(state)
                }
            }
        }
    }

    //region State Management
    override fun setPaused(paused: Boolean) {
        val state = getState() ?: return
        val path = if (!paused && !state.isPlaying) "track-play" else if (paused && state.isPlaying) "track-pause" else null
        path?.let(this::command)
    }

    override fun setShuffle(shuffle: Boolean) {
        // We don't shuffle 'round these parts!
    }

    override fun setRepeat(repeat: RepeatState) {
        val state = getState() ?: return
        val repeatState: String? = when {
            checkRepeatState(RepeatState.OFF, repeat, state) -> "NONE"
            checkRepeatState(RepeatState.SONG, repeat, state) -> "ONE"
            checkRepeatState(RepeatState.ALL, repeat, state) -> "ALL"
            else -> null
        }
        repeatState?.let {
            command("player-repeat", it)
        }
    }

    override fun move(forward: Boolean) {
        command(if (forward) "track-next" else "track-previous")
    }

    override fun setVolume(volume: Int, notify: Boolean) {
        command("player-set-volume", volume.toString()) {
            onVolumeChange(volume, notify)
        }
    }
    //endregion

    //region Utils
    private fun command(command: String, callback: HttpCallback = {}) =
        API_URL.post(body = "{\"command\": \"$command\"}", contentType = ContentType.Application.Json, headers = authHeaders, callback = callback)
    private fun command(command: String, value: String, callback: HttpCallback = {}) =
        API_URL.post(body = "{\"command\": \"$command\", \"value\": \"$value\"}", contentType = ContentType.Application.Json, headers = authHeaders, callback = callback)

    private fun checkRepeatState(repeat: RepeatState, check: RepeatState, state: State) = check == repeat && state.player.repeat != repeat
    //endregion

}