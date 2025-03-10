package net.horizonsend.ion.server.features.ai.module.listeners

import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.event.StarshipSunkEvent
import net.horizonsend.ion.server.listener.SLEventListener
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PlayerDeathEvent

object AIModuleListener : SLEventListener() {

	@EventHandler
	fun onShipSink(event : StarshipSunkEvent) {
		for (controller in getAllAIControllers()) {
			controller.getAllModules().filterIsInstance<AIModuleHandleShipSink>().forEach { it.onShipSink(event) }
		}
	}

	@EventHandler
	fun onPlayerDeath(event: PlayerDeathEvent) {
		for (controller in getAllAIControllers()) {
			controller.getAllModules().filterIsInstance<AIModuleHandlePlayerDeath>().forEach { it.onPLayerDeath(event) }
		}
	}


	fun getAllAIControllers() : List<AIController> {
		return ActiveStarships.all().map { it.controller }.filterIsInstance<AIController>()
	}
}
