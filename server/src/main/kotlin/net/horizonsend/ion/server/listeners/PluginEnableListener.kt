package net.horizonsend.ion.server.listeners

import net.starlegacy.feature.nations.NationsMap
import net.starlegacy.feature.space.SpaceMap
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginEnableEvent

/**
 * Dynmap loads POSTWORLD which is after Ion which loads STARTUP, this means when Ion goes to put map markers down, the
 * plugin is not yet ready, and it ignores it.
 */
class PluginEnableListener : Listener {
	@EventHandler
	fun onPluginEnableEvent(event: PluginEnableEvent) {
		if (event.plugin.name != "dynmap") return

		SpaceMap.onEnable()
		NationsMap.onEnable()
	}
}
