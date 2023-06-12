package net.horizonsend.ion.common.database

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.id.IdTable

class PlayerData(uuid: EntityID<UUID>) : Entity<UUID>(uuid) {
	val uuid by Table.uuid
	var username by Table.username
	var snowflake by Table.snowflake
	var wasKilled by Table.wasKilled

	val achievements by PlayerAchievement referrersOn PlayerAchievement.Table.player
	val cryopods by Cryopod referrersOn Cryopod.Table.owner

	var particle by Table.particle
	var color by Table.color
	val selectedCryopod get() = cryopods.find { it.active }

	companion object : IonEntityClass<UUID, PlayerData>(Table, PlayerData::class.java, ::PlayerData) {
		operator fun get(snowflake: Long): PlayerData? = find(Table.snowflake eq snowflake).firstOrNull()
		operator fun get(username: String): PlayerData? = find(Table.username leq username).firstOrNull()
	}

	object Table : IdTable<UUID>("player_data") {
		val uuid = uuid("uuid").entityId()
		val username = varchar("username", 16).uniqueIndex()
		val snowflake = long("snowflake").nullable().uniqueIndex()
		val wasKilled = bool("wasKilled").default(false)

		val particle = varchar("particle", 24).nullable()
		val color = integer("color").nullable()

		override val id = uuid
		override val primaryKey = PrimaryKey(uuid)
	}
}
