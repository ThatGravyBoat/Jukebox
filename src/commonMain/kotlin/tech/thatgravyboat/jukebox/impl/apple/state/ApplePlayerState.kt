package tech.thatgravyboat.jukebox.impl.apple.state

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tech.thatgravyboat.jukebox.api.state.*

@Serializable
data class Url(
    @SerialName("cider") val cider: String,
    @SerialName("appleMusic") val apple: String
)

@Serializable
data class Artwork(
    @SerialName("width") val width: Int,
    @SerialName("height") val height: Int,
    @SerialName("url") val url: String,
) {
    fun toUrl() = url.replace("{w}", width.toString()).replace("{h}", height.toString())
}


@Serializable
data class StateData(
    @SerialName("artistName") val artist: String,
    @SerialName("artwork") val artwork: Artwork,
    @SerialName("durationInMillis") val duration: Int,
    @SerialName("name") val title: String,
    @SerialName("status") val playing: Boolean,
    @SerialName("url") val url: Url,
    @SerialName("remainingTime") val remainingTime: Float,
    @SerialName("volume") val volume: Float,
    @SerialName("shuffleMode") val shuffleMode: Int,
    @SerialName("repeatMode") val repeatMode: Int,
)

@Serializable
data class ApplePlayerState(
    @SerialName("status") val status: Int,
    @SerialName("data") val data: StateData,
    @SerialName("message") val message: String,
    @SerialName("type") val type: String,
) : AppleState {

    val state: State
        get() {
            val songState = SongState(data.duration - data.remainingTime.toInt(), data.duration, data.playing)
            val song = Song(
                data.title,
                listOf(data.artist),
                data.artwork.toUrl(),
                data.url.apple,
                PlayingType.TRACK
            )
            val playerState = PlayerState(if (data.shuffleMode == 1) ShuffleState.ON else ShuffleState.OFF, base(data.repeatMode), (data.volume * 100).toInt())
            return State(playerState, song, songState)
        }

    private fun base(repeatMode: Int): RepeatState {
        return when (repeatMode) {
            0 -> RepeatState.OFF
            1 -> RepeatState.SONG
            2 -> RepeatState.ALL
            else -> RepeatState.DISABLED
        }
    }
}