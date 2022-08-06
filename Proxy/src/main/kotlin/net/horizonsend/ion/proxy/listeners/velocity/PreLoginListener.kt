package net.horizonsend.ion.proxy.listeners.velocity

import com.velocitypowered.api.event.EventTask
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PreLoginEvent
import net.horizonsend.ion.common.utilities.feedback.FeedbackType
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage

class PreLoginListener {
	private val disconnectMessage =
		miniMessage().deserialize("<${FeedbackType.USER_ERROR.colour}>Only version 1.19.1/.2 can be used on Horizon's End.")

	@Suppress("Unused")
	@Subscribe(order = PostOrder.FIRST)
	fun onPreLoginEvent(event: PreLoginEvent): EventTask = EventTask.async {
		if (event.connection.protocolVersion.protocol == 760) return@async

		event.result = PreLoginEvent.PreLoginComponentResult.denied(disconnectMessage)
	}
}