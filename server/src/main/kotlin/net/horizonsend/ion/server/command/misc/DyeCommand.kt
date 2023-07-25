package net.horizonsend.ion.server.command.misc

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.success
import net.minecraft.world.item.DyeableArmorItem
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
import org.bukkit.Color
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

@CommandAlias("dye")
@CommandPermission("starlegacy.dye")
object DyeCommand : net.horizonsend.ion.server.command.SLCommand() {
	@Suppress("Unused")
	@Default()
	fun execute(sender: Player, newColor: String) {
		val newDyeColor = enumValueOfOrNull<DyeColor>(newColor.uppercase(Locale.getDefault()))
			?: fail { "Valid colors are " + DyeColor.values().joinToString() }

		val item = sender.inventory.itemInMainHand
		val oldDyeColor = dyeItem(item, newDyeColor)
		sender.success("Dyed from $oldDyeColor -> $newDyeColor")
	}

	private fun dyeItem(item: ItemStack, newDyeColor: DyeColor): DyeColor {
		if (item.itemMeta is DyeableArmorItem) {
			val nmsItem = CraftItemStack.asNMSCopy(item)
			val dyeableItemMeta = item.itemMeta as? DyeableArmorItem
			val oldDyeColor = dyeableItemMeta?.getColor(nmsItem) ?: 0
			dyeableItemMeta?.setColor(nmsItem, newDyeColor.color.asRGB())
			(item.itemMeta as? DyeableArmorItem)?.setColor(nmsItem, newDyeColor.color.asRGB())
			return DyeColor.getByColor(Color.fromRGB(oldDyeColor)) ?: DyeColor.PINK
		}
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
