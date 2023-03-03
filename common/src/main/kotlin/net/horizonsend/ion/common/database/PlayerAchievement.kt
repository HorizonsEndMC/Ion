package net.horizonsend.ion.common.database

import net.horizonsend.ion.common.database.enums.Achievement
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction

class PlayerAchievement(id: EntityID<Int>) : IntEntity(id) {
	var player by PlayerData.EntityClass referencedOn Table.player
	var achievement by Table.achievement

	companion object {
		fun new(block: PlayerAchievement.() -> Unit) = transaction { EntityClass.new(block) }

		fun remove(player: PlayerData, achievement: Achievement) = transaction { Table.deleteWhere { (Table.player eq player.uuid) and (Table.achievement eq achievement) } }
	}

	internal object EntityClass : IntEntityClass<PlayerAchievement>(Table)

	internal object Table : IntIdTable("player_achievements") {
		val player = reference("player", PlayerData.Table).index()
		val achievement = enumerationByName<Achievement>("achievement", 18)
	}
}
