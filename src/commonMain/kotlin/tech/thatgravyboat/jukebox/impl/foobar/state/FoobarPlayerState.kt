package tech.thatgravyboat.jukebox.impl.foobar.state

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tech.thatgravyboat.jukebox.api.state.*

private val DEFAULT_SONG = Song(
    "No song playing",
    listOf("No artist"),
    "",
    "https://www.foobar2000.org/",
    PlayingType.UNKNOWN
)

@Serializable
data class ActiveItem(
    @SerialName("columns") val dataColumns: List<String>,
    @SerialName("duration") val duration: Float,
    @SerialName("position") val progress: Float,
    @SerialName("playlistId") val playlist: String,
    @SerialName("index") val index: Int,
)

@Serializable
data class Volume(
    @SerialName("value") val amount: Float,
)

@Serializable
data class Player(
    @SerialName("activeItem") val activeItem: ActiveItem,
    @SerialName("playbackMode") val playbackMode: Int = 0,
    @SerialName("playbackModes") val playbackModes: List<String>,
    @SerialName("playbackState") val playbackState: FoobarPlaybackState,
    @SerialName("volume") val volume: Volume,
)


@Serializable
data class FoobarPlayerState(
    @SerialName("player") val player: Player
): FoobarState {

    private val shuffle: ShuffleState
        get() = when (player.playbackModes.getOrNull(player.playbackMode)) {
            "Shuffle (tracks)" -> ShuffleState.ON
            "Shuffle (albums)" -> ShuffleState.ON
            "Shuffle (folders)" -> ShuffleState.ON
            "Default" -> ShuffleState.OFF
            else -> ShuffleState.DISABLED
        }

    private val repeat: RepeatState
        get() = when (player.playbackModes.getOrNull(player.playbackMode)) {
            "Repeat (playlist)" -> RepeatState.ALL
            "Repeat (track)" -> RepeatState.SONG
            "Default" -> RepeatState.OFF
            else -> RepeatState.DISABLED
        }

    val state: State
        get() {
            val songState = SongState(player.activeItem.progress.toInt(), player.activeItem.duration.toInt(), player.playbackState == FoobarPlaybackState.PLAYING)
            val artist: String? = player.activeItem.dataColumns.getOrNull(0)
            val title: String? = player.activeItem.dataColumns.getOrNull(1)

            val song = if (artist != null && title != null) {
                Song(
                    title,
                    listOf(artist),
                    "",
                    "https://craftify.thatgravyboat.tech",
                    PlayingType.TRACK
                )
            } else {
                DEFAULT_SONG
            }
            val playerState = PlayerState(shuffle, repeat, (player.volume.amount + 100).toInt().coerceIn(0, 100))
            return State(playerState, song, songState)
        }
}

