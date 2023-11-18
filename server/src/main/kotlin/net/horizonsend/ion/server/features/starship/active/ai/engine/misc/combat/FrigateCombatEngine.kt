package net.horizonsend.ion.server.features.starship.active.ai.engine.misc.combat

import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ai.engine.misc.targeting.TargetingEngine
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getDirection
import net.horizonsend.ion.server.miscellaneous.utils.leftFace
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import net.horizonsend.ion.server.miscellaneous.utils.vectorToBlockFace

class FrigateCombatEngine(controller: AIController, targetingEngine: TargetingEngine) : CombatEngine(controller, targetingEngine) {
	var leftFace: Boolean = false
	var ticks = 0

	override fun tick() {
		ticks++
		val target = targetingSupplier.findTarget() ?: return

		// Get the closest axis
		(starship as ActiveControlledStarship).speedLimit = -1

		val targetBlockFace = vectorToBlockFace(getDirection(Vec3i(getCenter()), target.getVec3i(true)), includeVertical = false)

		if (ticks % 900 == 0) {
			leftFace = !leftFace
		}

		val faceDirection = if (leftFace) targetBlockFace.leftFace else targetBlockFace.rightFace

		if (ticks % turnCooldown == 0) {
			leftFace = !leftFace
		}

		handleRotation(faceDirection)

		val direction = getDirection(Vec3i(getCenter()), target.getVec3i(false)).normalize()

		fireAllWeapons(
			origin = starship.centerOfMass,
			target = target.getVec3i(false).toVector(),
			direction = direction
		)
	}
}
