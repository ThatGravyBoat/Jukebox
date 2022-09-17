package tech.thatgravyboat.jukebox.impl.youtube.state

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tech.thatgravyboat.jukebox.api.state.*

@Serializable
data class DeviceState(
    @SerialName("isPaused") val isPaused: Boolean,
    @SerialName("volumePercent") val volumePercentage: Int,
    @SerialName("statePercent") val progress: Double?,
    @SerialName("repeatType") val repeat: YoutubeRepeatState
)

@Serializable
data class TrackState(
    @SerialName("author") val artist: String,
    @SerialName("title") val title: String,
    @SerialName("cover") val cover: String,
    @SerialName("duration") val duration: Int,
    @SerialName("url") val url: String,
    @SerialName("isAdvertisement") val isAd: Boolean
)

@Serializable
data class YoutubePlayerState(
    @SerialName("player") val player: DeviceState,
    @SerialName("track") val track: TrackState
) {

    val state: State
        get() {
            val songState = SongState((track.duration * (player.progress ?: 0.0)).toInt(), track.duration, !player.isPaused)
            val song = Song(
                track.title,
                listOf(track.artist),
                track.cover,
                track.url,
                if (track.isAd) PlayingType.AD else PlayingType.TRACK
            )
            val playerState = PlayerState(ShuffleState.DISABLED, player.repeat.base, player.volumePercentage)
            return State(playerState, song, songState)
        }
}