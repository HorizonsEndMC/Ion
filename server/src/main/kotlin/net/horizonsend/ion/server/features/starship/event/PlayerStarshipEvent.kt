package net.horizonsend.ion.server.features.starship.event

import net.horizonsend.ion.server.features.misc.NewPlayerProtection.updateProtection
import net.horizonsend.ion.server.features.starship.active.ActivePlayerStarship

abstract class PlayerStarshipEvent(
	override val starship: ActivePlayerStarship
) : StarshipEvent(starship) {
	init {
		starship.pilot?.player?.updateProtection()
	}
}
