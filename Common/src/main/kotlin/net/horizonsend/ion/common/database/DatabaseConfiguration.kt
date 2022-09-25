package net.horizonsend.ion.common.database

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment

@ConfigSerializable
internal data class DatabaseConfiguration(
	@Comment("Store data in \"common/data.json\"? Only use for testing!")
	internal val useJsonFile: Boolean = true,

	@Comment("See https://www.mongodb.com/docs/manual/reference/connection-string/")
	internal val mongoConnectionUri: String,

	internal val databaseName: String
)