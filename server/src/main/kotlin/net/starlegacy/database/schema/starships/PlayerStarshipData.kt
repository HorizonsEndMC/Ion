package net.starlegacy.database.schema.starships

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.starlegacy.database.DbObject
import net.starlegacy.database.Oid
import net.starlegacy.database.OidDbObjectCompanion
import net.starlegacy.database.schema.misc.SLPlayerId
import net.starlegacy.database.slPlayerId
import net.starlegacy.feature.starship.StarshipType
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player
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

	var starshipType: StarshipType,

	var serverName: String?,
	var levelName: String,
	var blockKey: Long,
	val subShips: MutableMap<Long, LongOpenHashSet>,

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
		ensureIndex(PlayerStarshipData::subShips)
		ensureIndex(PlayerStarshipData::serverName)
		ensureIndex(PlayerStarshipData::levelName)
		ensureUniqueIndex(PlayerStarshipData::levelName, PlayerStarshipData::blockKey)
	}) {
		const val LOCK_TIME_MS = 1_000 * 300

		fun add(data: PlayerStarshipData) {
			col.insertOne(data)
		}

		fun remove(dataId: Oid<PlayerStarshipData>) {
			col.deleteOneById(dataId)
		}

		fun findByPilot(playerId: SLPlayerId) =
			find(or(PlayerStarshipData::captain eq playerId, PlayerStarshipData::pilots contains playerId))
	}

	fun bukkitWorld(): World = requireNotNull(Bukkit.getWorld(levelName)) {
		"World $levelName is not loaded, but tried getting it for computer $_id"
	}

	fun isPilot(player: Player): Boolean {
		val id = player.slPlayerId
		return captain == id || pilots.contains(id)
	}

	/** assumes that it's also deactivated */
	fun isLockActive(): Boolean {
		return isLockEnabled && System.currentTimeMillis() - lastUsed >= LOCK_TIME_MS
	}
}
