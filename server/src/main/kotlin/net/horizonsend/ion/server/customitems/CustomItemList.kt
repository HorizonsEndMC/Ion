package net.horizonsend.ion.server.customitems

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.customitems.blasters.AutoRifle
import net.horizonsend.ion.server.customitems.blasters.Pistol
import net.horizonsend.ion.server.customitems.blasters.Rifle
import net.horizonsend.ion.server.customitems.blasters.Shotgun
import net.horizonsend.ion.server.customitems.blasters.Sniper
import net.kyori.adventure.text.minimessage.MiniMessage
import net.starlegacy.util.updateMeta
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

enum class CustomItemList(val itemStack: ItemStack) {
	/**
	 * This is the list of all the custom items
	 */

	SNIPER(
		ItemStack(Material.SPYGLASS).updateMeta {
			it.setCustomModelData(1)

			it.displayName(MiniMessage.miniMessage().deserialize("<bold><red>Blaster Sniper"))
			it.persistentDataContainer.set(
				NamespacedKey(IonServer.Ion, "BlasterType"),
				PersistentDataType.STRING, "SNIPER"
			)

			it.persistentDataContainer.set(
				NamespacedKey(IonServer.Ion, "ammo"),
				PersistentDataType.INTEGER, 5
			)
			it.lore(mutableListOf(MiniMessage.miniMessage().deserialize("<bold><gray>Ammo: 5/5")))
		}
	),

	RIFLE(
		ItemStack(Material.NETHERITE_HOE).updateMeta {
			it.setCustomModelData(2)

			it.displayName(MiniMessage.miniMessage().deserialize("<bold><red>Blaster Rifle"))
			it.persistentDataContainer.set(
				NamespacedKey(IonServer.Ion, "BlasterType"),
				PersistentDataType.STRING, "RIFLE"
			)

			it.persistentDataContainer.set(
				NamespacedKey(IonServer.Ion, "ammo"),
				PersistentDataType.INTEGER, 15
			)
			it.lore(mutableListOf(MiniMessage.miniMessage().deserialize("<bold><gray>Ammo: 15/15")))
		}
	),

	PISTOL(
		ItemStack(Material.NETHERITE_HOE).updateMeta {
			it.setCustomModelData(3)

			it.displayName(MiniMessage.miniMessage().deserialize("<bold><red>Blaster Pistol"))
			it.persistentDataContainer.set(
				NamespacedKey(IonServer.Ion, "BlasterType"),
				PersistentDataType.STRING, "PISTOL"
			)

			it.persistentDataContainer.set(
				NamespacedKey(IonServer.Ion, "ammo"),
				PersistentDataType.INTEGER, 15
			)
			it.lore(mutableListOf(MiniMessage.miniMessage().deserialize("<bold><gray>Ammo: 15/15")))
		}
	),

	SHOTGUN(
		ItemStack(Material.NETHERITE_HOE).updateMeta {
			it.setCustomModelData(4)

			it.displayName(MiniMessage.miniMessage().deserialize("<bold><red>Blaster Shotgun"))
			it.persistentDataContainer.set(
				NamespacedKey(IonServer.Ion, "BlasterType"),
				PersistentDataType.STRING, "SHOTGUN"
			)

			it.persistentDataContainer.set(
				NamespacedKey(IonServer.Ion, "ammo"),
				PersistentDataType.INTEGER, 2
			)
			it.lore(mutableListOf(MiniMessage.miniMessage().deserialize("<bold><gray>Ammo: 2/2")))
		}
	),

	AUTO_RIFLE(
		ItemStack(Material.NETHERITE_HOE).updateMeta {
			it.setCustomModelData(2); it.displayName(

			MiniMessage.miniMessage().deserialize("<bold><red>Auto Rifle"))
			it.persistentDataContainer.set(
				NamespacedKey(IonServer.Ion, "BlasterType"),
				PersistentDataType.STRING, "AUTO_RIFLE"
			)

			it.persistentDataContainer.set(
				NamespacedKey(IonServer.Ion, "ammo"),
				PersistentDataType.INTEGER, 30
			)
			it.lore(mutableListOf(MiniMessage.miniMessage().deserialize("<bold><gray>Ammo: 30/30")))
		}
	)
}

fun ItemStack.getBlasterType(): String? = this.itemMeta.persistentDataContainer.get(NamespacedKey(IonServer.Ion, "BlasterType"), PersistentDataType.STRING)

fun ItemStack.getCustomItem(): CustomItem? {
	val arrayOfWeapon: Map<String, CustomItem> =
		mapOf(
			Sniper.customItemlist.itemStack.getBlasterType()!! to Sniper,
			Rifle.customItemlist.itemStack.getBlasterType()!! to Rifle,
			AutoRifle.customItemlist.itemStack.getBlasterType()!! to AutoRifle,
			Pistol.customItemlist.itemStack.getBlasterType()!! to Pistol,
			Shotgun.customItemlist.itemStack.getBlasterType()!! to Shotgun
		)

	val itemMap = when (CustomItemList.values()
		.find { it.itemStack.itemMeta.customModelData == this.itemMeta.customModelData && this.type == it.itemStack.type }) {

		CustomItemList.SNIPER -> {
			arrayOfWeapon
		}
		CustomItemList.RIFLE -> {
			arrayOfWeapon
		}
		CustomItemList.AUTO_RIFLE -> {
			arrayOfWeapon
		}
		CustomItemList.PISTOL -> {
			arrayOfWeapon
		}
		CustomItemList.SHOTGUN -> {
			arrayOfWeapon
		}
		else -> return null
	}

	return itemMap[this.itemMeta.persistentDataContainer.get(NamespacedKey(IonServer.Ion, "BlasterType"), PersistentDataType.STRING)]
}