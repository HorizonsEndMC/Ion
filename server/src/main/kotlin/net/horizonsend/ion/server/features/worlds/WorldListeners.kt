package net.horizonsend.ion.server.features.worlds

import net.horizonsend.ion.server.miscellaneous.handle
import net.starlegacy.feature.machine.AreaShields
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldInitEvent
import org.bukkit.event.world.WorldUnloadEvent

class WorldListeners : Listener {
	@EventHandler
	@Suppress("Unused")
	fun onWorldInitEvent(event: WorldInitEvent) {
		IonWorld.register(event.world.handle)
		AreaShields.loadData()
	}

	@EventHandler
	@Suppress("Unused")
	fun onWorldUnloadEvent(event: WorldUnloadEvent) {
		IonWorld.unregister(event.world.handle)
	}
}
