package net.starlegacy.feature.nations.region.types

import com.mongodb.client.model.changestream.ChangeStreamDocument
import net.horizonsend.ion.server.database.binary
import net.horizonsend.ion.server.database.get
import net.horizonsend.ion.server.database.nullable
import net.horizonsend.ion.server.database.oid
import net.horizonsend.ion.server.database.schema.nations.landsieges.ForwardOperatingBase
import net.horizonsend.ion.server.database.string
import net.starlegacy.feature.nations.NationsMap
import net.starlegacy.feature.nations.region.unpackTerritoryPolygon
import org.bukkit.entity.Player
import java.awt.Polygon

class RegionForwardOperatingBase(fob: ForwardOperatingBase) : Region<ForwardOperatingBase>(fob) {
	override val priority: Int = 0
	override var world: String = fob.world
	var polygon: Polygon = unpackTerritoryPolygon(fob.polygonData); private set
	var nation = fob.nation
	var name = fob.name

	override fun contains(x: Int, y: Int, z: Int): Boolean {
		TODO("Not yet implemented")
	}

	override fun calculateInaccessMessage(player: Player): String? {
		TODO("Not yet implemented")
	}

	override fun update(delta: ChangeStreamDocument<ForwardOperatingBase>) {
		delta[ForwardOperatingBase::name]?.let { name = it.string() }
		delta[ForwardOperatingBase::world]?.let { world = it.string() }
		delta[ForwardOperatingBase::polygonData]?.let {
			polygon = unpackTerritoryPolygon(it.binary())
		}
		delta[ForwardOperatingBase::nation]?.let { nation = it.nullable()?.oid() }

		NationsMap.updateForwardOperatingBase(this)
	}
}
