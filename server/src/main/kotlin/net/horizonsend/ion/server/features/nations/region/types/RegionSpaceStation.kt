package net.horizonsend.ion.server.features.nations.region.types

import com.mongodb.client.model.changestream.ChangeStreamDocument
import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.enumValue
import net.horizonsend.ion.common.database.get
import net.horizonsend.ion.common.database.id
import net.horizonsend.ion.common.database.int
import net.horizonsend.ion.common.database.mappedSet
import net.horizonsend.ion.common.database.oid
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.NationRelation
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.schema.nations.spacestation.SpaceStationCompanion
import net.horizonsend.ion.common.database.schema.nations.spacestation.SpaceStationInterface
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.common.database.string
import net.horizonsend.ion.server.features.spacestations.CachedNationSpaceStation
import net.horizonsend.ion.server.features.spacestations.CachedPlayerSpaceStation
import net.horizonsend.ion.server.features.spacestations.CachedSettlementSpaceStation
import net.horizonsend.ion.server.features.spacestations.SpaceStations
import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.horizonsend.ion.server.features.nations.NationsMap
import net.horizonsend.ion.server.miscellaneous.utils.d
import net.horizonsend.ion.server.miscellaneous.utils.distanceSquared
import net.horizonsend.ion.server.miscellaneous.utils.squared
import org.bukkit.entity.Player
import org.litote.kmongo.Id
import kotlin.jvm.optionals.getOrNull

class RegionSpaceStation<T: SpaceStationInterface<Owner>, Owner: DbObject>(spaceStation: SpaceStationInterface<Owner>) : Region<T>(spaceStation), RegionTopLevel {
	override val priority: Int = 0

	override var world: String = spaceStation.world; private set

	var name: String = spaceStation.name; private set
	var x: Int = spaceStation.x; private set
	var z: Int = spaceStation.z; private set
	var radius: Int = spaceStation.radius; private set
	var ownerId: Id<Owner> = spaceStation.owner; private set
	var trustLevel: SpaceStationCompanion.TrustLevel = spaceStation.trustLevel; private set
	var trustedPlayers: Set<SLPlayerId> = spaceStation.trustedPlayers; private set
	var trustedSettlements: Set<Oid<Settlement>> = spaceStation.trustedSettlements; private set
	var trustedNations: Set<Oid<Nation>> = spaceStation.trustedNations; private set

	val color: Int get() = SpaceStations.spaceStationCache[name].get().color
	val ownerName: String get() = SpaceStations.spaceStationCache[name].get().ownerName
	val ownerType: String get() = SpaceStations.spaceStationCache[name].get().ownershipType

	override fun contains(x: Int, y: Int, z: Int): Boolean {
		return distanceSquared(this.x.d(), 0.0, this.z.d(), x.d(), 0.0, z.d()) <= radius.toDouble().squared()
	}

	override fun onCreate() {
		NationsMap.addSpaceStation(this)
	}

	override fun update(delta: ChangeStreamDocument<T>) {
		delta[SpaceStationInterface<Owner>::name]?.let { name = it.string() }
		delta[SpaceStationInterface<Owner>::world]?.let { world = it.string() }
		delta[SpaceStationInterface<Owner>::x]?.let { x = it.int() }
		delta[SpaceStationInterface<Owner>::z]?.let { z = it.int() }
		delta[SpaceStationInterface<Owner>::radius]?.let { radius = it.int() }
		delta[SpaceStationInterface<Owner>::owner]?.let { ownerId = it.id() }
		delta[SpaceStationInterface<Owner>::trustLevel]?.let { trustLevel = it.enumValue() }
		delta[SpaceStationInterface<Owner>::trustedPlayers]?.let { col -> trustedPlayers = col.mappedSet { it.slPlayerId() } }
		delta[SpaceStationInterface<Owner>::trustedNations]?.let { col -> trustedNations = col.mappedSet { it.oid() } }

		NationsMap.updateSpaceStation(this)
	}

	override fun onDelete() {
		NationsMap.removeSpaceStation(this)
	}

	override fun calculateInaccessMessage(player: Player): String? {
		val cached = SpaceStations.spaceStationCache[name].getOrNull() ?: return "&cStation not cached"

		if (cached is CachedPlayerSpaceStation && cached.owner == player.slPlayerId) return null

		val playerData = PlayerCache[player]

		if (trustedPlayers.contains(player.slPlayerId)) return null

		// if they're in a settlement, check for trust level auto perms, and trusted settlements
		playerData.settlementOid?.let { playerSettlement ->
			if (trustedSettlements.contains(playerSettlement)) return null

			if (trustLevel == SpaceStationCompanion.TrustLevel.SETTLEMENT_MEMBER &&
				cached is CachedSettlementSpaceStation &&
				cached.owner == playerSettlement) return null
		}

		// if they're in a nation, check for trust level auto perms, and trusted nations
		playerData.nationOid?.let { playerNation ->
			if (trustedNations.contains(playerNation)) return null

			if (cached !is CachedNationSpaceStation) return@let

			if (trustLevel == SpaceStationCompanion.TrustLevel.NATION_MEMBER &&
				cached.owner == playerNation) return null

			val relation = RelationCache[cached.owner, playerNation]

			if (relation.ordinal >= NationRelation.Level.ALLY.ordinal) return null
		}

		return "&cSpace station $name is claimed by ${ cached.ownerName } @ $x,$z x $radius"
	}
}
