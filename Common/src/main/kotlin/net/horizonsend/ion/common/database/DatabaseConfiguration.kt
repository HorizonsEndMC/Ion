package net.horizonsend.ion.common.database

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment

@ConfigSerializable
internal data class DatabaseConfiguration(
	@Comment("See https://www.mongodb.com/docs/manual/reference/connection-string/")
	internal val mongoConnectionUri: String = "mongodb://test:test@mongo",

	internal val databaseName: String = "ion"
)