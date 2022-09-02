package tech.thatgravyboat.jukebox.api.events

import tech.thatgravyboat.jukebox.api.events.callbacks.Event

class EventHolder<T: Event> {

    private val listeners = mutableListOf<(T) -> Unit>()

    fun add(listener: (T) -> Unit) {
        listeners.add(listener)
    }

    fun remove(listener: (T) -> Unit) {
        listeners.remove(listener)
    }

    fun fire(event: T) {
        listeners.forEach { it.invoke(event) }
    }
}