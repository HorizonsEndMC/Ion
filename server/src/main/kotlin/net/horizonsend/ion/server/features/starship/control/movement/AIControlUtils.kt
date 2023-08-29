package net.horizonsend.ion.server.features.starship.control.movement

import net.horizonsend.ion.server.features.starship.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Location
import org.bukkit.entity.Player

object AIControlUtils {
	/** Will stop moving if provided a null location **/
	fun moveToLocation(controller: AIController, location: Location?) = Tasks.async {
		val starshipLocation = controller.starship.centerOfMass.toLocation(controller.starship.world)

		if (location == null) {
			controller.isShiftFlying = false
			return@async
		}

		controller.isShiftFlying = true

		val dir = starshipLocation.toVector().subtract(location.toVector()).normalize()

		starshipLocation.direction = dir

		controller.pitch = location.pitch
		controller.yaw = location.yaw
	}

	/** Will stop moving if provided a null player **/
	fun shiftFlyTowardsPlayer(controller: AIController, player: Player?) = moveToLocation(controller, player?.location)

	fun shootAtPlayer() {

	}
}
