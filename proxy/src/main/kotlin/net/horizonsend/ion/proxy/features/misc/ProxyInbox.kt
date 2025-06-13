package net.horizonsend.ion.proxy.features.misc

import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.ServerConnectedEvent
import net.horizonsend.ion.common.utils.messages.Inboxes
import net.horizonsend.ion.proxy.PLUGIN
import net.horizonsend.ion.proxy.features.cache.Listener
import net.horizonsend.ion.proxy.utils.slPlayerId

object ProxyInbox : Inboxes(), Listener {
	override fun runAsync(task: () -> Unit) {
		PLUGIN.proxy.scheduler.async(task)
	}

	@Subscribe(order = PostOrder.LAST)
	fun onLogin(event: ServerConnectedEvent) {
		sendInboxBreakdown(event.player.slPlayerId, event.player)
	}
}
