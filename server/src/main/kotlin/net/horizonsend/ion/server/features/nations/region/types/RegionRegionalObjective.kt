package net.horizonsend.ion.server.features.nations.region.types

import com.mongodb.client.model.changestream.ChangeStreamDocument
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.get
import net.horizonsend.ion.common.database.int
import net.horizonsend.ion.common.database.nullable
import net.horizonsend.ion.common.database.oid
import net.horizonsend.ion.common.database.string
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.RegionalObjective
import net.horizonsend.ion.common.database.schema.nations.RegionalObjectiveType
import net.horizonsend.ion.common.utils.miscellaneous.d
import net.horizonsend.ion.common.utils.miscellaneous.squared
import net.horizonsend.ion.server.features.nations.NationsMap
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.distanceSquared
import org.bukkit.entity.Player

class RegionRegionalObjective(objective: RegionalObjective) :
	Region<RegionalObjective>(objective),
	RegionTopLevel {

	override val priority: Int = 0
	override var world: String = objective.world
	var name: String = objective.name; private set
	var x: Int = objective.x; private set
	var z: Int = objective.z; private set
	var nation: Oid<Nation>? = objective.nation; private set
	val type: RegionalObjectiveType = objective.type

	companion object {
		const val RADIUS = 500
	}

	override fun contains(x: Int, y: Int, z: Int): Boolean =
		distanceSquared(x.d(), 0.0, z.d(), this.x.d(), 0.0, this.z.d()) <= RADIUS.squared()

	override fun update(delta: ChangeStreamDocument<RegionalObjective>) {
		delta[RegionalObjective::world]?.let { world = it.string() }
		delta[RegionalObjective::name]?.let { name = it.string() }
		delta[RegionalObjective::x]?.let { x = it.int() }
		delta[RegionalObjective::z]?.let { z = it.int() }
		delta[RegionalObjective::nation]?.let { nation = it.nullable()?.oid() }
		NationsMap.updateRegionalObjective(this)
	}

	override fun calculateInaccessMessage(player: Player): String? = null
}
