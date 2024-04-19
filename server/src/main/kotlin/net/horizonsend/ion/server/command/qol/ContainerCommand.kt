package net.horizonsend.ion.server.command.qol

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.miscellaneous.utils.LegacyItemUtils
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.getSelection
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

@CommandAlias("container")
@CommandPermission("ion.containercommand")
object ContainerCommand : SLCommand() {
	@Subcommand("empty")
	@Suppress("unused")
	fun onEmpty(sender: Player, @Optional material: Material?) {
		val maxSelectionVolume = 200000
		val selection = sender.getSelection() ?: return
		if(selection.volume > maxSelectionVolume) {
			sender.userError("Selection too large! The maximum volume is $maxSelectionVolume.")
			return
		}
		if(sender.world.name != selection.world?.name) return

		for (blockPosition in selection) {
			val x = blockPosition.x
			val y = blockPosition.y
			val z = blockPosition.z

			val block = sender.world.getBlockAt(x, y, z).state as? InventoryHolder ?: continue
			if (material == null) block.inventory.clear()
			else if (block.inventory.contains(material)) {
				for (item in block.inventory.contents) {
					if (item != null && item.type == material) block.inventory.remove(item)
				}
			}
		}
		if (material == null) sender.success("Cleared containers")
		else sender.success("Cleared containers storing $material.")

	}

	@Subcommand("fill")
	@Suppress("unused")
	fun onFill(sender: Player, material: Material) {
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
		Tasks.async {
			val itemStack = ItemStack(material, material.maxStackSize)
			for(block in containerList) {
				if (LegacyItemUtils.canFit(block.inventory, itemStack)) {
					count++
					for ((index, item) in block.inventory.withIndex()) {
						if (item == null) block.inventory.setItem(index, itemStack)
					}
				}
			}
			sender.success("Filled $count containers with $material.")
		}
	}
}
