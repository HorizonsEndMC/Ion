package net.horizonsend.ion.server.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PotionSplashEvent

class PotionSplashListener : Listener {
	@EventHandler
	fun onPotionSplashEvent(event: PotionSplashEvent) {
		event.isCancelled = true
	}
}