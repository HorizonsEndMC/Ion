package net.starlegacy.listener.misc

import net.horizonsend.ion.server.legacy.feedback.FeedbackType
import net.horizonsend.ion.server.legacy.feedback.sendFeedbackActionMessage
import net.horizonsend.ion.server.legacy.feedback.sendFeedbackMessage
import net.kyori.adventure.text.minimessage.MiniMessage
import net.starlegacy.feature.machine.PowerMachines
import net.starlegacy.feature.misc.CustomBlockItem
import net.starlegacy.feature.misc.CustomBlocks
import net.starlegacy.feature.misc.CustomItems
import net.starlegacy.feature.misc.getPower
import net.starlegacy.feature.misc.setPower
import net.starlegacy.feature.multiblock.Multiblocks
import net.starlegacy.feature.multiblock.PowerStoringMultiblock
import net.starlegacy.feature.multiblock.dockingtube.DockingTubeMultiblock
import net.starlegacy.feature.multiblock.drills.DrillMultiblock
import net.starlegacy.feature.multiblock.misc.AirlockMultiblock
import net.starlegacy.feature.multiblock.misc.TractorBeamMultiblock
import net.starlegacy.listener.SLEventListener
import net.starlegacy.util.LegacyBlockUtils
import net.starlegacy.util.Tasks
import net.starlegacy.util.axis
import net.starlegacy.util.getFacing
import net.starlegacy.util.isBed
import net.starlegacy.util.isStainedGlass
import net.starlegacy.util.isWallSign
import net.starlegacy.util.leftFace
import net.starlegacy.util.rightFace
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

object InteractListener : SLEventListener() {
	// When someone clicks a drill sign, toggle it
	// If they don't have any prismarine crystals, warn them
	// (noobs used to ask why it wasn't working so much and this is usually why, lack of crystals)
	@EventHandler
	fun onPlayerInteractEventA(event: PlayerInteractEvent) {
		if (event.action != Action.RIGHT_CLICK_BLOCK) return
		if (event.clickedBlock?.type?.isWallSign != true) return

		val block = event.clickedBlock ?: return

		val sign = block.getState(false) as Sign

		val furnace = block.getRelative(sign.getFacing().oppositeFace).getState(false) as? Furnace
			?: return

		val multiblock = Multiblocks[sign]

		if (multiblock is DrillMultiblock) {
			if (furnace.inventory.let { it.fuel == null || it.smelting?.type != Material.PRISMARINE_CRYSTALS }) {
				event.player.sendFeedbackMessage(
					FeedbackType.USER_ERROR,
					"You need Prismarine Crystals in both slots of the furnace!"
				)
				return
			}

			val player = when {
				DrillMultiblock.isEnabled(sign) -> null
				else -> event.player.name
			}

			DrillMultiblock.setUser(sign, player)
		}
	}

	// Bring player down when they right click a tractor beam sign
	@EventHandler
	fun onPlayerInteractEventB(event: PlayerInteractEvent) {
		if (event.item?.type != Material.CLOCK) return
		if (event.action != Action.RIGHT_CLICK_BLOCK) return
		if (event.clickedBlock?.type?.isWallSign != true) return

		val sign = event.clickedBlock ?: return
		val below = event.player.location.block.getRelative(BlockFace.DOWN)

		if (below.type != Material.GLASS && !below.type.isStainedGlass) return
		if (Multiblocks[sign.getState(false) as Sign] !is TractorBeamMultiblock) return

		var distance = 1
		val maxDistance = below.y - 1

		while (distance < maxDistance) {
			val relative = below.getRelative(BlockFace.DOWN, distance)

			if (relative.type != Material.AIR) {
				break
			}

			distance++
		}

		if (distance < 3) return

		val relative = below.getRelative(BlockFace.DOWN, distance)
		if (relative.type != Material.AIR) {
			event.player.teleport(relative.location.add(0.5, 1.5, 0.5))
		}
	}

	// Bring the player up if they right click while facing up with a clock
	// and there's a tractor beam above them
	@EventHandler
	fun onPlayerInteractEventC(event: PlayerInteractEvent) {
		if (event.item?.type != Material.CLOCK) return
		if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return
		if (event.player.location.pitch > -60) return

		val original = event.player.location.block
		for (i in event.player.world.minHeight..(event.player.world.maxHeight - original.y)) {
			val block = original.getRelative(BlockFace.UP, i)
			if (block.type == Material.AIR) continue

			if (block.type == Material.GLASS || block.type.isStainedGlass) {
				for (face in LegacyBlockUtils.PIPE_DIRECTIONS) {
					val sign = block.getRelative(face, 2)
					if (!sign.type.isWallSign) continue

					if (Multiblocks[sign.getState(false) as Sign] !is TractorBeamMultiblock) continue
					event.player.teleport(block.location.add(0.5, 1.5, 0.5))
				}
				continue
			}
			return
		}
	}

