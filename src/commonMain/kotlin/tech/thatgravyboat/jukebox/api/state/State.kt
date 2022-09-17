package tech.thatgravyboat.jukebox.api.state

data class SongState(val progress: Int, val duration: Int, val isPlaying: Boolean)

data class Song(val title: String, val artists: List<String>, val cover: String, val url: String, val type: PlayingType)

data class PlayerState(val shuffle: ShuffleState, val repeat: RepeatState, val volume: Int)

data class State(val player: PlayerState, val song: Song, val songState: SongState) {

    val isPlaying
        get() = songState.isPlaying

    val isShuffling
        get() = player.shuffle == ShuffleState.ON

    fun isSame(other: State): Boolean {
        return this.song.title == other.song.title
    }
}
