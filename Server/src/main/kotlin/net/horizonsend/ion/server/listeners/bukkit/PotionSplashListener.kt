package net.horizonsend.ion.server.listeners.bukkit

import net.horizonsend.ion.server.annotations.BukkitListener
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PotionSplashEvent

@BukkitListener
@Suppress("Unused")
class PotionSplashListener : Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	fun onPotionSplashEvent(event: PotionSplashEvent) {
		event.isCancelled = true
	}
}