package net.horizonsend.ion.server.command.qol

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.GlobalCompletions
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
	@CommandCompletion("@anyItem")
	fun onEmpty(sender: Player, @Optional str: String?) {
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
			val block = sender.world.getBlockAt(blockPosition.x(), blockPosition.y(), blockPosition.z()).state as? InventoryHolder ?: continue
			containerList.add(block)
			sender.debug(""+containerList.size)
		}
		val item = if(str != null) GlobalCompletions.stringToItem(str) else null
		Tasks.async {
			if(item == null){
				for(block in containerList) {
					if (!block.inventory.isEmpty) {
						count++
						block.inventory.clear()
					}
				}
				sender.success("Cleared $count containers")
			}else {
				item.amount = item.maxStackSize
				for (block in containerList) {
					if (block.inventory.contains(item.type)) {
						count++
						for (itemStack in block.inventory.contents) {
							if (itemStack != null && itemStack.type == item.type) block.inventory.remove(itemStack)
						}
					}
				}
				sender.success("Cleared $count containers storing ${toItemString(item)}.")
			}
		}
	}

	@Suppress("unused")
	@Subcommand("fill")
	@CommandCompletion("@anyItem")
	fun onFill(sender: Player, str: String) {
		val item = GlobalCompletions.stringToItem(str) ?: sender.inventory.itemInMainHand
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
			val block = sender.world.getBlockAt(blockPosition.x(), blockPosition.y(), blockPosition.z()).state as? InventoryHolder ?: continue
			containerList.add(block)
		}
		Tasks.async {
			item.amount = item.maxStackSize
			for(block in containerList) {
				if (LegacyItemUtils.canFit(block.inventory, item)) {
					count++
					for ((index, itemStack) in block.inventory.withIndex()) {
						if (itemStack == null) block.inventory.setItem(index, item)
					}
				}
			}
			sender.success("Filled $count containers with ${toItemString(item)}.")
		}
	}
}
