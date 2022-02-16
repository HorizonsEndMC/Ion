package net.starlegacy.command.misc

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import java.util.Locale
import net.starlegacy.command.SLCommand
import net.starlegacy.util.action
import net.starlegacy.util.enumValueOfOrNull
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@CommandAlias("dye")
@CommandPermission("starlegacy.dye")
object DyeCommand : SLCommand() {
	@Default()
	fun execute(sender: Player, newColor: String) {
		val newDyeColor = enumValueOfOrNull<DyeColor>(newColor.uppercase(Locale.getDefault()))
			?: fail { "Valid colors are " + DyeColor.values().joinToString() }

		val item = sender.inventory.itemInMainHand
		val oldDyeColor = dyeItem(item, newDyeColor)
			?: fail { "Item is not colorable" }
		sender action "&aDyed from $oldDyeColor -> $newDyeColor"
	}

	private fun dyeItem(item: ItemStack, newDyeColor: DyeColor): DyeColor? {
		val oldDyeColor = DyeColor.values()
			.filter { color -> item.type.name.contains(color.name) }
			.maxByOrNull { it.name.length }

		val newName = item.type.name.replace(oldDyeColor!!.name, newDyeColor.name)
		val newMaterial = Material.getMaterial(newName) ?: fail { "Item material $newName doesn't exist" }
		item.type = newMaterial
		return oldDyeColor
	}

	@Subcommand("inventory|inv|all")
	@CommandPermission("starlegacy.dyeinventoy")
	fun executeInv(sender: Player, newColor: String) {
		val newDyeColor = enumValueOfOrNull<DyeColor>(newColor.uppercase(Locale.getDefault()))
			?: fail { "Valid colors are " + DyeColor.values().joinToString() }

		for (item: ItemStack? in sender.inventory) {
			if (item == null) {
				continue
			}

			dyeItem(item, newDyeColor)
		}
	}

	override fun supportsVanilla(): Boolean {
		return true
	}
}
