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
import org.bukkit.inventory.ItemStack

enum class CustomItemList(val itemStack: ItemStack) {
	/**
	 * This is the list of all the custom items
	 */
	SNIPER(ItemStack(Material.SPYGLASS).updateMeta {
		it.setCustomModelData(1); it.displayName(
		MiniMessage.miniMessage().deserialize("<bold><red>Blaster Sniper")
	)
		it.persistentDataContainer.set(
			org.bukkit.NamespacedKey(
				IonServer.Ion, "Blaster"
			), org.bukkit.persistence.PersistentDataType.INTEGER, 1
		)
		it.persistentDataContainer.set(
			org.bukkit.NamespacedKey(
				IonServer.Ion, "ammo"
			), org.bukkit.persistence.PersistentDataType.INTEGER, 5
		)
		it.lore(
			mutableListOf(
				MiniMessage.miniMessage().deserialize("<bold><gray>Ammo: 5/5")
			)
		)
	}),
	RIFLE(ItemStack(Material.NETHERITE_HOE).updateMeta {
		it.setCustomModelData(2); it.displayName(
		MiniMessage.miniMessage().deserialize("<bold><red>Blaster Rifle")
	)
		it.persistentDataContainer.set(
			org.bukkit.NamespacedKey(
				IonServer.Ion, "Blaster"
			), org.bukkit.persistence.PersistentDataType.INTEGER, 2
		)
		it.persistentDataContainer.set(
			org.bukkit.NamespacedKey(
				IonServer.Ion, "ammo"
			), org.bukkit.persistence.PersistentDataType.INTEGER, 30
		)
		it.lore(
			mutableListOf(
				MiniMessage.miniMessage().deserialize("<bold><gray>Ammo: 30/30")
			)
		)
	}),
	PISTOL(ItemStack(Material.NETHERITE_HOE).updateMeta {
		it.setCustomModelData(3); it.displayName(
		MiniMessage.miniMessage().deserialize("<bold><red>Blaster Pistol")
	); it.persistentDataContainer.set(
		org.bukkit.NamespacedKey(
			IonServer.Ion, "Blaster"
		), org.bukkit.persistence.PersistentDataType.INTEGER, 3
	)
		it.persistentDataContainer.set(
			org.bukkit.NamespacedKey(
				IonServer.Ion, "ammo"
			), org.bukkit.persistence.PersistentDataType.INTEGER, 15
		)
		it.lore(
			mutableListOf(
				MiniMessage.miniMessage().deserialize("<bold><gray>Ammo: 15/15")
			)
		)
	}),
	SHOTGUN(ItemStack(Material.NETHERITE_HOE).updateMeta {
		it.setCustomModelData(4); it.displayName(
		MiniMessage.miniMessage().deserialize("<bold><red>Blaster Shotgun")
	); it.persistentDataContainer.set(
		org.bukkit.NamespacedKey(IonServer.Ion, "Blaster"),
		org.bukkit.persistence.PersistentDataType.INTEGER,
		4
	)
		it.persistentDataContainer.set(
			org.bukkit.NamespacedKey(
				IonServer.Ion, "ammo"
			), org.bukkit.persistence.PersistentDataType.INTEGER, 2
		)
		it.lore(mutableListOf(MiniMessage.miniMessage().deserialize("<bold><gray>Ammo: 2/2")))
	}),
	AUTO_RIFLE(ItemStack(Material.NETHERITE_HOE).updateMeta {
		it.setCustomModelData(4); it.displayName(
		MiniMessage.miniMessage().deserialize("<bold><red>Auto Rifle")
	); it.persistentDataContainer.set(
		org.bukkit.NamespacedKey(IonServer.Ion, "Blaster"),
		org.bukkit.persistence.PersistentDataType.INTEGER,
		4
	)
		it.persistentDataContainer.set(
			org.bukkit.NamespacedKey(
				IonServer.Ion, "ammo"
			), org.bukkit.persistence.PersistentDataType.INTEGER, 30
		)
		it.lore(mutableListOf(MiniMessage.miniMessage().deserialize("<bold><gray>Ammo: 30/30")))
	});
}

fun ItemStack.getCustomItem(): CustomItem? {
	val arrayOfWeapon: Map<Int, CustomItem> =
		mapOf(
			Sniper.customItemlist.itemStack.itemMeta.customModelData to Sniper,
			Rifle.customItemlist.itemStack.itemMeta.customModelData to Rifle,
			AutoRifle.customItemlist.itemStack.itemMeta.customModelData to AutoRifle,
			Pistol.customItemlist.itemStack.itemMeta.customModelData to Pistol,
			Shotgun.customItemlist.itemStack.itemMeta.customModelData to Shotgun
		)

	val itemMap = when (CustomItemList.values()
		.find { it.itemStack.itemMeta.customModelData == this.itemMeta.customModelData && this.type == it.itemStack.type }) {
		CustomItemList.SNIPER -> { arrayOfWeapon }
		CustomItemList.RIFLE -> { arrayOfWeapon }
		CustomItemList.AUTO_RIFLE -> { arrayOfWeapon }
		CustomItemList.PISTOL -> { arrayOfWeapon }
		CustomItemList.SHOTGUN -> { arrayOfWeapon }
		else -> return null
	}
	return itemMap[this.itemMeta.customModelData]
}