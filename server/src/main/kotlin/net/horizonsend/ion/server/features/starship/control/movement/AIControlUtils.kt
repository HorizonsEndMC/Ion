package net.horizonsend.ion.server.features.starship.control.movement

import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.control.weaponry.StarshipWeaponry
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.leftFace
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import net.horizonsend.ion.server.miscellaneous.utils.vectorToPitchYaw
import net.horizonsend.ion.server.miscellaneous.utils.yawToBlockFace
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.math.roundToInt

object AIControlUtils {
	/** Will stop moving if provided a null vector **/
	fun shiftFlyInDirection(controller: AIController, direction: Vector?) {
		if (direction == null) {
			controller.isShiftFlying = false
			return
		}

		val (pitch, yaw) = vectorToPitchYaw(direction)

		controller.pitch = pitch
		controller.yaw = yaw

		controller.isShiftFlying = true
	}

	/** Will stop moving if provided a null location **/
	fun shiftFlyToLocation(controller: AIController, location: Location?) = Tasks.async {
		val starshipLocation = controller.starship.centerOfMass.toLocation(controller.starship.world)

		if (location == null) {
			controller.isShiftFlying = false
			return@async
		}

		val dir = location.toVector().subtract(starshipLocation.toVector())

		shiftFlyInDirection(controller, dir)
	}

	/** Will stop moving if provided a null player **/
	fun shiftFlyTowardsPlayer(controller: AIController, player: Player?) = shiftFlyToLocation(controller, player?.location)

	/** Will attempt to face in the specified direction **/
	fun faceDirection(controller: AIController, direction: BlockFace) {
		if (controller.starship !is ActiveControlledStarship) return

		if (direction == BlockFace.UP || direction == BlockFace.DOWN) return
		val starship = (controller.starship as? ActiveControlledStarship) ?: return

		val isFacing = controller.starship.forward
		if (starship.pendingRotations.isNotEmpty()) return

		// 1.5 sec turn delay
		if (System.currentTimeMillis() - controller.lastRotation < 1500) return

		when (direction) {
			// Facing the same direction
			isFacing -> return

			// New direction is to the right
			isFacing.rightFace -> starship.tryRotate(true)

			// New direction is to the left
			isFacing.leftFace -> starship.tryRotate(false)

			// New direction is backwards, just rotate either way
			isFacing.oppositeFace -> starship.tryRotate(true)

			else -> return
		}

		controller.lastRotation = System.currentTimeMillis()
	}

	fun shootAtPlayer(
		controller: AIController,
		player: Player,
		leftClick: Boolean,
		controllerLoc: Vector? = null,
		weaponSet: String? = null,
	) {
		shootAtTarget(
			controller,
			player.location.toVector(),
			leftClick,
			controllerLoc,
			weaponSet
		)
	}

	fun shootAtTarget(
		controller: AIController,
		target: Vector,
		leftClick: Boolean,
		controllerLoc: Vector? = null,
		weaponSet: String? = null,
	) {
		val originLocation = controllerLoc ?: controller.starship.centerOfMass.toVector()

		val direction = target.clone().subtract(originLocation)

		shootInDirection(
			controller,
			direction,
			leftClick,
			controllerLoc,
			weaponSet
		)
	}

	fun shootInDirection(
		controller: AIController,
		direction: Vector,
		leftClick: Boolean,
		target: Vector? = null,
		weaponSet: String? = null,
		controllerLoc: Location? = null
	) {
		val originLocation = controllerLoc ?: controller.starship.centerOfMass.toLocation(controller.starship.world)

		StarshipWeaponry.manualFire(
			controller,
			controller.starship,
			leftClick,
			yawToBlockFace(controller.yaw.roundToInt()),
			direction,
			target ?: StarshipWeaponry.getTarget(originLocation, direction, controller.starship),
			weaponSet
		)
	}

	fun setAutoWeapons(controller: AIController, target: Player) {

	}
}
