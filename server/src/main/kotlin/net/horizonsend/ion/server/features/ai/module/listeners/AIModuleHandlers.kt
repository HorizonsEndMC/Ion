package net.horizonsend.ion.server.features.ai.module.listeners

import net.horizonsend.ion.server.features.starship.event.StarshipSunkEvent
import org.bukkit.event.entity.PlayerDeathEvent

sealed interface AIModuleHandleEvent {}

interface AIModuleHandleShipSink : AIModuleHandleEvent {
	fun onShipSink(event : StarshipSunkEvent)
}

interface AIModuleHandlePlayerDeath : AIModuleHandleEvent {
	fun onPLayerDeath(event : PlayerDeathEvent)
}
