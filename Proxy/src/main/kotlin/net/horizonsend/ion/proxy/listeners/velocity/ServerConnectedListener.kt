package net.horizonsend.ion.proxy.listeners.velocity

import com.velocitypowered.api.event.EventTask
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.ServerConnectedEvent
import java.net.URL
import java.security.MessageDigest
import net.horizonsend.ion.proxy.IonProxy
import net.horizonsend.ion.proxy.annotations.VelocityListener
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage

@VelocityListener
@Suppress("Unused")
class ServerConnectedListener(private val plugin: IonProxy) {
	private val resourcePackOffer = kotlin.run {
		val tag = URL("https://api.github.com/repos/HorizonsEndMC/ResourcePack/releases/latest")
			.readText()
			.substringAfter("\",\"tag_name\":\"")
			.substringBefore("\",")

		val url = "https://github.com/HorizonsEndMC/ResourcePack/releases/download/$tag/HorizonsEndResourcePack.zip"

		val fileHash = MessageDigest
			.getInstance("SHA-1")
			.digest(URL(url).readBytes())

		plugin.velocity.createResourcePackBuilder(url)
			.setHash(fileHash)
			.setPrompt(miniMessage().deserialize("""
				Horizon's End uses a Resource Pack to achieve many mod-like features. While you can play on the
				server without the Resource Pack, it is not recommended to do so as it makes it harder to play on the
				server.
			""".trimIndent()))
			.build()
	}

	@Subscribe(order = PostOrder.LAST)
	fun onServerConnectedEvent(event: ServerConnectedEvent): EventTask = EventTask.async {
		event.player.sendResourcePackOffer(resourcePackOffer)
	}
}