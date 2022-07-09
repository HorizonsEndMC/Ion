package net.horizonsend.ion.common.database

import java.util.UUID
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.transactions.transaction

object Players : UUIDTable(columnName = "mcUUID") {
	val mcUsername: Column<String> = varchar("mcUsername", 16).uniqueIndex()
}

class Player(mcUUID: EntityID<UUID>) : UUIDEntity(mcUUID) {
	companion object : UUIDEntityClass<Player>(Players) {
		fun getOrCreate(mcUUID: UUID, mcUsername: String) = transaction {
			findById(mcUUID) ?: new(mcUUID) { this.mcUsername = mcUsername }
		}
	}

	var mcUsername by Players.mcUsername
}