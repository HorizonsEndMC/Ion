package net.horizonsend.ion.server.features.space.spacestations

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.ManualCache
import net.horizonsend.ion.common.database.enumValue
import net.horizonsend.ion.common.database.get
import net.horizonsend.ion.common.database.int
import net.horizonsend.ion.common.database.mappedSet
import net.horizonsend.ion.common.database.oid
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.NationRole
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.schema.nations.SettlementRole
import net.horizonsend.ion.common.database.schema.nations.spacestation.NationSpaceStation
import net.horizonsend.ion.common.database.schema.nations.spacestation.PlayerSpaceStation
import net.horizonsend.ion.common.database.schema.nations.spacestation.SettlementSpaceStation
import net.horizonsend.ion.common.database.schema.nations.spacestation.SpaceStationCompanion
import net.horizonsend.ion.common.database.schema.nations.spacestation.SpaceStationInterface
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.common.database.string
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KProperty

object SpaceStationCache : ManualCache() {
	private val STATION_DATA = ConcurrentHashMap<Oid<*>, CachedSpaceStation<*, *, *>>()
	private val nameCache = ConcurrentHashMap<String, Oid<*>>()

	enum class SpaceStationPermission(val nation: NationRole.Permission, val settlement: SettlementRole.Permission) {
		CREATE_STATION(NationRole.Permission.CREATE_STATION, SettlementRole.Permission.CREATE_STATION),
		MANAGE_STATION(NationRole.Permission.MANAGE_STATION, SettlementRole.Permission.MANAGE_STATION),
		DELETE_STATION(NationRole.Permission.DELETE_STATION, SettlementRole.Permission.DELETE_STATION)
	}

	override fun load() {
		STATION_DATA.clear()
		nameCache.clear()

		for (playerSpaceStation in PlayerSpaceStation.all()) {
			createCached(playerSpaceStation)
		}

		for (settlementSpaceStation in SettlementSpaceStation.all()) {
			createCached(settlementSpaceStation)
		}

		for (nationSpaceStation in NationSpaceStation.all()) {
			createCached(nationSpaceStation)
		}

		setupDb(
			PlayerSpaceStation.Companion,
			PlayerSpaceStation::name,
			PlayerSpaceStation::world,
			PlayerSpaceStation::x,
			PlayerSpaceStation::z,
			PlayerSpaceStation::radius,
			PlayerSpaceStation::trustedPlayers,
			PlayerSpaceStation::trustedSettlements,
			PlayerSpaceStation::trustedNations,
			PlayerSpaceStation::trustLevel,
		)

		setupDb(
			SettlementSpaceStation.Companion,
			SettlementSpaceStation::name,
			SettlementSpaceStation::world,
			SettlementSpaceStation::x,
			SettlementSpaceStation::z,
			SettlementSpaceStation::radius,
			SettlementSpaceStation::trustedPlayers,
			SettlementSpaceStation::trustedSettlements,
			SettlementSpaceStation::trustedNations,
			SettlementSpaceStation::trustLevel,
		)

		setupDb(
			NationSpaceStation.Companion,
			NationSpaceStation::name,
			NationSpaceStation::world,
			NationSpaceStation::x,
			NationSpaceStation::z,
			NationSpaceStation::radius,
			NationSpaceStation::trustedPlayers,
			NationSpaceStation::trustedSettlements,
			NationSpaceStation::trustedNations,
			NationSpaceStation::trustLevel,
		)
	}

