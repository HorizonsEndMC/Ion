package net.horizonsend.ion.server.features.multiblock.type

import net.horizonsend.ion.common.utils.text.toComponent
import net.horizonsend.ion.common.utils.text.wrap
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

interface DisplayNameMultilblock {
	val displayName: Component
	val description: Component

//	val iconModel: Key

	companion object {
		fun Multiblock.getDisplayName() = if (this is DisplayNameMultilblock) displayName else javaClass.simpleName.toComponent()
		fun Multiblock.getDescription() = if (this is DisplayNameMultilblock) description else Component.text("Multiblock has no description.")

		fun Multiblock.getIcon(): ItemStack {
			val base = ItemStack(Material.CHEST)
				.updateDisplayName(getDisplayName())

			return if (this is DisplayNameMultilblock) base
//				.updateData(DataComponentTypes.ITEM_MODEL, iconModel)
				.updateLore(description.wrap(150))
			else
				base
		}
	}
}
