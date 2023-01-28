package net.starlegacy.feature.nations.region.types

import com.mongodb.client.model.changestream.ChangeStreamDocument
import net.starlegacy.cache.nations.NationCache
import net.starlegacy.cache.nations.PlayerCache
import net.starlegacy.database.Oid
import net.starlegacy.database.array
import net.starlegacy.database.enumValue
import net.starlegacy.database.get
import net.starlegacy.database.int
import net.starlegacy.database.mappedSet
import net.starlegacy.database.nullable
import net.starlegacy.database.oid
import net.starlegacy.database.schema.nations.CapturableStation
import net.starlegacy.database.schema.nations.Nation
import net.starlegacy.database.schema.nations.NationRelation
import net.starlegacy.database.string
import net.starlegacy.feature.nations.NATIONS_BALANCE
import net.starlegacy.feature.nations.NationsMap
import net.starlegacy.util.d
import net.starlegacy.util.distanceSquared
import net.starlegacy.util.squared
import org.bukkit.entity.Player
import java.time.DayOfWeek

class RegionCapturableStation(station: CapturableStation) :
	Region<CapturableStation>(station),
	RegionTopLevel {
	override val priority: Int = 0

	override var world: String = station.world
	var name: String = station.name; private set
	var x: Int = station.x; private set
	var z: Int = station.z; private set
	var siegeHour: Int = station.siegeHour; private set
	var siegeDays: Set<DayOfWeek> = station.siegeDays; private set
	var siegeTimeFrame: Int = station.siegeTimeFrame; private set
	var nation: Oid<Nation>? = station.nation; private set

	override fun contains(x: Int, y: Int, z: Int): Boolean {
		val radiusSquared = NATIONS_BALANCE.capturableStation.radius.squared()
		return distanceSquared(x.d(), 0.0, z.d(), this.x.d(), 0.0, this.z.d()) <= radiusSquared
	}

	override fun update(delta: ChangeStreamDocument<CapturableStation>) {
		delta[CapturableStation::world]?.let { world = it.string() }
		delta[CapturableStation::name]?.let { name = it.string() }
		delta[CapturableStation::x]?.let { x = it.int() }
		delta[CapturableStation::z]?.let { z = it.int() }
		delta[CapturableStation::siegeHour]?.let { siegeHour = it.int() }
		delta[CapturableStation::siegeDays]?.let { bson ->
			siegeDays = bson.array().mappedSet { it.enumValue<DayOfWeek>() }
		}
		delta[CapturableStation::siegeTimeFrame]?.let { siegeTimeFrame = it.int() }
		delta[CapturableStation::nation]?.let { nation = it.nullable()?.oid() }

		NationsMap.updateCapturableStation(this)
	}

	override fun calculateInaccessMessage(player: Player): String? {
		val nation = nation ?: return "$name is not claimed by any nation!".intern()

		val noAccessMessage = "$name is a station claimed by ${NationCache[nation].name}".intern()

		// if they're not in a nation they can't access any nation outposts
		val playerNation = PlayerCache[player].nation ?: return noAccessMessage

		// if they're at least an ally they can build]

		if (NationRelation.getRelationActual(playerNation, nation).ordinal >= 5) return null

		return noAccessMessage
	}
}
