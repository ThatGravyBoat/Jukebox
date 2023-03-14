package tech.thatgravyboat.jukebox.impl.foobar.state

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FoobarErrorState(
    @SerialName("error") val error: String = "Unknown Error"
) : FoobarState
