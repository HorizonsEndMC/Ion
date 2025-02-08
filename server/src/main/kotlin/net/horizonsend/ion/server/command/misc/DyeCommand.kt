package net.horizonsend.ion.server.command.misc

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.DyedItemColor
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.customItem
import net.horizonsend.ion.server.features.custom.items.type.armor.PowerArmorItem
import net.horizonsend.ion.server.miscellaneous.utils.enumValueOfOrNull
import net.horizonsend.ion.server.miscellaneous.utils.isBed
import net.horizonsend.ion.server.miscellaneous.utils.isCandle
import net.horizonsend.ion.server.miscellaneous.utils.isCarpet
import net.horizonsend.ion.server.miscellaneous.utils.isConcrete
import net.horizonsend.ion.server.miscellaneous.utils.isConcretePowder
import net.horizonsend.ion.server.miscellaneous.utils.isGlass
import net.horizonsend.ion.server.miscellaneous.utils.isGlassPane
import net.horizonsend.ion.server.miscellaneous.utils.isGlazedTerracotta
import net.horizonsend.ion.server.miscellaneous.utils.isStainedTerracotta
import net.horizonsend.ion.server.miscellaneous.utils.isWool
import net.minecraft.util.ARGB
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.DyeItem
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.Locale

@CommandAlias("dye")
@CommandPermission("starlegacy.dye")
object DyeCommand : net.horizonsend.ion.server.command.SLCommand() {
	fun canHexDye(itemStack: ItemStack): Boolean {
		if (itemStack.customItem is PowerArmorItem) return true
		if (itemStack.type == Material.LEATHER_BOOTS) return true
		if (itemStack.type == Material.LEATHER_LEGGINGS) return true
		if (itemStack.type == Material.LEATHER_CHESTPLATE) return true
		if (itemStack.type == Material.LEATHER_HELMET) return true
		return false
	}

	@Suppress("Unused")
	@Default
	fun execute(sender: Player, enteredColor: String) {
		val item = sender.inventory.itemInMainHand

		val (oldColor: String, newColor: String) = if (canHexDye(item)) {
			val parsedInt = runCatching { Integer.parseInt(enteredColor.removePrefix("#"), 16) }.getOrNull() ?: fail { "Invalid color $enteredColor" }
			dyeDyedItem(item, parsedInt)
		} else {
			val newDyeColor = enumValueOfOrNull<DyeColor>(enteredColor.uppercase(Locale.getDefault())) ?: fail { "Valid colors are " + DyeColor.entries.joinToString() }
			dyeItem(sender, item, newDyeColor)
		}

		sender.success("Dyed from $oldColor -> $newColor")
	}

	private fun dyeDyedItem(itemStack: ItemStack, color: Int): Pair<String, String> {
		val bukkitColor = Color.fromRGB(color)
		val oldColor = itemStack.getData(DataComponentTypes.DYED_COLOR)?.color()?.asRGB()?.let { Integer.toHexString(it) } ?: "None"
		val dyedItemColor = DyedItemColor.dyedItemColor(bukkitColor, true)
		itemStack.setData(DataComponentTypes.DYED_COLOR, dyedItemColor)
		return oldColor to "#${Integer.toHexString(color)}"
	}

	private fun dyeItem(player: Player, item: ItemStack, newDyeColor: DyeColor): Pair<String, String> {
		if (!(item.type.isConcrete
				|| item.type.isConcretePowder
				|| item.type.isWool
				|| item.type.isGlass
				|| item.type.isGlassPane
				|| item.type.isStainedTerracotta
				|| item.type.isGlazedTerracotta
				|| item.type.isCarpet
				|| item.type.isBed)
				|| item.type.isCandle) {
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

	fun applyDye(stack: ItemStack, dyes: Collection<DyeItem>): ItemStack {
		val itemStack = stack.asOne()
		var i = 0
		var i1 = 0
		var i2 = 0
		var i3 = 0
		var i4 = 0
		val dyedItemColor = itemStack.getData(DataComponentTypes.DYED_COLOR)

		if (dyedItemColor != null) {
			val i5 = ARGB.red(dyedItemColor.color().asRGB())
			val i6 = ARGB.green(dyedItemColor.color().asRGB())
			val i7 = ARGB.blue(dyedItemColor.color().asRGB())

			i3 += maxOf(i5, i6, i7)
			i += i5
			i1 += i6
			i2 += i7
			i4++
		}

		for (dyeItem in dyes) {
			val i7 = dyeItem.dyeColor.textureDiffuseColor
			val i8 = ARGB.red(i7)
			val i9 = ARGB.green(i7)
			val i10 = ARGB.blue(i7)
			i3 += maxOf(i8, i9, i10)
			i += i8
			i1 += i9
			i2 += i10
			i4++
		}

		var finalR = i / i4
		var finalB = i1 / i4
		var finalG = i2 / i4

		val f = i3.toFloat() / i4.toFloat()
		val f1 = maxOf(finalR, finalB, finalG).toFloat()

		finalR = (finalR * f / f1).toInt()
		finalB = (finalB * f / f1).toInt()
		finalG = (finalG * f / f1).toInt()

		val final = ARGB.color(0, finalR, finalB, finalG)
		val flag = dyedItemColor == null || dyedItemColor.showInTooltip()

		itemStack.setData(DataComponentTypes.DYED_COLOR, DyedItemColor.dyedItemColor(Color.fromRGB(final), flag))
		return itemStack
	}
}
