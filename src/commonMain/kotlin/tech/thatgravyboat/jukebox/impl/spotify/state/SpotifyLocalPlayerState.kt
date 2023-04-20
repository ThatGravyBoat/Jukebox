package tech.thatgravyboat.jukebox.impl.spotify.state

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tech.thatgravyboat.jukebox.api.state.*

private val DEFAULT_SONG = Song(
    "No local song playing",
    listOf("No artist"),
    "",
    "https://open.spotify.com",
    PlayingType.UNKNOWN
)

@Serializable
data class LocalPlayerItem(
    @SerialName("duration_ms") val duration: Long,
    @SerialName("name") val title: String
)

@Serializable
data class SpotifyLocalPlayerState(
    @SerialName("shuffle_state") val isShuffling: Boolean = false,
    @SerialName("repeat_state") val repeat: SpotifyRepeatState = SpotifyRepeatState.OFF,
    @SerialName("progress_ms") val progress: Long = 0,
    @SerialName("is_playing") val isPlaying: Boolean,
    @SerialName("item") val item: LocalPlayerItem? = null,
    @SerialName("device") val device: DeviceData,
    @SerialName("currently_playing_type") val playingType: SpotifyPlayingType = SpotifyPlayingType.UNKNOWN
) : SpotifyState {

    private fun getDuration(): Long {
        return item?.duration ?: 0
    }

    override fun getState(): State {
        val songState = SongState((progress / 1000).toInt(), (getDuration() / 1000).toInt(), isPlaying)
        val song = if (item != null) {
            Song(
                item.title,
                listOf(),
                "",
                "https://open.spotify.com",
                playingType.base
            )
        } else {
            DEFAULT_SONG
        }
        val playerState = PlayerState(if (isShuffling) ShuffleState.ON else ShuffleState.OFF, repeat.base, device.volumePercent)
        return State(playerState, song, songState)
    }
}