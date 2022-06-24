package net.horizonsend.ion.core.listeners

import net.horizonsend.ion.core.commands.GracePeriod
import net.horizonsend.ion.core.feedback.FeedbackType.INFORMATION
import net.horizonsend.ion.core.feedback.sendFeedbackActionMessage
import net.starlegacy.listener.SLEventListener
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent

object PlayerJoinListener : SLEventListener() {
	@Suppress("Unused")
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun onPlayerJoin(event: PlayerJoinEvent){
		if (GracePeriod.isGracePeriod) event.player.sendFeedbackActionMessage(INFORMATION, "We strongly advise you to read this guide, and please ask questions in chat ( https://wiki.horizonsend.net/wiki/New_Player_Guide )")
	}
}