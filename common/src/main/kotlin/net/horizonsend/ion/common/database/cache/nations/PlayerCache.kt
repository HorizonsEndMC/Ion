package net.horizonsend.ion.common.database.cache.nations

import net.horizonsend.ion.common.database.*
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.*
import net.horizonsend.ion.common.database.cache.ManualCache
import org.litote.kmongo.`in`
import java.util.*
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

		var contactsStarships: Boolean = true,
		var lastStarshipEnabled: Boolean = true,
		var planetsEnabled: Boolean = true,
		var starsEnabled: Boolean = true,
		var beaconsEnabled: Boolean = true,
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

					val newNation = it.int()
					data.xp = newNation
				}
			}

			change[SLPlayer::level]?.let {
				synced {
					val data = PLAYER_DATA[id.uuid] ?: return@synced

					val newNation = it.int()
					data.level = newNation
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

		PLAYER_DATA[id.uuid] = PlayerData(id, data.xp, data.level, settlement, nation, settlementTag, nationTag)
	}

	abstract fun getColoredTag(nameColorPair: Pair<String, SLTextStyleDB>?): String?

	operator fun get(playerId: UUID): PlayerData = PLAYER_DATA[playerId]
		?: error("Data wasn't cached for $playerId")

	operator fun get(playerId: SLPlayerId): PlayerData = PLAYER_DATA[playerId.uuid]
		?: error("Data wasn't cached for $playerId")
}
