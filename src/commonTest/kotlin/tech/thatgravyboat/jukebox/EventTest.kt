package tech.thatgravyboat.jukebox

import tech.thatgravyboat.jukebox.api.events.EventType
import tech.thatgravyboat.jukebox.api.events.callbacks.ServiceEndedEvent
import kotlin.test.Test
import kotlin.test.assertTrue

object EventTest {

    @Test
    fun testEventHolder() {
        val holder = EventType.SERVICE_ENDED.createHolder()
        var eventRan = false
        holder.add { eventRan = true }
        holder.fire(ServiceEndedEvent())

        assertTrue(eventRan, "Event did not fire properly.")
    }
}