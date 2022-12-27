package net.horizonsend.ion.server.listeners

import net.horizonsend.ion.server.items.CustomItems
import net.horizonsend.ion.server.items.CustomItems.customItem
import net.horizonsend.ion.server.items.objects.Magazine
import net.horizonsend.ion.server.items.objects.Magazine.getAmmunition
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareItemCraftEvent

class PrepareItemCraftListener : Listener {
	@EventHandler
	fun onPrepareItemCraftEvent(event: PrepareItemCraftEvent) {
		if (!event.isRepair) return

		val magazines = event.inventory.matrix.filter { it?.customItem is Magazine }.filterNotNull()
		if (magazines.isEmpty()) return

		val totalAmmo = magazines.sumOf { getAmmunition(it) }.coerceIn(0..30)

		val resultItemStack = CustomItems.MAGAZINE.constructItemStack()
		CustomItems.MAGAZINE.setAmmunition(resultItemStack, event.inventory, totalAmmo)

		event.inventory.result = resultItemStack
	}
}