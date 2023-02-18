package net.starlegacy.command.misc

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.server.miscellaneous.extensions.success
import net.starlegacy.command.SLCommand
import net.starlegacy.util.enumValueOfOrNull
import net.starlegacy.util.isBed
import net.starlegacy.util.isCarpet
import net.starlegacy.util.isConcrete
import net.starlegacy.util.isConcretePowder
import net.starlegacy.util.isGlass
import net.starlegacy.util.isGlassPane
import net.starlegacy.util.isGlazedTerracotta
import net.starlegacy.util.isStainedTerracotta
import net.starlegacy.util.isWool
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

@CommandAlias("dye")
@CommandPermission("starlegacy.dye")
object DyeCommand : SLCommand() {
	@Suppress("Unused")
	@Default()
	fun execute(sender: Player, newColor: String) {
		val newDyeColor = enumValueOfOrNull<DyeColor>(newColor.uppercase(Locale.getDefault()))
			?: fail { "Valid colors are " + DyeColor.values().joinToString() }

		val item = sender.inventory.itemInMainHand
		val oldDyeColor = dyeItem(item, newDyeColor)
			?: fail { "Item is not colorable" }
		sender.success("Dyed from $oldDyeColor -> $newDyeColor")
	}

	private fun dyeItem(item: ItemStack, newDyeColor: DyeColor): DyeColor {
		if (!(item.type.isConcrete || item.type.isConcretePowder || item.type.isWool || item.type.isGlass || item.type.isGlassPane || item.type.isStainedTerracotta || item.type.isGlazedTerracotta || item.type.isCarpet || item.type.isBed)) {
			fail { "This item can not be dyed." }
		}

		val oldDyeColor = DyeColor.values()
			.filter { color -> item.type.name.contains(color.name) }
			.maxByOrNull { it.name.length }

		val newName = item.type.name.replace(oldDyeColor!!.name, newDyeColor.name)
		val newMaterial = Material.getMaterial(newName) ?: fail { "Item material $newName doesn't exist" }
		item.type = newMaterial
		return oldDyeColor
	}

	@Suppress("Unused")
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
