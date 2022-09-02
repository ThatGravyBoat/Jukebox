package tech.thatgravyboat.jukebox.impl.spotify.state

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tech.thatgravyboat.jukebox.api.state.PlayerState
import tech.thatgravyboat.jukebox.api.state.Song
import tech.thatgravyboat.jukebox.api.state.SongState
import tech.thatgravyboat.jukebox.api.state.State

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
    @SerialName("shuffle_state") val isShuffling: Boolean,
    @SerialName("repeat_state") val repeat: SpotifyRepeatState,
    @SerialName("progress_ms") val progress: Long = 0,
    @SerialName("is_playing") val isPlaying: Boolean,
    @SerialName("item") val item: PlayerItem,
    @SerialName("device") val device: DeviceData,
    @SerialName("currently_playing_type") val playingType: SpotifyPlayingType
) : SpotifyState {

    val state: State
        get() {
            val songState = SongState((progress / 1000).toInt(), (item.duration / 1000).toInt(), isPlaying)
            val song = Song(
                item.title,
                item.artists.map(Artist::name),
                item.album.images.sortedWith(compareByDescending(AlbumImage::width)).first().url,
                item.urls.url,
                playingType.base)
            val playerState = PlayerState(isShuffling, repeat.base, device.volumePercent)
            return State(playerState, song, songState)
        }
}

