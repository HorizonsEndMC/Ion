package net.horizonsend.ion.common.utils.redis.serialization

import com.google.gson.InstanceCreator
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import net.horizonsend.ion.common.extensions.CommonPlayer
import net.horizonsend.ion.common.utils.redis.types.CommonPlayerData
import java.lang.reflect.Type
import java.util.UUID

object CommonPlayerSerializer : JsonSerializer<CommonPlayer>, JsonDeserializer<CommonPlayer>, InstanceCreator<CommonPlayer> {
	override fun serialize(src: CommonPlayer, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
		val node = JsonObject()

		node.add("uuid", JsonPrimitive(src.uniqueId.toString()))
		node.add("name", JsonPrimitive(src.name))

		return node
	}

	@Throws(JsonParseException::class)
	override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): CommonPlayer {
		val string = json.asJsonObject

		val uuid = string.get("uuid").asString
		val name = string.get("name").asString

		return CommonPlayerData(UUID.fromString(uuid), name)
	}

	override fun createInstance(type: Type?): CommonPlayer {
		return CommonPlayerData(UUID.randomUUID(), "blean")
	}
}


