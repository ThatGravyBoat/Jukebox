package tech.thatgravyboat.jukebox.impl.foobar.state

import kotlinx.serialization.Serializer
import kotlinx.serialization.json.*

@Serializer(FoobarState::class)
object FoobarStateSerializer : JsonContentPolymorphicSerializer<FoobarState>(FoobarState::class) {

    override fun selectDeserializer(element: JsonElement) = when {
        "error" in element.jsonObject -> FoobarErrorState.serializer()
        else -> FoobarPlayerState.serializer()
    }

}