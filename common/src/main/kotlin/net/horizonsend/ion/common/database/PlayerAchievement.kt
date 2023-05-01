package net.horizonsend.ion.common.database

import net.horizonsend.ion.common.database.enums.Achievement
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.ReferenceOption.CASCADE

class PlayerAchievement(id: EntityID<Int>) : Entity<Int>(id) {
	var player by PlayerData referencedOn Table.player
	var achievement by Table.achievement

	companion object : IonEntityClass<Int, PlayerAchievement>(Table, PlayerAchievement::class.java, ::PlayerAchievement)

	object Table : IdTable<Int>("player_achievements") {
		override val id = integer("id").autoIncrement().entityId()
		val player = reference("player", PlayerData.Table, onDelete = CASCADE).index()
		val achievement = enumerationByName<Achievement>("achievement", 18)

		override val primaryKey = PrimaryKey(id)
	}
}
