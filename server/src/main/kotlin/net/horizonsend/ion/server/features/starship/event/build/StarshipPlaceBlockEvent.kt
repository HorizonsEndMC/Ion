package net.horizonsend.ion.server.features.starship.event.build

import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class StarshipPlaceBlockEvent(
    val controller: Controller,
    val block: Block,
    val newState: BlockState,
    val placedAgainst: Block
) : Event(), Cancellable {
	private var isCancelled: Boolean = false

	init {
		isCancelled = !controller.canPlaceBlock(block, newState, placedAgainst)
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
