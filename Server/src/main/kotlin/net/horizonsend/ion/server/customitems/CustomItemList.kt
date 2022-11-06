package net.horizonsend.ion.server.customitems

import net.horizonsend.ion.server.IonServer
import net.kyori.adventure.text.minimessage.MiniMessage
import net.starlegacy.util.updateMeta
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

enum class CustomItemList(val itemStack: ItemStack) {
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
			net.horizonsend.ion.server.IonServer.Ion, "ammo"
		), org.bukkit.persistence.PersistentDataType.INTEGER, 5
	)
		it.lore(
		kotlin.collections.mutableListOf(
			net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize("<bold><gray>Ammo: 5/5")
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
				net.horizonsend.ion.server.IonServer.Ion, "ammo"
			), org.bukkit.persistence.PersistentDataType.INTEGER, 30
		)
		it.lore(
		kotlin.collections.mutableListOf(
			net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize("<bold><gray>Ammo: 30/30")
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
	); it.lore(
		kotlin.collections.mutableListOf(
			net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize("<bold><gray>Ammo: 15/15")
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
	); it.lore(mutableListOf(MiniMessage.miniMessage().deserialize("<bold><gray>Ammo: 2/2")))
	});
}