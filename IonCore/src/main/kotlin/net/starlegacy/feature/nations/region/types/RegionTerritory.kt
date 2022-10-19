package net.starlegacy.feature.nations.region.types

import com.mongodb.client.model.changestream.ChangeStreamDocument
import java.awt.Polygon
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt
import net.starlegacy.SETTINGS
import net.starlegacy.cache.nations.NationCache
import net.starlegacy.cache.nations.PlayerCache
import net.starlegacy.cache.nations.SettlementCache
import net.starlegacy.database.Oid
import net.starlegacy.database.binary
import net.starlegacy.database.boolean
import net.starlegacy.database.get
import net.starlegacy.database.nullable
import net.starlegacy.database.oid
import net.starlegacy.database.schema.nations.NPCTerritoryOwner
import net.starlegacy.database.schema.nations.Nation
import net.starlegacy.database.schema.nations.NationRelation
import net.starlegacy.database.schema.nations.Settlement
import net.starlegacy.database.schema.nations.SettlementRole
import net.starlegacy.database.schema.nations.Territory
import net.starlegacy.database.slPlayerId
import net.starlegacy.database.string
import net.starlegacy.feature.nations.NationsMap
import net.starlegacy.feature.nations.region.unpackTerritoryPolygon
import org.bukkit.entity.Player
import org.litote.kmongo.eq

class RegionTerritory(territory: Territory) : Region<Territory>(territory),
	RegionTopLevel, RegionParent {
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
		get() = sqrt((polygon.bounds.width * polygon.bounds.height).toDouble()).times(SETTINGS.territoryCost).toInt()
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

		when {
			// if it's a nation outpost
			nation != null -> {
				val playerNation: Oid<Nation>? = playerData.nation

				/*                // if they're at least an ally they can build
												if (playerNation != null && RelationCache[playerNation, nation] >= NationRelation.Level.ALLY) {
														return null
												}*/

				// allow nation members
				if (playerNation == nation) {
					return null
				}

				return "$name is claimed by ${NationCache[nation].name}".intern()
			}

			// if it's a settlement
			settlement != null -> {
				val playerSettlement: Oid<Settlement>? = playerData.settlement

				val minBuildAccess = SettlementCache[settlement].minBuildAccess
					?: Settlement.ForeignRelation.SETTLEMENT_MEMBER

				// anyone can build o_o
				if (minBuildAccess == Settlement.ForeignRelation.NONE) {
					return null
				}

				// other than that ^, building in a settlement requires being in a settlement
				if (playerSettlement != null) {
					// if they're a member of the settlement...
					if (playerSettlement == settlement) {
						// if it's set to settlement members+, they can build
						if (minBuildAccess <= Settlement.ForeignRelation.SETTLEMENT_MEMBER) {
							return null
						}

						if (SettlementCache[settlement].leader == player.slPlayerId) {
							return null
						}

						if (SettlementRole.hasPermission(player.slPlayerId, SettlementRole.Permission.BUILD)) {
							return null
						}

						return "You don't have the BUILD permission and minbuildaccess is STRICT!"
					}

					val playerNation: Oid<Nation>? = playerData.nation

					// if they're in a nation, and min build access is nation member or ally there's a chance they can build
					if (playerNation != null && minBuildAccess <= Settlement.ForeignRelation.NATION_MEMBER) {
						val settlementNation = SettlementCache[settlement].nation

						// if it's nation access, they can build if they're the same nation
						if (minBuildAccess == Settlement.ForeignRelation.NATION_MEMBER && settlementNation == playerNation) {
							return null
						}

						// if the min build access is ally and they're at least an ally, they can build
						for (relation in NationRelation.find(NationRelation::nation eq settlementNation)) {
							if (minBuildAccess == Settlement.ForeignRelation.ALLY && settlementNation != null && relation.other == playerNation && relation.actual == NationRelation.Level.ALLY)
								return null
						}
					}
				}

				return "$name is claimed by the settlement ${SettlementCache[settlement].name}"
			}

			// nobody can build in npc outposts without dutymode bypass
			npcOwner != null -> {
				return "$name is claimed as the NPC outpost ${NPCTerritoryOwner.getName(npcOwner)}".intern()
			}

			// if it's unclaimed, allow them to build
			else -> return null
		}
	}

	override fun toString(): String = "$name ($world@[$centerX,$centerZ])"
}
