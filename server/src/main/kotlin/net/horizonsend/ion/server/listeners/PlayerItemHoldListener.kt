package net.horizonsend.ion.server.listeners

import net.horizonsend.ion.server.items.CustomItems.customItem
import net.horizonsend.ion.server.items.objects.Blaster
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.RED
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType.FAST_DIGGING

class PlayerItemHoldListener : Listener {

	@EventHandler
	fun onPlayerItemHoldEvent(event: PlayerItemHeldEvent) {
		val itemStack = event.player.inventory.getItem(event.newSlot) ?: return
		val customItem = itemStack.customItem as? Blaster<*> ?: return

		// adding a potion effect because it takes ages for that attack cooldown to come up
		event.player.addPotionEffect(PotionEffect(FAST_DIGGING, 20, 5, false, false, false))

		val ammunition = customItem.getAmmunition(itemStack)

		event.player.sendActionBar(text("Ammo: $ammunition / ${customItem.balancing.magazineSize}", RED))
	}
}