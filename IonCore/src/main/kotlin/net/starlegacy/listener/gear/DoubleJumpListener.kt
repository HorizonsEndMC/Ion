package net.starlegacy.listener.gear

import java.util.UUID
import net.starlegacy.feature.space.SpaceWorlds
import net.starlegacy.listener.SLEventListener
import org.bukkit.FluidCollisionMode
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerToggleFlightEvent
import org.bukkit.util.Vector

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

		if (player.gameMode != GameMode.SURVIVAL || SpaceWorlds.contains(player.world)) {
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
		val loc = player.location
		val dir = Vector(0.0, -1.0, 0.0)
		val maxDistance = 0.1
		val fluidCollisionMode = FluidCollisionMode.ALWAYS
		return player.world.rayTraceBlocks(loc, dir, maxDistance, fluidCollisionMode) != null
	}
}
