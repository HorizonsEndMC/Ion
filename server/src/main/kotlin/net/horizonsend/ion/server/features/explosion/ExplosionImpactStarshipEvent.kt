package net.starlegacy.feature.starship.access.explosion

import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.controllers.Controller
import org.bukkit.event.HandlerList

class ExplosionImpactStarshipEvent(
	originator: Controller,
	explosion: StarshipExplosion,
	val impactedStarship: Starship
) : StarshipCauseExplosionEvent(originator, explosion) {
	private var isCancelled: Boolean = false

	override fun getHandlers(): HandlerList {
		return handlerList
	}

	companion object {
		@JvmStatic
		val handlerList = HandlerList()
	}

	override fun isCancelled(): Boolean = isCancelled

	override fun setCancelled(cancel: Boolean) {
		isCancelled = cancel
	}
}
