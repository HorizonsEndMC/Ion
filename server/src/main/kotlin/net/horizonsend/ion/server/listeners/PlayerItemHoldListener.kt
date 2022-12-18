package net.horizonsend.ion.server.listeners

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.customitems.blasters.constructors.AmmoRequiringMultiShotBlaster
import net.horizonsend.ion.server.customitems.blasters.constructors.AmmoRequiringSingleShotBlaster
import net.horizonsend.ion.server.customitems.getCustomItem
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class PlayerItemHoldListener : Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	fun onPlayerItemHoldEvent(event: PlayerItemHeldEvent){
		val player = event.player

		// just so people dont abuse this somehow
		player.removePotionEffect(PotionEffectType.FAST_DIGGING)

		val item = event.player.inventory.getItem(event.newSlot)

		val customItem = item?.getCustomItem() ?: return

		// adding a potion effect because it takes ages for that attack cooldown to come up
		player.addPotionEffect(PotionEffect(PotionEffectType.FAST_DIGGING, 20, 5, false, false, false))
		if (customItem is AmmoRequiringSingleShotBlaster) {
			val magazineSize = customItem.singleShotWeaponBalancing.magazineSize
			val itemAmmoPdc = item.itemMeta.persistentDataContainer.get(
				NamespacedKey(IonServer.Ion, "ammo"),
				PersistentDataType.INTEGER
			)

			if (itemAmmoPdc != null) {
				player.sendActionBar(MiniMessage.miniMessage().deserialize("<red>Ammo: $itemAmmoPdc/$magazineSize"))
			}
		}

		if (customItem is AmmoRequiringMultiShotBlaster) {
			val magazineSize = customItem.multiShotWeaponBalancing.magazineSize
			val itemAmmoPdc = item.itemMeta.persistentDataContainer.get(
				NamespacedKey(IonServer.Ion, "ammo"),
				PersistentDataType.INTEGER
			)

			if (itemAmmoPdc != null) {
				player.sendActionBar(MiniMessage.miniMessage().deserialize("<red>Ammo: $itemAmmoPdc/$magazineSize"))
			}
		}
	}
}