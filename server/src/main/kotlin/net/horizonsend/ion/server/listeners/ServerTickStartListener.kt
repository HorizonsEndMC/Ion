package net.horizonsend.ion.server.listeners

import com.destroystokyo.paper.event.server.ServerTickStartEvent
import net.starlegacy.feature.starship.active.ActiveStarships
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ServerTickStartListener : Listener {
	@EventHandler
	fun onServerTickStartEvent(event: ServerTickStartEvent) {
		for (starship in ActiveStarships.all()) starship.tick()
	}
}
