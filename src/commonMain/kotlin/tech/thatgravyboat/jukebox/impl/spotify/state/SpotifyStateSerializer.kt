package tech.thatgravyboat.jukebox.impl.spotify.state

import kotlinx.serialization.Serializer
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

@Serializer(SpotifyState::class)
object SpotifyStateSerializer : JsonContentPolymorphicSerializer<SpotifyState>(SpotifyState::class) {

    override fun selectDeserializer(element: JsonElement) = when {
        "error" in element.jsonObject -> SpotifyErrorState.serializer()
        else -> SpotifyPlayerState.serializer()
    }

}