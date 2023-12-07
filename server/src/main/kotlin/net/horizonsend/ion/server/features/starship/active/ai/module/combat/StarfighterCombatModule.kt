package net.horizonsend.ion.server.features.starship.active.ai.module.combat

import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getDirection
import net.horizonsend.ion.server.miscellaneous.utils.vectorToBlockFace
import java.util.function.Supplier

class StarfighterCombatModule(controller: AIController, targetingSupplier: Supplier<AITarget?>) : CombatModule(controller, targetingSupplier) {
	override var shouldFaceTarget: Boolean = true

	override fun tick() {
		val target = targetingSupplier.get() ?: return

		// Get the closest axis
		(starship as ActiveControlledStarship).speedLimit = -1

		val faceDirection = vectorToBlockFace(getDirection(Vec3i(getCenter()), target.getVec3i(true)), includeVertical = false)

		rotateToFace(faceDirection)

		val direction = getDirection(Vec3i(getCenter()), target.getVec3i(false)).normalize()

		fireAllWeapons(
			origin = starship.centerOfMass,
			target = target.getVec3i(false).toVector(),
			direction = direction
		)
	}
}
