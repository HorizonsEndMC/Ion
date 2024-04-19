package net.horizonsend.ion.server.command.qol

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.GlobalCompletions
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.miscellaneous.utils.LegacyItemUtils
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.getSelection
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.InventoryHolder

@CommandAlias("container")
@CommandPermission("ion.containercommand")
object ContainerCommand : SLCommand() {
	@Subcommand("empty")
	@Suppress("unused")
	@CommandCompletion("@anyItem")
	fun onEmpty(sender: Player, @Optional itemString: String?) {
		val maxSelectionVolume = 200000
		val selection = sender.getSelection() ?: return
		if(selection.volume > maxSelectionVolume) {
			sender.userError("Selection too large! The maximum volume is $maxSelectionVolume.")
			return
		}
		if(sender.world.name != selection.world?.name) return
		val containerList = mutableListOf<InventoryHolder>()
		var count = 0
		for (blockPosition in selection) {
			val x = blockPosition.x
			val y = blockPosition.y
			val z = blockPosition.z
			val block = sender.world.getBlockAt(x, y, z).state as? InventoryHolder ?: continue
			containerList.add(block)
		}
		Tasks.async {
			for(block in containerList) {
				if (itemString == null) block.inventory.clear()
				else if (block.inventory.contains(Material.valueOf(itemString))) {
					count++
					for (item in block.inventory.contents) {
						if (item != null && item.type == Material.valueOf(itemString)) block.inventory.remove(item)
					}
				}
			}
			if (itemString == null) sender.success("Cleared $count containers")
			else sender.success("Cleared $count containers storing $itemString.")
		}
	}

	@Subcommand("fill")
	@Suppress("unused")
	@CommandCompletion("@anyItem")
	fun onFill(sender: Player, itemString: String) {
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
			val x = blockPosition.x
			val y = blockPosition.y
			val z = blockPosition.z
			val block = sender.world.getBlockAt(x, y, z).state as? InventoryHolder ?: continue
			containerList.add(block)
		}
		val itemStack = GlobalCompletions.stringToItem(itemString) ?: return
		Tasks.async {
			itemStack.amount = itemStack.maxStackSize
			for(block in containerList) {
				if (LegacyItemUtils.canFit(block.inventory, itemStack)) {
					count++
					for ((index, item) in block.inventory.withIndex()) {
						if (item == null) block.inventory.setItem(index, itemStack)
					}
				}
			}
			sender.success("Filled $count containers with $itemString.")
		}
	}
}
