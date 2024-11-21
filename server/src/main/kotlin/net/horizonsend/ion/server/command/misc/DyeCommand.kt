package net.horizonsend.ion.server.command.misc

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.DyedItemColor
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.miscellaneous.utils.enumValueOfOrNull
import net.horizonsend.ion.server.miscellaneous.utils.isBed
import net.horizonsend.ion.server.miscellaneous.utils.isCarpet
import net.horizonsend.ion.server.miscellaneous.utils.isConcrete
import net.horizonsend.ion.server.miscellaneous.utils.isConcretePowder
import net.horizonsend.ion.server.miscellaneous.utils.isGlass
import net.horizonsend.ion.server.miscellaneous.utils.isGlassPane
import net.horizonsend.ion.server.miscellaneous.utils.isGlazedTerracotta
import net.horizonsend.ion.server.miscellaneous.utils.isStainedTerracotta
import net.horizonsend.ion.server.miscellaneous.utils.isWool
import net.minecraft.world.item.DyeColor
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.Locale

@CommandAlias("dye")
@CommandPermission("starlegacy.dye")
object DyeCommand : net.horizonsend.ion.server.command.SLCommand() {
	@Suppress("Unused")
	@Default
	fun execute(sender: Player, enteredColor: String) {
		val item = sender.inventory.itemInMainHand

		val (oldColor: String, newColor: String) = if (item.type.defaultDataTypes.contains(DataComponentTypes.DYED_COLOR)) {
			val parsedInt = runCatching { Integer.parseInt(enteredColor, 16) }.getOrNull() ?: fail { "Invalid color $enteredColor" }
			dyeDyedItem(item, parsedInt)
		} else {
			val newDyeColor = enumValueOfOrNull<DyeColor>(enteredColor.uppercase(Locale.getDefault())) ?: fail { "Valid colors are " + DyeColor.entries.joinToString() }
			dyeItem(sender, item, newDyeColor)
		}

		sender.success("Dyed from $oldColor -> $newColor")
	}

	private fun dyeDyedItem(itemStack: ItemStack, color: Int): Pair<String, String> {
		val bukkitColor = Color.fromRGB(color)
		val oldColor = itemStack.getData(DataComponentTypes.DYED_COLOR)?.color()?.asRGB()?.let { Integer.toHexString(it) } ?: "Null"
		val dyedItemColor = DyedItemColor.dyedItemColor(bukkitColor, true)
		itemStack.setData(DataComponentTypes.DYED_COLOR, dyedItemColor)
		return oldColor to "#${Integer.toHexString(color)}"
	}

	private fun dyeItem(player: Player, item: ItemStack, newDyeColor: DyeColor): Pair<String, String> {
		if (!(item.type.isConcrete || item.type.isConcretePowder || item.type.isWool || item.type.isGlass || item.type.isGlassPane || item.type.isStainedTerracotta || item.type.isGlazedTerracotta || item.type.isCarpet || item.type.isBed)) {
			fail { "This item can not be dyed." }
		}

		val oldDyeColor = DyeColor.entries
			.filter { color -> item.type.name.contains(color.name) }
			.maxByOrNull { it.name.length }

		val newName = item.type.name.replace(oldDyeColor!!.name, newDyeColor.name)
		val newMaterial = Material.getMaterial(newName) ?: fail { "Item material $newName doesn't exist" }

		val newStack = item.withType(newMaterial)
		player.inventory.setItemInMainHand(newStack)
		return oldDyeColor.name to newDyeColor.name
	}

	@Suppress("Unused")
	@Subcommand("inventory|inv|all")
	@CommandPermission("starlegacy.dyeinventoy")
	fun executeInv(sender: Player, newColor: String) {
		val newDyeColor = enumValueOfOrNull<DyeColor>(newColor.uppercase(Locale.getDefault())) ?: fail { "Valid colors are " + DyeColor.entries.joinToString() }

		for (item: ItemStack? in sender.inventory) {
			if (item == null) {
				continue
			}

			dyeItem(sender, item, newDyeColor)
		}
	}

	override fun supportsVanilla(): Boolean {
		return true
	}
}
