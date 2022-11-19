package net.horizonsend.ion.server.listeners

import net.horizonsend.ion.server.legacy.utilities.enumSetOf
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockFormEvent

class BlockFormListener : Listener {
	private val cancelTypes = enumSetOf(
		Material.WHITE_CONCRETE_POWDER,
		Material.ORANGE_CONCRETE_POWDER,
		Material.MAGENTA_CONCRETE_POWDER,
		Material.LIGHT_BLUE_CONCRETE_POWDER,
		Material.YELLOW_CONCRETE_POWDER,
		Material.LIME_CONCRETE_POWDER,
		Material.PINK_CONCRETE_POWDER,
		Material.GRAY_CONCRETE_POWDER,
		Material.LIGHT_GRAY_CONCRETE_POWDER,
		Material.CYAN_CONCRETE_POWDER,
		Material.PURPLE_CONCRETE_POWDER,
		Material.BLUE_CONCRETE_POWDER,
		Material.BROWN_CONCRETE_POWDER,
		Material.GREEN_CONCRETE_POWDER,
		Material.RED_CONCRETE_POWDER,
		Material.BLACK_CONCRETE_POWDER
	)

	@EventHandler
	@Suppress("Unused")
	fun onBlockFormEvent(event: BlockFormEvent) {
		if (!cancelTypes.contains(event.block.type)) return

		event.isCancelled = true
	}
}