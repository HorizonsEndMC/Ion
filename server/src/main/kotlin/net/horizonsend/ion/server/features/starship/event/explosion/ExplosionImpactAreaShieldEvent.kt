package net.horizonsend.ion.server.features.starship.event.explosion

import net.horizonsend.ion.server.features.explosion.StarshipExplosion
import net.horizonsend.ion.server.features.starship.controllers.Controller
import org.bukkit.block.Sign
import org.bukkit.event.HandlerList

class ExplosionImpactAreaShieldEvent(
	controller: Controller,
	explosion: StarshipExplosion,
	val areaShield: Sign
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
