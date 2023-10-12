package net.horizonsend.ion.server.features.starship.control.controllers

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.noOpDamager
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.block.Block
import org.bukkit.block.BlockState

class NoOpController(starship: ActiveStarship) : Controller(noOpDamager, starship, "idle") {
	// Can't move
	override val isShiftFlying: Boolean = false
	override val selectedDirectControlSpeed: Int = 0
	override val yaw: Float = 0f
	override val pitch: Float = 0f
	override val pilotName: Component = text("idle")

	// Shouldn't be treated like they're still piloting it
	override fun audience(): Audience = Audience.empty()
	override fun canDestroyBlock(block: Block): Boolean = false
	override fun canPlaceBlock(block: Block, newState: BlockState, placedAgainst: Block): Boolean = false
}
