package net.horizonsend.ion.server.starships.control

import net.horizonsend.ion.server.enumSetOf
import net.horizonsend.ion.server.mainThreadCheck
import net.horizonsend.ion.server.starships.Starship
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket.RelativeArgument
import net.minecraft.server.level.ServerPlayer
import net.starlegacy.feature.starship.movement.StarshipMovement

class DirectController(
	override val starship: Starship,
	override val serverPlayer: ServerPlayer
) : PlayerController {
	override val name: String = "Direct"

	private var centerX = serverPlayer.blockX
	private var centerZ = serverPlayer.blockZ

	init {
		serverPlayer.speed = 0.01f
	}

	fun onShipMovement(starshipMovement: StarshipMovement) {
		mainThreadCheck()

		centerX = starshipMovement.displaceX(centerX, centerZ)
		centerZ = starshipMovement.displaceZ(centerZ, centerX)
	}

	override fun accelerationTick(): Triple<Int, Int, Int> {
		mainThreadCheck()

		val deltaX = -(centerX.toDouble() + 0.5 - serverPlayer.x)
		val deltaZ = -(centerZ.toDouble() + 0.5 - serverPlayer.z)

		serverPlayer.connection.teleport(
			/* Position */ centerX + 0.5, serverPlayer.y, centerZ + 0.5,
			/* Rotation */ serverPlayer.yRot, serverPlayer.xRot,
			/* Relative */ enumSetOf(RelativeArgument.Y, RelativeArgument.Y_ROT, RelativeArgument.X_ROT)
		)

		val movementX = if (deltaX > 0.05) 1 else if (deltaX < -0.05) -1 else 0
		val movementZ = if (deltaZ > 0.05) 1 else if (deltaZ < -0.05) -1 else 0

		return starship.globalToRelative(movementX, 0, movementZ)
	}

	override fun cleanup() {
		mainThreadCheck()

		serverPlayer.speed = 1f
	}
}
