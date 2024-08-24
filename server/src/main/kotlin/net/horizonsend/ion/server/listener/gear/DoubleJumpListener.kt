package net.horizonsend.ion.server.listener.gear

import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.listener.SLEventListener
import org.bukkit.FluidCollisionMode
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerToggleFlightEvent
import org.bukkit.util.Vector
import java.util.UUID

object DoubleJumpListener : SLEventListener() {
	private val jumpingPlayers = HashSet<UUID>()

	@EventHandler
	fun onMove(event: PlayerMoveEvent) {
		val player = event.player

		if (isGrounded(player)) {
			jumpingPlayers.remove(player.uniqueId)
			player.allowFlight = true
		}
	}

	@EventHandler
	fun onToggleFlight(event: PlayerToggleFlightEvent) {
		if (!event.isFlying) {
			return
		}

		val player = event.player

		if (player.gameMode != GameMode.SURVIVAL || player.world.ion.hasFlag(WorldFlag.SPACE_WORLD)) {
			return
		}

		event.isCancelled = true

		if (jumpingPlayers.contains(player.uniqueId)) {
			return
		}

		player.velocity = player.velocity.add(Vector(0.0, 0.75, 0.0))
		jumpingPlayers.add(player.uniqueId)
		player.allowFlight = false
	}

	private fun isGrounded(player: Player): Boolean {
		return player.world.rayTraceBlocks(
			player.location,
			Vector(0.0, -1.0, 0.0),
			0.1,
			FluidCollisionMode.ALWAYS
		)?.hitBlock?.isCollidable == true
	}
}
