package net.horizonsend.ion.server.features.nations.region.types

import com.mongodb.client.model.changestream.ChangeStreamDocument
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.get
import net.horizonsend.ion.common.database.int
import net.horizonsend.ion.common.database.nullable
import net.horizonsend.ion.common.database.oid
import net.horizonsend.ion.common.database.schema.nations.GasDepot
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.string
import net.horizonsend.ion.common.utils.miscellaneous.d
import net.horizonsend.ion.common.utils.miscellaneous.squared
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.distanceSquared
import org.bukkit.entity.Player

class RegionGasDepot(depot: GasDepot) :
	Region<GasDepot>(depot),
	RegionTopLevel {
	override val priority: Int = 0

	override var world: String = depot.world
	var name: String = depot.name; private set
	var x: Int = depot.x; private set
	var z: Int = depot.z; private set
	var nation: Oid<Nation>? = depot.nation; private set

	companion object {
		const val RADIUS = 1000
	}

	override fun contains(x: Int, y: Int, z: Int): Boolean {
		return distanceSquared(x.d(), 0.0, z.d(), this.x.d(), 0.0, this.z.d()) <= RADIUS.squared()
	}

	override fun update(delta: ChangeStreamDocument<GasDepot>) {
		delta[GasDepot::world]?.let { world = it.string() }
		delta[GasDepot::name]?.let { name = it.string() }
		delta[GasDepot::x]?.let { x = it.int() }
		delta[GasDepot::z]?.let { z = it.int() }
		delta[GasDepot::nation]?.let { nation = it.nullable()?.oid() }
	}

	override fun calculateInaccessMessage(player: Player): String? = null
}
