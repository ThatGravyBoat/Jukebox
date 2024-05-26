package tech.thatgravyboat.jukebox.api.service

enum class ServiceFunction {
    VOLUME,
    SHUFFLE,
    REPEAT,
    MOVE,
    ;

    companion object {
        val VALUES = values().toSet()
    }
}