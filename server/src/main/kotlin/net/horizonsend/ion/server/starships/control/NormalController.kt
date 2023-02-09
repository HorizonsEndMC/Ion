package net.horizonsend.ion.server.starships.control

import net.horizonsend.ion.server.enumSetOf
import net.horizonsend.ion.server.mainThreadCheck
import net.horizonsend.ion.server.starships.Starship
import net.minecraft.core.Direction
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket.RelativeArgument
import net.minecraft.server.level.ServerPlayer
import net.starlegacy.feature.starship.movement.StarshipMovement
import org.bukkit.event.player.PlayerMoveEvent

class NormalController(
	override val starship: Starship,
	override val serverPlayer: ServerPlayer
) : PlayerController {
	override val name: String = "Direct"

	private var centerX = serverPlayer.blockX
	private var centerZ = serverPlayer.blockZ

	private var moveX = 0.0
	private var moveZ = 0.0

	init {
		mainThreadCheck()

		serverPlayer.speed = 0.01f

		recenter()
	}

	override fun onShipMovement(starshipMovement: StarshipMovement) {
		mainThreadCheck()

		centerX = starshipMovement.displaceX(centerX, centerZ)
		centerZ = starshipMovement.displaceZ(centerZ, centerX)

		recenter()
	}

	override fun onPlayerMoveEvent(event: PlayerMoveEvent) {
		mainThreadCheck()

		moveX += event.to.x - event.from.x
		moveZ += event.to.z - event.from.z
	}

	override fun tick() {
		if (moveX == 0.0 && moveZ == 0.0) return

		mainThreadCheck()

		recenter()

		val inputX = if (moveX >= 0.075) 1 else if (moveX <= -0.075) -1 else 0
		val inputZ = if (moveZ >= 0.075) 1 else if (moveZ <= -0.075) -1 else 0

		moveX = 0.0
		moveZ = 0.0

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

	override fun cleanup() {
		mainThreadCheck()

		serverPlayer.speed = 1f
	}

	private fun recenter() {
		serverPlayer.connection.teleport(
			/* Position */ centerX + 0.5, serverPlayer.y, centerZ + 0.5,
			/* Rotation */ serverPlayer.yRot, serverPlayer.xRot,
			/* Relative */ enumSetOf(RelativeArgument.Y, RelativeArgument.Y_ROT, RelativeArgument.X_ROT)
		)
	}
}
