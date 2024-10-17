package net.horizonsend.ion.server.features.multiblock.crafting.ingredient

import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.GasCanister
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.RecipeExecutionContext
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import org.bukkit.inventory.FurnaceInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class ItemIngredient<A: MultiblockEntity, B: Inventory>(
	private val itemConsumable: Consumable<A>,
	private val cast: RecipeExecutionContext<A>.() -> B?,
	private val acquireItem: (B) -> ItemStack?
) : MultiblockRecipeIngredient<A> {
	override fun checkRequirement(context: RecipeExecutionContext<A>): Boolean {
		val acquiredItem = getItem(context) ?: return false
		return itemConsumable.checkRequirement(acquiredItem, context)
	}

	override fun consumeIngredient(context: RecipeExecutionContext<A>): Boolean {
		val acquiredItem = getItem(context) ?: return false
		return itemConsumable.consumeIngredient(acquiredItem, context)
	}

	fun getItem(context: RecipeExecutionContext<A>): ItemStack? {
		val inventory = cast.invoke(context) ?: return null
		return acquireItem.invoke(inventory)
	}

	companion object {
		const val MAIN_FURNACE_STRING = "mainFurnace"

		fun <A: MultiblockEntity> furnaceFuelConsumable(consumable: Consumable<A>) = ItemIngredient(
			consumable,
			{ getLabeledInventory(MAIN_FURNACE_STRING) as? FurnaceInventory },
			{ it.fuel }
		)

		fun <A: MultiblockEntity> furnaceFuelIngredient(item: ItemStack, amount: Int) = furnaceFuelConsumable<A>(Consumable.ItemConsumable(item, amount))
		fun <A: MultiblockEntity> furnaceFuelIngredient(canister: GasCanister, amount: Int) = furnaceFuelConsumable<A>(Consumable.GasCanisterConsumable(canister, amount))
		fun <A: MultiblockEntity> furnaceFuelIngredient(item: CustomItem, amount: Int) = furnaceFuelIngredient<A>(item.constructItemStack(), amount)

		fun <A: MultiblockEntity> furnaceSmeltingIngredient(item: ItemStack, amount: Int) = ItemIngredient<A, FurnaceInventory>(
			Consumable.ItemConsumable(item, amount),
			{ getLabeledInventory(MAIN_FURNACE_STRING) as? FurnaceInventory },
			{ it.smelting }
		)

		fun <A: MultiblockEntity> furnaceSmeltingIngredient(item: CustomItem, amount: Int) = ItemIngredient<A, FurnaceInventory>(
			Consumable.ItemConsumable(item.constructItemStack(), amount),
			{ getLabeledInventory(MAIN_FURNACE_STRING) as? FurnaceInventory },
			{ it.smelting }
		)
	}
}
