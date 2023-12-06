package net.horizonsend.ion.common.utils.redis.serialization

import com.google.gson.GsonBuilder
import net.horizonsend.ion.common.database.Oid
import net.kyori.adventure.text.Component
import java.lang.reflect.Type

object RedisSerialization {
	private val gson = GsonBuilder()
		.registerTypeAdapter(Oid::class.java, OidSerializer)
		.registerTypeAdapter(Component::class.java, ComponentSerializer)
		.create()

	fun <T> serialize(data: T, type: Type): String = gson.toJson(data, type)

	fun <T> readTyped(content: String, type: Type): T = gson.fromJson(content, type)

	fun read(content: String, type: Type): Any = gson.fromJson(content, type)
}
