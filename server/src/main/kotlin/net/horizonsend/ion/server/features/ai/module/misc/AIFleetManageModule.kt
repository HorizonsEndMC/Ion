package net.horizonsend.ion.server.features.ai.module.misc

import net.horizonsend.ion.server.features.ai.module.AIModule
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import java.lang.ref.WeakReference

class AIFleetManageModule(controller: AIController, val fleet: AIFleet) : AIModule(controller) {
	init {
	    fleet.members.add(WeakReference(this.starship))
	}

	override fun tick() {
		// Quick check to see if the ship has sunk
		if (!starship.isExploding) return
		// If sunk, remove this ship, or any ones that got GC'd
		fleet.members.removeAll { (it.get() ?: return@removeAll true) == this.starship }
	}

	fun size(): Int = fleet.members.size

	class AIFleet {
		val members: MutableSet<WeakReference<Starship>> = mutableSetOf()
	}
}
