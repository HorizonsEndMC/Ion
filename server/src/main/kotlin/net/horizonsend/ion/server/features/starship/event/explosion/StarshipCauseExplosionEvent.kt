package net.horizonsend.ion.server.features.starship.event.explosion

import net.horizonsend.ion.server.features.explosion.Explosion
import net.horizonsend.ion.server.features.starship.controllers.Controller
import org.bukkit.block.Block
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Event called when a starship causes any kind of explosion
 * @see Explosion
 **/
open class StarshipCauseExplosionEvent(
	val controller: Controller,
	val explosion: Explosion,
	val blocks: MutableSet<Block>
) : Event(true), Cancellable {
	private var isCancelled: Boolean = false

	override fun isCancelled(): Boolean = isCancelled

	override fun setCancelled(cancel: Boolean) {
		isCancelled = cancel
	}

	override fun getHandlers(): HandlerList {
		return handlerList
	}

	companion object {
		@JvmStatic
		val handlerList = HandlerList()
	}
}
