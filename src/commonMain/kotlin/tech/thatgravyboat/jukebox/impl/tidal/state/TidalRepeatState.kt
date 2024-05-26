package tech.thatgravyboat.jukebox.impl.tidal.state

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tech.thatgravyboat.jukebox.api.state.RepeatState

@Serializable
enum class TidalRepeatState {
    @SerialName("off") OFF,
    @SerialName("single") SINGLE,
    @SerialName("all") ALL;

    val base: RepeatState
        get() = when (this) {
            OFF -> RepeatState.OFF
            SINGLE -> RepeatState.SONG
            ALL -> RepeatState.ALL
        }
}