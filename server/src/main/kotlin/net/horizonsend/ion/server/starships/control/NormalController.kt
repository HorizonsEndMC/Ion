package net.horizonsend.ion.server.starships.control

import net.horizonsend.ion.server.starships.Starship
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerPlayer

class NormalController(starship: Starship, serverPlayer: ServerPlayer) : DirectController(starship, serverPlayer) {
	override val name: String = "Normal"

	override fun onGlobalInput(inputX: Int, inputZ: Int) {
		val (velocityX, _, velocityZ) = starship.getGlobalVelocity()

		val targetVelocityX = when (inputX) {
			1 -> starship.getSpeedInGlobalDirection(Direction.EAST)
			-1 -> -starship.getSpeedInGlobalDirection(Direction.WEST)
			else -> 0.0
		}

		val velocityXDelta = targetVelocityX - velocityX

		val thrustX = velocityXDelta.coerceIn(
			-starship.getThrustInGlobalDirection(Direction.WEST),
			starship.getThrustInGlobalDirection(Direction.EAST)
		)

		val targetVelocityZ = when (inputZ) {
			1 -> starship.getSpeedInGlobalDirection(Direction.SOUTH)
			-1 -> -starship.getSpeedInGlobalDirection(Direction.NORTH)
			else -> 0.0
		}

		val velocityZDelta = targetVelocityZ - velocityZ

		val thrustZ = velocityZDelta.coerceIn(
			-starship.getThrustInGlobalDirection(Direction.NORTH),
			starship.getThrustInGlobalDirection(Direction.SOUTH)
		)

		starship.applyGlobalAcceleration(thrustX, 0.0, thrustZ)
	}
}
