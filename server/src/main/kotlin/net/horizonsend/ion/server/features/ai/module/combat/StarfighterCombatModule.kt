package net.horizonsend.ion.server.features.ai.module.combat

import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getDirection
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.vectorToBlockFace
import java.util.function.Supplier

class StarfighterCombatModule(controller: AIController, targetingSupplier: Supplier<AITarget?>) : CombatModule(controller, targetingSupplier) {
	override var shouldFaceTarget: Boolean = true

	override fun tick() {
		val target = targetingSupplier.get() ?: return

		// Get the closest axis
		starship.speedLimit = -1

		val faceDirection = vectorToBlockFace(getDirection(Vec3i(getCenter()), target.getVec3i(true)), includeVertical = false)

		rotateToFace(faceDirection)

		val direction = getDirection(Vec3i(getCenter()), target.getVec3i(false)).normalize()

		handleAutoWeapons(starship.centerOfMass, target)
		fireAllWeapons(
			origin = starship.centerOfMass,
			target = target.getVec3i(true).toVector(),
			direction = direction
		)
	}
}
