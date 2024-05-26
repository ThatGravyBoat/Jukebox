package tech.thatgravyboat.jukebox.api.service

import tech.thatgravyboat.jukebox.api.state.RepeatState
import tech.thatgravyboat.jukebox.api.state.State

interface Service {

    // Life Cycle
    fun start()
    fun stop(): Boolean
    fun restart()

    fun getPhase(): ServicePhase

    // State

    fun getState(): State?

    fun setPaused(paused: Boolean): Boolean
    fun setShuffle(shuffle: Boolean): Boolean
    fun setRepeat(repeat: RepeatState): Boolean
    fun setVolume(volume: Int, notify: Boolean = false): Boolean
    fun move(forward: Boolean): Boolean

    fun next() = move(true)
    fun prev() = move(false)

    fun getServiceType() = ServiceType.UNKNOWN

    fun getFunctions() = ServiceFunction.VALUES
}