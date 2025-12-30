package net.horizonsend.ion.server.features.nations.region.types

import com.mongodb.client.model.changestream.ChangeStreamDocument
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.binary
import net.horizonsend.ion.common.database.boolean
import net.horizonsend.ion.common.database.cache.nations.FrontierNationCache
import net.horizonsend.ion.common.database.get
import net.horizonsend.ion.common.database.nullable
import net.horizonsend.ion.common.database.oid
import net.horizonsend.ion.common.database.schema.nations.FrontierNation
import net.horizonsend.ion.common.database.schema.nations.FrontierTerritory
import net.horizonsend.ion.common.database.string
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.nations.region.unpackTerritoryPolygon
import org.bukkit.entity.Player
import java.awt.Polygon
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt

class RegionFrontierTerritory(territory: FrontierTerritory) :
	Region<FrontierTerritory>(territory),
	RegionTopLevel,
	RegionParent {
	override val priority: Int = 0
	var name: String = territory.name; private set
	override var world: String = territory.world; private set
	var frontierNation: Oid<FrontierNation>? = territory.frontierNation; private set
	var alias: String? = territory.alias; private set
	var isCapital: Boolean = territory.isCapital; private set
	override var children: MutableSet<Region<*>> = ConcurrentHashMap.newKeySet()
	var polygon: Polygon = unpackTerritoryPolygon(territory.polygonData); private set

	var centerX = polygon.xpoints.average().roundToInt(); private set
	var centerZ = polygon.ypoints.average().roundToInt(); private set

	override fun contains(x: Int, y: Int, z: Int): Boolean = polygon.contains(x, z)

	override fun update(delta: ChangeStreamDocument<FrontierTerritory>) {
		delta[FrontierTerritory::name]?.let { name = it.string() }
		delta[FrontierTerritory::world]?.let { world = it.string() }
		delta[FrontierTerritory::polygonData]?.let {
			polygon = unpackTerritoryPolygon(it.binary())
		}
		delta[FrontierTerritory::frontierNation]?.let { frontierNation = it.nullable()?.oid() }
		delta[FrontierTerritory::alias]?.let { alias = it.string() }
		delta[FrontierTerritory::isCapital]?.let { isCapital = it.boolean() }

		// TODO: Update Dynmap
	}

	override fun calculateInaccessMessage(player: Player): String? {
		val playerData = PlayerCache[player]
		val playerNation: Oid<FrontierNation>? = playerData.frontierNationOid
		val nation = frontierNation ?: return null

		if (playerNation == nation) {
			return null
		}

		return "$name is claimed by ${FrontierNationCache[nation].name}"
	}

	override fun toString(): String = "$name ($world@[$centerX,$centerZ])"
}
