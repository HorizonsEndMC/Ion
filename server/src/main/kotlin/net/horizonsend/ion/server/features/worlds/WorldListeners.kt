package net.horizonsend.ion.server.features.worlds

import net.starlegacy.feature.machine.AreaShields
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldInitEvent
import org.bukkit.event.world.WorldUnloadEvent

class WorldListeners : Listener {
	@EventHandler
	@Suppress("Unused")
	fun onWorldInitEvent(event: WorldInitEvent) {
		IonWorld.register((event.world as CraftWorld).handle)
		AreaShields.loadData()
	}

	@EventHandler
	@Suppress("Unused")
	fun onWorldUnloadEvent(event: WorldUnloadEvent) {
		IonWorld.unregister((event.world as CraftWorld).handle)
	}
}
