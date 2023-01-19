package net.horizonsend.ion.server.listeners

import net.starlegacy.feature.economy.city.CityNPCs
import net.starlegacy.feature.economy.collectors.Collectors
import net.starlegacy.feature.nations.NationsMap
import net.starlegacy.feature.space.SpaceMap
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginEnableEvent

/**
 * Some plugins load POSTWORLD which is after Ion which loads STARTUP, this means some code which expects certain
 * plugins to be there won't work as the needed plugin is not yet ready, this will delay loading that code until later.
 */
class PluginEnableListener : Listener {
	@EventHandler
	fun onPluginEnableEvent(event: PluginEnableEvent) {
		when (event.plugin.name) {
			"dynmap" -> {
				SpaceMap.onEnable()
				NationsMap.onEnable()
			}
			"Citizens" -> {
				Collectors.onEnable()
				CityNPCs.onEnable()
			}
		}
	}
}
