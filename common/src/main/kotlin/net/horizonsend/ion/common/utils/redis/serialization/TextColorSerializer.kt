package net.horizonsend.ion.common.utils.redis.serialization

import com.google.gson.InstanceCreator
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import java.lang.reflect.Type

object TextColorSerializer : JsonSerializer<TextColor>, JsonDeserializer<TextColor>, InstanceCreator<TextColor> {
	override fun serialize(src: TextColor, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
		return JsonPrimitive(src.value())
	}

	override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): TextColor {
		return TextColor.color(json.asInt)
	}

	override fun createInstance(type: Type?): TextColor {
		return NamedTextColor.WHITE
	}
}
