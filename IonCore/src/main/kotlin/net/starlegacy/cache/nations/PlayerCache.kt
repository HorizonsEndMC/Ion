package net.starlegacy.cache.nations

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import net.starlegacy.cache.ManualCache
import net.starlegacy.database.DbObject
import net.starlegacy.database.Oid
import net.starlegacy.database.containsUpdated
import net.starlegacy.database.get
import net.starlegacy.database.nullable
import net.starlegacy.database.oid
import net.starlegacy.database.schema.misc.SLPlayer
import net.starlegacy.database.schema.misc.SLPlayerId
import net.starlegacy.database.schema.nations.Nation
import net.starlegacy.database.schema.nations.NationRole
import net.starlegacy.database.schema.nations.Role
import net.starlegacy.database.schema.nations.RoleCompanion
import net.starlegacy.database.schema.nations.Settlement
import net.starlegacy.database.schema.nations.SettlementRole
import net.starlegacy.database.slPlayerId
import net.starlegacy.database.uuid
import net.starlegacy.feature.nations.region.Regions
import net.starlegacy.util.Tasks
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.litote.kmongo.`in`

object PlayerCache : ManualCache() {
	/** Values should only be set here*/
	data class PlayerData(
		val id: SLPlayerId,
		var settlement: Oid<Settlement>?,
		var nation: Oid<Nation>?,
		var settlementTag: String?,
		var nationTag: String?
	)

	private val PLAYER_DATA: MutableMap<UUID, PlayerData> = ConcurrentHashMap()

	override fun load() {
		PLAYER_DATA.clear()

		val onlinePlayerIds = Bukkit.getOnlinePlayers().map { it.uniqueId.slPlayerId }

		fun cache(id: SLPlayerId, data: SLPlayer) {
			val settlement: Oid<Settlement>? = data.settlement
			val nation: Oid<Nation>? = data.nation

			val settlementTag: String? = if (settlement == null) null
			else SettlementRole.getTag(id)

			val nationTag: String? = if (nation == null) null
			else NationRole.getTag(id)

			PLAYER_DATA[id.uuid] = PlayerData(id, settlement, nation, settlementTag, nationTag)
		}

		for (data in SLPlayer.find(SLPlayer::_id `in` onlinePlayerIds)) {
			cache(data._id, data)
		}

		// priority monitor so it happens after the insert in SLCore, which is at HIGHEST
		plugin.listen<AsyncPlayerPreLoginEvent>(priority = EventPriority.MONITOR, ignoreCancelled = true) { event ->
			val uuid = event.uniqueId
			cache(uuid.slPlayerId, checkNotNull(SLPlayer[uuid]))
		}

		// lowest in case anything uses this data in other join listeners, and since lowest join event
		// is always after monitor async player pre login event, though the async player pre login event
		// may not be called if the plugin is not registered when they initiate authentication, like in a restart
		plugin.listen<PlayerJoinEvent>(priority = EventPriority.LOWEST) { event ->
			if (!PLAYER_DATA.containsKey(event.player.uniqueId)) {
				event.player.kick(miniMessage().deserialize("<red>Failed to load data! Please try again."))
			}
		}

		plugin.listen<PlayerQuitEvent> { event ->
			PLAYER_DATA.remove(event.player.uniqueId)
		}

		// SLPlayers are inserted on join and never deleted, so listening to updates and joins is sufficient.
		SLPlayer.watchUpdates { change ->
			val id = change.slPlayerId

			change[SLPlayer::settlement]?.let {
				Tasks.sync {
					val data = PLAYER_DATA[id.uuid] ?: return@sync

					val newSettlement = it.nullable()?.oid<Settlement>()

					data.settlement = newSettlement
					data.settlementTag = null // when leaving/joining a settlement, you have no role either way
				}
			}

			change[SLPlayer::nation]?.let {
				Tasks.sync {
					val data = PLAYER_DATA[id.uuid] ?: return@sync

					val newNation = it.nullable()?.oid<Nation>()

					data.nation = newNation
					data.nationTag = null // when leaving/joining a nation, you have no role either way
				}
			}

			// refresh everything for a player if their settlement or nation changes
			if (change.containsUpdated(SLPlayer::settlement) || change.containsUpdated(SLPlayer::nation)) {
				Tasks.async {
					Regions.refreshPlayerLocally(id.uuid)
				}
			}
		}

		val mutex = Any()

		/** for each cached player, update their settlement tag if they're in a settlement & nation tag if in a nation */
		fun recalculateTags(): Unit = synchronized(mutex) {
			for ((id, data) in PLAYER_DATA) {
				val slPlayerId = id.slPlayerId
				data.settlement?.let { data.settlementTag = SettlementRole.getTag(slPlayerId) }
				data.nation?.let { data.nationTag = NationRole.getTag(slPlayerId) }
			}
		}

		fun <Parent : DbObject, T : Role<Parent, *>> watchRoles(companion: RoleCompanion<Parent, *, T>) {
			companion.watchUpdates {
				if (it.containsUpdated(companion.membersProperty)) {
					recalculateTags()
				}
			}

			companion.watchDeletes {
				recalculateTags()
			}
		}

		watchRoles(SettlementRole.Companion)
		watchRoles(NationRole.Companion)
	}

	fun getIfOnline(player: Player): PlayerData? = PLAYER_DATA[player.uniqueId]

	operator fun get(player: Player): PlayerData = PLAYER_DATA[player.uniqueId]
		?: error("Data wasn't cached for ${player.name}")
}
