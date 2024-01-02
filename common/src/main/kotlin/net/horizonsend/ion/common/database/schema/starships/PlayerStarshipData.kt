package net.horizonsend.ion.common.database.schema.starships

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.OidDbObjectCompanion
import net.horizonsend.ion.common.database.StarshipTypeDB
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.server.features.nations.utils.isInactive
import org.litote.kmongo.contains
import org.litote.kmongo.deleteOneById
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

    var starshipType: StarshipTypeDB,

    var serverName: String?,
    var levelName: String,
    var blockKey: Long,

    /** UUIDs of players who have been added to the ship by the captain. Should never include the captain. */
	val pilots: MutableSet<SLPlayerId> = mutableSetOf(),
    var name: String? = null,
    /** Chunk combined coordinates, of each chunk the detected blocks reside in */
	var containedChunks: Set<Long>? = null,

    var lastUsed: Long = System.currentTimeMillis(),
    var isLockEnabled: Boolean = false
) : DbObject {
	companion object : OidDbObjectCompanion<PlayerStarshipData>(PlayerStarshipData::class, setup = {
		ensureIndex(PlayerStarshipData::captain)
		ensureIndex(PlayerStarshipData::pilots)
		ensureIndex(PlayerStarshipData::name)
		ensureIndex(PlayerStarshipData::serverName)
		ensureIndex(PlayerStarshipData::levelName)
		ensureUniqueIndex(PlayerStarshipData::levelName, PlayerStarshipData::blockKey)
	}) {
		const val LOCK_TIME_MS = 1_000 * 60 * 5

		fun add(data: PlayerStarshipData) {
			col.insertOne(data)
		}

		fun remove(dataId: Oid<PlayerStarshipData>) {
			col.deleteOneById(dataId)
		}

		fun findByPilot(playerId: SLPlayerId) =
			find(or(PlayerStarshipData::captain eq playerId, PlayerStarshipData::pilots contains playerId))
	}

	/** assumes that it's also deactivated */
	fun isLockActive(): Boolean {
		return isLockEnabled && System.currentTimeMillis() - lastUsed >= LOCK_TIME_MS && !isInactive(captain.lastSeen)
	}
}
