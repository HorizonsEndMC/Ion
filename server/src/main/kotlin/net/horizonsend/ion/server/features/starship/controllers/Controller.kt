package net.horizonsend.ion.server.features.starship.controllers

import net.horizonsend.ion.server.features.starship.Starship
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import net.kyori.adventure.text.Component
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.entity.Player

abstract class Controller(val starship: Starship, val name: String) : ForwardingAudience.Single {
	abstract val pilotName: Component

	override fun audience(): Audience = Audience.empty()

	/** Called on each server tick. */
	open fun tick() {}

	/** Called when the controller or its ship is removed. Any cleanup logic should be done here. */
	open fun destroy() {}

	/** Checks weather or not the controller can break a specific block **/
	abstract fun canDestroyBlock(block: Block): Boolean


	/** Checks weather or not the controller can place a specific block **/
	abstract fun canPlaceBlock(block: Block, newState: BlockState, placedAgainst: Block): Boolean

	val playerPilot: Player? get() = (this as? PlayerController)?.player
}
