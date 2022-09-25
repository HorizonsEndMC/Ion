package net.horizonsend.ion.server.listeners.ioncore

import net.horizonsend.ion.common.database.collections.PlayerData
import net.horizonsend.ion.common.database.enums.Achievement
import net.horizonsend.ion.common.database.enums.Ranktrack
import net.horizonsend.ion.core.events.ShipKillEvent
import net.horizonsend.ion.server.utilities.addRanktrackXP
import net.horizonsend.ion.server.utilities.feedback.FeedbackType
import net.horizonsend.ion.server.utilities.feedback.sendFeedbackAction
import net.horizonsend.ion.server.utilities.feedback.sendFeedbackActionMessage
import net.horizonsend.ion.server.utilities.feedback.sendFeedbackMessage
import net.horizonsend.ion.server.utilities.rewardAchievement
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

@Suppress("Unused")
class ShipKillListener : Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	fun onShipKillEvent(event: ShipKillEvent) {
		if(event.killer != event.player){
			event.player.rewardAchievement(Achievement.KILL_SHIP)
			val killerdata = PlayerData[event.killer.uniqueId]
			val playerData = PlayerData[event.player.uniqueId]
			var xp: Int = event.xp
			if (killerdata.ranktracktype == Ranktrack.INDUSTRIALIST || (killerdata.ranktracktype == Ranktrack.PRIVATEER && playerData.ranktracktype != Ranktrack.OUTLAW)) return
			if (killerdata.ranktracktype == Ranktrack.PRIVATEER) xp*2.25
			event.killer.sendFeedbackActionMessage(FeedbackType.ALERT, "Killed ${event.killer.name}'s ship and recieved $xp")
			event.killer.addRanktrackXP(xp)
		}
	}
}