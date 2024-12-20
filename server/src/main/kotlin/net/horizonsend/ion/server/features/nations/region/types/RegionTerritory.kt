package net.horizonsend.ion.server.features.nations.region.types

import com.mongodb.client.model.changestream.ChangeStreamDocument
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.binary
import net.horizonsend.ion.common.database.boolean
import net.horizonsend.ion.common.database.cache.nations.AbstractPlayerCache
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.common.database.cache.nations.SettlementCache
import net.horizonsend.ion.common.database.get
import net.horizonsend.ion.common.database.nullable
import net.horizonsend.ion.common.database.oid
import net.horizonsend.ion.common.database.schema.nations.NPCTerritoryOwner
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.NationRelation
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.schema.nations.SettlementRole
import net.horizonsend.ion.common.database.schema.nations.Territory
import net.horizonsend.ion.common.database.string
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.nations.NationsMap
import net.horizonsend.ion.server.features.nations.region.unpackTerritoryPolygon
import org.bukkit.entity.Player
import java.awt.Polygon
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

class RegionTerritory(territory: Territory) :
	Region<Territory>(territory),
	RegionTopLevel,
	RegionParent {
	override val priority: Int = 0

	var name: String = territory.name; private set
	override var world: String = territory.world; private set
	var settlement: Oid<Settlement>? = territory.settlement; private set
	var nation: Oid<Nation>? = territory.nation; private set
	var npcOwner: Oid<NPCTerritoryOwner>? = territory.npcOwner; private set
	override val children: MutableSet<Region<*>> = ConcurrentHashMap.newKeySet()
	var isProtected: Boolean = territory.isProtected; private set
	var polygon: Polygon = unpackTerritoryPolygon(territory.polygonData); private set

	val oldCost
		get() = sqrt((polygon.bounds.width * polygon.bounds.height).toDouble()).times(ConfigurationFiles.legacySettings().territoryCost).toInt()
	val cost: Int
		get() {
			val n = polygon.npoints
			var sum = 0
			for (i in 0 until n) {
				sum += polygon.xpoints[i] * (polygon.ypoints[(i + 1) % n] - polygon.ypoints[(i + n - 1) % n])
			}
			return abs(sum / 2) / 100
		}

	var centerX = polygon.xpoints.average().roundToInt(); private set
	var centerZ = polygon.ypoints.average().roundToInt(); private set

	val isUnclaimed get() = settlement == null && nation == null && npcOwner == null
	val isClaimed get() = settlement != null || nation != null || npcOwner != null

	override fun contains(x: Int, y: Int, z: Int): Boolean = polygon.contains(x, z)

	override fun update(delta: ChangeStreamDocument<Territory>) {
		delta[Territory::name]?.let { name = it.string() }
		delta[Territory::world]?.let { world = it.string() }
		delta[Territory::polygonData]?.let {
			polygon = unpackTerritoryPolygon(it.binary())
		}
		delta[Territory::settlement]?.let { settlement = it.nullable()?.oid() }
		delta[Territory::nation]?.let { nation = it.nullable()?.oid() }
		delta[Territory::npcOwner]?.let { npcOwner = it.nullable()?.oid() }
		delta[Territory::isProtected]?.let {
			isProtected = it.boolean()
			centerX = polygon.xpoints.average().roundToInt()
			centerZ = polygon.ypoints.average().roundToInt()
		}

		NationsMap.updateTerritory(this)
	}

	override fun onDelete() = TODO("The world isn't ready for territories to be deleted, yet $id ($name) was deleted!")

	override fun calculateInaccessMessage(player: Player): String? {
		val playerData = PlayerCache[player]

		// to stop the compiler from complaining about mutability...
		val nation = nation
		val settlement = settlement
		val npcOwner = npcOwner

		return when {
			// if it's a nation outpost
			nation != null -> handleNationClaim(playerData, nation)

			// if it's a settlement
			settlement != null -> handleSettlement(playerData, settlement)

			// nobody can build in npc outposts without dutymode bypass
			npcOwner != null -> handleNPCOwner(npcOwner)

			// if it's unclaimed, allow them to build
			else -> null
		}
	}

	private fun handleNationClaim(playerData: AbstractPlayerCache.PlayerData, nation: Oid<Nation>): String? {
		val playerNation: Oid<Nation>? = playerData.nationOid

		/*                // if they're at least an ally they can build
										if (playerNation != null && RelationCache[playerNation, nation] >= NationRelation.Level.ALLY) {
												return null
										}*/

		// allow nation members
		if (playerNation == nation) {
			return null
		}

		return "$name is claimed by ${ NationCache[nation].name }".intern()
	}

	private fun handleSettlement(playerData: AbstractPlayerCache.PlayerData, settlement: Oid<Settlement>): String? {
		val territorySettlement = SettlementCache[settlement]

		// Default is settlement member
		val settlementBuildAccess = territorySettlement.minBuildAccess ?: Settlement.ForeignRelation.SETTLEMENT_MEMBER

		// anyone can build o_o
		if (settlementBuildAccess == Settlement.ForeignRelation.NONE) {
			return null
		}

		// If specifically trusted
		if (territorySettlement.trustedPlayers.contains(playerData.id)) {
			return null
		}
		if (territorySettlement.trustedSettlements.contains(playerData.settlementOid)) {
			return null
		}
		if (territorySettlement.trustedNations.contains(playerData.nationOid)) {
			return null
		}

		// other than that ^, building in a settlement requires being in a settlement
		val interactingPlayerSettlement: Oid<Settlement> = playerData.settlementOid ?: return "$name is claimed by the settlement ${SettlementCache[settlement].name}"

		// if they're a member of the settlement...
		if (interactingPlayerSettlement == settlement) {
			// if it's set to settlement members+, they can build
			if (settlementBuildAccess <= Settlement.ForeignRelation.SETTLEMENT_MEMBER) {
				return null
			}

			if (SettlementCache[settlement].leader == playerData.id) {
				return null
			}

			if (SettlementRole.hasPermission(playerData.id, SettlementRole.Permission.BUILD)) {
				return null
			}

			return "You don't have the BUILD permission and minbuildaccess is STRICT!"
		}

		val playerNation: Oid<Nation>? = playerData.nationOid

		// if they're in a nation, and min build access is nation member or ally there's a chance they can build
		if (playerNation != null && settlementBuildAccess >= Settlement.ForeignRelation.ALLY) {
			val settlementNation = SettlementCache[settlement].nation

			if (territorySettlement.trustedNations.contains(playerNation)) {
				return null
			}

			// if it's nation access, they can build if they're the same nation
			if (settlementBuildAccess == Settlement.ForeignRelation.NATION_MEMBER && settlementNation == playerNation) {
				return null
			}

			// if the min build access is ally, and they're at least an ally, they can build
			if (settlementNation != null) {
				if (RelationCache[settlementNation, playerNation].ordinal >= NationRelation.Level.ALLY.ordinal) {
					return null
				}
			}
		}

		return "$name is claimed by the settlement ${SettlementCache[settlement].name}"
	}

	private fun handleNPCOwner(npcTerritoryOwner: Oid<NPCTerritoryOwner>): String {
		return "$name is claimed as the NPC outpost ${NPCTerritoryOwner.getName(npcTerritoryOwner)}".intern()
	}

	override fun toString(): String = "$name ($world@[$centerX,$centerZ])"
}
