package tech.thatgravyboat.jukebox.impl.applescript.state

import tech.thatgravyboat.jukebox.api.state.PlayerState
import tech.thatgravyboat.jukebox.api.state.PlayingType
import tech.thatgravyboat.jukebox.api.state.RepeatState
import tech.thatgravyboat.jukebox.api.state.ShuffleState
import tech.thatgravyboat.jukebox.api.state.Song
import tech.thatgravyboat.jukebox.api.state.SongState
import tech.thatgravyboat.jukebox.api.state.State

class AppleMusicState(
    val progress: String,
    val duration: String,
    val state: String,
    val title: String,
    val artist: String,
    val shuffle: String,
    val repeat: String,
    val volume: String,
) {

    fun getShuffleState() = when (this.shuffle) {
        "true" -> ShuffleState.ON
        "false" -> ShuffleState.OFF
        else -> ShuffleState.DISABLED
    }

    fun getRepeatState() = when (this.repeat) {
        "one" -> RepeatState.SONG
        "all" -> RepeatState.ALL
        "off" -> RepeatState.OFF
        else -> RepeatState.DISABLED
    }

    fun getState(): State {
        return State(
            PlayerState(getShuffleState(), getRepeatState(), this.volume.toIntOrNull() ?: 0),
            Song(this.title, listOf(this.artist), "", "", PlayingType.TRACK),
            SongState(this.progress.toIntOrNull() ?: 0, this.duration.toIntOrNull() ?: 0, this.state == "playing"),
        )
    }

    companion object Companion {

        fun parse(result: String?): AppleMusicState? {
            val lines = result?.trim()?.lines()?.takeIf { it.size == 8 } ?: return null

            return AppleMusicState(
                lines[0], lines[1], lines[2],
                lines[3], lines[4],
                lines[5], lines[6], lines[7]
            )
        }
    }
}