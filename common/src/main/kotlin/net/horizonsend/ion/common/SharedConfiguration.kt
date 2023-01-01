package net.horizonsend.ion.common

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment

@ConfigSerializable
internal data class SharedConfiguration(
	@Comment("See https://www.mongodb.com/docs/manual/reference/connection-string/")
	internal val mongoConnectionUri: String = "mongodb://test:test@mongo",
	internal val redisConnectionUri: String = "redis",
	internal val databaseName: String = "ion"
)
