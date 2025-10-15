package net.horizonsend.ion.server.features.starship.control.controllers

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.input.InputHandler
import net.horizonsend.ion.server.features.starship.control.input.NoInput
import net.horizonsend.ion.server.features.starship.control.movement.MovementHandler
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.damager.noOpDamager
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.block.Block
import org.bukkit.block.BlockState

open class NoOpController(starship: ActiveStarship, previousDamager: Damager?) : Controller(previousDamager ?: noOpDamager, starship, "Idle") {
	// Can't move
	override var movementHandler: MovementHandler = object : MovementHandler(this, "Idle") {
		override val input: InputHandler = NoInput(this@NoOpController)
	}

	// Shouldn't be treated like they're still piloting it
	override fun audience(): Audience = Audience.empty()
	override fun canDestroyBlock(block: Block): Boolean = false
	override fun canPlaceBlock(block: Block, newState: BlockState, placedAgainst: Block): Boolean = false

	override val pilotName: Component = text("idle")
}
