package net.starlegacy.feature.starship.event

import net.starlegacy.feature.starship.active.ActiveStarship
import org.bukkit.event.Event

abstract class StarshipEvent(
    open val starship: ActiveStarship
) : Event()
