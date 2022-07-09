package net.horizonsend.ion.common.database

import java.util.UUID
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.transactions.transaction

object Players : UUIDTable(columnName = "minecraftUUID") {
	val minecraftUsername: Column<String> = varchar("minecraftUsername", 16).uniqueIndex()
}

class Player(minecraftUUID: EntityID<UUID>) : UUIDEntity(minecraftUUID) {
	companion object : UUIDEntityClass<Player>(Players) {
		fun getOrCreate(minecraftUUID: UUID, minecraftUsername: String) = transaction {
			findById(minecraftUUID) ?: new(minecraftUUID) { this.mcUsername = minecraftUsername }
		}
	}

	var mcUsername by Players.minecraftUsername
}