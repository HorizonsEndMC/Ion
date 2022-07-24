package net.horizonsend.ion.server.listeners.bukkit

import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack

class PlayerJoinListener : Listener {
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
			event.player.inventory.addItem(ItemStack(Material.COOKED_BEEF, 32))
			event.player.inventory.addItem(ItemStack(Material.CLOCK))
			event.player.inventory.addItem(kitstarterasking)
		}
	}
}