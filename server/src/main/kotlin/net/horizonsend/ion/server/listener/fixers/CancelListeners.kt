package net.horizonsend.ion.server.listener.fixers

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent
import io.papermc.paper.event.player.PlayerOpenSignEvent
import net.horizonsend.ion.common.extensions.successActionMessage
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks.customBlock
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.customItem
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.enumSetOf
import net.horizonsend.ion.server.miscellaneous.utils.isBed
import net.horizonsend.ion.server.miscellaneous.utils.isShulkerBox
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.trialspawner.PlayerDetector
import net.minecraft.world.level.block.entity.vault.VaultConfig
import net.minecraft.world.level.block.entity.vault.VaultServerData
import net.minecraft.world.level.entity.EntityTypeTest
import net.minecraft.world.phys.AABB
import org.bukkit.Material
import org.bukkit.craftbukkit.block.CraftVault
import org.bukkit.entity.EnderPearl
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockFadeEvent
import org.bukkit.event.block.BlockFormEvent
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPistonRetractEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.PotionSplashEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.ItemStack
import java.lang.reflect.Field
import java.util.Optional
import java.util.function.Predicate

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
		/*
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
		 */

		// disable crafting if any item is a new CustomItem and the result is not a new or legacy CustomItem
		if (event.inventory.matrix.any {
					// any item is in the matrix and not a new custom item
					it != null &&
							it.customItem != null &&
							// the result exists and is neither a new nor legacy CustomItem
							event.inventory.result != null &&
							event.inventory.result!!.customItem == null
				}) {
			event.inventory.result = ItemStack(Material.AIR)
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	@Suppress("Unused")
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

	@EventHandler
	fun onPlayerEditSign(event: PlayerOpenSignEvent) {
		if (event.cause == PlayerOpenSignEvent.Cause.INTERACT) {
			event.isCancelled = true
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onExplode(event: EntityExplodeEvent) {
		event.blockList().removeAll { it.customBlock == CustomBlocks.BATTLECRUISER_REACTOR_CORE || it.customBlock == CustomBlocks.CRUISER_REACTOR_CORE }
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun onExplode(event: BlockExplodeEvent) {
		event.blockList().removeAll { it.customBlock == CustomBlocks.BATTLECRUISER_REACTOR_CORE || it.customBlock == CustomBlocks.CRUISER_REACTOR_CORE}
	}

	@EventHandler
	fun onThrowEnderPearl(event: PlayerLaunchProjectileEvent) {
		if (event.projectile is EnderPearl) {
			event.isCancelled = true
		}
	}

	// Disable beds
	@EventHandler
	fun onPlayerInteractEventH(event: PlayerInteractEvent) {
		if (event.action != Action.RIGHT_CLICK_BLOCK) return
		val item = event.clickedBlock!!
		val player = event.player

		if (item.type.isBed) {
			event.isCancelled = true
			player.successActionMessage(
				"Beds are disabled on this server! Use a cryopod instead"
			)
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun onTrialVaultPlacement(event: BlockPlaceEvent) {
		Tasks.sync {
			val state = event.block.state
			if (state !is CraftVault) return@sync

			val entity = state.tileEntity

			entity.config = disabledVaultConfig
			vaultServerDataResumeField.set(entity.serverData, Long.MAX_VALUE)
		}
	}

	private val vaultServerDataResumeField: Field = VaultServerData::class.java.getDeclaredField("stateUpdatingResumesAt").apply { isAccessible = true }

	private val emptyPlayerDetector = PlayerDetector { _, _, _, _, _ -> listOf() }
	private val emptyEntitySelector = object : PlayerDetector.EntitySelector {
		override fun getPlayers(p0: ServerLevel, p1: Predicate<in Player>): List<Player> = listOf()
		override fun <T : Entity> getEntities(p0: ServerLevel, p1: EntityTypeTest<Entity, T>, p2: AABB, p3: Predicate<in T>): List<T> = listOf()
	}

	private val disabledVaultConfig = VaultConfig(
		net.minecraft.world.entity.EntityType.BAT.defaultLootTable.get(),
		0.0,
		0.0001,
		net.minecraft.world.item.ItemStack(Blocks.BEDROCK),
		Optional.empty(),
		emptyPlayerDetector,
		emptyEntitySelector,
	)
}
