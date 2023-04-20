package tech.thatgravyboat.jukebox.utils

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SocketIoOpenPacket(
    @SerialName("pingInterval") val interval: Int = 25000,
    @SerialName("pingTimeout") val timeout: Int = 5000
)