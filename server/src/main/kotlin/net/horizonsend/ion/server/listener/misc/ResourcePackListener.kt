package net.horizonsend.ion.server.listener.misc

import com.google.common.io.BaseEncoding
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_BLUE
import net.horizonsend.ion.common.utils.text.formatLink
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.custom.CustomItemRegistry.PERSONAL_TRANSPORTER
import net.horizonsend.ion.server.features.player.NewPlayerProtection.hasProtection
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.text.Component.text
import org.bukkit.GameMode
import org.bukkit.Material.CHAINMAIL_BOOTS
import org.bukkit.Material.CHAINMAIL_CHESTPLATE
import org.bukkit.Material.CHAINMAIL_HELMET
import org.bukkit.Material.CHAINMAIL_LEGGINGS
import org.bukkit.Material.CLOCK
import org.bukkit.Material.COOKED_BEEF
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerResourcePackStatusEvent
import org.bukkit.inventory.ItemStack
import java.net.URL
import java.security.MessageDigest

class ResourcePackListener : SLEventListener() {
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
				log.warn("Exception was thrown while updating resource pack URL!", exception)
				null
			}

			cachedHash = try {
				BaseEncoding.base16().encode(MessageDigest.getInstance("SHA-1").digest(URL(cachedURL).readBytes()))
			} catch (exception: Exception) {
				log.warn("Exception was thrown while computing resource pack hash!", exception)
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

		event.player.sendMessage(ofChildren(
			text("You can load in instantly by downloading the texture pack yourself at ", HE_LIGHT_BLUE),
			formatLink("this link!", "https://github.com/HorizonsEndMC/ResourcePack")
		))
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
				addItem(PERSONAL_TRANSPORTER.constructItemStack())
			}
		}

		Tasks.async {
			val (url, hash) = getURLAndHash() ?: let {
				event.player.serverError("Unable to provide resource pack. This error may correct itself within 10 minutes, if not, contact an administrator.")
				return@async
			}

			Tasks.sync { event.player.setResourcePack(url, hash) }
		}
	}
}
