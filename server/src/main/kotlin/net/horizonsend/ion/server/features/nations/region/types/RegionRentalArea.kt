package net.horizonsend.ion.server.features.nations.region.types

import com.mongodb.client.model.changestream.ChangeStreamDocument
import net.horizonsend.ion.common.database.document
import net.horizonsend.ion.common.database.double
import net.horizonsend.ion.common.database.get
import net.horizonsend.ion.common.database.int
import net.horizonsend.ion.common.database.long
import net.horizonsend.ion.common.database.schema.economy.StationRentalArea
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.common.database.string
import net.horizonsend.ion.common.utils.DBVec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import org.bukkit.entity.Player

class RegionRentalArea(zone: StationRentalArea) : Region<StationRentalArea>(zone) {
	override val priority: Int = 1

	override val world: String = zone.world

	var minPoint: Vec3i = Vec3i(zone.minPoint); private set
	var maxPoint: Vec3i = Vec3i(zone.maxPoint); private set

	var name: String = zone.name; private set

	var rent: Int = zone.rent; private set
	var owner: SLPlayerId? = zone.owner; private set
	var rentBalance: Double = zone.rentBalance; private set
	var rentLastCharged: Long = zone.rentLastCharged; private set

	override fun contains(x: Int, y: Int, z: Int): Boolean {
		if (x > maxPoint.x || x < minPoint.x) return false
		if (y > maxPoint.y || y < minPoint.y) return false
		return !(z > maxPoint.z || z < minPoint.z)
	}

	override fun update(delta: ChangeStreamDocument<StationRentalArea>) {
		delta[StationRentalArea::name]?.let { name = it.string() }

		delta[StationRentalArea::minPoint]?.let { minPoint = Vec3i(it.document<DBVec3i>()) }
		delta[StationRentalArea::maxPoint]?.let { maxPoint = Vec3i(it.document<DBVec3i>()) }

		delta[StationRentalArea::owner]?.let { owner = it.slPlayerId() }
		delta[StationRentalArea::rent]?.let { rent = it.int() }
		delta[StationRentalArea::rentBalance]?.let { rentBalance = it.double() }
		delta[StationRentalArea::rentLastCharged]?.let { rentLastCharged = it.long() }
	}

	override fun calculateInaccessMessage(player: Player): String? {
		if (player.slPlayerId != owner) return "You don't own this zone!".intern()
		return null
	}
}

