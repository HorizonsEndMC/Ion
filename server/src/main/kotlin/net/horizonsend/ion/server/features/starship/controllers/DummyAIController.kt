package net.horizonsend.ion.server.features.starship.controllers

import net.horizonsend.ion.server.features.starship.Starship
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.block.Block
import org.bukkit.block.BlockState

class DummyAIController(starship: Starship) : Controller(starship, "AI") {
	override val pilotName: Component = text("AI Pilot Matrix", NamedTextColor.RED, TextDecoration.BOLD)

	override val yaw: Float get() = ((System.currentTimeMillis() / 100) % 360) - 180F
	override val pitch: Float = 0.0F

	override val selectedDirectControlSpeed: Int = 0

	override val isShiftFlying: Boolean = true

	override fun canDestroyBlock(block: Block): Boolean {
		return false
	}

	override fun canPlaceBlock(block: Block, newState: BlockState, placedAgainst: Block): Boolean {
		return false
	}
}
