package net.horizonsend.ion.server.features.ai.module.combat

import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import net.horizonsend.ion.server.miscellaneous.utils.getDirection
import net.horizonsend.ion.server.miscellaneous.utils.leftFace
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import net.horizonsend.ion.server.miscellaneous.utils.vectorToBlockFace
import java.util.function.Supplier

class MultiTargetFrigateCombatModule(controller: AIController, private val toggleRandomTargeting: Boolean = true, targetingSupplier: Supplier<List<AITarget>>) : MultiTargetCombatModule(controller, targetingSupplier) {
	var leftFace: Boolean = false
	var ticks = 0
	private var aimAtRandom = false

	override fun tick() {
		ticks++
		val targets = targetingSupplier.get() ?: return
        val numTargets = targets.size
		val target = targets[ticks % numTargets]

		// Get the closest axis
		(starship as ActiveControlledStarship).speedLimit = -1

		if (shouldFaceTarget) handleRotation(target)

		val direction = getDirection(Vec3i(getCenter()), target.getVec3i(false)).normalize()

		handleAutoWeapons(starship.centerOfMass, target)
		fireAllWeapons(
			origin = starship.centerOfMass,
			target = target.getVec3i(aimAtRandom).toVector(),
			direction = direction
		)

		if (toggleRandomTargeting && ticks % 40 == 0) {
			aimAtRandom = !aimAtRandom
		}
	}

	private fun handleRotation(target: AITarget) {
		if (ticks % 900 == 0) {
			leftFace = !leftFace
		}

		val targetBlockFace = vectorToBlockFace(getDirection(Vec3i(getCenter()), target.getVec3i(true)), includeVertical = false)
		val faceDirection = if (leftFace) targetBlockFace.leftFace else targetBlockFace.rightFace

		if (ticks % turnCooldown == 0) {
			leftFace = !leftFace
		}

		debugAudience.debug("$this: Trying to face $faceDirection")
		rotateToFace(faceDirection)
	}
}