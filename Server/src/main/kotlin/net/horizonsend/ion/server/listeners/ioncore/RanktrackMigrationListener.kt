package net.horizonsend.ion.server.listeners.ioncore

import kotlin.math.pow
import kotlin.math.sqrt
import net.horizonsend.ion.common.database.collections.PlayerData
import net.horizonsend.ion.common.database.update
import net.horizonsend.ion.core.events.RanktrackMigrateEvent
import net.horizonsend.ion.server.utilities.addRanktrackXP
import net.horizonsend.ion.server.utilities.feedback.FeedbackType
import net.horizonsend.ion.server.utilities.feedback.sendFeedbackMessage
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class RanktrackMigrationListener : Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	fun onRankTrackMigration(event: RanktrackMigrateEvent){
		val playerData = PlayerData[event.player.uniqueId]

		if (playerData == null){
			event.player.sendFeedbackMessage(FeedbackType.SERVER_ERROR, "Somehow, you dont exist in our database!")
			return
		}

		var xpToGive = 0
		if ((0 <= event.xp) && (event.xp <= 20000)){
			xpToGive = event.xp
		}
		else if (event.xp>20000){
			xpToGive = sqrt(event.xp.toDouble()).pow((event.xp.div(8.0).pow(-1))).toInt() - 333553
		}

		event.player.sendFeedbackMessage(FeedbackType.SUCCESS, "You have been migrated your old xp to ranktrack xp")
		event.player.addRanktrackXP(xpToGive)
	}
}