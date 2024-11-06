package net.horizonsend.ion.server.features.multiblock.type.crafting.ingredient

import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.multiblock.Multiblock
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.inventory.ItemStack

class ConsumedMatchingIngredient(
	match: String,
	ignore: Set<Material>,
	amount: Int
) : MatchingIngredient(match, ignore, amount), ItemConsumable {

	override fun consume(multiblock: Multiblock, sign: Sign, itemStack: ItemStack) {
		itemStack.amount -= amount
	}
}
