package net.horizonsend.ion.server.features.starship.event.explosion

import net.horizonsend.ion.server.features.explosion.Explosion
import net.horizonsend.ion.server.features.starship.controllers.Controller
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.event.HandlerList

class ExplosionImpactAreaShieldEvent(
    controller: Controller,
    explosion: Explosion,
	blocks: MutableSet<Block>,
    val areaShield: Sign
) : StarshipCauseExplosionEvent(controller, explosion, blocks) {
	private var isCancelled: Boolean = false

	override fun getHandlers(): HandlerList {
		return handlerList
	}

	companion object {
		@JvmStatic
		val handlerList = HandlerList()
	}
}
