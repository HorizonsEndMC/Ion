package net.horizonsend.ion.server.features.multiblock.crafting.recipe.result

import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.multiblock.crafting.input.ItemResultEnviornment
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

interface ItemResult<E: ItemResultEnviornment> : RecipeResult<E> {
	fun asItem(): ItemStack

	override fun verifySpace(enviornment: E): Boolean {
		val resultItem = getResultItem(enviornment) ?: return true
		return enviornment.getResultSpaceFor(resultItem) >= resultItem.amount
	}

	/**
	 * Executes the result
	 **/
	fun buildTransaction(
		recipeEnviornment: E,
		resultEnviornment: ResultExecutionEnviornment<E>
	)

	/**
	 * Gets the result item.
	 **/
	fun getResultItem(enviornment: E): ItemStack?

	companion object {
		fun <E: ItemResultEnviornment> simpleResult(itemStack: ItemStack): SimpleResult<E> = SimpleResult(itemStack)
		fun <E: ItemResultEnviornment> simpleResult(customItem: CustomItem): SimpleResult<E> = SimpleResult(customItem.constructItemStack())
		fun <E: ItemResultEnviornment> simpleResult(material: Material): SimpleResult<E> = SimpleResult(ItemStack(material, 1))
	}

	class SimpleResult<E: ItemResultEnviornment>(private val item: ItemStack) : ItemResult<E> {
		override fun asItem(): ItemStack = item
		override fun getResultItem(enviornment: E): ItemStack = item
		override fun buildTransaction(
			recipeEnviornment: E,
			resultEnviornment: ResultExecutionEnviornment<E>
		) {
			resultEnviornment.addResult {
				recipeEnviornment.addItem(item)
				RecipeExecutionResult.SuccessExecutionResult
			}
		}
	}
}
