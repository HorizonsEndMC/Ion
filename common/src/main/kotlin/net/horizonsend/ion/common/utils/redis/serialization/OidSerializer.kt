package net.horizonsend.ion.common.utils.redis.serialization

import com.google.gson.InstanceCreator
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import net.horizonsend.ion.common.database.Oid
import org.bson.types.ObjectId
import java.lang.reflect.Type

object OidSerializer : JsonSerializer<Oid<*>>, JsonDeserializer<Oid<*>>, InstanceCreator<Oid<*>> {
	override fun serialize(src: Oid<*>, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
		return JsonPrimitive(src.toString())
	}

	@Throws(JsonParseException::class)
	override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Oid<*> {
		return Oid<Any>(json.asString)
	}

	override fun createInstance(type: Type): Oid<*> {
		return Oid<Any>(ObjectId())
	}
}
