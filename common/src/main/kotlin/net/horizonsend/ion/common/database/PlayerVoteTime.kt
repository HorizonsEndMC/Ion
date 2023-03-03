package net.horizonsend.ion.common.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction

class PlayerVoteTime(id: EntityID<Int>) : IntEntity(id) {
	var player by PlayerData.EntityClass referencedOn Table.player
	var serviceName by Table.serviceName
	var dateTime by Table.dateTime

	companion object {
		fun new(block: PlayerVoteTime.() -> Unit) = transaction { EntityClass.new(block) }
	}

	internal object EntityClass : IntEntityClass<PlayerVoteTime>(Table)

	internal object Table : IntIdTable("player_vote_times") {
		val player = reference("player", PlayerData.Table).index()
		val serviceName = varchar("service_name", 32)
		var dateTime = datetime("datetime")
	}
}
