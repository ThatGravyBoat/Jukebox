package tech.thatgravyboat.jukebox.api.events.callbacks

import tech.thatgravyboat.jukebox.api.state.State

// State Events
class UpdateEvent(val state: State): Event
class SongChangeEvent(val state: State): Event
class VolumeChangeEvent(val volume: Int, val shouldNotify: Boolean = false): Event

// Life Cycle events
class ServiceErrorEvent(val error: String? = null): Event
class ServiceEndedEvent(val error: String? = null): Event
class ServiceUnauthorizedEvent: Event