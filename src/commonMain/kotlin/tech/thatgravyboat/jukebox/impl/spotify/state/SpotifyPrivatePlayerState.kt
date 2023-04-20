package tech.thatgravyboat.jukebox.impl.spotify.state

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tech.thatgravyboat.jukebox.api.state.*

private val PRIVATE_SONG = Song(
    "Private Session",
    listOf("Cant get song info"),
    "",
    "https://open.spotify.com",
    PlayingType.UNKNOWN
)

@Serializable
data class SpotifyPrivatePlayerState(
    @SerialName("shuffle_state") val isShuffling: Boolean = false,
    @SerialName("repeat_state") val repeat: SpotifyRepeatState = SpotifyRepeatState.OFF,
    @SerialName("is_playing") val isPlaying: Boolean,
    @SerialName("device") val device: DeviceData,
    @SerialName("currently_playing_type") val playingType: SpotifyPlayingType = SpotifyPlayingType.UNKNOWN
) : SpotifyState {

    override fun getState(): State {
        val songState = SongState(0, 0, isPlaying)
        val playerState = PlayerState(if (isShuffling) ShuffleState.ON else ShuffleState.OFF, repeat.base, device.volumePercent)
        return State(playerState, PRIVATE_SONG, songState)
    }
}

