package tech.thatgravyboat.jukebox.api.events.callbacks

import tech.thatgravyboat.jukebox.api.state.State

// State Events
class UpdateEvent(val state: State)
class SongChangeEvent(val state: State)
class VolumeChangeEvent(val volume: Int, val shouldNotify: Boolean = false)

// Life Cycle events
class ServiceErrorEvent(val error: String? = null)
class ServiceEndedEvent(val error: String? = null)
class ServiceUnauthorizedEvent