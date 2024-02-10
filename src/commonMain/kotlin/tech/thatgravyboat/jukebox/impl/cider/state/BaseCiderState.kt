package tech.thatgravyboat.jukebox.impl.cider.state

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BaseCiderState(
    @SerialName("type") val type: String,
): CiderState
