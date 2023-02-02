package net.horizonsend.ion.server.starships.control

import net.horizonsend.ion.server.enumSetOf
import net.horizonsend.ion.server.mainThreadCheck
import net.horizonsend.ion.server.starships.Starship
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

	init {
		mainThreadCheck()

		serverPlayer.speed = 0.01f

		recenter()
	}

	override fun onShipMovement(starshipMovement: StarshipMovement) {
		mainThreadCheck()

		centerX = starshipMovement.displaceX(centerX, centerZ)
		centerZ = starshipMovement.displaceZ(centerZ, centerX)
	}

	override fun onPlayerMoveEvent(event: PlayerMoveEvent) {
		mainThreadCheck()

		recenter()
	}

	override fun tick() {
		mainThreadCheck()

		recenter()
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
