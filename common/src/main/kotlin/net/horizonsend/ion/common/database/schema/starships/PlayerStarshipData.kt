package net.horizonsend.ion.common.database.schema.starships

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.StarshipTypeDB
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import org.litote.kmongo.contains
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.ensureUniqueIndex
import org.litote.kmongo.eq
import org.litote.kmongo.or

/**
 * This can either represent an unpiloted ship, which is stored in database,
 * or a piloted ship, which is only stored in memory.
 *
 * In general this should be cached and only one instance should exist per ship, even if it is piloted and
 * not currently in the database.
 */
data class PlayerStarshipData(
	override val _id: Oid<PlayerStarshipData>,
	/** Player UUID of the captain of the ship */
	var captain: SLPlayerId,

	override var starshipType: StarshipTypeDB,

	override var serverName: String?,
	override var levelName: String,
	override var blockKey: Long,

	/** UUIDs of players who have been added to the ship by the captain. Should never include the captain. */
	val pilots: MutableSet<SLPlayerId> = mutableSetOf(),
	var name: String? = null,
	/** Chunk combined coordinates, of each chunk the detected blocks reside in */
	override var containedChunks: Set<Long>? = null,

	override var lastUsed: Long = System.currentTimeMillis(),
	override var isLockEnabled: Boolean = false
) : StarshipData {
	companion object : StarshipDataCompanion<PlayerStarshipData>(
		PlayerStarshipData::class,
		PlayerStarshipData::serverName,
		PlayerStarshipData::levelName,
		PlayerStarshipData::blockKey,
		setup = {
			ensureIndex(PlayerStarshipData::captain)
			ensureIndex(PlayerStarshipData::pilots)
			ensureIndex(PlayerStarshipData::serverName)
			ensureIndex(PlayerStarshipData::serverName)
			ensureUniqueIndex(PlayerStarshipData::levelName, PlayerStarshipData::blockKey)
		}
	) {
		const val LOCK_TIME_MS = 1_000 * 60 * 5

		fun findByPilot(playerId: SLPlayerId) =
			find(or(PlayerStarshipData::captain eq playerId, PlayerStarshipData::pilots contains playerId))
	}

	/** assumes that it's also deactivated */
	fun isLockActive(): Boolean {
		return isLockEnabled && System.currentTimeMillis() - lastUsed >= LOCK_TIME_MS
	}
}
