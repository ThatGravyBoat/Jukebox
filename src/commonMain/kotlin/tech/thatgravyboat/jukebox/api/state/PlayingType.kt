package tech.thatgravyboat.jukebox.api.state

enum class PlayingType {
    TRACK,
    AD,
    UNKNOWN;

    fun isAd(): Boolean {
        return this == AD
    }
}