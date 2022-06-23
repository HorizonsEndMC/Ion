package net.horizonsend.ion.proxy.listeners

import com.velocitypowered.api.event.EventTask
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.PlayerResourcePackStatusEvent
import net.horizonsend.ion.common.utilities.feedback.FeedbackType
import net.kyori.adventure.text.minimessage.MiniMessage

class PlayerResourcePackStatusListener {
	@Subscribe(order = PostOrder.LAST)
	@Suppress("Unused")
	fun onPlayerResourcePackStatusListener(event: PlayerResourcePackStatusEvent): EventTask = EventTask.async {
		if (event.status == PlayerResourcePackStatusEvent.Status.ACCEPTED) event.player.sendActionBar(MiniMessage.miniMessage().deserialize("<${FeedbackType.USER_ERROR.colour}>Please consider downloading the resource pack for better login times! ( https://github.com/HorizonsEndMC/ResourcePack )"))
	}
}