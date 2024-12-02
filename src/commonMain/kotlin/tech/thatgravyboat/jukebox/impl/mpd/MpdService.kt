package tech.thatgravyboat.jukebox.impl.mpd

import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import tech.thatgravyboat.jukebox.api.service.BaseService
import tech.thatgravyboat.jukebox.api.service.ServicePhase
import tech.thatgravyboat.jukebox.api.service.ServiceType
import tech.thatgravyboat.jukebox.api.state.RepeatState
import tech.thatgravyboat.jukebox.utils.Scheduler
import tech.thatgravyboat.jukebox.utils.Scheduler.invokeCancel
import kotlin.time.DurationUnit

class MpdService(port: Int = 6600) : BaseService() {

    private var socket: MpdSocket = MpdSocket(port)
    private var poller: Job? = null

    private var stopped: Boolean = false

    override fun start() {
        this.socket.start()
        this.poller = Scheduler.schedule(0, 2, DurationUnit.SECONDS) {
            val song = command("currentsong") ?: return@schedule
            val status = command("status") ?: return@schedule
            val state = MpdState.parse(song, status) ?: return@schedule
            stopped = "state: stop" in status
            onSuccess(state)
        }
    }

    override fun stop(): Boolean {
        this.socket.close()
        return this.poller?.invokeCancel() ?: false
    }

    override fun restart() {
        stop()
        start()
    }

    override fun getPhase() = when {
        !socket.isOpen() -> ServicePhase.STOPPED
        getState() == null -> ServicePhase.STARTING
        getState() != null -> ServicePhase.RUNNING
        else -> ServicePhase.STOPPED
    }

    //region State Management
    override fun setPaused(paused: Boolean): Boolean {
        if (stopped) return command("play") != null
        return command("pause ${if (paused) "1" else "0"}") != null
    }

    override fun toggleShuffle(): Boolean {
        val state = if (getState()?.isShuffling == true) "0" else "1"
        return command("random $state") != null
    }

    override fun toggleRepeat(): Boolean {
        val state = if (getState()?.player?.repeat != RepeatState.OFF) "0" else "1"
        return command("repeat $state") != null
    }

    override fun move(forward: Boolean): Boolean {
        return command(if (forward) "next" else "previous") != null
    }

    override fun setVolume(volume: Int, notify: Boolean): Boolean {
        if (command("setvol ${volume.coerceIn(0..100)}") != null) {
            onVolumeChange(volume.coerceIn(0..100), notify)
            return true
        }
        return false
    }
    //endregion

    override fun getServiceType() = ServiceType.TCP

    private fun command(command: String): String? = runBlocking {
        socket.send(command)
    }.onFailure {
        this.onError(it.message)
    }.getOrNull()
}