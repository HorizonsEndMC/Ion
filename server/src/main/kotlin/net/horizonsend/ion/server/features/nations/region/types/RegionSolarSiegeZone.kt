package net.horizonsend.ion.server.features.nations.region.types

import com.mongodb.client.model.changestream.ChangeStreamDocument
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.common.database.get
import net.horizonsend.ion.common.database.int
import net.horizonsend.ion.common.database.nullable
import net.horizonsend.ion.common.database.oid
import net.horizonsend.ion.common.database.schema.nations.CapturableStation
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.SolarSiegeZone
import net.horizonsend.ion.common.database.string
import net.horizonsend.ion.common.utils.miscellaneous.d
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.nations.NationsMap
import net.horizonsend.ion.server.miscellaneous.utils.distanceSquared
import org.bukkit.entity.Player

class RegionSolarSiegeZone(station: SolarSiegeZone) : Region<SolarSiegeZone>(station), RegionTopLevel {
	override val priority: Int = 0

	override var world: String = station.world
	var name: String = station.name; private set
	var x: Int = station.x; private set
	var z: Int = station.z; private set
	var nation: Oid<Nation>? = station.nation; private set

	companion object {
		const val RADIUS = 2000
		const val RADIUS_SQUARED = RADIUS * RADIUS
	}

	override fun contains(x: Int, y: Int, z: Int): Boolean {
		return distanceSquared(x.d(), 0.0, z.d(), this.x.d(), 0.0, this.z.d()) <= RADIUS_SQUARED
	}

	override fun update(delta: ChangeStreamDocument<SolarSiegeZone>) {
		delta[SolarSiegeZone::world]?.let { world = it.string() }
		delta[SolarSiegeZone::name]?.let { name = it.string() }
		delta[SolarSiegeZone::x]?.let { x = it.int() }
		delta[SolarSiegeZone::z]?.let { z = it.int() }
		delta[CapturableStation::nation]?.let { nation = it.nullable()?.oid() }

		NationsMap.updateSolarSiege(this)
	}

	override fun onCreate() {
		NationsMap.addSolarSiege(this)
	}

	override fun onDelete() {
		NationsMap.removeSolarSiege(this)
	}

	override fun calculateInaccessMessage(player: Player): String? {
		val nation = nation ?: return "$name is not claimed by any nation!".intern()

		val noAccessMessage = "$name is a station claimed by ${ NationCache[nation].name }".intern()

		// if they're not in a nation they can't access any nation outposts
		val playerNation = PlayerCache[player].nationOid ?: return noAccessMessage

		// if they're at least an ally they can build]

		if (RelationCache[playerNation, nation].ordinal >= 5) return null

		return noAccessMessage
	}
}
