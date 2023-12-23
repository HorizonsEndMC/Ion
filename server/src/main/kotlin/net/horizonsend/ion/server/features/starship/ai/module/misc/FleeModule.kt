package net.horizonsend.ion.server.features.starship.ai.module.misc

import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.ai.module.AIModule
import net.horizonsend.ion.server.features.starship.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import java.util.function.Supplier

class FleeModule(controller: AIController, private val standardPosition: Supplier<Vec3i?>) : AIModule(controller), Supplier<Vec3i?> {
	var target: AITarget? = null

	override fun get(): Vec3i? = target?.let { getOppositeLocation(it) } ?: standardPosition.get()

	override fun onDamaged(damager: Damager) {
		target = damager.getAITarget()

		(starship as ActiveControlledStarship).speedLimit = -1
	}

	private fun getOppositeLocation(target: AITarget): Vec3i {
		// Gets a position opposite of the target 500 blocks away
		val away = target
			.getLocation()
			.toVector()
			.subtract(location.toVector())
			.normalize()
			.multiply(-500)

		return Vec3i(getCenter().clone().add(away))
	}
}
