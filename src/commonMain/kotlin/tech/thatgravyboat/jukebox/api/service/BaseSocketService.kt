package tech.thatgravyboat.jukebox.api.service

import tech.thatgravyboat.jukebox.utils.CloseableSocket
import tech.thatgravyboat.jukebox.utils.Scheduler
import kotlin.time.DurationUnit

abstract class BaseSocketService(val socket: CloseableSocket): BaseService() {

    init {
        socket.setHandler(::onMessage)
    }

    override fun start() {
        socket.start()
    }

    override fun stop(): Boolean {
        socket.close()
        return true
    }

    override fun restart() {
        if (!stop()) {
            Scheduler.schedule(2, DurationUnit.SECONDS) { start() }
        } else {
            start()
        }
    }

    override fun getPhase() = when {
        socket.isConnected() && getState() == null -> ServicePhase.STARTING
        socket.isConnected() && getState() != null -> ServicePhase.RUNNING
        else -> ServicePhase.STOPPED
    }

    override fun getServiceType() = ServiceType.WEBSOCKET

    abstract fun onMessage(message: String)
}