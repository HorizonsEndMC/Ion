package net.starlegacy.feature.nations.region.types

import com.mongodb.client.model.changestream.ChangeStreamDocument
import net.starlegacy.cache.nations.PlayerCache
import net.starlegacy.cache.nations.RelationCache
import net.starlegacy.cache.nations.SettlementCache
import net.starlegacy.command.nations.settlementZones.SettlementZoneCommand
import net.starlegacy.database.Oid
import net.starlegacy.database.document
import net.starlegacy.database.enumValue
import net.starlegacy.database.get
import net.starlegacy.database.int
import net.starlegacy.database.mappedSet
import net.starlegacy.database.nullable
import net.starlegacy.database.oid
import net.starlegacy.database.schema.misc.SLPlayerId
import net.starlegacy.database.schema.nations.Nation
import net.starlegacy.database.schema.nations.NationRelation
import net.starlegacy.database.schema.nations.Settlement
import net.starlegacy.database.schema.nations.SettlementZone
import net.starlegacy.database.schema.nations.Territory
import net.starlegacy.database.slPlayerId
import net.starlegacy.database.string
import net.starlegacy.feature.nations.region.Regions
import net.starlegacy.util.PerPlayerCooldown
import net.starlegacy.util.Vec3i
import org.bukkit.entity.Player

class RegionSettlementZone(zone: SettlementZone) : Region<SettlementZone>(zone) {
    override val priority: Int = 1

    var settlement: Oid<Settlement> = zone.settlement; private set
    var territory: Oid<Territory> = zone.territory; private set
    var name: String = zone.name; private set
    var minPoint: Vec3i = zone.minPoint; private set
    var maxPoint: Vec3i = zone.maxPoint; private set
    var cachedPrice: Int? = zone.price; private set
    var cachedRent: Int? = zone.rent; private set
    var owner: SLPlayerId? = zone.owner; private set
    var trustedPlayers: Set<SLPlayerId>? = zone.trustedPlayers; private set
    var trustedNations: Set<Oid<Nation>>? = zone.trustedNations; private set
    var trustedSettlements: Set<Oid<Settlement>>? = zone.trustedSettlements; private set
    var minBuildAccess: Settlement.ForeignRelation? = zone.minBuildAccess; private set
    override val world: String get() = Regions.get<RegionTerritory>(territory).world

    private fun getRegionTerritory(): RegionTerritory {
        val territory: RegionTerritory? = Regions[territory]
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
            trustedNations = bson.nullable()?.mappedSet { it.oid<Nation>() }
        }
        delta[SettlementZone::trustedSettlements]?.let { bson ->
            trustedSettlements = bson.nullable()?.mappedSet { it.oid<Settlement>() }
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

        val playerNation = playerData.nation
        val playerSettlement = playerData.settlement

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

            //this makes the mega lag
            //SettlementZoneCommand.visualizeRegion(minPoint, maxPoint, player, name.hashCode())
        }
    }
}
