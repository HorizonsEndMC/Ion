package net.horizonsend.ion.server.features.starship.event

import net.horizonsend.ion.server.features.misc.NewPlayerProtection.updateProtection
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship

abstract class ControlledStarshipEvent(
	final override val starship: ActiveControlledStarship
) : StarshipEvent(starship) {
	init {
		starship.playerPilot?.updateProtection()
	}
}
