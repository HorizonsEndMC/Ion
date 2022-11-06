package net.horizonsend.ion.server.listeners.bukkit

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.customitems.CustomItem
import net.horizonsend.ion.server.customitems.CustomItemList
import net.horizonsend.ion.server.customitems.blasters.AmmoRequiringSingleShotBlaster
import net.horizonsend.ion.server.customitems.blasters.Pistol
import net.horizonsend.ion.server.customitems.blasters.Rifle
import net.horizonsend.ion.server.customitems.blasters.Shotgun
import net.horizonsend.ion.server.customitems.blasters.Sniper
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataContainer

class PlayerInteractListener : Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	@Suppress("Unused")
	fun onPlayerInteractEvent(event: PlayerInteractEvent) {
		val item = event.item // If material is valid, then item is not null
		if (item != null) {
			val arrayOfWeapon: Map<Int, CustomItem> =
				mapOf(
					Sniper.customItemlist.itemStack.itemMeta.customModelData to Sniper,
					Rifle.customItemlist.itemStack.itemMeta.customModelData to Rifle,
					Pistol.customItemlist.itemStack.itemMeta.customModelData to Pistol,
					Shotgun.customItemlist.itemStack.itemMeta.customModelData to Shotgun
				)

			val itemMap = when (CustomItemList.values().find { it.itemStack.itemMeta.customModelData == item.itemMeta.customModelData && item.type == it.itemStack.type }){
				CustomItemList.SNIPER -> { arrayOfWeapon }
				CustomItemList.RIFLE ->  { arrayOfWeapon }
				CustomItemList.PISTOL -> { arrayOfWeapon }
				CustomItemList.SHOTGUN -> { arrayOfWeapon }
				else -> return
			}

			if (!item.itemMeta.hasCustomModelData()) return

			itemMap[item.itemMeta.customModelData]?.apply {
				when (event.action) {
					Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK -> onPrimaryInteract(event.player, item)
					Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK -> onSecondaryInteract(event.player, item)
					else -> return // Unknown Action Enum - We probably don't care, silently fail
				}
			}

			event.isCancelled = true
		}
	}
}