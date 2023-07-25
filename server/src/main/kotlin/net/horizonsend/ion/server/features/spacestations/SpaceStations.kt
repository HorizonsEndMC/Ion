package net.horizonsend.ion.server.features.spacestations

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.NationRole
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.schema.nations.SettlementRole
import net.horizonsend.ion.common.database.schema.nations.spacestation.NationSpaceStation
import net.horizonsend.ion.common.database.schema.nations.spacestation.PlayerSpaceStation
import net.horizonsend.ion.common.database.schema.nations.spacestation.SettlementSpaceStation
import net.horizonsend.ion.common.database.schema.nations.spacestation.SpaceStationInterface
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.miscellaneous.utils.optional
import java.util.Optional

object SpaceStations : IonServerComponent() {
	private val spaceStations = mutableListOf<CachedSpaceStation<*, *, *>>()

	val spaceStationCache: LoadingCache<String, Optional<CachedSpaceStation<*, *, *>>> =
		CacheBuilder.newBuilder().weakKeys().build(
			CacheLoader.from { name ->
				return@from optional(spaceStations.firstOrNull { it.name.equals(name, ignoreCase = true) })
			}
		)

	enum class SpaceStationPermission(val nation: NationRole.Permission, val settlement: SettlementRole.Permission) {
		CREATE_STATION(NationRole.Permission.CREATE_STATION, SettlementRole.Permission.CREATE_STATION),
		MANAGE_STATION(NationRole.Permission.MANAGE_STATION, SettlementRole.Permission.MANAGE_STATION),
		DELETE_STATION(NationRole.Permission.DELETE_STATION, SettlementRole.Permission.DELETE_STATION)
	}

	override fun onEnable() {
		reload()

		NationSpaceStation.watchInserts { change ->
			change.fullDocument?.let { createCached(it) }
		}

		SettlementSpaceStation.watchInserts { change ->
			change.fullDocument?.let { createCached(it) }
		}

		PlayerSpaceStation.watchInserts { change ->
			change.fullDocument?.let { createCached(it) }
		}
	}

	fun all() = spaceStations

	fun invalidate(station: SpaceStationInterface<*>) {
		spaceStations.removeAll { it.name == station.name }
		with(spaceStationCache) { invalidate(station.name); cleanUp() }

		createCached(station)
	}

	fun reload() {
		spaceStations.clear()

		for (nationSpaceStation in NationSpaceStation.all()) {
			createCached(nationSpaceStation)
		}

		for (settlementSpaceStation in SettlementSpaceStation.all()) {
			createCached(settlementSpaceStation)
		}

		for (playerSpaceStation in PlayerSpaceStation.all()) {
			createCached(playerSpaceStation)
		}

		with(spaceStationCache) { invalidateAll(); cleanUp() }
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

		spaceStations += cachedStation

		return cachedStation
	}
}
