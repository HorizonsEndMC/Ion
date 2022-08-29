package net.horizonsend.ion.common

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class CommonConfiguration(
	val databaseType: DatabaseType = DatabaseType.SQLITE,
	val databaseDetails: DatabaseDetails = DatabaseDetails()
) {
	enum class DatabaseType { SQLITE, MYSQL }

	@ConfigSerializable
	data class DatabaseDetails(
		val host: String = "localhost",
		val port: Int = 3306,
		val database: String = "ion",
		val username: String = "ion",
		val password: String = "password"
	)
}