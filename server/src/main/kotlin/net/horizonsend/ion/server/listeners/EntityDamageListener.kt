package net.horizonsend.ion.server.listeners

import net.starlegacy.feature.space.SpaceMechanics
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageCause

class EntityDamageListener : Listener {
	@EventHandler
	fun onEntityDamage(event: EntityDamageEvent) {
		val player: Player = event.entity as? Player ?: return
		val damageSource = event.cause

		check(damageSource == DamageCause.DROWNING) { return }

		if (SpaceMechanics.isWearingSpaceSuit(player)) { event.isCancelled = true }
	}
}
