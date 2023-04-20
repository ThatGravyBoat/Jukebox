package tech.thatgravyboat.jukebox.impl.spotify.state

import tech.thatgravyboat.jukebox.api.state.State

sealed interface SpotifyState {

    fun getState(): State? {
        return null
    }
}