	// Toggle airlocks upon right clicking the sign
	@EventHandler
	fun onPlayerInteractEventD(event: PlayerInteractEvent) {
		if (event.hand != EquipmentSlot.HAND) return
		if (event.action != Action.RIGHT_CLICK_BLOCK) return

		val block = event.clickedBlock ?: return
		val sign = block.getState(false) as? Sign ?: return
		if (Multiblocks[sign] !is AirlockMultiblock) return

		val direction = sign.getFacing().oppositeFace
		val right = direction.rightFace
		val topPortal = block.getRelative(direction).getRelative(right)
		val bottomPortal = topPortal.getRelative(BlockFace.DOWN)

		val enabled = topPortal.type == Material.IRON_BARS

		val newData = if (enabled) {
			Material.NETHER_PORTAL.createBlockData {
				(it as org.bukkit.block.data.Orientable).axis = direction.rightFace.axis
			}
		} else {
			Material.IRON_BARS.createBlockData {
				(it as org.bukkit.block.data.MultipleFacing).setFace(direction.rightFace, true)
				it.setFace(direction.leftFace, true)
			}
		}

		topPortal.blockData = newData
		bottomPortal.blockData = newData

		val component =
			if (enabled) MiniMessage.miniMessage().deserialize(AirlockMultiblock.ON) else MiniMessage.miniMessage()
				.deserialize(AirlockMultiblock.OFF)

		sign.line(1, component)
		sign.update()
	}

	// Put power into the sign if right clicking with a battery
	@EventHandler
	fun onPlayerInteractEventE(event: PlayerInteractEvent) {
		if (event.action != Action.RIGHT_CLICK_BLOCK) return
		if (CustomItems[event.item] !is CustomItems.BatteryItem) return

		val sign = event.clickedBlock?.getState(false) as? Sign ?: return
		val multiblock = Multiblocks[sign] as? PowerStoringMultiblock ?: return

		event.isCancelled = true

		val item = event.item ?: return

		val power = getPower(item)
		var powerToTransfer = power * item.amount
		if (powerToTransfer == 0) return

		val machinePower = PowerMachines.getPower(sign)
		val maxMachinePower = multiblock.maxPower
		if (maxMachinePower - machinePower < powerToTransfer) {
			powerToTransfer = maxMachinePower - machinePower
		}

		setPower(item, power - powerToTransfer / item.amount)
		PowerMachines.addPower(sign, powerToTransfer)
	}

	@EventHandler
	fun onBlockPlaceEvent(event: BlockPlaceEvent) {
		if (event.isCancelled) return
		if (!event.canBuild()) return

		val item = CustomItems[event.itemInHand] as? CustomBlockItem ?: return
		event.block.setBlockData(item.customBlock.blockData, false)
	}

	// When not in creative mode, make breaking a custom item drop the proper drops
	@EventHandler(priority = EventPriority.MONITOR)
	fun onBlockBreakEvent(event: BlockBreakEvent) {
		if (event.isCancelled) return
		if (event.player.gameMode == GameMode.CREATIVE) return

		val block = event.block
		val customBlock = CustomBlocks[block] ?: return

		if (event.isDropItems) {
			event.isDropItems = false
			block.type = Material.AIR
		}

		val itemUsed = event.player.inventory.itemInMainHand
		val location = block.location.toCenterLocation()
		Tasks.sync {
			for (drop in customBlock.getDrops(itemUsed)) {
				block.world.dropItem(location, drop)
			}
		}
	}

	@EventHandler
	fun onPlayerInteractEventF(event: PlayerInteractEvent) {
		if (event.hand != EquipmentSlot.HAND) return
		if (event.action != Action.RIGHT_CLICK_BLOCK) return

		val sign = event.clickedBlock?.getState(false) as? Sign ?: return
		val multiblock = Multiblocks[sign, true, false] as? DockingTubeMultiblock ?: return

		multiblock.toggle(sign, event.player)
	}

	// Disable beds
	@EventHandler
	fun onPlayerInteractEventH(event: PlayerInteractEvent) {
		if (event.action != Action.RIGHT_CLICK_BLOCK) return
		val item = event.clickedBlock!!
		val player = event.player

		if (item.type.isBed) {
			event.isCancelled = true
			player.sendFeedbackActionMessage(
				FeedbackType.INFORMATION,
				"Beds are disabled on this server! Use a cryopod instead"
			)
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onBlockPlace(event: BlockPlaceEvent) {
		val player = event.player

		val hand = event.hand
		val itemStack = player.inventory.getItem(hand).clone()
		val item: CustomBlockItem = CustomItems[itemStack] as? CustomBlockItem ?: return

		event.block.location.block.setBlockData(item.customBlock.blockData, true)
	}
}