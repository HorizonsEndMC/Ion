package net.starlegacy.feature.starship.subsystem

import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.util.Vec3i

abstract class StarshipSubsystem(open val starship: ActiveStarship, var pos: Vec3i) {
	/**
	 * Check if the subsystem is damaged or not
	 * @return True if it's undamaged, false if it's damaged beyond usability
	 */
	abstract fun isIntact(): Boolean
}
