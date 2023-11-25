package net.horizonsend.ion.server.listener.fixers

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.customitems.CustomItems.customItem
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomBlockItem
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomBlocks
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItem
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems
import net.horizonsend.ion.server.miscellaneous.utils.enumSetOf
import net.horizonsend.ion.server.miscellaneous.utils.isShulkerBox
import net.minecraft.world.entity.item.ItemEntity
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.event.block.BlockFadeEvent
import org.bukkit.event.block.BlockFormEvent
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPistonRetractEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PotionSplashEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.ItemStack

class CancelListeners : SLEventListener() {
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
			PlayerTeleportEvent.TeleportCause.SPECTATE -> !event.player.hasPermission("group.dutymode")
			else -> false
		}
	}

	@EventHandler
	@Suppress("Unused")
	fun onPotionSplashEvent(event: PotionSplashEvent) {
		event.isCancelled = true
	}

	@EventHandler
	@Suppress("Unused")
	fun onDispenseShulker(event: BlockDispenseEvent) {
		if (event.item.type.isShulkerBox || event.block.type.isShulkerBox) {
			event.isCancelled = true
		}
	}

	@EventHandler
	@Suppress("Unused")
	fun onAlchemy(event: PrepareItemCraftEvent) {
		if (event.isRepair) return

		val player = event.view.player

		// disable crafting for legacy MineralCustomItems or CustomBlockItem
		// (There is no way to get the CustomBlockItems, so hard code for iron ingot uncraft
		if (event.inventory.matrix.any {
			it != null &&
					CustomItems[it] != null &&
					// if the custom item is a mineral item, or the crafting recipe will result in 9 iron ingots or an anvil
					(CustomItems[it] is CustomItems.MineralCustomItem ||
							CustomItems[it] is CustomBlockItem) &&
					it.customItem == null
			}) {
			player.userError("Legacy mineral item detected; use the /convert command to transfer legacy items.")
			event.inventory.result = ItemStack(Material.AIR)
		}

		// disable crafting if any item is a new CustomItem and the result is not a new or legacy CustomItem
		else if (event.inventory.matrix.any {
			// any item is in the matrix and not a new custom item
			it != null &&
					it.customItem != null &&
					// the result exists and is neither a new nor legacy CustomItem
					event.inventory.result != null &&
					event.inventory.result!!.customItem == null &&
					CustomItems[event.inventory.result] == null
			}) {
			event.inventory.result = ItemStack(Material.AIR)
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	@Suppress("Unused", "Deprecation")
	fun onPlayerKickEvent(event: PlayerKickEvent) {
		// Really dumb solution for players being kicked due to "out of order chat messages"
		if (event.reason.lowercase().contains("out-of-order")) {
			event.player.userError(
				"The server attempted to kick you for out-of-order chat messages. You may need to retry any recent commands."
			)

			event.isCancelled = true
		}
	}

	@EventHandler
	fun onItemDamage(event: EntityDamageEvent) {
		if (event.entity !is ItemEntity) return

		event.isCancelled = true
	}

	@EventHandler
	fun onBlockPistonExtendEvent(event: BlockPistonExtendEvent) {
		if (event.blocks.any { it.type == Material.BROWN_MUSHROOM_BLOCK }) {
            event.isCancelled = true
		}
	}

	@EventHandler
	fun onBlockPistonRetractEvent(event: BlockPistonRetractEvent) {
		if (event.blocks.any { it.type == Material.BROWN_MUSHROOM_BLOCK }) {
            event.isCancelled = true
		}
	}

	//TODO cancel recipes requiring iron supplied with custom items
}
