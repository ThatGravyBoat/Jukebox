package tech.thatgravyboat.jukebox.impl.spotify.state

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpotifyError(
    @SerialName("status") val status: UShort,
    @SerialName("message") val message: String
)
@Serializable
data class SpotifyErrorState(
    @SerialName("error") val error: SpotifyError
) : SpotifyState