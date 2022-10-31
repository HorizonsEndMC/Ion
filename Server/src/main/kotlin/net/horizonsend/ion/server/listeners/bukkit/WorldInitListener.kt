package net.horizonsend.ion.server.listeners.bukkit

import net.horizonsend.ion.server.IonWorld
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldInitEvent

class WorldInitListener : Listener {
	@EventHandler
	fun onWorldInitEvent(event: WorldInitEvent) {
		IonWorld.register((event.world as CraftWorld).handle)
	}
}