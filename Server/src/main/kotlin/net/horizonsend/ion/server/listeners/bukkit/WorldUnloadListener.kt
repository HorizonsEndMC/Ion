package net.horizonsend.ion.server.listeners.bukkit

import net.horizonsend.ion.server.IonLevelData
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldUnloadEvent

class WorldUnloadListener : Listener {
	@EventHandler
	fun onWorldUnloadEvent(event: WorldUnloadEvent) {
		IonLevelData.unregister((event.world as CraftWorld).handle)
	}
}