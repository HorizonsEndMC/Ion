package net.horizonsend.ion.server.features.starship.event.build

import net.horizonsend.ion.server.features.starship.controllers.Controller
import org.bukkit.block.Block
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class StarshipBreakBlockEvent(
	val controller: Controller,
	val block: Block
) : Event(), Cancellable {
	private var isCancelled: Boolean = false
	private var dropItems: Boolean = true

	init {
	    isCancelled = !controller.canDestroyBlock(block)
	}

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
