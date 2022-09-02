package tech.thatgravyboat.jukebox.api.service

import tech.thatgravyboat.jukebox.api.events.EventHolder
import tech.thatgravyboat.jukebox.api.events.EventType
import tech.thatgravyboat.jukebox.api.events.callbacks.*
import tech.thatgravyboat.jukebox.api.state.State

abstract class BaseService: Service {

    private val listeners = mutableMapOf<EventType<out Event>, EventHolder<out Event>>()
    private var lastState: State? = null
    private var errorCount = 0

    //region Listeners
    @Suppress("UNCHECKED_CAST") // We know this is safe because event types can not change.
    fun <T: Event> getHolder(eventType: EventType<T>): EventHolder<T> = listeners.getOrPut(eventType) { eventType.createHolder() } as EventHolder<T>
    fun <T : Event> registerListener(eventType: EventType<T>, listener: (T) -> Unit) = getHolder(eventType).add(listener)
    fun <T : Event> unregisterListener(eventType: EventType<T>, listener: (T) -> Unit) = getHolder(eventType).remove(listener)
    //endregion

    //region Error Handling

    fun onError(error: String?) {
        if (errorCount == 10) {
            stop()
            getHolder(EventType.SERVICE_ENDED).fire(ServiceEndedEvent(error))
        } else {
            errorCount++
            getHolder(EventType.SERVICE_ERROR).fire(ServiceErrorEvent(error))
        }
    }

    fun onSuccess(state: State) {
        errorCount = 0
        getHolder(EventType.UPDATE).fire(UpdateEvent(state))
        if (this.lastState?.isSame(state) != true) getHolder(EventType.SONG_CHANGE).fire(SongChangeEvent(state))
        this.lastState = state
    }

    fun onUnauthorized() {
        getHolder(EventType.SERVICE_UNAUTHORIZED).fire(ServiceUnauthorizedEvent())
        onError("Unauthorized") //Error so that if too many authorization errors occur the service will stop.
    }
    //endregion

    fun onVolumeChange(volume: Int, notify: Boolean) {
        getHolder(EventType.VOLUME_CHANGE).fire(VolumeChangeEvent(volume, notify))
    }

    override fun getState(): State? {
        return this.lastState
    }
}