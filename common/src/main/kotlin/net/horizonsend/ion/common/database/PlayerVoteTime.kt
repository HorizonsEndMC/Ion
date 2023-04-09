package net.horizonsend.ion.common.database

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

class PlayerVoteTime(id: EntityID<Int>) : Entity<Int>(id) {
	var player by PlayerData referencedOn Table.player
	var serviceName by Table.serviceName
	var dateTime by Table.dateTime

	companion object : IonEntityClass<Int, PlayerVoteTime>(Table, PlayerVoteTime::class.java, ::PlayerVoteTime)

	internal object Table : IntIdTable("player_vote_times") {
		val player = reference("player", PlayerData.Table).index()
		val serviceName = varchar("service_name", 32)
		var dateTime = datetime("datetime")
	}
}
