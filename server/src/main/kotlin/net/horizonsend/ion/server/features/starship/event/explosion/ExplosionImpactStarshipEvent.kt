package net.horizonsend.ion.server.features.starship.event.explosion

import net.horizonsend.ion.server.features.explosion.Explosion
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.controllers.Controller
import org.bukkit.event.HandlerList

class ExplosionImpactStarshipEvent(
    controller: Controller,
    explosion: Explosion,
    val impactedStarship: Starship
) : StarshipCauseExplosionEvent(controller, explosion) {
	private var isCancelled: Boolean = false

	override fun getHandlers(): HandlerList {
		return handlerList
	}

	companion object {
		@JvmStatic
		val handlerList = HandlerList()
	}
}
