package net.horizonsend.ion.server.features.starship.subsystem

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.hyperspace.HyperspaceMovement
import net.horizonsend.ion.server.features.starship.movement.TranslationAccessor
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i

abstract class StarshipSubsystem(open val starship: ActiveStarship, var pos: Vec3i) {
	/**
	 * Check if the subsystem is damaged or not
	 * @return True if it's undamaged, false if it's damaged beyond usability
	 */
	abstract fun isIntact(): Boolean

	/**
	 * Called when the world that the starship is in ticks
	 **/
	open fun tick() {}

	/**
	 * Called when the starship is destroyed
	 **/
	open fun onDestroy() {}

	open fun handleRelease() {}

	open fun onMovement(movement: TranslationAccessor, success: Boolean) {}

	/**
	 * Executed AFTER jump is completed
	 **/
	open fun handleJump(jump: HyperspaceMovement) {}
}
