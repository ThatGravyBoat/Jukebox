package tech.thatgravyboat.jukebox.impl.foobar.state

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class FoobarPlaybackState {
    @SerialName("playing") PLAYING,
    @SerialName("stopped") STOPPED,
    @SerialName("paused") PAUSED,
}