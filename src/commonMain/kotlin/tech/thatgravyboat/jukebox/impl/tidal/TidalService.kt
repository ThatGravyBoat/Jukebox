package tech.thatgravyboat.jukebox.impl.tidal

import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Job
import kotlinx.serialization.json.Json
import tech.thatgravyboat.jukebox.api.service.BaseService
import tech.thatgravyboat.jukebox.api.service.ServiceFunction
import tech.thatgravyboat.jukebox.api.service.ServicePhase
import tech.thatgravyboat.jukebox.api.state.RepeatState
import tech.thatgravyboat.jukebox.api.state.ShuffleState
import tech.thatgravyboat.jukebox.impl.tidal.state.TidalErrorState
import tech.thatgravyboat.jukebox.impl.tidal.state.TidalPlayerState
import tech.thatgravyboat.jukebox.impl.tidal.state.TidalStateSerializer
import tech.thatgravyboat.jukebox.utils.Http.get
import tech.thatgravyboat.jukebox.utils.Http.plus
import tech.thatgravyboat.jukebox.utils.Http.post
import tech.thatgravyboat.jukebox.utils.Scheduler
import tech.thatgravyboat.jukebox.utils.Scheduler.invokeCancel
import kotlin.time.DurationUnit

private val FUNCTIONS = setOf(ServiceFunction.SHUFFLE, ServiceFunction.REPEAT, ServiceFunction.MOVE)

class TidalService(port: Int = 47836): BaseService() {

    private val url: Url = Url("http://localhost:${port}/player/")
    private val pollingUrl: Url = Url("http://localhost:${port}/current")
    private var poller: Job? = null

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
        pollingUrl.get { response ->
            if (response.status == HttpStatusCode.OK) {
                val json = Json { ignoreUnknownKeys = true }

                val body = response.bodyAsText()
                if (body.isNotBlank()) {
                    val data = try {
                        json.decodeFromString(TidalStateSerializer, body)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        println("Json Error: $body")
                        TidalErrorState("Error parsing Player JSON")
                    }

                    if (data is TidalErrorState) {
                        onError("Polling error: ${data.error}")
                    } else if (data is TidalPlayerState) {
                        onSuccess(data.getState())
                    }
                }
            }
        }
    }

    override fun setPaused(paused: Boolean): Boolean {
        getState() ?: return false
        val action = if (paused) "pause" else "play"
        (url + action).post {
            if (it.status != HttpStatusCode.OK) {
                onError("Error setting paused state")
            }
        }
        return true
    }

    override fun setShuffle(shuffle: Boolean): Boolean {
        val state = getState() ?: return false
        if (state.player.shuffle == ShuffleState.ON == shuffle) return true
        (url + "/shuffle/toggle").post {
            if (it.status != HttpStatusCode.OK) {
                onError("Error setting shuffle state")
            }
        }
        return true
    }

    override fun setRepeat(repeat: RepeatState): Boolean {
        val state = getState() ?: return false
        if (state.player.repeat == repeat) return true
        (url + "/repeat/toggle").post {
            if (it.status != HttpStatusCode.OK) {
                onError("Error setting repeat state")
            }
        }
        return true
    }

    override fun setVolume(volume: Int, notify: Boolean): Boolean {
        return false
    }

    override fun move(forward: Boolean): Boolean {
        getState() ?: return false
        val action = if (forward) "next" else "previous"
        (url + action).post {
            if (it.status != HttpStatusCode.OK) {
                onError("Error moving to next track")
            }
        }
        return true
    }

    override fun getFunctions() = FUNCTIONS
}