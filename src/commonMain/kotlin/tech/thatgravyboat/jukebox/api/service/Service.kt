package tech.thatgravyboat.jukebox.api.service

import tech.thatgravyboat.jukebox.api.state.RepeatState
import tech.thatgravyboat.jukebox.api.state.ShuffleState
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
    fun toggleShuffle(): Boolean
    fun toggleRepeat(): Boolean
    fun setVolume(volume: Int, notify: Boolean = false): Boolean
    fun move(forward: Boolean): Boolean

    fun next() = move(true)
    fun prev() = move(false)

    fun getServiceType() = ServiceType.UNKNOWN

    fun getFunctions() = ServiceFunction.VALUES

    @Deprecated(
        "Shuffle can not be manually set on some platforms so toggle is needed.",
        replaceWith = ReplaceWith("toggleShuffle()"),
        level = DeprecationLevel.ERROR
    )
    fun setShuffle(shuffle: Boolean): Boolean {
        val state = getState()?.player?.shuffle
        return when {
            state == ShuffleState.DISABLED -> false
            (state == ShuffleState.ON) != shuffle -> toggleShuffle()
            else -> false
        }
    }

    @Deprecated(
        "Repeat can not be manually set on some platforms so toggle is needed.",
        replaceWith = ReplaceWith("toggleRepeat()"),
        level = DeprecationLevel.ERROR
    )
    fun setRepeat(repeat: RepeatState): Boolean {
        val state = getState()?.player?.repeat
        return when {
            state == RepeatState.DISABLED -> false
            state != repeat -> toggleRepeat()
            else -> false
        }
    }
}