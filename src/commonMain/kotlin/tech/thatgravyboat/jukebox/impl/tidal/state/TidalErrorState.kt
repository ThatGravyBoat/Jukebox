package tech.thatgravyboat.jukebox.impl.tidal.state

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TidalErrorState(
    @SerialName("error") val error: String = "Unknown Error"
) : TidalState