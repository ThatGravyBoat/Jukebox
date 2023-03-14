package tech.thatgravyboat.jukebox.impl.foobar

import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Job
import kotlinx.serialization.json.Json
import tech.thatgravyboat.jukebox.api.service.BaseService
import tech.thatgravyboat.jukebox.api.service.ServicePhase
import tech.thatgravyboat.jukebox.api.service.ServiceType
import tech.thatgravyboat.jukebox.api.state.RepeatState
import tech.thatgravyboat.jukebox.api.state.ShuffleState
import tech.thatgravyboat.jukebox.impl.foobar.state.FoobarErrorState
import tech.thatgravyboat.jukebox.impl.foobar.state.FoobarPlayerState
import tech.thatgravyboat.jukebox.impl.foobar.state.FoobarStateSerializer
import tech.thatgravyboat.jukebox.utils.Http.get
import tech.thatgravyboat.jukebox.utils.Http.plus
import tech.thatgravyboat.jukebox.utils.Http.post
import tech.thatgravyboat.jukebox.utils.HttpCallback
import tech.thatgravyboat.jukebox.utils.Scheduler
import tech.thatgravyboat.jukebox.utils.Scheduler.invokeCancel
import kotlin.time.DurationUnit

class FoobarService(port: Int = 7684) : BaseService() {

    private val url: Url = Url("http://localhost:${port}/api/query?player=true&trcolumns=%25artist%25%2C%25title%25")
    private val basePlayerUrl: Url = Url("http://localhost:${port}/api/player")
    private var poller: Job? = null

    private var options: MutableMap<String, Int> = mutableMapOf()

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
        url.get { response ->
            if (response.status == HttpStatusCode.OK) {
                val json = Json { ignoreUnknownKeys = true }

                val body = response.bodyAsText()
                if (body.isNotBlank()) {
                    val data = try {
                        json.decodeFromString(FoobarStateSerializer, body)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        println("Json Error: $body")
                        FoobarErrorState("Error parsing Player JSON")
                    }

                    if (data is FoobarErrorState) {
                        onError("Polling error: ${data.error}")
                    } else if (data is FoobarPlayerState) {
                        options.clear()
                        data.player.playbackModes.forEachIndexed { index, playbackMode ->
                            options[playbackMode] = index
                        }
                        onSuccess(data.state)
                    }
                }
            }
        }
    }

    //region State Management
    override fun setPaused(paused: Boolean): Boolean {
        val state = getState()
        if (state == null) return false
        val path = if (!paused && !state.isPlaying) "play" else if (paused && state.isPlaying) "pause" else null
        path?.let {
            postCode(basePlayerUrl + path) {}
        }
        return path != null
    }

    override fun setShuffle(shuffle: Boolean): Boolean {
        val state = getState()
        if (state == null) return false
        val shuffleState: Boolean? = when {
            (shuffle && state.player.shuffle == ShuffleState.OFF) -> true
            (!shuffle && state.player.shuffle == ShuffleState.ON) -> false
            else -> null
        }
        shuffleState?.let {
            val id = if (shuffleState) options["Shuffle (tracks)"] else options["Default"] ?: return false
            val data = "{\"options\":[{\"id\":\"playbackOrder\",\"value\":${id}}]}"
            postCode(basePlayerUrl, data) {}
        }
        return shuffleState != null
    }

    override fun setRepeat(repeat: RepeatState): Boolean {
        val state = getState()
        if (state == null) return false
        val repeatState: String? = when {
            checkRepeatState(RepeatState.OFF, repeat, state) -> "oDefaultff"
            checkRepeatState(RepeatState.SONG, repeat, state) -> "Repeat (track)"
            checkRepeatState(RepeatState.ALL, repeat, state) -> "Repeat (playlist)"
            else -> null
        }
        repeatState?.let {
            val id = options[repeatState] ?: return false
            val data = "{\"options\":[{\"id\":\"playbackOrder\",\"value\":${id}}]}"
            postCode(basePlayerUrl, data) {}
        }
        return repeatState != null
    }

    override fun move(forward: Boolean): Boolean {
        val path = if (forward) "next" else "previous"
        postCode(basePlayerUrl + path) {}
        return true
    }

    override fun setVolume(volume: Int, notify: Boolean): Boolean {
        if (volume in 0..100) return false
        val data = "{\"volume\":${volume * -1}}"
        postCode(basePlayerUrl, data) {}
        return volume in 0..100
    }
    //endregion

    override fun getServiceType() = ServiceType.HTTP

    //region Utils
    private fun postCode(url: Url, body: String = "", callback: HttpCallback) = url.post(body = body, callback = callback)
    //endregion

}