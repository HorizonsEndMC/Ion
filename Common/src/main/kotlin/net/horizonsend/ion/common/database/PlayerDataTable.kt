package net.horizonsend.ion.common.database

import java.util.UUID
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column

object PlayerDataTable : UUIDTable(columnName = "minecraftUUID") {
	val minecraftUsername: Column<String> = varchar("minecraftUsername", 16).uniqueIndex()
	val discordUUID: Column<Long?> = long("discordUUID").nullable().uniqueIndex()
}

class PlayerData(minecraftUUID: EntityID<UUID>) : UUIDEntity(minecraftUUID) {
	companion object : UUIDEntityClass<PlayerData>(PlayerDataTable) {
		fun getOrCreate(minecraftUUID: UUID, minecraftUsername: String) =
			findById(minecraftUUID) ?: new(minecraftUUID) { this.mcUsername = minecraftUsername }
	}

	var mcUsername by PlayerDataTable.minecraftUsername
	var discordUUID by PlayerDataTable.discordUUID
}