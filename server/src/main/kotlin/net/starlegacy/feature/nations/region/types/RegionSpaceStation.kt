package net.starlegacy.feature.nations.region.types

import com.mongodb.client.model.changestream.ChangeStreamDocument
import net.horizonsend.ion.server.database.DbObject
import net.starlegacy.cache.nations.PlayerCache
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.enumValue
import net.horizonsend.ion.server.database.get
import net.horizonsend.ion.server.database.id
import net.horizonsend.ion.server.database.int
import net.horizonsend.ion.server.database.mappedSet
import net.horizonsend.ion.server.database.oid
import net.horizonsend.ion.server.database.schema.misc.SLPlayerId
import net.horizonsend.ion.server.database.schema.nations.Nation
import net.horizonsend.ion.server.database.schema.nations.NationRelation
import net.horizonsend.ion.server.database.schema.nations.Settlement
import net.horizonsend.ion.server.database.schema.nations.spacestation.SpaceStation
import net.horizonsend.ion.server.database.slPlayerId
import net.horizonsend.ion.server.database.string
import net.horizonsend.ion.server.features.spacestations.CachedNationSpaceStation
import net.horizonsend.ion.server.features.spacestations.CachedPlayerSpaceStation
import net.horizonsend.ion.server.features.spacestations.CachedSettlementSpaceStation
import net.horizonsend.ion.server.features.spacestations.SpaceStations
import net.starlegacy.cache.nations.RelationCache
import net.starlegacy.feature.nations.NationsMap
import net.starlegacy.util.d
import net.starlegacy.util.distanceSquared
import net.starlegacy.util.squared
import org.bukkit.entity.Player
import org.litote.kmongo.Id
import kotlin.jvm.optionals.getOrNull

class RegionSpaceStation<T: SpaceStation<Owner>, Owner: DbObject>(spaceStation: SpaceStation<Owner>) : Region<T>(spaceStation), RegionTopLevel {
	override val priority: Int = 0

	override var world: String = spaceStation.world; private set

	var name: String = spaceStation.name; private set
	var x: Int = spaceStation.x; private set
	var z: Int = spaceStation.z; private set
	var radius: Int = spaceStation.radius; private set
	var ownerId: Id<Owner> = spaceStation.owner; private set
	var trustLevel: SpaceStations.TrustLevel = spaceStation.trustLevel; private set
	var trustedPlayers: Set<SLPlayerId> = spaceStation.trustedPlayers; private set
	var trustedSettlements: Set<Oid<Settlement>> = spaceStation.trustedSettlements; private set
	var trustedNations: Set<Oid<Nation>> = spaceStation.trustedNations; private set

	override fun contains(x: Int, y: Int, z: Int): Boolean {
		return distanceSquared(this.x.d(), 0.0, this.z.d(), x.d(), 0.0, z.d()) <= radius.toDouble().squared()
	}

	override fun onCreate() {
		NationsMap.addSpaceStation(this)
	}

	override fun update(delta: ChangeStreamDocument<T>) {
		delta[SpaceStation<Owner>::name]?.let { name = it.string() }
		delta[SpaceStation<Owner>::world]?.let { world = it.string() }
		delta[SpaceStation<Owner>::x]?.let { x = it.int() }
		delta[SpaceStation<Owner>::z]?.let { z = it.int() }
		delta[SpaceStation<Owner>::radius]?.let { radius = it.int() }
		delta[SpaceStation<Owner>::owner]?.let { ownerId = it.id() }
		delta[SpaceStation<Owner>::trustLevel]?.let { trustLevel = it.enumValue() }
		delta[SpaceStation<Owner>::trustedPlayers]?.let { col -> trustedPlayers = col.mappedSet { it.slPlayerId() } }
		delta[SpaceStation<Owner>::trustedNations]?.let { col -> trustedNations = col.mappedSet { it.oid() } }

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

			if (trustLevel == SpaceStations.TrustLevel.SETTLEMENT_MEMBER &&
				cached is CachedSettlementSpaceStation &&
				cached.owner == playerSettlement) return null
		}

		// if they're in a nation, check for trust level auto perms, and trusted nations
		playerData.nationOid?.let { playerNation ->
			if (trustedNations.contains(playerNation)) return null

			if (cached !is CachedNationSpaceStation) return@let

			if (trustLevel == SpaceStations.TrustLevel.NATION_MEMBER &&
				cached.owner == playerNation) return null

			val relation = RelationCache[cached.owner, playerNation]

			if (relation.ordinal >= NationRelation.Level.ALLY.ordinal) return null
		}

		return "&cSpace station $name is claimed by ${ cached.ownerName } @ $x,$z x $radius"
	}
}
