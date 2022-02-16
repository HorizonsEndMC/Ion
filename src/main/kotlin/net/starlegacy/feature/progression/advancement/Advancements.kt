package net.starlegacy.feature.progression.advancement

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set
import net.minecraft.server.v1_16_R3.AdvancementProgress
import net.starlegacy.SLComponent
import net.starlegacy.database.schema.misc.SLPlayer
import net.starlegacy.database.slPlayerId
import net.starlegacy.util.*
import net.starlegacy.util.redisaction.RedisAction
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.json.JSONObject

class AdvancementConfig(val baseCost: Double = 1000.0)

internal lateinit var advancementBalancing: AdvancementConfig

object Advancements : SLComponent() {
	private val synchronizationTiming = timing("Advancement Vanilla Synchronization")

	lateinit var namespace: String

	private val playerAdvancementCache = ConcurrentHashMap<UUID, Set<SLAdvancement>>()

	fun reloadConfig() {
		advancementBalancing = loadConfig(plugin.sharedDataFolder, "advancement_balancing")
	}


	override fun onEnable() {
		reloadConfig()

		namespace = NamespacedKey(plugin, "temp").namespace

		val advancementKeys = mutableSetOf<NamespacedKey>() // keep track of the advancements that exist

		for (category in SLAdvancementCategory.values()) {
			advancementKeys.add(category.namespacedKey)
			if (Bukkit.getAdvancement(category.namespacedKey) == null) Bukkit.getUnsafe().loadAdvancement(
				category.namespacedKey, JSONObject(
					mapOf(
						"display" to mapOf(
							"icon" to mapOf("item" to category.icon.key.toString()),
							"title" to mapOf("text" to category.title),
							"description" to mapOf("text" to category.description),
							"background" to category.background,
							"frame" to "task",
							"show_toast" to false,
							"announce_to_chat" to false
						),
						"criteria" to mapOf("auto" to mapOf("trigger" to "minecraft:location"))
					)
				).toString()
			)
		}

		// register each advancement defined in SLTechAdvancement
		SLAdvancement.values().forEach {
			advancementKeys.add(it.namespacedKey)
			if (Bukkit.getAdvancement(it.namespacedKey) == null) Bukkit.getUnsafe().loadAdvancement(
				it.namespacedKey, JSONObject(
					mapOf(
						"parent" to (it.parent?.namespacedKey ?: it.category.namespacedKey).toString(),
						"display" to mapOf(
							"icon" to mapOf("item" to it.icon.key.toString()),
							"title" to mapOf("text" to it.title + "....................................."),
							"description" to mapOf("text" to it.description),
							"frame" to "task"
						),
						"criteria" to mapOf("0" to mapOf("trigger" to "minecraft:impossible"))
					)
				).toString()
			)
		}

		// delete all the old advancements that were removed
		Bukkit.advancementIterator().asSequence()
			.filter { it.key.namespace == namespace }
			.filter { !advancementKeys.contains(it.key) }
			.forEach { advancement ->
				val namespacedKey: NamespacedKey = advancement.key

				SLPlayer.removeAdvancementGlobally(namespacedKey.key)

				@Suppress("DEPRECATION") // only way to do this atm
				Bukkit.getUnsafe().removeAdvancement(namespacedKey)
				log.info("Deleted $namespacedKey")
			}

		// update people's advancements from database
		Bukkit.getOnlinePlayers().forEach { player ->
			fetchAdvancements(player.uniqueId)
			giveBasicAdvancements(player)
		}

		log.info("Loaded ${advancementKeys.size} advancements: ${advancementKeys.joinToString()}")

		Bukkit.reloadData() // Update advancement on the server, so that they are actually loaded

		Bukkit.getOnlinePlayers().forEach {
			syncVanillaAdvancements(it)
		}

		plugin.listen<AsyncPlayerPreLoginEvent>(priority = EventPriority.MONITOR, ignoreCancelled = true) { event ->
			val playerId = event.uniqueId
			fetchAdvancements(playerId)
		}

		plugin.listen<PlayerJoinEvent> { event ->
			val player = event.player

			if (!playerAdvancementCache.containsKey(player.uniqueId)) {
				fetchAdvancements(player.uniqueId)
				log.warn("Had to load ${player.name}'s advancement data on the main thread!")
			}

			giveBasicAdvancements(player)
			syncVanillaAdvancements(player)
		}

		plugin.listen<PlayerQuitEvent> { event -> playerAdvancementCache.remove(event.player.uniqueId) }
	}

