package net.horizonsend.ion.server.customitems.blasters.constructors

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.customitems.CustomItem
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

abstract class Magazine : CustomItem() {
	abstract val capacity: Int

	fun setAmmo(item: ItemStack, player: Player, newValue: Int) {

		if (newValue > this.capacity) { // Ammo shouldn't be higher than capacity
			Throwable().printStackTrace()
			throw (Exception("Ammo out of Bounds!"))
		} else if (newValue > 0) {
			item.editMeta {
				it.lore()?.clear()
				it.lore(mutableListOf(
					MiniMessage.miniMessage()
						.deserialize("<bold><gray>Ammo:${newValue}/${this.capacity}")
				)
				)
			}

			item.editMeta { it.persistentDataContainer.remove(NamespacedKey(IonServer.Ion, "ammo")) }

			item.editMeta {
				it.persistentDataContainer.set(NamespacedKey(IonServer.Ion, "ammo"), PersistentDataType.INTEGER,
					newValue
				)
			}
		} else { // Delete item if ammo goes negative
			player.inventory.removeItemAnySlot(item.clone())
			player.updateInventory()
		}
	}

	open fun getAmmo(magazine: ItemStack): Int? =
		magazine.itemMeta.persistentDataContainer.get(NamespacedKey(IonServer.Ion, "ammo"), PersistentDataType.INTEGER)
}