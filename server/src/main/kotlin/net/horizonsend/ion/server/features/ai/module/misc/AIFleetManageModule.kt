package net.horizonsend.ion.server.features.ai.module.misc

import net.horizonsend.ion.server.features.ai.module.AIModule
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.fleet.Fleet
import net.horizonsend.ion.server.features.starship.fleet.toFleetMember
import java.lang.ref.WeakReference

class AIFleetManageModule(controller: AIController, val fleet: Fleet) : AIModule(controller) {
	init {
	    fleet.add(this.starship.toFleetMember())
	}

	override fun tick() {
		// Quick check to see if the ship has sunk
		if (!starship.isExploding) return
		// If sunk, remove this ship
		fleet.remove(this.starship.toFleetMember())
	}

	fun size(): Int = fleet.members.size

	class AIFleet {
		val members: MutableSet<WeakReference<Starship>> = mutableSetOf()
	}
}
