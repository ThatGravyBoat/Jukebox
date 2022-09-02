package tech.thatgravyboat.jukebox.impl.spotify.state

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tech.thatgravyboat.jukebox.api.state.RepeatState

@Serializable
enum class SpotifyRepeatState {
    @SerialName("off") OFF,
    @SerialName("track") TRACK,
    @SerialName("context") CONTEXT;

    val base: RepeatState
        get() = when (this) {
            OFF -> RepeatState.OFF
            TRACK -> RepeatState.SONG
            CONTEXT -> RepeatState.ALL
        }
}