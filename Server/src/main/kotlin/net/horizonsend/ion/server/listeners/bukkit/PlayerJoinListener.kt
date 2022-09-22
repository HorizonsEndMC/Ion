package net.horizonsend.ion.server.listeners.bukkit

import java.net.URL
import java.security.MessageDigest
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

@Suppress("Unused")
class PlayerJoinListener(private val plugin: IonServer) : Listener {
	private val url = "https://github.com/HorizonsEndMC/ResourcePack/releases/download/${
		URL("https://api.github.com/repos/HorizonsEndMC/ResourcePack/releases/latest")
			.readText()
			.substringAfter("\",\"tag_name\":\"")
			.substringBefore("\",")
	}/HorizonsEndResourcePack.zip"

	private val hash = try { MessageDigest.getInstance("SHA-1").digest(URL(url).readBytes()) } catch (_: Exception) { null }

	@EventHandler(priority = EventPriority.NORMAL)
	fun onPlayerJoinEvent(event: PlayerJoinEvent) {
		event.joinMessage(null)

		if (!event.player.hasPlayedBefore() && event.player.gameMode == GameMode.SURVIVAL) {
			val armor: Array<ItemStack> = arrayOf(
				ItemStack(Material.CHAINMAIL_BOOTS, 1),
				ItemStack(Material.CHAINMAIL_LEGGINGS, 1),
				ItemStack(Material.CHAINMAIL_CHESTPLATE, 1),
				ItemStack(Material.CHAINMAIL_HELMET, 1)
			)
			event.player.inventory.armorContents = armor

			val kitstarterasking = ItemStack(Material.PAPER)
			kitstarterasking.itemMeta.displayName(
				MiniMessage.miniMessage()
					.deserialize("Please do /kit starter if you run out of steak or need a new set!").color(
						TextColor.color(0, 255, 255)
					)
			)
			kitstarterasking.itemMeta.persistentDataContainer.set(
				NamespacedKey(plugin, "SpawnPaper"),
				PersistentDataType.BYTE,
				1
			)
			event.player.inventory.addItem(ItemStack(Material.COOKED_BEEF, 32))
			event.player.inventory.addItem(ItemStack(Material.CLOCK))
			event.player.inventory.addItem(kitstarterasking)
		}

		event.player.setResourcePack(url, hash)
	}
}