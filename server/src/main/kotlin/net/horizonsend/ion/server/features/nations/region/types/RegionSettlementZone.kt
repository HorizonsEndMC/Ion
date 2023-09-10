package net.horizonsend.ion.server.features.nations.region.types

import com.mongodb.client.model.changestream.ChangeStreamDocument
import net.horizonsend.ion.common.database.Oid
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
import net.horizonsend.ion.common.database.schema.nations.SettlementZone
import net.horizonsend.ion.common.database.schema.nations.Territory
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.common.database.string
import net.horizonsend.ion.server.command.nations.settlementZones.SettlementZoneCommand
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.miscellaneous.utils.PerPlayerCooldown
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import org.bukkit.entity.Player

class RegionSettlementZone(zone: SettlementZone) : Region<SettlementZone>(zone) {
	override val priority: Int = 1

	var settlement: Oid<Settlement> = zone.settlement; private set
	var territory: Oid<Territory> = zone.territory; private set
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
	override val world: String get() = Regions.get<RegionTerritory>(territory).world

	private fun getRegionTerritory(): RegionTerritory {
		val territory: RegionTerritory = Regions[territory]
		requireNotNull(territory) { "Zone $id ($name) of settlement $settlement's territory $territory isn't cached!" }
		return territory
	}

	init {
		getRegionTerritory().children.add(this)
	}

	override fun onDelete() {
		getRegionTerritory().children.remove(this)
	}

	override fun contains(x: Int, y: Int, z: Int): Boolean {
		return x >= minPoint.x && x <= maxPoint.x &&
			y >= minPoint.y && y <= maxPoint.y &&
			z >= minPoint.z && z <= maxPoint.z
	}

	override fun update(delta: ChangeStreamDocument<SettlementZone>) {
		delta[SettlementZone::settlement]?.let { settlement = it.oid() }
		delta[SettlementZone::territory]?.let { territory = it.oid() }
		delta[SettlementZone::name]?.let { name = it.string() }
		delta[SettlementZone::minPoint]?.let { minPoint = it.document() }
		delta[SettlementZone::maxPoint]?.let { maxPoint = it.document() }
		delta[SettlementZone::price]?.let { cachedPrice = it.nullable()?.int() }
		delta[SettlementZone::rent]?.let { cachedRent = it.nullable()?.int() }
		delta[SettlementZone::owner]?.let { owner = it.nullable()?.slPlayerId() }
		delta[SettlementZone::trustedPlayers]?.let { bson ->
			trustedPlayers = bson.nullable()?.mappedSet { it.slPlayerId() }
		}
		delta[SettlementZone::trustedNations]?.let { bson ->
			trustedNations = bson.nullable()?.mappedSet { it.oid() }
		}
		delta[SettlementZone::trustedSettlements]?.let { bson ->
			trustedSettlements = bson.nullable()?.mappedSet { it.oid() }
		}
		delta[SettlementZone::minBuildAccess]?.let { bson ->
			minBuildAccess = bson.nullable()?.enumValue<Settlement.ForeignRelation>()
		}
	}

	override fun calculateInaccessMessage(player: Player): String? {
		if (owner == null) {
			return "This is the settlement zone $name, and it's unclaimed"
		}

		val playerData = PlayerCache[player]

		val playerNation = playerData.nationOid
		val playerSettlement = playerData.settlementOid

		if (minBuildAccess != null && minBuildAccess != Settlement.ForeignRelation.STRICT) {
			when (minBuildAccess) {
				// if someone is dumb enough to set it to none, they set it so anyone can build /shrug
				Settlement.ForeignRelation.NONE -> {
					return null
				}

				Settlement.ForeignRelation.ALLY -> {
					SettlementCache[settlement].nation?.let { nation ->
						if (playerNation != null && RelationCache[nation, playerNation] >= NationRelation.Level.ALLY) {
							return null
						}
					}
				}

				Settlement.ForeignRelation.NATION_MEMBER -> {
					SettlementCache[settlement].nation?.let { nation ->
						if (playerNation == nation) {
							return null
						}
					}
				}

				Settlement.ForeignRelation.SETTLEMENT_MEMBER -> {
					if (playerSettlement == settlement) {
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
			else -> "This is part of the settlement zone $name".intern()
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
}
