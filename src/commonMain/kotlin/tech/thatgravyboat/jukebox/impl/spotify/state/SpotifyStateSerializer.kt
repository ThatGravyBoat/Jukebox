package tech.thatgravyboat.jukebox.impl.spotify.state

import kotlinx.serialization.Serializer
import kotlinx.serialization.json.*

@Serializer(SpotifyState::class)
object SpotifyStateSerializer : JsonContentPolymorphicSerializer<SpotifyState>(SpotifyState::class) {

    override fun selectDeserializer(element: JsonElement) = when {
        "error" in element.jsonObject -> SpotifyErrorState.serializer()
        isPrivateSession(element) -> SpotifyPrivatePlayerState.serializer()
        isLocalSession(element) -> SpotifyLocalPlayerState.serializer()
        else -> SpotifyPlayerState.serializer()
    }

    private fun isPrivateSession(element: JsonElement)
        = element.objectOrNull()?.get("device")?.objectOrNull()?.get("is_private_session")?.primitiveOrNull()?.booleanOrNull ?: false

    private fun isLocalSession(element: JsonElement)
        = element.objectOrNull()?.get("item")?.objectOrNull()?.get("item")?.objectOrNull()?.get("is_local")?.primitiveOrNull()?.booleanOrNull ?: false

    private fun JsonElement?.objectOrNull() = if (this is JsonObject) this else null

    private fun JsonElement?.primitiveOrNull() = if (this is JsonPrimitive) this else null

}