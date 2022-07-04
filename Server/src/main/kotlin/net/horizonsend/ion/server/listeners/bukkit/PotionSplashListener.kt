package net.horizonsend.ion.server.listeners.bukkit

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PotionSplashEvent

class PotionSplashListener : Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	@Suppress("Unused")
	fun onPotionSplashEvent(event: PotionSplashEvent) {
		event.isCancelled = true
	}
}