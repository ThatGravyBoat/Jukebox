package tech.thatgravyboat.jukebox.impl.cider.state

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tech.thatgravyboat.jukebox.api.state.*

@Serializable
data class Url(
    @SerialName("cider") val cider: String = "",
    @SerialName("appleMusic") val apple: String = "",
)

@Serializable
data class Artwork(
    @SerialName("width") val width: Int = 600,
    @SerialName("height") val height: Int = 600,
    @SerialName("url") val url: String = "",
) {
    fun toUrl() = url.replace("{w}", width.toString()).replace("{h}", height.toString())
}


@Serializable
data class StateData(
    @SerialName("artwork") val artwork: Artwork = Artwork(),
    @SerialName("url") val url: String = "",

    @SerialName("name") val title: String = "",
    @SerialName("artistName") val artist: String = "Unknown Artist",
) {

    fun getState(volume: Float, repeat: RepeatState, shuffle: ShuffleState, playback: PlaybackData) = State(
        PlayerState(shuffle, repeat, (volume * 100).toInt()),
        Song(
            title,
            listOf(artist),
            artwork.toUrl(),
            url,
            PlayingType.TRACK
        ),
        SongState(playback.time.toInt(), playback.duration.toInt(), playback.isPlaying)
    )
}

@Serializable
data class AttributeData(
    @SerialName("attributes") val attributes: StateData = StateData(),
)

@Serializable
data class PlaybackData(
    @SerialName("currentPlaybackTime") val time: Float = 0f,
    @SerialName("currentPlaybackDuration") val duration: Float = 0f,
    @SerialName("isPlaying") val isPlaying: Boolean = false,
)

@Serializable
data class CiderPlayerState(
    @SerialName("data") val data: StateData = StateData(),
    @SerialName("type") val type: String,
) : CiderState {

    fun getState(volume: Float, repeat: RepeatState, shuffle: ShuffleState, playback: PlaybackData) =
        data.getState(volume, repeat, shuffle, playback)
}

@Serializable
data class CiderPlaybackState(
    @SerialName("data") val data: PlaybackData = PlaybackData(),
    @SerialName("type") val type: String,
) : CiderState

@Serializable
data class CiderPlayerAttributeState(
    @SerialName("data") val data: AttributeData = AttributeData(),
    @SerialName("type") val type: String,
) : CiderState {

    fun getState(volume: Float, repeat: RepeatState, shuffle: ShuffleState, playback: PlaybackData) =
        data.attributes.getState(volume, repeat, shuffle, playback)
}