package tech.thatgravyboat.jukebox.impl.cider.state

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CiderFloatState(
    @SerialName("data") val data: Float,
    @SerialName("type") val type: String,
) : CiderState