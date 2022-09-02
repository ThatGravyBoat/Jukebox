package tech.thatgravyboat.jukebox.api.events

import tech.thatgravyboat.jukebox.api.events.callbacks.*

class EventType<T: Event> private constructor() {

     companion object {
         /** Called when a song has been fetched. */
         val UPDATE = EventType<UpdateEvent>()
         /** Called when a song has changed. */
         val SONG_CHANGE = EventType<SongChangeEvent>()
         /** Called when the player has been told to change the volume. */
         val VOLUME_CHANGE = EventType<VolumeChangeEvent>()

         /** Called when the service has an error. */
         val SERVICE_ERROR = EventType<ServiceErrorEvent>()
         /** Called when the service has ended due to an error. */
         val SERVICE_ENDED = EventType<ServiceEndedEvent>()
         /** Called when the service has encountered an unauthorized error, this is a separate event used for the purpose of re-authenticating the user. */
         val SERVICE_UNAUTHORIZED = EventType<ServiceUnauthorizedEvent>()
    }

    fun createHolder(): EventHolder<T> {
        return EventHolder()
    }
}