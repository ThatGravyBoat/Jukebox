package tech.thatgravyboat.jukebox.impl.spotify.state

import kotlinx.serialization.Serializer
import kotlinx.serialization.json.*

@Serializer(SpotifyState::class)
object SpotifyStateSerializer : JsonContentPolymorphicSerializer<SpotifyState>(SpotifyState::class) {

    override fun selectDeserializer(element: JsonElement) = when {
        "error" in element.jsonObject -> SpotifyErrorState.serializer()
        isPrivateSession(element) -> SpotifyPrivatePlayerState.serializer()
        else -> SpotifyPlayerState.serializer()
    }

    private fun isPrivateSession(element: JsonElement)
        = element.jsonObject["device"]?.jsonObject?.get("is_private_session")?.jsonPrimitive?.booleanOrNull ?: false

}