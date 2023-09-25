package net.horizonsend.ion.server.features.starship.control.controllers.ai.util

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import org.bukkit.Location

interface CombatController {
	var target: ActiveStarship

	fun getTargetLocation(): Location
}