	fun setupDb(
		companion: SpaceStationCompanion<*, *>,
		nameProperty: KProperty<String>,
		worldProperty: KProperty<String>,
		xProperty: KProperty<Int>,
		zProperty: KProperty<Int>,
		radiusProperty: KProperty<Int>,
		trustedPlayersProperty: KProperty<Set<SLPlayerId>>,
		trustedSettlementsProperty: KProperty<Set<Oid<Settlement>>>,
		trustedNationsProperty: KProperty<Set<Oid<Nation>>>,
		trustLevelProperty: KProperty<SpaceStationCompanion.TrustLevel>,
	) {
		companion.watchInserts { change ->
			change.fullDocument?.let { createCached(it) }
		}

		companion.watchDeletes {change ->
			val id = change.oid

			val data = STATION_DATA[id] ?: error("$id wasn't cached")

			STATION_DATA.remove(id)
			nameCache.remove(data.name)
		}

		companion.watchUpdates { change ->
			val id = change.oid

			synced {
				val data = STATION_DATA[id] ?: return@synced

				change[nameProperty]?.let {
					nameCache.remove(data.name)
					data.name = it.string()
					nameCache[data.name] = id
				}
			}
		}

		companion.watchUpdates { change ->
			val id = change.oid

			synced {
				val data = STATION_DATA[id] ?: return@synced

				change[worldProperty]?.let {
					data.world = it.string()
				}
			}
		}

		companion.watchUpdates { change ->
			val id = change.oid

			synced {
				val data = STATION_DATA[id] ?: return@synced

				change[xProperty]?.let {
					data.x = it.int()
				}
			}
		}

		companion.watchUpdates { change ->
			val id = change.oid

			synced {
				val data = STATION_DATA[id] ?: return@synced

				change[zProperty]?.let {
					data.x = it.int()
				}
			}
		}

		companion.watchUpdates { change ->
			val id = change.oid

			synced {
				val data = STATION_DATA[id] ?: return@synced

				change[radiusProperty]?.let {
					data.radius = it.int()
				}
			}
		}

		companion.watchUpdates { change ->
			val id = change.oid

			synced {
				val data = STATION_DATA[id] ?: return@synced

				change[trustedPlayersProperty]?.let { set ->
					data.trustedPlayers = set.mappedSet { it.slPlayerId() }
				}
			}
		}

		companion.watchUpdates { change ->
			val id = change.oid

			synced {
				val data = STATION_DATA[id] ?: return@synced

				change[trustedSettlementsProperty]?.let { set ->
					data.trustedSettlements = set.mappedSet { it.oid() }
				}
			}
		}

		companion.watchUpdates { change ->
			val id = change.oid

			synced {
				val data = STATION_DATA[id] ?: return@synced

				change[trustedNationsProperty]?.let { set ->
					data.trustedNations = set.mappedSet { it.oid() }
				}
			}
		}

		companion.watchUpdates { change ->
			val id = change.oid

			synced {
				val data = STATION_DATA[id] ?: return@synced

				change[trustLevelProperty]?.let {
					data.trustLevel = it.enumValue()
				}
			}
		}
	}

	fun createCached(station: SpaceStationInterface<*>): CachedSpaceStation<*, *, *> {
		val cachedStation: CachedSpaceStation<*, *, *> = when (station) {
			is NationSpaceStation -> CachedNationSpaceStation(
				databaseId = station._id,
				owner = station.owner as Oid<Nation>,
				name = station.name,
				world = station.world,
				x = station.x,
				z = station.z,
				radius = station.radius,
				trustedPlayers = station.trustedPlayers,
				trustedSettlements = station.trustedSettlements,
				trustedNations = station.trustedNations,
				trustLevel = station.trustLevel,
			)

			is SettlementSpaceStation -> CachedSettlementSpaceStation(
				databaseId = station._id,
				owner = station.owner as Oid<Settlement>,
				name = station.name,
				world = station.world,
				x = station.x,
				z = station.z,
				radius = station.radius,
				trustedPlayers = station.trustedPlayers,
				trustedSettlements = station.trustedSettlements,
				trustedNations = station.trustedNations,
				trustLevel = station.trustLevel,
			)

			is PlayerSpaceStation -> CachedPlayerSpaceStation(
				databaseId = station._id,
				owner = station.owner as SLPlayerId,
				name = station.name,
				world = station.world,
				x = station.x,
				z = station.z,
				radius = station.radius,
				trustedPlayers = station.trustedPlayers,
				trustedSettlements = station.trustedSettlements,
				trustedNations = station.trustedNations,
				trustLevel = station.trustLevel,
			)

			else -> throw NotImplementedError()
		}

		STATION_DATA[station._id] = cachedStation
		nameCache[station.name] = cachedStation.databaseId

		return cachedStation
	}

	fun all() = STATION_DATA.values

	operator fun get(id: Oid<*>) = STATION_DATA[id]
	operator fun get(name: String) = nameCache[name]?.let { STATION_DATA[it] }
}
