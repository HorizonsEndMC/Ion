package net.horizonsend.ion.server.features.starship.event

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import org.bukkit.event.Event

abstract class StarshipEvent(
	open val starship: ActiveStarship
) : Event()
