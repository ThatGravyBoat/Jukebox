package tech.thatgravyboat.jukebox.impl.apple.state

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BaseAppleState(
    @SerialName("status") val status: Int,
    @SerialName("message") val message: String,
    @SerialName("type") val type: String,
): AppleState