package net.horizonsend.ion.server.miscellaneous.listeners

import net.horizonsend.ion.server.listener.SLEventListener
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.lang.System.currentTimeMillis
import java.util.UUID

@Suppress("Unused")
class HeadListener : SLEventListener() {
	private val coolDowns = mutableMapOf<UUID, Long>()

	@EventHandler
	@Suppress("Unused")
	fun onPlayerDeathEvent(event: PlayerDeathEvent) {
		val victim = event.player

		// Player Head Drops
		val headDropCooldownEnd = coolDowns.getOrDefault(victim.uniqueId, 0)
		coolDowns[victim.uniqueId] = currentTimeMillis() + 1000 * 60 * 60
		if (headDropCooldownEnd > currentTimeMillis()) return

		val head = ItemStack(Material.PLAYER_HEAD)
		head.editMeta(SkullMeta::class.java) {
			it.owningPlayer = victim
		}

		event.entity.world.dropItem(victim.location, head)
		// Skulls end
	}
}
