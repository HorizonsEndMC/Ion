package net.horizonsend.ion.server.miscellaneous.listeners

import net.horizonsend.ion.server.miscellaneous.enumSetOf
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockFadeEvent
import org.bukkit.event.block.BlockFormEvent
import org.bukkit.event.entity.PotionSplashEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerTeleportEvent

class CancelListeners : Listener {
	private val preventFormBlocks = enumSetOf(
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
	fun onBlockFadeEvent(event: BlockFadeEvent) {
		if (event.block.type != Material.ICE) return

		event.isCancelled = true
	}

	@EventHandler
	@Suppress("Unused")
	fun onBlockFormEvent(event: BlockFormEvent) {
		if (preventFormBlocks.contains(event.block.type) || event.newState.type == Material.ICE) {
			event.isCancelled = true
		}
	}

	@EventHandler
	@Suppress("Unused")
	fun onPlayerFishEvent(event: PlayerFishEvent) {
		event.isCancelled = true
	}

	@EventHandler
	@Suppress("Unused")
	fun onPlayerItemConsumeEvent(event: PlayerItemConsumeEvent) {
		if (event.item.type != Material.POTION) return

		event.isCancelled = true
		event.setItem(null)
	}

	@EventHandler
	@Suppress("Unused")
	fun onPlayerTeleportEvent(event: PlayerTeleportEvent) {
		event.isCancelled = when (event.cause) {
			PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT, PlayerTeleportEvent.TeleportCause.ENDER_PEARL -> true
			else -> false
		}
	}

	@EventHandler
	@Suppress("Unused")
	fun onPotionSplashEvent(event: PotionSplashEvent) {
		event.isCancelled = true
	}
}
