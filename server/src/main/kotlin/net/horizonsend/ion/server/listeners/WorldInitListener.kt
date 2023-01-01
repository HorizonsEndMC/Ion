package net.horizonsend.ion.server.listeners

import net.horizonsend.ion.server.IonWorld
import net.starlegacy.feature.machine.AreaShields
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldInitEvent

class WorldInitListener : Listener {
	@EventHandler
	@Suppress("Unused")
	fun onWorldInitEvent(event: WorldInitEvent) {
		IonWorld.register((event.world as CraftWorld).handle)
		AreaShields.loadData()
	}
}
