package net.horizonsend.ion.common.database.cache.nations

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.SLTextStyleDB
import net.horizonsend.ion.common.database.boolean
import net.horizonsend.ion.common.database.cache.ManualCache
import net.horizonsend.ion.common.database.containsUpdated
import net.horizonsend.ion.common.database.double
import net.horizonsend.ion.common.database.get
import net.horizonsend.ion.common.database.int
import net.horizonsend.ion.common.database.mappedSet
import net.horizonsend.ion.common.database.nullable
import net.horizonsend.ion.common.database.oid
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.NationRole
import net.horizonsend.ion.common.database.schema.nations.Role
import net.horizonsend.ion.common.database.schema.nations.RoleCompanion
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.schema.nations.SettlementRole
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.common.database.uuid
import org.litote.kmongo.`in`
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

abstract class AbstractPlayerCache : ManualCache() {
	/** Values should only be set here*/
	data class PlayerData(
		val id: SLPlayerId,
		var xp: Int?,
		var level: Int?,
		var settlementOid: Oid<Settlement>?,
		var nationOid: Oid<Nation>?,
		var settlementTag: String?,
		var nationTag: String?,
		var bounty: Double,

		var contactsDistance: Int = 6000,
		var contactsEnabled: Boolean = true,
		var contactsStarships: Boolean = true,
		var lastStarshipEnabled: Boolean = true,
		var planetsEnabled: Boolean = true,
		var starsEnabled: Boolean = true,
		var beaconsEnabled: Boolean = true,
		var stationsEnabled: Boolean = false,
		var bookmarksEnabled: Boolean = true,

		var waypointsEnabled: Boolean = true,
		var compactWaypoints: Boolean = true,

		var starshipsEnabled: Boolean = true,
		var advancedStarshipInfo: Boolean = false,
		var rotateCompass: Boolean = false,

		var hudPlanetsImage: Boolean = true,
		var hudPlanetsSelector: Boolean = true,

		var showItemSearchItem: Boolean = true,

		var blockedPlayerIDs: Set<SLPlayerId> = setOf(),
	)

	val PLAYER_DATA: MutableMap<UUID, PlayerData> = ConcurrentHashMap()

	abstract fun onlinePlayerIds(): List<SLPlayerId>
	abstract fun kickUUID(uuid: UUID, msg: String)

	// priority monitor so it happens after the insert in SLCore, which is at HIGHEST
	fun callOnPreLogin(uuid: UUID) {
		cache(uuid.slPlayerId, SLPlayer[uuid] ?: return)
	}

	// lowest in case anything uses this data in other join listeners, and since lowest join event
	// is always after monitor async player pre login event, though the async player pre login event
	// may not be called if the plugin is not registered when they initiate authentication, like in a restart
	fun callOnLoginLow(uuid: UUID) {
		if (!PLAYER_DATA.containsKey(uuid)) {
			kickUUID(uuid, "<red>Failed to load data! Please try again.")
		}
	}

	fun callOnQuit(uuid: UUID) {
		PLAYER_DATA.remove(uuid)
	}

	override fun load() {
		PLAYER_DATA.clear()

		val onlinePlayerIds = onlinePlayerIds()
		for (data in SLPlayer.find(SLPlayer::_id `in` onlinePlayerIds)) {
			cache(data._id, data)
		}

		// SLPlayers are inserted on join and never deleted, so listening to updates and joins is sufficient.
		SLPlayer.watchUpdates { change ->
			val id = change.slPlayerId

			change[SLPlayer::settlement]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val newSettlement = it.nullable()?.oid<Settlement>()

					data.settlementOid = newSettlement
					data.settlementTag = null // when leaving/joining a settlement, you have no role either way
				}
			}

			change[SLPlayer::nation]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val newNation = it.nullable()?.oid<Nation>()

