package tech.thatgravyboat.jukebox.impl.cider.state

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializer(CiderState::class)
object CiderStateSerializer : JsonContentPolymorphicSerializer<CiderState>(CiderState::class) {

    override fun selectDeserializer(element: JsonElement): KSerializer<out CiderState> {
        return when (element.jsonObject["type"]?.jsonPrimitive?.content) {
            null -> BaseCiderState.serializer()
            "playbackStatus.playbackTimeDidChange" -> CiderPlaybackState.serializer()
            "playbackStatus.nowPlayingItemDidChange" -> CiderPlayerState.serializer()
            "playbackStatus.playbackStateDidChange" -> CiderPlayerAttributeState.serializer()
            "playerStatus.repeatModeDidChange" -> CiderFloatState.serializer()
            "playerStatus.volumeDidChange" -> CiderFloatState.serializer()
            "playerStatus.shuffleModeDidChange" -> CiderFloatState.serializer()
            else -> BaseCiderState.serializer()
        }
    }

}