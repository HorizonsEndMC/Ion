package net.horizonsend.ion.common.database

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import net.horizonsend.ion.common.database.OriginalDatabaseConfiguration.DatabaseType
import net.horizonsend.ion.common.database.collections.PlayerData
import java.io.File
import net.horizonsend.ion.common.loadConfiguration
import org.bson.UuidRepresentation
import org.litote.kmongo.KMongo.createClient
import org.litote.kmongo.util.KMongoJacksonFeature
import java.lang.System.setProperty

fun initializeDatabase(dataDirectory: File) {
	val configuration: DatabaseConfiguration = loadConfiguration(dataDirectory.resolve("shared"), "database.conf")

	setProperty("org.litote.mongo.test.mapping.service", "org.litote.kmongo.jackson.JacksonClassMappingTypeService")

	KMongoJacksonFeature.setUUIDRepresentation(UuidRepresentation.STANDARD)

	val client = createClient(
		MongoClientSettings
			.builder()
			.uuidRepresentation(UuidRepresentation.STANDARD)
			.applyConnectionString(ConnectionString(configuration.mongoConnectionUri))
			.build()
	)

	val database = client.getDatabase(configuration.databaseName)

	PlayerData.initialize(database)

	// Begin Temporary Migration Code
	val originalConfiguration: OriginalDatabaseConfiguration = loadConfiguration(dataDirectory.resolve("shared"), "common.conf")

	if (originalConfiguration.databaseType == DatabaseType.MYSQL) {
		originalConfiguration.databaseDetails.run {
			org.jetbrains.exposed.sql.Database.connect("jdbc:mysql://$host:$port/$database", "com.mysql.cj.jdbc.Driver", username, password)
		}

		org.jetbrains.exposed.sql.transactions.transaction {
			for (playerData in net.horizonsend.ion.common.database.sql.PlayerData.all()) {
				PlayerData[playerData.id.value].update {
					discordId = playerData.discordUUID
					minecraftUsername = playerData.mcUsername
					achievements = playerData.achievements.toMutableList()
				}
			}
		}
	}
	// End Temporary Migration Code
}