package tech.thatgravyboat.jukebox.impl.tidal.state

import tech.thatgravyboat.jukebox.api.state.State

sealed interface TidalState {

    fun getState(): State? {
        return null
    }
}