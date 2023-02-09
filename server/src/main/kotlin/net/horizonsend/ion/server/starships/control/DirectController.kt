package net.horizonsend.ion.server.starships.control

import net.horizonsend.ion.server.enumSetOf
import net.horizonsend.ion.server.mainThreadCheck
import net.horizonsend.ion.server.starships.Starship
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
import net.minecraft.server.level.ServerPlayer
import net.starlegacy.feature.starship.movement.StarshipMovement
import org.bukkit.event.player.PlayerMoveEvent

abstract class DirectController(
	final override val starship: Starship,
	final override val serverPlayer: ServerPlayer
) : PlayerController {
	private var centerX = serverPlayer.blockX
	private var centerZ = serverPlayer.blockZ

	private var moveX = 0.0
	private var moveZ = 0.0

	init {
		mainThreadCheck()
		serverPlayer.speed = 0.005f
		recenter()
	}

	override fun onShipMovement(starshipMovement: StarshipMovement) {
		mainThreadCheck()

		val new = starshipMovement.displaceX(centerX, centerZ)
		centerZ = starshipMovement.displaceZ(centerZ, centerX)
		centerX = new

		recenter()
	}

	override fun onPlayerMoveEvent(event: PlayerMoveEvent) {
		mainThreadCheck()

		moveX += event.to.x - event.from.x
		moveZ += event.to.z - event.from.z
	}

	override fun onShipTick() {
		if (moveX == 0.0 && moveZ == 0.0) return

		mainThreadCheck()

		recenter()

		val inputX = if (moveX >= 0.075) 1 else if (moveX <= -0.075) -1 else 0
		val inputZ = if (moveZ >= 0.075) 1 else if (moveZ <= -0.075) -1 else 0

		moveX = 0.0
		moveZ = 0.0

		onGlobalInput(inputX, inputZ)
	}

	abstract fun onGlobalInput(inputX: Int, inputZ: Int)

	override fun onControllerRemove() {
		mainThreadCheck()

		serverPlayer.speed = 1f
	}

	private fun recenter() {
		serverPlayer.connection.teleport(
			/* Position */ centerX + 0.5, serverPlayer.y, centerZ + 0.5,
			/* Rotation */ serverPlayer.yRot, serverPlayer.xRot,
			/* Relative */ enumSetOf(ClientboundPlayerPositionPacket.RelativeArgument.Y, ClientboundPlayerPositionPacket.RelativeArgument.Y_ROT, ClientboundPlayerPositionPacket.RelativeArgument.X_ROT)
		)
	}
}
