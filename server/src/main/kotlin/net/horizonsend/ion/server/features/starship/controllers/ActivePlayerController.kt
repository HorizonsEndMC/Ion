package net.horizonsend.ion.server.features.starship.controllers

import net.horizonsend.ion.server.features.starship.Starship
import org.bukkit.entity.Player

class ActivePlayerController(player: Player, starship: Starship) : PlayerController(player, starship, "Player") {
	override var isShiftFlying: Boolean = false
		get() = player.isSneaking

	override var selectedDirectControlSpeed: Int = 0
		get() = player.inventory.heldItemSlot

	init {
		activePlayerControllers += this
	}

	override fun destroy() {
		activePlayerControllers.remove(this)
	}

	companion object {
		val activePlayerControllers = mutableListOf<ActivePlayerController>()

		operator fun get(player: Player): ActivePlayerController? = activePlayerControllers.firstOrNull { it.player == player }
	}
}
