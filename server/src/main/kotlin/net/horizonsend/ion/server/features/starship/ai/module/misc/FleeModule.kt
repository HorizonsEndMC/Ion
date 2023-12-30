package net.horizonsend.ion.server.features.starship.ai.module.misc

import net.horizonsend.ion.server.features.starship.ai.module.AIModule
import net.horizonsend.ion.server.features.starship.ai.module.targeting.TargetingModule
import net.horizonsend.ion.server.features.starship.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import java.util.function.Supplier

/**
 * @param standardPosition, the positioning supplier to use if it is not fleeing
 * @param selector, if the average shield health reaches this value, it will flee from that target
 **/
class FleeModule(
	controller: AIController,
	private val standardPosition: Supplier<Vec3i?>,
	private val targetingModule: TargetingModule,
	private val selector: (AIController, AITarget?) -> Boolean
) : AIModule(controller), Supplier<Vec3i?> {
	override fun get(): Vec3i? {
		val target = targetingModule.findTarget()
		return if (selector(controller, target)) fleeFinder(target) else standardPosition.get()
	}

	private fun fleeFinder(target: AITarget?): Vec3i? {
		if (target == null) { // Unlikely edge case
			return standardPosition.get()
		}

		// Gets a position opposite of the target 500 blocks away
		val away = target
			.getLocation()
			.toVector()
			.subtract(location.toVector())
			.normalize()
			.multiply(-500)

		return Vec3i(getCenter().clone().add(away))
	}

	override fun toString(): String {
		return "FleeModule[fleeting: ${targetingModule.findTarget() != null}, target: $targetingModule.findTarget()]"
	}
}
