package net.starlegacy.feature.starship.event

import net.horizonsend.ion.core.NewPlayerProtection.updateProtection
import net.starlegacy.feature.starship.active.ActivePlayerStarship

abstract class PlayerStarshipEvent(
	override val starship: ActivePlayerStarship
) : StarshipEvent(starship) {
	init {
		starship.pilot?.player?.updateProtection()
	}
}
