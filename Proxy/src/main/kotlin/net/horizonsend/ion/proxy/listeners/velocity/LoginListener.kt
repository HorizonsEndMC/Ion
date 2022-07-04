package net.horizonsend.ion.proxy.listeners.velocity

import com.velocitypowered.api.event.EventTask
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.LoginEvent
import net.horizonsend.ion.proxy.IonProxy
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage

class LoginListener(private val plugin: IonProxy) {
	@Subscribe(order = PostOrder.LAST)
	@Suppress("Unused")
	fun onLoginEvent(event: LoginEvent): EventTask = EventTask.async {
		var headerComponent = Component.text().append(Component.text("\nHorizon's End\n", TextColor.color(255, 127, 63), TextDecoration.BOLD))

		val headerText = plugin.proxyConfiguration.tablistHeaderMessage

		if (headerText.isNotEmpty()) {
			headerComponent = headerComponent
				.append(Component.text("\n"))
				.append(MiniMessage.miniMessage().deserialize(headerText))
				.append(Component.text("\n"))
		}

		event.player.sendPlayerListHeader(headerComponent)
	}
}