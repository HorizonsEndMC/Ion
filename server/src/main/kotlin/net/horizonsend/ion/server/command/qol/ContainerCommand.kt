package net.horizonsend.ion.server.command.qol

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.AnyItem
import net.horizonsend.ion.server.command.GlobalCompletions.toItemString
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.miscellaneous.utils.LegacyItemUtils
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.getSelection
import org.bukkit.entity.Player
import org.bukkit.inventory.InventoryHolder

@CommandAlias("container")
@CommandPermission("ion.containercommand")
object ContainerCommand : SLCommand() {
	@Suppress("unused")
	@Subcommand("empty")
	fun onEmpty(sender: Player, anyItem: AnyItem) {
		val maxSelectionVolume = 200000
		val selection = sender.getSelection() ?: return
		if(selection.volume > maxSelectionVolume) {
			sender.userError("Selection too large! The maximum volume is $maxSelectionVolume.")
			return
		}
		if(sender.world.name != selection.world?.name) return
		var count = 0
		val containerList = mutableListOf<InventoryHolder>()
		for (blockPosition in selection) {
			val block =
				sender.world.getBlockAt(blockPosition.x, blockPosition.y, blockPosition.z).state as? InventoryHolder ?: continue
			containerList.add(block)
			sender.debug(""+containerList.size)
		}
		Tasks.async {
			anyItem.amount = anyItem.maxStackSize
			for(block in containerList) {
				if (block.inventory.contains(anyItem.type)) {
					count++
					for (item in block.inventory.contents) {
						if (item != null && item.type == anyItem.type) block.inventory.remove(item)
					}
				}
			}
			sender.success("Cleared $count containers storing ${toItemString(anyItem)}.")
		}
	}
	@Suppress("unused")
	@Subcommand("empty")
	fun onEmpty(sender: Player) {
		val maxSelectionVolume = 200000
		val selection = sender.getSelection() ?: return
		if(selection.volume > maxSelectionVolume) {
			sender.userError("Selection too large! The maximum volume is $maxSelectionVolume.")
			return
		}
		if(sender.world.name != selection.world?.name) return
		var count = 0
		val containerList = mutableListOf<InventoryHolder>()
		for (blockPosition in selection) {
			val block =
				sender.world.getBlockAt(blockPosition.x, blockPosition.y, blockPosition.z).state as? InventoryHolder ?: continue
			containerList.add(block)
			sender.debug(""+containerList.size)
		}

		Tasks.async {
			for(block in containerList) {
				if (!block.inventory.isEmpty) {
					count++
					block.inventory.clear()
				}
			}
			sender.success("Cleared $count containers")
		}
	}

	@Suppress("unused")
	@Subcommand("fill")
	@CommandCompletion("@anyItem")
	fun onFill(sender: Player, anyItem: AnyItem) {
		val maxSelectionVolume = 200000
		val selection = sender.getSelection() ?: return
		if(selection.volume > maxSelectionVolume) {
			sender.userError("Selection too large! The maximum volume is $maxSelectionVolume.")
			return
		}
		if(sender.world.name != selection.world?.name) return
		var count = 0
		val containerList = mutableListOf<InventoryHolder>()
		for (blockPosition in selection) {
			val block = sender.world.getBlockAt(blockPosition.x, blockPosition.y, blockPosition.z).state as? InventoryHolder ?: continue
			containerList.add(block)
		}
		Tasks.async {
			anyItem.amount = anyItem.maxStackSize
			for(block in containerList) {
				if (LegacyItemUtils.canFit(block.inventory, anyItem)) {
					count++
					for ((index, item) in block.inventory.withIndex()) {
						if (item == null) block.inventory.setItem(index, anyItem)
					}
				}
			}
			sender.success("Filled $count containers with ${toItemString(anyItem)}.")
		}
	}
}
