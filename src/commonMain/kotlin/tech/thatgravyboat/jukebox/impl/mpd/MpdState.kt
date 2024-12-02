package tech.thatgravyboat.jukebox.impl.mpd

import tech.thatgravyboat.jukebox.api.state.*

object MpdState {

    private val DEFAULT_SONG = Song(
        "No song playing",
        listOf("No artist"),
        "",
        "https://craftify.thatgravyboat.tech",
        PlayingType.UNKNOWN
    )

    private fun parseToMap(message: String): Map<String, String>? = runCatching {
        message.lines().associate {
            it.split(": ", limit = 2).let { it[0] to it[1] }
        }
    }.getOrNull()

    private fun parsePlayerState(status: Map<String, String>) = PlayerState(
        when (status["random"]) {
            "0" -> ShuffleState.OFF
            "1" -> ShuffleState.ON
            else -> ShuffleState.DISABLED
        },
        when (status["repeat"]) {
            "0" -> RepeatState.OFF
            "1" -> RepeatState.SONG
            else -> RepeatState.DISABLED
        },
        status["volume"]?.toIntOrNull()?.coerceIn(0..100) ?: 0
    )

    private fun parseSongState(status: Map<String, String>) = SongState(
        status["elapsed"]?.toFloatOrNull()?.toInt() ?: 0,
        status["duration"]?.toFloatOrNull()?.toInt() ?: 0,
        status["state"] == "play"
    )

    private fun parseSong(song: Map<String, String>) = Song(
        song["Title"] ?: song["file"]?.removeFileExtensions() ?: DEFAULT_SONG.title,
        song["Artist"]?.let(::listOf) ?: listOf(),
        "",
        "",
        PlayingType.TRACK
    )

    fun parse(songMessage: String, statusMessage: String): State? {
        val song = parseToMap(songMessage) ?: return null
        val status = parseToMap(statusMessage) ?: return null
        return State(
            parsePlayerState(status),
            parseSong(song),
            parseSongState(status)
        )
    }

    // Utils
    private fun String.removeFileExtensions(): String {
        val lastDot = this.lastIndexOf(".")
        return this.substring(0 until lastDot)
    }
}