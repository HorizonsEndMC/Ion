package net.horizonsend.ion.server.listeners.bukkit

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PotionSplashEvent

@Suppress("Unused")
class PotionSplashListener : Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	fun onPotionSplashEvent(event: PotionSplashEvent) {
		event.isCancelled = true
	}
}