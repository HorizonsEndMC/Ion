package net.horizonsend.ion.common.database

import net.horizonsend.ion.common.database.PlayerData.EntityClass.referrersOn
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class PlayerData(uuid: EntityID<UUID>) : UUIDEntity(uuid) {
	val uuid by Table.uuid
	var username by Table.username
	var snowflake by Table.snowflake

	val achievements by PlayerAchievement.EntityClass referrersOn PlayerAchievement.Table.player
	val voteTimes by PlayerVoteTime.EntityClass referrersOn PlayerVoteTime.Table.player

	var acceptedBounty by Table.acceptedBounty
	var bounty by Table.bounty

	var particle by Table.particle
	var color by Table.color

	companion object {
		operator fun get(uuid: UUID): PlayerData? = transaction { EntityClass.findById(uuid) }

		operator fun get(snowflake: Long): PlayerData? = transaction {
			EntityClass.find(Table.snowflake eq snowflake).firstOrNull()
		}

		operator fun get(username: String): PlayerData? = transaction {
			//                                   v                       v why are these different kotlin :catstare:
			EntityClass.find(Table.username.lowerCase() eq username.lowercase()).firstOrNull()
		}

		fun new(uuid: UUID, block: PlayerData.() -> Unit): PlayerData = transaction { EntityClass.new(uuid, block) }
	}

	internal object EntityClass : UUIDEntityClass<PlayerData>(Table)

	internal object Table : UUIDTable("player_data", "uuid") {
		val uuid = id
		val username = varchar("username", 16).uniqueIndex()
		val snowflake = long("snowflake").nullable().uniqueIndex()

		val acceptedBounty = uuid("accepted_bounty").nullable()
		val bounty = integer("bounty").default(0)

		val particle = varchar("particle", 24).nullable()
		val color = integer("color").nullable()
	}
}
