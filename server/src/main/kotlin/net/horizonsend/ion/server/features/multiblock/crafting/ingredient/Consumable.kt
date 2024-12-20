package net.horizonsend.ion.server.features.multiblock.crafting.ingredient

import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.customItem
import net.horizonsend.ion.server.features.custom.items.type.GasCanister
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.RecipeExecutionContext
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import org.bukkit.inventory.ItemStack

interface Consumable<A: MultiblockEntity> {
	fun checkRequirement(item: ItemStack?, context: RecipeExecutionContext<A>): Boolean

	fun consumeIngredient(item: ItemStack, context: RecipeExecutionContext<A>): Boolean

	class ItemConsumable<A: MultiblockEntity>(val itemStack: ItemStack, val amount: Int) : Consumable<A> {
		override fun checkRequirement(item: ItemStack?, context: RecipeExecutionContext<A>): Boolean {
			if (item == null) return false
			if (!itemStack.isSimilar(item)) return false

			return item.amount >= amount
		}

		override fun consumeIngredient(item: ItemStack, context: RecipeExecutionContext<A>): Boolean {
			item.amount -= amount
			return true
		}
	}

	class GasCanisterConsumable<A: MultiblockEntity>(private val canister: GasCanister, val amount: Int) : Consumable<A> {
		override fun checkRequirement(item: ItemStack?, context: RecipeExecutionContext<A>): Boolean {
			if (item == null) return false

			val customItem = item.customItem

			if (customItem !is GasCanister) return false
			if (customItem.gas != canister.gas) return false

			return canister.getFill(item) > amount
		}

		override fun consumeIngredient(item: ItemStack, context: RecipeExecutionContext<A>): Boolean {
			val oldFill = canister.getFill(item)
			canister.setFill(item, oldFill - amount)
			return true
		}
	}
}
