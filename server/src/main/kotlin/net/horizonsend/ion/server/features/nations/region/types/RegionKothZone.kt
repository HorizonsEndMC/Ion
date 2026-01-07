package net.horizonsend.ion.server.features.nations.region.types

import com.mongodb.client.model.changestream.ChangeStreamDocument
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.array
import net.horizonsend.ion.common.database.enumValue
import net.horizonsend.ion.common.database.get
import net.horizonsend.ion.common.database.int
import net.horizonsend.ion.common.database.mappedSet
import net.horizonsend.ion.common.database.schema.nations.FrontierNation
import net.horizonsend.ion.common.database.schema.nations.KothStation
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.string
import net.horizonsend.ion.common.utils.miscellaneous.d
import net.horizonsend.ion.common.utils.miscellaneous.squared
import net.horizonsend.ion.server.features.nations.NATIONS_BALANCE
import net.horizonsend.ion.server.features.nations.NationsMap
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.distanceSquared
import org.bukkit.entity.Player
import java.time.DayOfWeek

class RegionKothZone(station: KothStation) :
	Region<KothStation>(station),
	RegionTopLevel {
	override val priority: Int = 0

	override var world: String = station.world
	var name: String = station.name; private set
	val type: Boolean = station.type
	var x: Int = station.x; private set
	var z: Int = station.z; private set
	var siegeHour: Int = station.kothHour; private set
	var siegeDays: Set<DayOfWeek> = station.kothDays; private set
	var kothTimeFrame: Int = station.kothTimeFrame; private set
	var dominantNation: Oid<FrontierNation>? = station.nation; private set

	override fun contains(x: Int, y: Int, z: Int): Boolean {

		val radiusSquared = if (type) NATIONS_BALANCE.koths.majorKothradius.squared() else NATIONS_BALANCE.koths.majorKothradius.squared()
		return distanceSquared(x.d(), 0.0, z.d(), this.x.d(), 0.0, this.z.d()) <= radiusSquared
	}

	override fun update(delta: ChangeStreamDocument<KothStation>) {
		delta[KothStation::world]?.let { world = it.string() }
		delta[KothStation::name]?.let { name = it.string() }
		delta[KothStation::x]?.let { x = it.int() }
		delta[KothStation::z]?.let { z = it.int() }
		delta[KothStation::kothHour]?.let { siegeHour = it.int() }
		delta[KothStation::kothDays]?.let { bson ->
			siegeDays = bson.array().mappedSet { it.enumValue<DayOfWeek>() }
		}
		delta[KothStation::kothTimeFrame]?.let { kothTimeFrame = it.int() }
		NationsMap.updateKingOfTheHill(this)
	}

	override fun calculateInaccessMessage(player: Player): String? {
		val noAccessMessage = "You cannot build in a King Of The Hill zone!".intern()
		return noAccessMessage
	}
}
