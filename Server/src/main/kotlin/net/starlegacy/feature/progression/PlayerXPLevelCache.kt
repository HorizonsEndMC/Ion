package net.starlegacy.feature.progression

import com.mongodb.client.model.changestream.ChangeStreamDocument
import net.starlegacy.SLComponent
import net.starlegacy.database.get
import net.starlegacy.database.int
import net.starlegacy.database.schema.misc.SLPlayer
import net.starlegacy.database.schema.misc.SLPlayerId
import net.starlegacy.database.slPlayerId
import net.starlegacy.database.uuid
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.sql.Timestamp
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

object PlayerXPLevelCache : SLComponent() {
	private val cacheThread: ExecutorService = Executors.newSingleThreadExecutor()

	override fun onEnable() {
		// cache data for online players, to avoid having to look them up later
		for (player in Bukkit.getOnlinePlayers()) {
			cachePlayerData(player.uniqueId)
		}

		// automatically update xp and level
		SLPlayer.watchUpdates { change: ChangeStreamDocument<SLPlayer> ->
			val id: SLPlayerId = change.slPlayerId

			change[SLPlayer::xp]?.int() // get the changed xp value
				?.let { xp -> map[id.uuid]?.cachedXP = xp } // if updated, update the cached xp (if present)

			change[SLPlayer::level]?.int() // get the changed level value
				?.let { lvl -> map[id.uuid]?.cachedLevel = lvl } // attempt to update the cached level as well
		}
	}

	override fun onDisable() {
		cacheThread.shutdown()
	}

	/** A data class which stores cached data on online players.
	 * Shouldn't be updated directly, database should be updated and caches should be refreshed. */
	data class CachedAdvancePlayer(val uniqueId: UUID, var cachedXP: Int, var cachedLevel: Int) {
		val xp get() = cachedXP
		val level get() = cachedLevel
	}

	private val map = ConcurrentHashMap<UUID, CachedAdvancePlayer>()

	@Synchronized
	private fun cachePlayerData(uniqueId: UUID) = map.computeIfAbsent(uniqueId) {
		val (xp: Int, level: Int) = SLPlayer.getXPAndLevel(uniqueId.slPlayerId)
			?: error("Cached advancement data had no matching database mirror! UUID $uniqueId")

		return@computeIfAbsent CachedAdvancePlayer(uniqueId, xp, level)
	}

	private fun onJoin(playerID: UUID) {
		cachePlayerData(playerID)
		Levels.markForCheck(playerID)
	}

	/** Retrieve data from the database and fill the player's cache with it when they join */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun onPreLogin(event: AsyncPlayerPreLoginEvent) = onJoin(event.uniqueId)

	@EventHandler
	fun onJoin(event: PlayerJoinEvent) {
		val playerID = event.player.uniqueId
		if (!map.containsKey(playerID)) {
			onJoin(playerID)
			log.warn("Had to load ${event.player.name}'s level/xp data on the main thread")
		}
	}

	/** Dispose of a player's cache when they log out */
	@EventHandler
	fun onQuit(event: PlayerQuitEvent) {
		map.remove(event.player.uniqueId)
	}

	/** Retrieve an online player's cached player data. Throws an exception if it is missing */
	operator fun get(player: Player): CachedAdvancePlayer = map[player.uniqueId] ?: error(
		"Missing cached player data for (online?) player ${player.name}." +
			" (isOnline = ${player.isOnline}, lastPlayed = ${Timestamp(player.lastPlayed).toLocalDateTime()})"
	)

	/** Gets the cached data if it is present for that player ID */
	operator fun get(playerID: UUID): CachedAdvancePlayer? {
		return map[playerID]
	}

	/** Sets the player's level in the database, then updates their permission group */
	fun setLevel(playerID: UUID, newLevel: Int) {
		require(newLevel in 0..MAX_LEVEL)
		{ "Level $newLevel is not within the level range! Max Level: $MAX_LEVEL" }

		SLPlayer.setLevel(playerID.slPlayerId, newLevel)
	}

	/** Calls database and gets the player's level. Shouldn't be called from the main thread. */
	fun fetchLevel(uniqueId: UUID): Int {
		return SLPlayer.getLevel(uniqueId.slPlayerId) ?: error("Expected $uniqueId to exist")
	}

	/** Calls database and gets the player's XP. Shouldn't be called from the main thread. */
	fun fetchSLXP(uniqueId: UUID): Int {
		return SLPlayer.getXP(uniqueId.slPlayerId) ?: error("Expected $uniqueId to exist")
	}

	/** Adds XP to a player in the database, then refreshes the changes */
	fun addSLXP(uniqueId: UUID, amount: Int) {
		SLPlayer.addXP(uniqueId.slPlayerId, amount)
	}

	/** Sets a player's XP in the database, then refreshes the changes */
	fun setSLXP(uniqueId: UUID, amount: Int) {
		SLPlayer.setXP(uniqueId.slPlayerId, amount)
	}

	/** Runs an async task on the player cache thread */
	fun async(block: PlayerXPLevelCache.() -> Unit): Future<*> = cacheThread.submit {
		block.invoke(this@PlayerXPLevelCache)
	}

	override fun supportsVanilla(): Boolean {
		return true
	}
}