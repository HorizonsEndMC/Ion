package net.horizonsend.ion.server.features.starship.control.controllers

import net.horizonsend.ion.server.features.starship.Damager
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.block.Block
import org.bukkit.block.BlockState

abstract class Controller(override val starship: ActiveStarship, val name: String) : ForwardingAudience.Single, Damager {
	abstract val pilotName: Component

	abstract val isShiftFlying: Boolean

	/** Current vertical direction **/
	abstract val pitch: Float
	/** Current horizontal direction **/
	abstract val yaw: Float

	abstract val selectedDirectControlSpeed: Int
	override fun audience(): Audience = Audience.empty()

	/** The color used for this controller. Currently, applies weapon color **/
	override val color = Color.fromRGB(Integer.parseInt("ffffff", 16))

	/** Called on each server tick. */
	open fun tick() {}

	/** Called when the controller or its ship is removed. Any cleanup logic should be done here. */
	open fun destroy() {}

	/** Checks weather or not the controller can break a specific block **/
	abstract fun canDestroyBlock(block: Block): Boolean

	/** Checks weather or not the controller can place a specific block **/
	abstract fun canPlaceBlock(block: Block, newState: BlockState, placedAgainst: Block): Boolean
}
