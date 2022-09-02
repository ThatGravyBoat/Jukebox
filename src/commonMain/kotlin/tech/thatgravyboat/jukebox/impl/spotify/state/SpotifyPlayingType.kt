package tech.thatgravyboat.jukebox.impl.spotify.state

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tech.thatgravyboat.jukebox.api.state.PlayingType

@Serializable
enum class SpotifyPlayingType {
    @SerialName("track") TRACK,
    @SerialName("episode") EPISODE,
    @SerialName("ad") AD,
    @SerialName("unknown") UNKNOWN;

    val base: PlayingType
        get() = when (this) {
            TRACK -> PlayingType.TRACK
            AD -> PlayingType.AD
            UNKNOWN, EPISODE -> PlayingType.UNKNOWN
        }
}