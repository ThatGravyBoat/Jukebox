package tech.thatgravyboat.jukebox.impl.tidal.state

import kotlinx.serialization.Serializer
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement

@Serializer(TidalState::class)
object TidalStateSerializer : JsonContentPolymorphicSerializer<TidalState>(TidalState::class) {

    override fun selectDeserializer(element: JsonElement) = TidalPlayerState.serializer()

}