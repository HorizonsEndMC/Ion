package net.horizonsend.ion.server.starships

import com.destroystokyo.paper.event.server.ServerTickStartEvent
import net.horizonsend.ion.server.starships.control.PlayerController
import net.starlegacy.feature.starship.active.ActiveStarships
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandSendEvent
import org.bukkit.event.player.PlayerMoveEvent

class ControlListeners : Listener {
	@EventHandler
	@Suppress("Unused")
	fun onPlayerCommandSendEvent(event: PlayerCommandSendEvent) {
		event.commands.removeIf {
			when (it) {
				"dc", "directcontrol" -> true
				else -> false
			}
		}
	}

	@EventHandler
	@Suppress("Unused")
	fun onPlayerMoveEvent(event: PlayerMoveEvent) {
		(ActiveStarships.findByPassenger(event.player)?.controller as? PlayerController)?.onPlayerMoveEvent(event)
	}

	@EventHandler
	@Suppress("Unused")
	fun onServerTickStartEvent(event: ServerTickStartEvent) {
		for (starship in ActiveStarships.all()) starship.tick()
	}
}
