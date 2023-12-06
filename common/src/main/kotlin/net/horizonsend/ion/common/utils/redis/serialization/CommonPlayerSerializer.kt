package net.horizonsend.ion.common.utils.redis.serialization

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import net.horizonsend.ion.common.extensions.CommonPlayer
import net.horizonsend.ion.common.utils.redis.types.CommonPlayerData
import java.lang.reflect.Type
import java.util.UUID

object CommonPlayerSerializer : JsonSerializer<CommonPlayer>, JsonDeserializer<CommonPlayer> {
	override fun serialize(src: CommonPlayer, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
		return JsonPrimitive("CommonPlayer:${src.name}:${src.uniqueId}")
	}

	@Throws(JsonParseException::class)
	override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): CommonPlayer {
		val string = json.asString

		val name = string.substringAfter("CommonPlayer:").substringBefore(":")
		val uuid = string.substringAfterLast(":")

		return CommonPlayerData(UUID.fromString(uuid), name)
	}
}


