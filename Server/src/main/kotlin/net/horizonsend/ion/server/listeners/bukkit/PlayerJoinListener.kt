package net.horizonsend.ion.server.listeners.bukkit

import net.horizonsend.ion.server.IonServer
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class PlayerJoinListener(private val plugin: IonServer) : Listener {
	@EventHandler(priority = EventPriority.NORMAL)
	@Suppress("Unused")
	fun onPlayerJoinEvent(event: PlayerJoinEvent) {
		event.joinMessage(null)

		if(!event.player.hasPlayedBefore() && event.player.gameMode == GameMode.SURVIVAL){
			val armor: Array<ItemStack> = arrayOf(
			ItemStack(Material.CHAINMAIL_BOOTS, 1),
			ItemStack(Material.CHAINMAIL_LEGGINGS, 1),
			ItemStack(Material.CHAINMAIL_CHESTPLATE, 1),
			ItemStack(Material.CHAINMAIL_HELMET, 1)
			)
			event.player.inventory.armorContents = armor

			val kitstarterasking = ItemStack(Material.PAPER)
			kitstarterasking.itemMeta.displayName(
				MiniMessage.miniMessage().deserialize("Please do /kit starter if you run out of steak or need a new set!").color(
					TextColor.color(0, 255, 255)))
			kitstarterasking.itemMeta.persistentDataContainer.set(NamespacedKey(plugin, "SpawnPaper"), PersistentDataType.BYTE, 1)
			event.player.inventory.addItem(ItemStack(Material.COOKED_BEEF, 32))
			event.player.inventory.addItem(ItemStack(Material.CLOCK))
			event.player.inventory.addItem(kitstarterasking)
		}
	}
}