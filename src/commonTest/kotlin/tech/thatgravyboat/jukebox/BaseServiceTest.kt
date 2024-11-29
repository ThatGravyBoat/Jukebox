package tech.thatgravyboat.jukebox

import tech.thatgravyboat.jukebox.api.events.*
import tech.thatgravyboat.jukebox.api.events.callbacks.*
import tech.thatgravyboat.jukebox.api.service.BaseService
import tech.thatgravyboat.jukebox.api.service.ServicePhase
import tech.thatgravyboat.jukebox.api.state.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private val TEST_STATE = State(
    PlayerState(ShuffleState.OFF, RepeatState.ALL, 100),
    Song("test", listOf("Artist1", "Artist2"), "https://localhost:6969/cover.png", "https://localhost:6969/song", PlayingType.TRACK),
    SongState(50, 100, true)
)

object BaseServiceTest {

    @Test
    fun errorStopTest() {
        val service = TestService()

        var serviceStopped = false
        val stopEvent: (ServiceEndedEvent) -> Unit = { serviceStopped = true }
        service.registerListener(EventType.SERVICE_ENDED, stopEvent)
        repeat(11) { service.onError("Error $it") }

        assertTrue(serviceStopped, "Service did not stop when error count exceeded 10.")
    }

    @Test
    fun errorTest() {
        val service = TestService()

        var serviceErrored = false
        val errorEvent: (ServiceErrorEvent) -> Unit = { serviceErrored = true }
        service.registerListener(EventType.SERVICE_ERROR, errorEvent)
        service.onError("Error")

        assertTrue(serviceErrored, "Service error event not fired")
    }

    @Test
    fun successTest() {
        val service = TestService()

        var songChanged = false
        var songUpdated = false

        val changeEvent: (SongChangeEvent) -> Unit = {
            assertFalse(songChanged, "Song change event called twice for same state.")
            songChanged = true
        }
        val updateEvent: (UpdateEvent) -> Unit = { songUpdated = true }

        service.registerListener(EventType.SONG_CHANGE, changeEvent)
        service.registerListener(EventType.UPDATE, updateEvent)
        service.onSuccess(TEST_STATE)
        service.onSuccess(TEST_STATE)

        assertTrue(songChanged, "Song change event not called.")
        assertTrue(songUpdated, "Song update event not called.")
        assertEquals(service.getState(), TEST_STATE, "Service state was not set.")
    }

    @Test
    fun unauthorizedTest() {
        val service = TestService()
        var userUnauthorized = false
        val event: (ServiceUnauthorizedEvent) -> Unit = { userUnauthorized = true }
        service.registerListener(EventType.SERVICE_UNAUTHORIZED, event)
        service.onUnauthorized()

        assertTrue(userUnauthorized, "User unauthorized event not fired.")
    }

    @Test
    fun volumeEventTest() {
        val service = TestService()
        var volumeChanged = false
        val event: (VolumeChangeEvent) -> Unit = { volumeChanged = true }
        service.registerListener(EventType.VOLUME_CHANGE, event)
        service.onVolumeChange(100, true)

        assertTrue(volumeChanged, "Volume change event not fired.")
    }

    @Test
    fun eventRegistrationTest() {
        val service = TestService()
        val event: (VolumeChangeEvent) -> Unit = {
            assertTrue(it.shouldNotify, "Volume change event was called after being unregistered.")
        }
        service.registerListener(EventType.VOLUME_CHANGE, event)
        service.onVolumeChange(100, true)
        service.unregisterListener(EventType.VOLUME_CHANGE, event)
        service.onVolumeChange(100, false)
    }

    class TestService : BaseService() {
        override fun start() {}
        override fun stop() = true
        override fun restart() {}
        override fun getPhase() = ServicePhase.STOPPED
        override fun setPaused(paused: Boolean) = true
        override fun toggleShuffle(): Boolean = true
        override fun toggleRepeat(): Boolean = true
        override fun setVolume(volume: Int, notify: Boolean) = true
        override fun move(forward: Boolean) = true
    }
}