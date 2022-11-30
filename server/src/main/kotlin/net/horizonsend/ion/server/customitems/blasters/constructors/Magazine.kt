package net.horizonsend.ion.server.customitems.blasters.constructors

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.customitems.CustomItem
import net.horizonsend.ion.server.customitems.getCustomItem
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.NamespacedKey
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

abstract class Magazine: CustomItem() {
	abstract val capacity: Int

	override fun onPrimaryInteract(source: LivingEntity, item: ItemStack) {}

	override fun onSecondaryInteract(entity: LivingEntity, item: ItemStack) {}

	fun removeAmmo(player: Player, count: Int): Int {
		/* Gets all the magazines in a player's inventory, and subtracts the count from their capacity.
		*
		*  It returns the maximum ammo the weapon can be filled to with the available ammo.
		*/

		if (!player.inventory.containsAtLeast(this, 1)) return 0

		val magazines = player.inventory.contents.filter { it!!.getCustomItem() == this }

		val magazineItem = magazines.first()!!
		val magazineAmmoValue = magazineItem.itemMeta.persistentDataContainer.get(NamespacedKey(IonServer.Ion, "ammo"), PersistentDataType.INTEGER)!!

		val newAmmoValue = magazineAmmoValue - count

		if (newAmmoValue > 0) {
			magazineItem.editMeta {
				it.lore()?.clear()
				it.lore(mutableListOf(
					MiniMessage.miniMessage()
						.deserialize("<bold><gray>Ammo:${magazineAmmoValue}/${this.capacity}")
					)
				)
			}

			magazineItem.editMeta { it.persistentDataContainer.remove(NamespacedKey(IonServer.Ion, "ammo")) }

			magazineItem.editMeta {
				it.persistentDataContainer.set(NamespacedKey(IonServer.Ion, "ammo"), PersistentDataType.INTEGER,
					newAmmoValue
				)
			}
			return newAmmoValue

		} else {
			player.inventory.removeItemAnySlot(magazineItem.clone())
			player.updateInventory()

			return this.capacity - magazineAmmoValue
		}
	}
}