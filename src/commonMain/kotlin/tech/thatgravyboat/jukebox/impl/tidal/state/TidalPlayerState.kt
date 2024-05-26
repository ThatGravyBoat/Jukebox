package tech.thatgravyboat.jukebox.impl.tidal.state

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tech.thatgravyboat.jukebox.api.state.*

@Serializable
data class TidalPlayerInfo(
    @SerialName("status") val status: String,
    @SerialName("shuffle") val shuffle: Boolean,
    @SerialName("repeat") val repeat: TidalRepeatState,
)

@Serializable
data class TidalPlayerState(
    @SerialName("title") val title: String,
    @SerialName("artists") val artists: String,
    @SerialName("album") val album: String,
    @SerialName("url") val url: String,
    @SerialName("currentInSeconds") val current: Int,
    @SerialName("durationInSeconds") val duration: Int,
    @SerialName("image") val image: String,
    @SerialName("player") val player: TidalPlayerInfo,
) : TidalState {

    override fun getState(): State {
        val songState = SongState(
            current,
            duration,
            player.status == "playing"
        )
        val song = Song(title, artists.split(", "), image, url, PlayingType.TRACK)
        val shuffle = if (player.shuffle) ShuffleState.ON else ShuffleState.OFF
        val repeat = player.repeat.base
        val playerState = PlayerState(shuffle, repeat, 0)
        return State(playerState, song, songState)
    }
}
