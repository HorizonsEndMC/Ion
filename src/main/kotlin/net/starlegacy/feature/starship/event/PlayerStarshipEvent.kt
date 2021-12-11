package net.starlegacy.feature.starship.event

import net.starlegacy.feature.starship.active.ActivePlayerStarship

abstract class PlayerStarshipEvent(
    override val starship: ActivePlayerStarship
) : StarshipEvent(starship)
