package net.horizonsend.ion.server.listeners

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServer.Companion.Ion
import net.horizonsend.ion.server.extensions.sendServerError
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.net.URL
import java.security.MessageDigest

class PlayerJoinListener(private val plugin: IonServer) : Listener {
	private var cachedURL: String? = null
	private var lastUpdated: Long = 0

	private val url: String?
		get() {
			if (System.currentTimeMillis() - lastUpdated < 600000) return cachedURL

			cachedURL = try {
				"https://github.com/HorizonsEndMC/ResourcePack/releases/download/${
				URL("https://api.github.com/repos/HorizonsEndMC/ResourcePack/releases/latest")
					.readText()
					.substringAfter("\",\"tag_name\":\"")
					.substringBefore("\",")
				}/HorizonsEndResourcePack.zip"
			} catch (exception: Exception) {
				Ion.slF4JLogger.warn("Unable to update resource pack URL!", exception)
				null
			}

			lastUpdated = System.currentTimeMillis()

			return cachedURL
		}

	private val hash = try {
		MessageDigest.getInstance("SHA-1").digest(URL(url).readBytes())
	} catch (_: Exception) {
		null
	}

	@EventHandler
	@Suppress("Unused")
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

		if (url == null) {
			event.player.sendServerError("Unable to get resource pack URL. Please try again later.")
		} else {
			event.player.setResourcePack(url!!, hash)
		}
	}
}
