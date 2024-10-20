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
		var contactsMaxNameLength: Int = 64,
		var contactsSort: Int = 0,
		var contactsColoring: Int = 0,
		var contactsEnabled: Boolean = true,
		var contactsStarships: Boolean = true,
		var lastStarshipEnabled: Boolean = true,
		var planetsEnabled: Boolean = true,
		var starsEnabled: Boolean = true,
		var beaconsEnabled: Boolean = true,
		var stationsEnabled: Boolean = false,
		var bookmarksEnabled: Boolean = true,
		var relationAiEnabled: Boolean = true,
		var relationNoneEnabled: Boolean = true,
		var relationEnemyEnabled: Boolean = true,
		var relationUnfriendlyEnabled: Boolean = true,
		var relationNeutralEnabled: Boolean = true,
		var relationFriendlyEnabled: Boolean = true,
		var relationAllyEnabled: Boolean = true,
		var relationNationEnabled: Boolean = true,
		var relationAiStationEnabled: Boolean = true,
		var relationNoneStationEnabled: Boolean = true,
		var relationEnemyStationEnabled: Boolean = true,
		var relationUnfriendlyStationEnabled: Boolean = true,
		var relationNeutralStationEnabled: Boolean = true,
		var relationFriendlyStationEnabled: Boolean = true,
		var relationAllyStationEnabled: Boolean = true,
		var relationNationStationEnabled: Boolean = true,

		var waypointsEnabled: Boolean = true,
		var compactWaypoints: Boolean = true,

		var starshipsEnabled: Boolean = true,
		var advancedStarshipInfo: Boolean = false,
		var rotateCompass: Boolean = false,

		var combatTimerEnabled: Boolean = true,

		var hudPlanetsImage: Boolean = true,
		var hudPlanetsSelector: Boolean = true,
		var hudIconStars: Boolean = true,
		var hudIconBeacons: Boolean = true,
		var hudIconStations: Boolean = false,
		var hudIconBookmarks: Boolean = false,

		var showItemSearchItem: Boolean = true,

		var protectionMessagesEnabled: Boolean = true,
		var useAlternateDCCruise: Boolean = true,
		var dcSpeedModifier: Int = 1,
		var enableAdditionalSounds: Boolean = true,
		var soundCruiseIndicator: Int = 0,
		var enableCombatTimerAlerts: Boolean = true,

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

			change[SLPlayer::contactsMaxNameLength]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val contactsMaxNameLength = it.int()
					data.contactsMaxNameLength = contactsMaxNameLength
				}
			}

			change[SLPlayer::contactsSort]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val contactsSort = it.int()
					data.contactsSort = contactsSort
				}
			}

			change[SLPlayer::contactsColoring]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val contactsColoring = it.int()
					data.contactsColoring = contactsColoring
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

			change[SLPlayer::relationAiEnabled]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val relationAiEnabled = it.boolean()
					data.relationAiEnabled = relationAiEnabled
				}
			}

			change[SLPlayer::relationNoneEnabled]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val relationNoneEnabled = it.boolean()
					data.relationNoneEnabled = relationNoneEnabled
				}
			}

			change[SLPlayer::relationEnemyEnabled]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val relationEnemyEnabled = it.boolean()
					data.relationEnemyEnabled = relationEnemyEnabled
				}
			}

			change[SLPlayer::relationUnfriendlyEnabled]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val relationUnfriendlyEnabled = it.boolean()
					data.relationUnfriendlyEnabled = relationUnfriendlyEnabled
				}
			}

			change[SLPlayer::relationNeutralEnabled]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val relationNeutralEnabled = it.boolean()
					data.relationNeutralEnabled = relationNeutralEnabled
				}
			}

			change[SLPlayer::relationFriendlyEnabled]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val relationFriendlyEnabled = it.boolean()
					data.relationFriendlyEnabled = relationFriendlyEnabled
				}
			}

			change[SLPlayer::relationAllyEnabled]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val relationAllyEnabled = it.boolean()
					data.relationAllyEnabled = relationAllyEnabled
				}
			}

			change[SLPlayer::relationNationEnabled]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val relationNationEnabled = it.boolean()
					data.relationNationEnabled = relationNationEnabled
				}
			}

			change[SLPlayer::relationAiStationEnabled]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val relationAiEnabled = it.boolean()
					data.relationAiEnabled = relationAiEnabled
				}
			}

			change[SLPlayer::relationNoneStationEnabled]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val relationNoneEnabled = it.boolean()
					data.relationNoneEnabled = relationNoneEnabled
				}
			}

			change[SLPlayer::relationEnemyStationEnabled]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val relationEnemyEnabled = it.boolean()
					data.relationEnemyEnabled = relationEnemyEnabled
				}
			}

			change[SLPlayer::relationUnfriendlyStationEnabled]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val relationUnfriendlyEnabled = it.boolean()
					data.relationUnfriendlyEnabled = relationUnfriendlyEnabled
				}
			}

			change[SLPlayer::relationNeutralStationEnabled]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val relationNeutralEnabled = it.boolean()
					data.relationNeutralEnabled = relationNeutralEnabled
				}
			}

			change[SLPlayer::relationFriendlyStationEnabled]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val relationFriendlyEnabled = it.boolean()
					data.relationFriendlyEnabled = relationFriendlyEnabled
				}
			}

			change[SLPlayer::relationAllyStationEnabled]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val relationAllyEnabled = it.boolean()
					data.relationAllyEnabled = relationAllyEnabled
				}
			}

			change[SLPlayer::relationNationStationEnabled]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val relationNationEnabled = it.boolean()
					data.relationNationEnabled = relationNationEnabled
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

			change[SLPlayer::combatTimerEnabled]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val combatTimerEnabled = it.boolean()
					data.combatTimerEnabled = combatTimerEnabled
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

			change[SLPlayer::hudIconStars]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val hudIconStars = it.boolean()
					data.hudIconStars = hudIconStars
				}
			}

			change[SLPlayer::hudIconBeacons]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val hudIconBeacons = it.boolean()
					data.hudIconBeacons = hudIconBeacons
				}
			}

			change[SLPlayer::hudIconStations]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val hudIconStations = it.boolean()
					data.hudIconStations = hudIconStations
				}
			}

			change[SLPlayer::hudIconBookmarks]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val hudIconBookmarks = it.boolean()
					data.hudIconBookmarks = hudIconBookmarks
				}
			}

			change[SLPlayer::showItemSearchItem]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					data.showItemSearchItem = it.boolean()
				}
			}

			change[SLPlayer::protectionMessagesEnabled]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					data.protectionMessagesEnabled = it.boolean()
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

			change[SLPlayer::useAlternateDCCruise]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val useAlternateDcCruise = it.boolean()
					data.useAlternateDCCruise = useAlternateDcCruise
				}
			}

			change[SLPlayer::dcSpeedModifier]?.let {
				synced{
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val dcSpeedModifier = it.int()
					data.dcSpeedModifier = dcSpeedModifier
				}
			}

			change[SLPlayer::enableAdditionalSounds]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val enableAdditionalSounds = it.boolean()
					data.enableAdditionalSounds = enableAdditionalSounds
				}
			}

			change[SLPlayer::soundCruiseIndicator]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val soundCruiseIndicator = it.int()
					data.soundCruiseIndicator = soundCruiseIndicator
				}
			}

			change[SLPlayer::enableCombatTimerAlerts]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val enableCombatTimerAlerts = it.boolean()
					data.enableCombatTimerAlerts = enableCombatTimerAlerts
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
			contactsMaxNameLength = data.contactsMaxNameLength,
			contactsSort = data.contactsSort,
			contactsColoring = data.contactsColoring,
			contactsEnabled = data.contactsEnabled,
			contactsStarships = data.contactsStarships,
			lastStarshipEnabled = data.lastStarshipEnabled,
			planetsEnabled = data.planetsEnabled,
			starsEnabled = data.starsEnabled,
			beaconsEnabled = data.beaconsEnabled,
			stationsEnabled = data.stationsEnabled,
			bookmarksEnabled = data.bookmarksEnabled,
			relationAiEnabled = data.relationAiEnabled,
			relationNoneEnabled = data.relationNoneEnabled,
			relationEnemyEnabled = data.relationEnemyEnabled,
			relationUnfriendlyEnabled = data.relationUnfriendlyEnabled,
			relationNeutralEnabled = data.relationNeutralEnabled,
			relationFriendlyEnabled = data.relationFriendlyEnabled,
			relationAllyEnabled = data.relationAllyEnabled,
			relationNationEnabled = data.relationNationEnabled,
			relationAiStationEnabled = data.relationAiEnabled,
			relationNoneStationEnabled = data.relationNoneEnabled,
			relationEnemyStationEnabled = data.relationEnemyEnabled,
			relationUnfriendlyStationEnabled = data.relationUnfriendlyEnabled,
			relationNeutralStationEnabled = data.relationNeutralEnabled,
			relationFriendlyStationEnabled = data.relationFriendlyEnabled,
			relationAllyStationEnabled = data.relationAllyEnabled,
			relationNationStationEnabled = data.relationNationEnabled,
			waypointsEnabled = data.waypointsEnabled,
			compactWaypoints = data.compactWaypoints,
			starshipsEnabled = data.starshipsEnabled,
			advancedStarshipInfo = data.advancedStarshipInfo,
			rotateCompass = data.rotateCompass,
			combatTimerEnabled = data.combatTimerEnabled,
			hudPlanetsImage = data.hudPlanetsImage,
			hudPlanetsSelector = data.hudPlanetsSelector,
			hudIconStars = data.hudIconStars,
			hudIconBeacons = data.hudIconBeacons,
			hudIconStations = data.hudIconStations,
			hudIconBookmarks = data.hudIconBookmarks,
			showItemSearchItem = data.showItemSearchItem,
			protectionMessagesEnabled = data.protectionMessagesEnabled,
			useAlternateDCCruise = data.useAlternateDCCruise,
			dcSpeedModifier = data.dcSpeedModifier,
			enableAdditionalSounds = data.enableAdditionalSounds,
			soundCruiseIndicator = data.soundCruiseIndicator,
			enableCombatTimerAlerts = data.enableCombatTimerAlerts,
		)
	}

	abstract fun getColoredTag(nameColorPair: Pair<String, SLTextStyleDB>?): String?

	operator fun get(playerId: UUID): PlayerData = PLAYER_DATA[playerId]
		?: error("Data wasn't cached for $playerId")

	operator fun get(playerId: SLPlayerId): PlayerData = PLAYER_DATA[playerId.uuid]
		?: error("Data wasn't cached for $playerId")
}
