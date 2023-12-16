package net.horizonsend.ion.server.listener.misc

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.PlayerDeathEvent

object PlayerDeathListener : SLEventListener() {
	@EventHandler(ignoreCancelled = false, priority = EventPriority.MONITOR)
	fun onPlayerDeath(event: PlayerDeathEvent) {
		if (event.isCancelled) return

		if (!IonServer.configuration.crossServerDeathMessages) return

		val message = event.deathMessage()
		message?.let {
			Notify.chatAndGlobal(message)
			event.deathMessage(null)
		}
	}
}
