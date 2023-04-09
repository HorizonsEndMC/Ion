package net.horizonsend.ion.common.database

import net.horizonsend.ion.common.database.enums.Achievement
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction

class PlayerAchievement(id: EntityID<Int>) : Entity<Int>(id) {
	var player by PlayerData referencedOn Table.player
	var achievement by Table.achievement

	companion object : IonEntityClass<Int, PlayerAchievement>(Table, PlayerAchievement::class.java, ::PlayerAchievement) {
		fun remove(player: PlayerData, achievement: Achievement) = transaction { Table.deleteWhere { (Table.player eq player.uuid) and (Table.achievement eq achievement) } }
	}

	object Table : IdTable<Int>("player_achievements") {
		override val id = integer("id").autoIncrement().entityId()
		val player = reference("player", PlayerData.Table).index()
		val achievement = enumerationByName<Achievement>("achievement", 18)

		override val primaryKey = PrimaryKey(id)
	}
}
