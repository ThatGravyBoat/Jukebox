package tech.thatgravyboat.jukebox.impl.applescript

import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import tech.thatgravyboat.jukebox.api.service.BaseService
import tech.thatgravyboat.jukebox.api.service.ServicePhase
import tech.thatgravyboat.jukebox.api.service.ServiceType
import tech.thatgravyboat.jukebox.impl.applescript.state.AppleMusicState
import tech.thatgravyboat.jukebox.utils.Scheduler
import tech.thatgravyboat.jukebox.utils.Scheduler.invokeCancel
import kotlin.time.DurationUnit

class AppleMusicService(var executor: suspend (String) -> String?) : BaseService() {

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
        val result = this.executor.invoke(AppleMusicCommands.POLL_COMMAND)
        val state = AppleMusicState.parse(result)

        if (state == null) {
            onError("Couldn't read player state: $result")
        } else {
            onSuccess(state.getState())
        }
    }

    //region State Management
    override fun setPaused(paused: Boolean): Boolean {
        return execCommand(if (paused) AppleMusicCommands.PAUSE_COMMAND else AppleMusicCommands.PLAY_COMMAND)
    }

    override fun toggleShuffle(): Boolean {
        return execCommand(AppleMusicCommands.TOGGLE_SHUFFLE_COMMAND)
    }

    override fun toggleRepeat(): Boolean {
        return execCommand(AppleMusicCommands.TOGGLE_REPEAT_COMMAND)
    }

    override fun move(forward: Boolean): Boolean {
        return execCommand(if (forward) AppleMusicCommands.NEXT_COMMAND else AppleMusicCommands.PREV_COMMAND)
    }

    override fun setVolume(volume: Int, notify: Boolean): Boolean {
        if (volume !in 0..100) return false
        if (execCommand(AppleMusicCommands.VOLUME_COMMAND, mapOf("VOLUME" to volume))) {
            onVolumeChange(volume, notify)
            return true
        }
        return false
    }
    //endregion

    override fun getServiceType() = ServiceType.NATIVE

    private fun execCommand(command: String, args: Map<String, Any> = mapOf()): Boolean {
        var command = command
        args.forEach { (key, value) ->
            command = command.replace("{{$key}}", value.toString())
        }
        return runBlocking {
            executor.invoke(command) != null
        }
    }
}