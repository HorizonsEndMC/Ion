package net.horizonsend.ion.server.listeners

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.customitems.blasters.StandardMagazine
import net.horizonsend.ion.server.customitems.blasters.StandardMagazine.getAmmo
import net.horizonsend.ion.server.customitems.getCustomItem
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.persistence.PersistentDataType

class CraftingListener : Listener {
	@EventHandler
	fun onPrepareCraftA(event: PrepareItemCraftEvent) { // For standard Magazines
		if (!event.isRepair) return

		val magazines = event.inventory.matrix.filter { it.getCustomItem() is StandardMagazine }

		if (magazines.isEmpty()) return

		val totalAmmo = magazines.sumOf { getAmmo(it!!) ?: 0 }.coerceIn(0..30) // Non-null because would be filtered out if so

		val result = StandardMagazine.customItemlist.itemStack

		result.editMeta {
			it.lore()?.clear()
			it.lore(
				mutableListOf(
					MiniMessage.miniMessage()
						.deserialize("<bold><gray>Ammo:${totalAmmo}/${StandardMagazine.capacity}")
				)
			)
		}

		result.editMeta { it.persistentDataContainer.remove(NamespacedKey(IonServer.Ion, "ammo")) }

		result.editMeta {
			it.persistentDataContainer.set(NamespacedKey(IonServer.Ion, "ammo"), PersistentDataType.INTEGER,
				totalAmmo
			)
		}

		event.inventory.result = result
	}
}