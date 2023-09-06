package net.horizonsend.ion.server.features.starship.control.controllers.ai

import net.horizonsend.ion.server.features.starship.control.movement.AIControlUtils
import net.horizonsend.ion.server.miscellaneous.utils.vectorToBlockFace

object Criterias {
	class Criteria(
        val decision: (AIController) -> Boolean,
        val action: (AIController) -> Unit
	)

	val followAndShoot = registerCriteria({ true }) { controller ->
		val starship = controller.starship

		val location = starship.centerOfMass.toLocation(starship.world)
		val nearestPlayer = AIControllers.getNearestPlayer(controller, location)

		val direction =
			nearestPlayer?.location?.toVector()?.subtract(starship.centerOfMass.toVector())

		direction?.let { AIControlUtils.faceDirection(controller, vectorToBlockFace(direction)) }

		AIControlUtils.shiftFlyTowardsPlayer(controller, nearestPlayer)

		nearestPlayer?.let {
			AIControlUtils.shootAtPlayer(controller, nearestPlayer, true)
			AIControlUtils.shootAtPlayer(controller, nearestPlayer, false, weaponSet = "phasers")
		}
	}

	fun registerCriteria(
        decision: (AIController) -> Boolean,
        action: (AIController) -> Unit
	) = Criteria(decision, action)
}
