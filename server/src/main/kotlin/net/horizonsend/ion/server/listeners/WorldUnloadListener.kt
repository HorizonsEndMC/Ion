package net.horizonsend.ion.server.listeners

import net.horizonsend.ion.server.IonWorld
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldUnloadEvent

class WorldUnloadListener : Listener {
	@EventHandler
	@Suppress("Unused")
	fun onWorldUnloadEvent(event: WorldUnloadEvent) {
		IonWorld.unregister((event.world as CraftWorld).handle)
	}
}
