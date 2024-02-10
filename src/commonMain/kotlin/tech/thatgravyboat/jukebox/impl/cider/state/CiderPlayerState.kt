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
    @SerialName("url") val url: Url = Url(),

    @SerialName("name") val title: String = "",
    @SerialName("artistName") val artist: String = "Unknown Artist",

    @SerialName("currentPlaybackTime") val time: Float = 1f,
    @SerialName("remainingTime") val remainingTime: Float = 0f,

    @SerialName("isPlaying") val playing: Boolean = false,

    @SerialName("volume") val volume: Float = 0.25f,
    @SerialName("shuffleMode") val shuffleMode: Int = 0,
    @SerialName("repeatMode") val repeatMode: Int = 0,
) {

    fun getState(volume: Float, repeat: RepeatState, shuffle: ShuffleState) = State(
        PlayerState(shuffle, repeat, (volume * 100).toInt()),
        Song(
            title,
            listOf(artist),
            artwork.toUrl(),
            url.apple,
            PlayingType.TRACK
        ),
        SongState(time.toInt(), (time + remainingTime / 1000).toInt(), playing)
    )
}

@Serializable
data class AttributeData(
    @SerialName("attributes") val attributes: StateData = StateData(),
)

@Serializable
data class CiderPlayerState(
    @SerialName("data") val data: StateData = StateData(),
    @SerialName("type") val type: String,
) : CiderState {

    fun getState(volume: Float, repeat: RepeatState, shuffle: ShuffleState) = data.getState(volume, repeat, shuffle)
}

@Serializable
data class CiderPlayerAttributeState(
    @SerialName("attributes") val data: AttributeData = AttributeData(),
    @SerialName("type") val type: String,
) : CiderState {

    fun getState(volume: Float, repeat: RepeatState, shuffle: ShuffleState) = data.attributes.getState(volume, repeat, shuffle)
}