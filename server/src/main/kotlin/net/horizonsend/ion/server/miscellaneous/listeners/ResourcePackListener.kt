package net.horizonsend.ion.server.miscellaneous.listeners

import com.google.common.io.BaseEncoding
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.legacy.NewPlayerProtection.hasProtection
import org.bukkit.GameMode
import org.bukkit.Material.CHAINMAIL_BOOTS
import org.bukkit.Material.CHAINMAIL_CHESTPLATE
import org.bukkit.Material.CHAINMAIL_HELMET
import org.bukkit.Material.CHAINMAIL_LEGGINGS
import org.bukkit.Material.CLOCK
import org.bukkit.Material.COOKED_BEEF
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerResourcePackStatusEvent
import org.bukkit.inventory.ItemStack
import java.net.URL
import java.security.MessageDigest

class ResourcePackListener : Listener {
	private var cachedURL: String? = null
	private var cachedHash: String? = null
	private var lastUpdated: Long = 0

	private fun getURLAndHash(): Pair<String, String>? {
		if (System.currentTimeMillis() - lastUpdated > 600000) {
			cachedURL = try {
				val tagName = URL("https://api.github.com/repos/HorizonsEndMC/ResourcePack/releases/latest")
					.readText()
					.substringAfter("\",\"tag_name\":\"")
					.substringBefore("\",")

				"https://github.com/HorizonsEndMC/ResourcePack/releases/download/$tagName/HorizonsEndResourcePack.zip"
			} catch (exception: Exception) {
				IonServer.slF4JLogger.warn("Exception was thrown while updating resource pack URL!", exception)
				null
			}

			cachedHash = try {
				BaseEncoding.base16().encode(MessageDigest.getInstance("SHA-1").digest(URL(cachedURL).readBytes()))
			} catch (exception: Exception) {
				IonServer.slF4JLogger.warn("Exception was thrown while computing resource pack hash!", exception)
				null
			}

			lastUpdated = System.currentTimeMillis()
		}

		return Pair(cachedURL ?: return null, cachedHash ?: return null)
	}

	@EventHandler
	@Suppress("Unused")
	fun onPlayerResourcePackStatusEvent(event: PlayerResourcePackStatusEvent) {
		if (event.status != PlayerResourcePackStatusEvent.Status.ACCEPTED) return

		event.player.userError(
			"Please consider downloading the resource pack for better login times! <click:open_url:'https://github.com/HorizonsEndMC/ResourcePack'>https://github.com/HorizonsEndMC/ResourcePack</click>"
		)
	}

	@EventHandler
	@Suppress("Unused")
	fun onPlayerJoinEvent(event: PlayerJoinEvent) {
		event.joinMessage(null)

		if (event.player.hasProtection()) {
			event.player.sendRichMessage(
				"The Wiki is a great resource for new players, be sure to use it! <white><u><click:open_url:'https://wiki.horizonsend.net'>wiki.horizonsend.net</click></u></white>"
			)
		}

		if (!event.player.hasPlayedBefore() && event.player.gameMode == GameMode.SURVIVAL) {
			event.player.inventory.apply {
				addItem(ItemStack(COOKED_BEEF, 32))
				addItem(ItemStack(CLOCK))
				addItem(ItemStack(CHAINMAIL_BOOTS))
				addItem(ItemStack(CHAINMAIL_LEGGINGS))
				addItem(ItemStack(CHAINMAIL_CHESTPLATE))
				addItem(ItemStack(CHAINMAIL_HELMET))
			}
		}

		val (url, hash) = getURLAndHash() ?: let {
			event.player.serverError("Unable to provide resource pack. This error may correct itself within 10 minutes, if not, contact an administrator.")
			return
		}

		event.player.setResourcePack(url, hash)
	}
}
