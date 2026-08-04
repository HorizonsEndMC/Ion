package net.horizonsend.ion.server.features.nations.region.types

import com.mongodb.client.model.changestream.ChangeStreamDocument
import net.horizonsend.ion.common.database.boolean
import net.horizonsend.ion.common.database.get
import net.horizonsend.ion.common.database.int
import net.horizonsend.ion.common.database.schema.nations.spacestation.NPCSpaceStation
import net.horizonsend.ion.common.database.string
import net.horizonsend.ion.common.utils.miscellaneous.d
import net.horizonsend.ion.common.utils.miscellaneous.squared
import net.horizonsend.ion.server.features.nations.NationsMap
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.distanceSquared
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap

class RegionNPCSpaceStation(spaceStation: NPCSpaceStation) : Region<NPCSpaceStation>(spaceStation), RegionTopLevel, RegionParent {
	override var world: String = spaceStation.world; private set

	var name: String = spaceStation.name; private set
	var x: Int = spaceStation.x; private set
	var z: Int = spaceStation.z; private set
	var radius: Int = spaceStation.radius; private set
	var dynmapDescription: String = spaceStation.dynmapDescription; private set
	var isProtected: Boolean = spaceStation.isProtected; private set
	var color: Int = spaceStation.color; private set

	override val priority: Int = 0

	override fun contains(x: Int, y: Int, z: Int): Boolean {
		return distanceSquared(this.x.d(), 0.0, this.z.d(), x.d(), 0.0, z.d()) <= radius.toDouble().squared()
	}

	override val children: MutableSet<Region<*>> = ConcurrentHashMap.newKeySet()

	override fun update(delta: ChangeStreamDocument<NPCSpaceStation>) {
		delta[NPCSpaceStation::name]?.let { name = it.string() }
		delta[NPCSpaceStation::world]?.let { world = it.string() }
		delta[NPCSpaceStation::x]?.let { x = it.int() }
		delta[NPCSpaceStation::z]?.let { z = it.int() }
		delta[NPCSpaceStation::radius]?.let { radius = it.int() }
		delta[NPCSpaceStation::color]?.let { color = it.int() }
		delta[NPCSpaceStation::dynmapDescription]?.let { dynmapDescription = it.string() }
		delta[NPCSpaceStation::isProtected]?.let { isProtected = it.boolean() }

		NationsMap.updateNpcSpaceStation(this)
	}

	override fun onCreate() {
		NationsMap.addNpcSpaceStation(this)
	}

	override fun onDelete() {
		NationsMap.removeNpcSpaceStation(this)
	}

	override fun calculateInaccessMessage(player: Player): String? {
		return "$name is claimed as the NPC outpost $name".intern()
	}
}
