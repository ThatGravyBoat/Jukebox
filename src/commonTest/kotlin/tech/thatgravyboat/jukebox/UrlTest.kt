package tech.thatgravyboat.jukebox

import io.ktor.http.*
import tech.thatgravyboat.jukebox.utils.Http.plus
import kotlin.test.Test
import kotlin.test.assertTrue


object UrlTest {

    @Test
    fun resolveUrlTest() {
        val uri = Url("http://localhost:8080")
        val newURI = uri + "subpath.html"

        assertTrue(newURI.toString() == "http://localhost:8080/subpath.html", "Url resolve did not create a valid URI.")
    }
}