package net.horizonsend.ion.server.listener.misc

import net.horizonsend.ion.common.extensions.successActionMessage
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.machine.PowerMachines
import net.horizonsend.ion.server.features.misc.getPower
import net.horizonsend.ion.server.features.misc.setPower
import net.horizonsend.ion.server.features.multiblock.InteractableMultiblock
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.Multiblocks
import net.horizonsend.ion.server.features.multiblock.PowerStoringMultiblock
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomBlockItem
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomBlocks
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.isBed
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

object InteractListener : SLEventListener() {
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
	fun handleMultiblockInteract(event: PlayerInteractEvent) {
		if (event.hand != EquipmentSlot.HAND) return
		if (event.action != Action.RIGHT_CLICK_BLOCK) return
		val player = event.player

		val sign = event.clickedBlock?.getState(false) as? Sign ?: return
		(Multiblocks[sign, true, false] as? InteractableMultiblock)?.let { multiblock ->
			(multiblock as Multiblock).requiredPermission?.let { permission ->
				if (!player.hasPermission(permission)) return player.userError("You don't have permission to use that multiblock!")
			}

			multiblock.onSignInteract(sign, player, event)
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

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onBlockPlace(event: BlockPlaceEvent) {
		val player = event.player

		val hand = event.hand
		val itemStack = player.inventory.getItem(hand).clone()
		val item: CustomBlockItem = CustomItems[itemStack] as? CustomBlockItem ?: return

		event.block.location.block.setBlockData(item.customBlock.blockData, true)
	}
}