	/**
	 * Pulls the advancements of the specified player from the database and inserts them into the cache.
	 */
	private fun fetchAdvancements(playerId: UUID) {
		val advancements: Set<SLAdvancement> = SLPlayer[playerId.slPlayerId]
			?.unlockedAdvancements
			?.map { SLAdvancement.valueOf(it.uppercase(Locale.getDefault())) }
			?.toSet()
			?: setOf()

		playerAdvancementCache[playerId] = advancements
	}

	private fun giveBasicAdvancements(player: Player) {
		val freeAdvancements = SLAdvancement.freeAdvancements

		val advancements = freeAdvancements.filter {
			!has(
				player,
				it
			) && it.hasParents(player)
		}
			.takeIf { it.isNotEmpty() } ?: return

		giveAdvancementAsync(
			player.uniqueId,
			*advancements.toTypedArray()
		)
	}

	/**
	 * Updates the player's vanilla advancements, removes ones they no longer have and adds ones they do have
	 * Can only be called on the main thread
	 */
	private fun syncVanillaAdvancements(player: Player) = synchronizationTiming.time {
		require(plugin.server.isPrimaryThread)

		val nmsPlayer: NMSPlayer = player.nms

		val oldAdvancements: List<NMSAdvancement> = nmsPlayer.advancementData.data.keys
			.filter { it.name.namespace == namespace && nmsPlayer.advancementData.getProgress(it).isDone }

		val newAdvancements: Set<SLAdvancement> = Advancements[player]

		val oldNames: Set<String> = oldAdvancements.asSequence().map { it.name.key }.toSet()
		val newNames: Set<String> = newAdvancements.asSequence().map { it.advancementKey }.toSet()

		val removed: List<NMSAdvancement> = oldAdvancements.filter { !newNames.contains(it.name.key) }
		val added: List<SLAdvancement> = newAdvancements.filter { !oldNames.contains(it.advancementKey) }

		removed.forEach { nmsAdvancement ->
			val progress: AdvancementProgress = nmsPlayer.advancementData.getProgress(nmsAdvancement)

			progress.awardedCriteria.forEach { criteria ->
				nmsPlayer.advancementData.revokeCritera(nmsAdvancement, criteria)
			}
		}

		added.forEach { advancement ->
			val progress: AdvancementProgress = nmsPlayer.advancementData.getProgress(advancement.nmsAdvancement)

			if (!progress.isDone) {
				progress.remainingCriteria.forEach { criteria ->
					nmsPlayer.advancementData.grantCriteria(advancement.nmsAdvancement, criteria)
				}
			}
		}
	}

	/** Get the cached advancements of the given player */
	operator fun get(player: Player): Set<SLAdvancement> = playerAdvancementCache[player.uniqueId]
		?: error("No cached advancements for player ${player.name}")


	/**
	 * Check the player's cache to see if it has the advancement unlocked.
	 * Requires the player to be online.
	 *
	 * @param player The player to check
	 * @param advancement The advancement to check for
	 * @return True if the player's cache advancement set contains the advancement, else false.
	 */
	fun has(player: Player, advancement: SLAdvancement): Boolean {
		require(player.isOnline)
		return this[player].contains(advancement)
	}

	private val refreshPlayerAdvancements: RedisAction<UUID> = { playerID: UUID ->
		if (playerAdvancementCache.containsKey(playerID)) { // only if they're cached
			Tasks.async {
				fetchAdvancements(playerID) // get the advancements from the database

				// on the main thread, refresh their vanilla advancement mirror
				Tasks.sync {
					Bukkit.getPlayer(playerID)?.let { player ->
						syncVanillaAdvancements(player)
					}
				}
			}
		}
	}.registerRedisAction("refresh_player_advancements")

	private fun giveAdvancementAsync(playerID: UUID, vararg advancements: SLAdvancement) {
		Tasks.async {
			giveAdvancementBlocking(playerID, *advancements)
		}
	}

	fun giveAdvancementBlocking(playerID: UUID, vararg advancements: SLAdvancement) {
		SLPlayer.addAdvancement(playerID.slPlayerId, *advancements.map { it.advancementKey }.toTypedArray())
		refreshPlayerAdvancements(playerID)
	}

	override fun supportsVanilla(): Boolean {
		return true
	}
}
