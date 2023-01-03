package tech.thatgravyboat.jukebox.impl.spotify.state

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tech.thatgravyboat.jukebox.api.state.*

private val DEFAULT_SONG = Song(
    "No song playing",
    listOf("No artist"),
    "",
    "https://open.spotify.com",
    PlayingType.UNKNOWN
)

@Serializable
data class PlayerItem(
    @SerialName("duration_ms") val duration: Long,
    @SerialName("name") val title: String,
    @SerialName("artists") val artists: List<Artist>,
    @SerialName("album") val album: Album,
    @SerialName("external_urls") val urls: ExternalUrls
)

@Serializable
data class ExternalUrls(
    @SerialName("spotify") val url: String
)

@Serializable
data class Artist(
    @SerialName("name") val name: String
)

@Serializable
data class Album(
    @SerialName("images") val images: List<AlbumImage>
)

@Serializable
data class AlbumImage(
    @SerialName("height") val height: Int,
    @SerialName("width") val width: Int,
    @SerialName("url") val url: String
)

@Serializable
data class DeviceData(
    @SerialName("volume_percent") val volumePercent: Int
)

@Serializable
data class SpotifyPlayerState(
    @SerialName("shuffle_state") val isShuffling: Boolean = false,
    @SerialName("repeat_state") val repeat: SpotifyRepeatState = SpotifyRepeatState.OFF,
    @SerialName("progress_ms") val progress: Long = 0,
    @SerialName("is_playing") val isPlaying: Boolean,
    @SerialName("item") val item: PlayerItem? = null,
    @SerialName("device") val device: DeviceData,
    @SerialName("currently_playing_type") val playingType: SpotifyPlayingType = SpotifyPlayingType.UNKNOWN
) : SpotifyState {

    private fun getDuration(): Long {
        return item?.duration ?: 0
    }

    val state: State
        get() {
            val songState = SongState((progress / 1000).toInt(), (getDuration() / 1000).toInt(), isPlaying)
            val song = if (item != null) {
                Song(
                    item.title,
                    item.artists.map(Artist::name),
                    item.album.images.sortedWith(compareByDescending(AlbumImage::width)).first().url,
                    item.urls.url,
                    playingType.base
                )
            } else {
                DEFAULT_SONG
            }
            val playerState = PlayerState(if (isShuffling) ShuffleState.ON else ShuffleState.OFF, repeat.base, device.volumePercent)
            return State(playerState, song, songState)
        }
}

