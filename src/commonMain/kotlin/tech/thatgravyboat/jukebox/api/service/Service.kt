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

    fun setPaused(paused: Boolean)
    fun setShuffle(shuffle: Boolean)
    fun setRepeat(repeat: RepeatState)
    fun setVolume(volume: Int, notify: Boolean = false)
    fun move(forward: Boolean)

    fun next() = move(true)
    fun prev() = move(false)
}