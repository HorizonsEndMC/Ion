package net.horizonsend.ion.server.listeners.bukkit

import com.destroystokyo.paper.event.server.ServerTickStartEvent
import net.horizonsend.ion.server.IonWorld
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ServerTickStartListener : Listener {
	@EventHandler
	fun onServerTickStartEvent(event: ServerTickStartEvent) {
		if (event.tickNumber % 20 != 0) return

		IonWorld.tick()
	}
}