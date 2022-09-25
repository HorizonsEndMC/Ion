package net.horizonsend.ion.server.listeners.ioncore

import net.horizonsend.ion.common.database.collections.PlayerData
import net.horizonsend.ion.common.database.enums.Achievement
import net.horizonsend.ion.common.database.enums.Ranktrack
import net.horizonsend.ion.core.events.CompleteCargoRunEvent
import net.horizonsend.ion.server.utilities.addRanktrackXP
import net.horizonsend.ion.server.utilities.feedback.FeedbackType
import net.horizonsend.ion.server.utilities.feedback.sendFeedbackActionMessage
import net.horizonsend.ion.server.utilities.rewardAchievement
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

@Suppress("Unused")
class CompleteCargoRunListener : Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	fun onCompleteCargoRunEvent(event: CompleteCargoRunEvent) {
		event.player.rewardAchievement(Achievement.COMPLETE_CARGO_RUN)
		val playerData = PlayerData[event.player.uniqueId]
		if (playerData.ranktracktype != Ranktrack.INDUSTRIALIST) return
		event.player.addRanktrackXP(event.xp)
	}
}