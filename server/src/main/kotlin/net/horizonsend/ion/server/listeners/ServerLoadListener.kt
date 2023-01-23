package net.horizonsend.ion.server.listeners

import net.starlegacy.feature.economy.city.CityNPCs
import net.starlegacy.feature.economy.collectors.Collectors
import net.starlegacy.feature.hyperspace.HyperspaceBeacons
import net.starlegacy.feature.nations.NationsMap
import net.starlegacy.feature.space.SpaceMap
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.ServerLoadEvent

class ServerLoadListener : Listener {
	@EventHandler
	fun onServerLoadEvent(event: ServerLoadEvent) {
		SpaceMap.onEnable()
		NationsMap.onEnable()
		HyperspaceBeacons.reloadDynmap()
		Collectors.onEnable()
		CityNPCs.onEnable()
	}
}
