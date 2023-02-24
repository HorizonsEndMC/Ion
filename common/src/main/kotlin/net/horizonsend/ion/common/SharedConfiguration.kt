package net.horizonsend.ion.common

import kotlinx.serialization.Serializable

@Serializable
internal data class SharedConfiguration(
	internal val mongoConnectionUri: String = "mongodb://test:test@mongo",
	internal val redisConnectionUri: String = "redis",
	internal val databaseName: String = "ion"
)
