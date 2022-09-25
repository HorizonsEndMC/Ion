package net.horizonsend.ion.server.listeners.ioncore

import net.horizonsend.ion.core.events.StationAllyCapture
import net.horizonsend.ion.server.utilities.addRanktrackXP
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class StationAllyCaptureListener : Listener{
	@EventHandler(priority = EventPriority.LOWEST)
	fun onStationAllyCaptureEvent(event: StationAllyCapture){
		event.player.addRanktrackXP(event.xp)
	}
}