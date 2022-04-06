package net.horizonsend.ion.listeners

import org.bukkit.Material.BLACK_CONCRETE_POWDER
import org.bukkit.Material.BLUE_CONCRETE_POWDER
import org.bukkit.Material.BROWN_CONCRETE_POWDER
import org.bukkit.Material.CYAN_CONCRETE_POWDER
import org.bukkit.Material.GRAY_CONCRETE_POWDER
import org.bukkit.Material.GREEN_CONCRETE_POWDER
import org.bukkit.Material.LIGHT_BLUE_CONCRETE_POWDER
import org.bukkit.Material.LIGHT_GRAY_CONCRETE_POWDER
import org.bukkit.Material.LIME_CONCRETE_POWDER
import org.bukkit.Material.MAGENTA_CONCRETE_POWDER
import org.bukkit.Material.ORANGE_CONCRETE_POWDER
import org.bukkit.Material.PINK_CONCRETE_POWDER
import org.bukkit.Material.PURPLE_CONCRETE_POWDER
import org.bukkit.Material.RED_CONCRETE_POWDER
import org.bukkit.Material.WHITE_CONCRETE_POWDER
import org.bukkit.Material.YELLOW_CONCRETE_POWDER
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPhysicsEvent

class ConcreteHardenListener: Listener {
	@EventHandler
	fun onBlockPhysicsEvent(event: BlockPhysicsEvent) {
		if (
			event.changedType == WHITE_CONCRETE_POWDER      ||
			event.changedType == ORANGE_CONCRETE_POWDER     ||
			event.changedType == MAGENTA_CONCRETE_POWDER    ||
			event.changedType == LIGHT_BLUE_CONCRETE_POWDER ||
			event.changedType == YELLOW_CONCRETE_POWDER     ||
			event.changedType == LIME_CONCRETE_POWDER       ||
			event.changedType == PINK_CONCRETE_POWDER       ||
			event.changedType == GRAY_CONCRETE_POWDER       ||
			event.changedType == LIGHT_GRAY_CONCRETE_POWDER ||
			event.changedType == CYAN_CONCRETE_POWDER       ||
			event.changedType == PURPLE_CONCRETE_POWDER     ||
			event.changedType == BLUE_CONCRETE_POWDER       ||
			event.changedType == BROWN_CONCRETE_POWDER      ||
			event.changedType == GREEN_CONCRETE_POWDER      ||
			event.changedType == RED_CONCRETE_POWDER        ||
			event.changedType == BLACK_CONCRETE_POWDER
		) {
			event.isCancelled = true
		}
	}
}