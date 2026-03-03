package net.horizonsend.ion.server.features.nations.region.types

import com.mongodb.client.model.changestream.ChangeStreamDocument
import net.horizonsend.ion.common.database.array
import net.horizonsend.ion.common.database.enumValue
import net.horizonsend.ion.common.database.get
import net.horizonsend.ion.common.database.int
import net.horizonsend.ion.common.database.mappedSet
import net.horizonsend.ion.common.database.schema.nations.SiegeTerritory
import net.horizonsend.ion.common.database.string
import net.horizonsend.ion.common.utils.miscellaneous.d
import net.horizonsend.ion.common.utils.miscellaneous.squared
import net.horizonsend.ion.server.features.nations.NATIONS_BALANCE
import net.horizonsend.ion.server.features.nations.NationsMap
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.distanceSquared
import org.bukkit.entity.Player
import java.time.DayOfWeek

class RegionSiegeTerritory(territory: SiegeTerritory) : Region<SiegeTerritory>(territory), RegionTopLevel {
	override val priority: Int = 0
	override var world: String = territory.world

	var name: String = territory.name; private set
	var x: Int = territory.x; private set
	var z: Int = territory.z; private set
	var siegeHour: Int = territory.siegeHour; private set
	var siegeDays: Set<DayOfWeek> = territory.siegeDays; private set

	val SIEGE_TERRITORY_RADIUS = 250

	override fun contains(x: Int, y: Int, z: Int): Boolean {
		val radiusSquared = NATIONS_BALANCE.koths.siegeTerritoryRadius.squared()
		return distanceSquared(x.d(), 0.0, z.d(), this.x.d(), 0.0, this.z.d()) <= radiusSquared
	}

	override fun update(delta: ChangeStreamDocument<SiegeTerritory>) {
		delta[SiegeTerritory::world]?.let { world = it.string() }
		delta[SiegeTerritory::name]?.let { name = it.string() }
		delta[SiegeTerritory::x]?.let { x = it.int() }
		delta[SiegeTerritory::z]?.let { z = it.int() }
		delta[SiegeTerritory::siegeHour]?.let { siegeHour = it.int() }
		delta[SiegeTerritory::siegeDays]?.let { bson ->
			siegeDays = bson.array().mappedSet { it.enumValue<DayOfWeek>() }
		}

		// TODO: add siege territory on map
		//NationsMap.updateKingOfTheHill(this)
	}

	override fun calculateInaccessMessage(player: Player): String? {
		val noAccessMessage = "You cannot build in a King Of The Hill zone!".intern()
		return noAccessMessage
	}
}
