package net.horizonsend.ion.server.features.starship.subsystem

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.hyperspace.HyperspaceMovement
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i

abstract class StarshipSubsystem(open val starship: ActiveStarship, var pos: Vec3i) {
	/**
	 * Check if the subsystem is damaged or not
	 * @return True if it's undamaged, false if it's damaged beyond usability
	 */
	abstract fun isIntact(): Boolean

	open fun tick() {}

	open fun handleRelease() {}

	open fun onMovement(movement: StarshipMovement, success: Boolean) {}

	/**
	 * Executed AFTER jump is completed
	 **/
	open fun handleJump(jump: HyperspaceMovement) {}
}
