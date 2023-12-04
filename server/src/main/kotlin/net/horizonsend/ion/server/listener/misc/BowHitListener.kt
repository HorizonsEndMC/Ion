package net.horizonsend.ion.server.listener.misc

import net.horizonsend.ion.server.listener.SLEventListener
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageByEntityEvent

object BowHitListener : SLEventListener() {
	@EventHandler(priority = EventPriority.LOW)
	@Suppress("unused")
	fun onBowHit(event: EntityDamageByEntityEvent) {
		if (event.damager is Projectile && ProtectionListener.isProtectedCity(event.entity.location)) {
			event.isCancelled = true
		}
	}
}