					data.nationOid = newNation
					data.nationTag = null // when leaving/joining a nation, you have no role either way
				}
			}

			change[SLPlayer::xp]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val newXp = it.int()
					data.xp = newXp
				}
			}

			change[SLPlayer::level]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val newLevel = it.int()
					data.level = newLevel
				}
			}

			change[SLPlayer::contactsDistance]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val contactsDistance = it.int()
					data.contactsDistance = contactsDistance
				}
			}

			change[SLPlayer::contactsEnabled]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val contactsEnabled = it.boolean()
					data.contactsEnabled = contactsEnabled
				}
			}

			change[SLPlayer::contactsStarships]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val contactsStarships = it.boolean()
					data.contactsStarships = contactsStarships
				}
			}

			change[SLPlayer::lastStarshipEnabled]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val lastStarshipEnabled = it.boolean()
					data.lastStarshipEnabled = lastStarshipEnabled
				}
			}

			change[SLPlayer::planetsEnabled]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val planetsEnabled = it.boolean()
					data.planetsEnabled = planetsEnabled
				}
			}

			change[SLPlayer::starsEnabled]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val starsEnabled = it.boolean()
					data.starsEnabled = starsEnabled
				}
			}

			change[SLPlayer::beaconsEnabled]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val beaconsEnabled = it.boolean()
					data.beaconsEnabled = beaconsEnabled
				}
			}

			change[SLPlayer::stationsEnabled]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val stationsEnabled = it.boolean()
					data.stationsEnabled = stationsEnabled
				}
			}

			change[SLPlayer::bookmarksEnabled]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val bookmarksEnabled = it.boolean()
					data.bookmarksEnabled = bookmarksEnabled
				}
			}

			change[SLPlayer::waypointsEnabled]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val waypointsEnabled = it.boolean()
					data.waypointsEnabled = waypointsEnabled
				}
			}

			change[SLPlayer::compactWaypoints]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val compactWaypoints = it.boolean()
					data.compactWaypoints = compactWaypoints
				}
			}

			change[SLPlayer::starshipsEnabled]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val starshipsEnabled = it.boolean()
					data.starshipsEnabled = starshipsEnabled
				}
			}

			change[SLPlayer::advancedStarshipInfo]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val advancedStarshipInfo = it.boolean()
					data.advancedStarshipInfo = advancedStarshipInfo
				}
			}

			change[SLPlayer::rotateCompass]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val rotateCompass = it.boolean()
					data.rotateCompass = rotateCompass
				}
			}

			change[SLPlayer::hudPlanetsImage]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val hudPlanetsImage = it.boolean()
					data.hudPlanetsImage = hudPlanetsImage
				}
			}

			change[SLPlayer::hudPlanetsSelector]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val hudPlanetsSelector = it.boolean()
					data.hudPlanetsSelector = hudPlanetsSelector
				}
			}

			change[SLPlayer::showItemSearchItem]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					data.showItemSearchItem = it.boolean()
				}
			}

			change[SLPlayer::bounty]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					data.bounty = it.double()
				}
			}

			change[SLPlayer::blockedPlayerIDs]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					data.blockedPlayerIDs = it.mappedSet { it.slPlayerId() }
				}
			}
		}

		val mutex = Any()

		/** for each cached player, update their settlement tag if they're in a settlement & nation tag if in a nation */
		fun recalculateTags(): Unit = synchronized(mutex) {
			for ((id, data) in PLAYER_DATA) {
				val slPlayerId = id.slPlayerId
				data.settlementOid?.let { data.settlementTag = getColoredTag(SettlementRole.getTag(slPlayerId)) }
				data.nationOid?.let { data.nationTag = getColoredTag(NationRole.getTag(slPlayerId)) }
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

	fun cache(id: SLPlayerId, data: SLPlayer) {
		val settlement: Oid<Settlement>? = data.settlement
		val nation: Oid<Nation>? = data.nation

		val settlementTag: String? = if (settlement == null) {
			null
		} else {
			getColoredTag(SettlementRole.getTag(id))
		}

		val nationTag: String? = if (nation == null) {
			null
		} else {
			getColoredTag(NationRole.getTag(id))
		}

		PLAYER_DATA[id.uuid] = PlayerData(
			id = id,
			xp = data.xp,
			level = data.level,
			settlementOid = settlement,
			nationOid = nation,
			settlementTag = settlementTag,
			nationTag = nationTag,
			bounty = data.bounty,
			blockedPlayerIDs = data.blockedPlayerIDs,
			contactsDistance = data.contactsDistance,
			contactsEnabled = data.contactsEnabled,
			contactsStarships = data.contactsStarships,
			lastStarshipEnabled = data.lastStarshipEnabled,
			planetsEnabled = data.planetsEnabled,
			starsEnabled = data.starsEnabled,
			beaconsEnabled = data.beaconsEnabled,
			stationsEnabled = data.stationsEnabled,
			bookmarksEnabled = data.bookmarksEnabled,
			waypointsEnabled = data.waypointsEnabled,
			compactWaypoints = data.compactWaypoints,
			starshipsEnabled = data.starshipsEnabled,
			advancedStarshipInfo = data.advancedStarshipInfo,
			rotateCompass = data.rotateCompass,
			hudPlanetsImage = data.hudPlanetsImage,
			hudPlanetsSelector = data.hudPlanetsSelector,
			showItemSearchItem = data.showItemSearchItem
		)
	}

	abstract fun getColoredTag(nameColorPair: Pair<String, SLTextStyleDB>?): String?

	operator fun get(playerId: UUID): PlayerData = PLAYER_DATA[playerId]
		?: error("Data wasn't cached for $playerId")

	operator fun get(playerId: SLPlayerId): PlayerData = PLAYER_DATA[playerId.uuid]
		?: error("Data wasn't cached for $playerId")
}
