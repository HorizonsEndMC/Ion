package net.horizonsend.ion.server.features.nations.region.types

import com.mongodb.client.model.changestream.ChangeStreamDocument
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.boolean
import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.common.database.cache.nations.SettlementCache
import net.horizonsend.ion.common.database.document
import net.horizonsend.ion.common.database.enumValue
import net.horizonsend.ion.common.database.get
import net.horizonsend.ion.common.database.int
import net.horizonsend.ion.common.database.mappedSet
import net.horizonsend.ion.common.database.nullable
import net.horizonsend.ion.common.database.oid
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.NationRelation
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.schema.nations.StationZone
import net.horizonsend.ion.common.database.schema.nations.spacestation.SpaceStationInterface
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.common.database.string
import net.horizonsend.ion.server.command.nations.settlementZones.SettlementZoneCommand
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.space.spacestations.CachedNationSpaceStation
import net.horizonsend.ion.server.features.space.spacestations.CachedPlayerSpaceStation
import net.horizonsend.ion.server.features.space.spacestations.CachedSettlementSpaceStation
import net.horizonsend.ion.server.features.space.spacestations.SpaceStationCache
import net.horizonsend.ion.server.miscellaneous.utils.PerPlayerCooldown
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import org.bukkit.Location
import org.bukkit.entity.Player

class RegionStationZone(zone: StationZone) : Region<StationZone>(zone) {
	override val priority: Int = 1

	var station: Oid<SpaceStationInterface<*>> = zone.station; private set
	var name: String = zone.name; private set
	var minPoint: Vec3i = Vec3i(zone.minPoint); private set
	var maxPoint: Vec3i = Vec3i(zone.maxPoint); private set
	var cachedPrice: Int? = zone.price; private set
	var cachedRent: Int? = zone.rent; private set
	var owner: SLPlayerId? = zone.owner; private set
	var trustedPlayers: Set<SLPlayerId>? = zone.trustedPlayers; private set
	var trustedNations: Set<Oid<Nation>>? = zone.trustedNations; private set
	var trustedSettlements: Set<Oid<Settlement>>? = zone.trustedSettlements; private set
	var minBuildAccess: Settlement.ForeignRelation? = zone.minBuildAccess; private set
	var allowFriendlyFire: Boolean? = zone.allowFriendlyFire; private set
	var interactableBlocks: Set<String> = zone.interactableBlocks; private set
	override val world: String get() = SpaceStationCache[station]?.world ?: ""

	override fun contains(x: Int, y: Int, z: Int): Boolean {
		println("$name: $x, $y, $z, ${minPoint.x}, ${minPoint.y}, ${minPoint.z}, ${maxPoint.x}, ${maxPoint.y}, ${maxPoint.z}}")
		return x >= minPoint.x && x <= maxPoint.x &&
			y >= minPoint.y && y <= maxPoint.y &&
			z >= minPoint.z && z <= maxPoint.z
	}

	override fun update(delta: ChangeStreamDocument<StationZone>) {
		delta[StationZone::station]?.let { station = it.oid() }
		delta[StationZone::name]?.let { name = it.string() }
		delta[StationZone::minPoint]?.let { minPoint = it.document() }
		delta[StationZone::maxPoint]?.let { maxPoint = it.document() }
		delta[StationZone::price]?.let { cachedPrice = it.nullable()?.int() }
		delta[StationZone::rent]?.let { cachedRent = it.nullable()?.int() }
		delta[StationZone::owner]?.let { owner = it.nullable()?.slPlayerId() }
		delta[StationZone::trustedPlayers]?.let { bson ->
			trustedPlayers = bson.nullable()?.mappedSet { it.slPlayerId() }
		}
		delta[StationZone::trustedNations]?.let { bson ->
			trustedNations = bson.nullable()?.mappedSet { it.oid() }
		}
		delta[StationZone::trustedSettlements]?.let { bson ->
			trustedSettlements = bson.nullable()?.mappedSet { it.oid() }
		}
		delta[StationZone::minBuildAccess]?.let { bson ->
			minBuildAccess = bson.nullable()?.enumValue<Settlement.ForeignRelation>()
		}
		delta[StationZone::allowFriendlyFire]?.let { bson ->
			allowFriendlyFire = bson.nullable()?.boolean()
		}
		delta[StationZone::interactableBlocks]?.let { bson ->
			interactableBlocks = bson.mappedSet { it.string() }
		}
	}

	override fun calculateInaccessMessage(player: Player): String? {
		if (owner == null) {
			return "This is the station zone $name, and it's unclaimed"
		}

		val playerData = PlayerCache[player]

		val playerNation = playerData.nationOid
		val playerSettlement = playerData.settlementOid

		if (minBuildAccess != null && minBuildAccess != Settlement.ForeignRelation.STRICT) {
			val stationNation = when (SpaceStationCache[station]) {
				is CachedNationSpaceStation -> (SpaceStationCache[station] as CachedNationSpaceStation).owner
				is CachedSettlementSpaceStation -> SettlementCache[(SpaceStationCache[station] as CachedSettlementSpaceStation).owner].nation
				is CachedPlayerSpaceStation -> PlayerCache[(SpaceStationCache[station] as CachedPlayerSpaceStation).owner].nationOid
				else -> null
			}
			val stationSettlement = if (SpaceStationCache[station] is CachedSettlementSpaceStation) {
				(SpaceStationCache[station] as CachedSettlementSpaceStation).owner
			} else null

			when (minBuildAccess) {
				// if someone is dumb enough to set it to none, they set it so anyone can build /shrug
				Settlement.ForeignRelation.NONE -> {
					return null
				}

				Settlement.ForeignRelation.ALLY -> {
					if (playerNation != null && stationNation != null && RelationCache[stationNation, playerNation] >= NationRelation.Level.ALLY) {
						return null
					}
				}

				Settlement.ForeignRelation.NATION_MEMBER -> {
					if (stationNation != null && playerNation == stationNation) {
						return null
					}
				}

				Settlement.ForeignRelation.SETTLEMENT_MEMBER -> {
					if (stationSettlement != null && playerSettlement == stationSettlement) {
						return null
					}
				}

				Settlement.ForeignRelation.STRICT -> error("WRONG! I ALREADY CHECKED! IT CAN'T BE! WHAT TRICKERY IS THIS?")
				else -> {}
			}
		}

		return when {
			player.slPlayerId == owner -> null
			trustedPlayers?.contains(player.slPlayerId) == true -> null
			trustedNations?.contains(playerNation) == true -> null
			trustedSettlements?.contains(playerSettlement) == true -> null
			else -> "This is part of the station zone $name".intern()
		}
	}

	private val visualizationCooldown by lazy { PerPlayerCooldown(SettlementZoneCommand.VISUALIZATION_DURATION) }

	override fun onFailedToAccess(player: Player) {
		visualizationCooldown.tryExec(player) {
			// I know I'm dumb for putting this in the settlement zone command class.
			// I don't care.

			// this makes the mega lag
			// SettlementZoneCommand.visualizeRegion(minPoint, maxPoint, player, name.hashCode())
		}
	}

	fun getInteractableBlocks(): String {
		return interactableBlocks.joinToString()
	}
}
