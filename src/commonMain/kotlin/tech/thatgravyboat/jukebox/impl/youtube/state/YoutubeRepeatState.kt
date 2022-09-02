package tech.thatgravyboat.jukebox.impl.youtube.state

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tech.thatgravyboat.jukebox.api.state.RepeatState

@Serializable
enum class YoutubeRepeatState {
    @SerialName("NONE") NONE,
    @SerialName("ONE") ONE,
    @SerialName("ALL") ALL;

    val base: RepeatState
        get() = when (this) {
            NONE -> RepeatState.OFF
            ONE -> RepeatState.SONG
            ALL -> RepeatState.ALL
        }
}