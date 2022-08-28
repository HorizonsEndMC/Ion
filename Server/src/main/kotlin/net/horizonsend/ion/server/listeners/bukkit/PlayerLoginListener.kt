package net.horizonsend.ion.server.listeners.bukkit

import java.net.URL
import java.security.MessageDigest
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent

@Suppress("Unused")
class PlayerLoginListener : Listener {
	private val url = "https://github.com/HorizonsEndMC/ResourcePack/releases/download/${
		URL("https://api.github.com/repos/HorizonsEndMC/ResourcePack/releases/latest")
			.readText()
			.substringAfter("\",\"tag_name\":\"")
			.substringBefore("\",")
	}/HorizonsEndResourcePack.zip"

	private val hash = MessageDigest.getInstance("SHA-1")
		.digest(URL(url).readBytes())

	@EventHandler(priority = EventPriority.MONITOR)
	fun onPlayerLoginEvent(event: PlayerLoginEvent) {
		event.player.setResourcePack(url, hash)
	}
}