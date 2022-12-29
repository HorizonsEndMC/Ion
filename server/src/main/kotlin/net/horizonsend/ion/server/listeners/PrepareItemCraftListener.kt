package net.horizonsend.ion.server.listeners

import net.horizonsend.ion.server.items.CustomItems
import net.horizonsend.ion.server.items.CustomItems.customItem
import net.horizonsend.ion.server.items.objects.Magazine
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareItemCraftEvent

class PrepareItemCraftListener : Listener {
	@EventHandler
	fun onPrepareItemCraftEvent(event: PrepareItemCraftEvent) {
		if (!event.isRepair) return // Will always be a combination of 2 items.

		val magazines = event.inventory.matrix.filter { it?.customItem is Magazine<*> }.filterNotNull()

		if (magazines.isEmpty()) return

		if (magazines.first().customItem?.identifier != magazines.last().customItem?.identifier) return

		val magazineType = magazines.first().customItem as Magazine<*>
		val totalAmmo = magazines.sumOf { magazineType.getAmmunition(it) }.coerceIn(0..magazineType.balancing.capacity)
		val resultItemStack = CustomItems.getByIdentifier(magazineType.identifier)!!.constructItemStack()

		magazineType.setAmmunition(resultItemStack, event.inventory, totalAmmo)

		event.inventory.result = resultItemStack
	}
}