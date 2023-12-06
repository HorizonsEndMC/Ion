package net.horizonsend.ion.common.utils.redis.serialization

import com.google.gson.InstanceCreator
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import java.lang.reflect.Type

object ComponentSerializer : JsonSerializer<Component>, JsonDeserializer<Component>, InstanceCreator<Component> {
	override fun serialize(src: Component, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
		return JsonPrimitive(GsonComponentSerializer.gson().serialize(src))
	}

	@Throws(JsonParseException::class)
	override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Component {
		return GsonComponentSerializer.gson().deserialize(json.asString)
	}

	override fun createInstance(type: Type): Component {
		return Component.text("")
	}
}
