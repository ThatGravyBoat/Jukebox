package tech.thatgravyboat.jukebox.utils

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

internal val JsonElement?.asFloat: Float? get() = (this as? JsonPrimitive)?.content?.toFloatOrNull()
internal val JsonElement?.asInt: Int? get() = (this as? JsonPrimitive)?.content?.toIntOrNull()
internal val JsonElement?.asObject: JsonObject? get() = this as? JsonObject