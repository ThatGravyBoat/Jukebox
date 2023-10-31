package tech.thatgravyboat.jukebox.impl.youtubev2.state

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tech.thatgravyboat.jukebox.api.state.*

private val DEFAULT_SONG = Song(
    "No song playing",
    listOf("No artist"),
    "",
    "https://youtube.com",
    PlayingType.UNKNOWN
)

@Serializable
data class PlayerState(
    @SerialName("trackState") val trackState: Int,
    @SerialName("videoProgress") val videoProgress: Double,
    @SerialName("volume") val volume: Double = 0.0,
    @SerialName("adPlaying") val adPlaying: Boolean = false,
    @SerialName("queue") val queue: QueueState?,
)

@Serializable
data class QueueState(
    @SerialName("repeatMode") val repeatMode: Int,
)

@Serializable
data class VideoState(
    @SerialName("author") val artist: String,
    @SerialName("title") val title: String,
    @SerialName("durationSeconds") val duration: Int,
    @SerialName("thumbnails") val thumbnails: List<VideoThumbnailState>,
    @SerialName("id") val id: String,
)

@Serializable
data class VideoThumbnailState(
    @SerialName("url") val url: String,
    @SerialName("width") val width: Double,
    @SerialName("height") val height: Double,
)

@Serializable
data class YoutubeV2PlayerState(
    @SerialName("player") val player: PlayerState,
    @SerialName("video") val video: VideoState?
) {

    private fun isLikelyAnAd()
        = player.adPlaying || (video != null && video.artist == "Video will play after ad" && video.title == "")

    private fun getRepeatState(): RepeatState {
        return when(player.queue?.repeatMode ?: -1) {
            0 -> RepeatState.OFF
            1 -> RepeatState.ALL
            2 -> RepeatState.SONG
            else -> RepeatState.DISABLED
        }
    }

    val state: State
        get() {
            return State(
                PlayerState(ShuffleState.DISABLED, getRepeatState(), player.volume.toInt()),
                if (video != null) {
                    Song(
                        video.title,
                        listOf(video.artist),
                        video.thumbnails.sortedWith(compareByDescending(VideoThumbnailState::width)).first().url,
                        "https://music.youtube.com/watch?v=${video.id}",
                        if (isLikelyAnAd()) PlayingType.AD else PlayingType.TRACK
                    )
                } else {
                    DEFAULT_SONG
                },
                SongState(player.videoProgress.toInt(), video?.duration ?: 0, player.trackState == 1)
            )
        }
}