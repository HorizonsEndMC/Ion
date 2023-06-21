package net.starlegacy.feature.nations.region.types

import com.mongodb.client.model.changestream.ChangeStreamDocument
import net.horizonsend.ion.server.database.binary
import net.horizonsend.ion.server.database.get
import net.horizonsend.ion.server.database.nullable
import net.horizonsend.ion.server.database.oid
import net.horizonsend.ion.server.database.schema.nations.moonsieges.SiegeTerritory
import net.horizonsend.ion.server.database.string
import net.starlegacy.feature.nations.NationsMap
import net.starlegacy.feature.nations.region.unpackTerritoryPolygon
import org.bukkit.entity.Player
import java.awt.Polygon

class RegionSiegeTerritory(territory: SiegeTerritory) : Region<SiegeTerritory>(territory) {
	override val priority: Int = 1
	override var world: String = territory.world
	var name = territory.name
	var nation = territory.nation

	var polygon: Polygon = unpackTerritoryPolygon(territory.polygonData); private set

	override fun contains(x: Int, y: Int, z: Int): Boolean {
		TODO("Not yet implemented")
	}

	override fun calculateInaccessMessage(player: Player): String? {
		TODO("Not yet implemented")
	}

	override fun update(delta: ChangeStreamDocument<SiegeTerritory>) {
		delta[SiegeTerritory::name]?.let { name = it.string() }
		delta[SiegeTerritory::world]?.let { world = it.string() }
		delta[SiegeTerritory::polygonData]?.let {
			polygon = unpackTerritoryPolygon(it.binary())
		}
		delta[SiegeTerritory::nation]?.let { nation = it.nullable()?.oid() }

		NationsMap.updateSiegeTerritory(this)
	}
}
