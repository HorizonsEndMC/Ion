package net.horizonsend.ion.server.features.starship.control.controllers

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.movement.MovementHandler
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.starship.movement.StarshipMovementException
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.block.Block
import org.bukkit.block.BlockState

abstract class Controller(
	val damager: Damager,
	val starship: ActiveStarship,
	val name: String
) : ForwardingAudience.Single {

	override fun audience(): Audience = Audience.empty()

	abstract var movementHandler: MovementHandler

	/** The color used for this controller. Currently, applies weapon color **/
	open fun getColor(): Color {
		return Color.fromRGB(Integer.parseInt("ff0000", 16))
	}

	/** Called on each server tick. */
	open fun tick() {}

	/** Called when the controller or its ship is removed. Any cleanup logic should be done here. */
	open fun destroy() {}

	/** Called when the ship moves. */
	open fun onMove(movement: StarshipMovement) {}

	/** Called when the ship's movement is blocked */
	open fun onBlocked(movement: StarshipMovement, reason: StarshipMovementException, location: Vec3i?) {}

	/** Called when a damager is added, or incremented */
	open fun onDamaged(damager: Damager) {}

	/** Checks weather or not the controller can break a specific block **/
	abstract fun canDestroyBlock(block: Block): Boolean

	/** Checks weather or not the controller can place a specific block **/
	abstract fun canPlaceBlock(block: Block, newState: BlockState, placedAgainst: Block): Boolean

	/** Return the display name of the controller */
	abstract val pilotName: Component
}
