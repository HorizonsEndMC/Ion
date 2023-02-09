package net.horizonsend.ion.server.starships.control

import net.horizonsend.ion.server.starships.Starship
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerPlayer

class AdvancedController(starship: Starship, serverPlayer: ServerPlayer) : DirectController(starship, serverPlayer) {
	override val name: String = "Advanced"

	override fun onGlobalInput(inputX: Int, inputZ: Int) {
		val (velocityX, _, velocityZ) = starship.getGlobalVelocity()

		val thrustX = when (inputX) {
			1 -> velocityX + starship.getThrustInGlobalDirection(Direction.EAST)
			-1 -> velocityX - starship.getThrustInGlobalDirection(Direction.WEST)
			else -> velocityX
		}.coerceIn(
			-starship.getSpeedInGlobalDirection(Direction.WEST),
			starship.getSpeedInGlobalDirection(Direction.EAST)
		) - velocityX

		val thrustZ = when (inputZ) {
			1 -> velocityZ + starship.getThrustInGlobalDirection(Direction.SOUTH)
			-1 -> velocityZ - starship.getThrustInGlobalDirection(Direction.NORTH)
			else -> velocityZ
		}.coerceIn(
			-starship.getSpeedInGlobalDirection(Direction.NORTH),
			starship.getSpeedInGlobalDirection(Direction.SOUTH)
		) - velocityZ

		starship.applyGlobalAcceleration(thrustX, 0.0, thrustZ)
	}
}
