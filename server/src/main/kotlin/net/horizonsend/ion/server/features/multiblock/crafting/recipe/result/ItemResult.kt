package net.horizonsend.ion.server.features.multiblock.crafting.recipe.result

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.multiblock.crafting.input.ItemResultEnvironment
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

interface ItemResult<E: ItemResultEnvironment> : RecipeResult<E> {
	fun asItem(): ItemStack

	override fun verifySpace(environment: E): Boolean {
		val resultItem = getResultItem(environment) ?: return true
		return environment.getResultSpaceFor(resultItem) >= resultItem.amount
	}

	/**
	 * Executes the result
	 **/
	fun buildTransaction(
		recipeEnvironment: E,
		resultEnvironment: ResultExecutionEnvironment<E>
	)

	/**
	 * Gets the result item.
	 **/
	fun getResultItem(environment: E): ItemStack?

	companion object {
		fun <E: ItemResultEnvironment> simpleResult(itemStack: ItemStack): SimpleResult<E> = SimpleResult(itemStack)
		fun <E: ItemResultEnvironment> simpleResult(customItem: IonRegistryKey<CustomItem, out CustomItem>): SimpleResult<E> = SimpleResult(customItem.getValue().constructItemStack())
		fun <E: ItemResultEnvironment> simpleResult(material: Material, amount: Int = 1): SimpleResult<E> = SimpleResult(ItemStack(material, amount))
	}

	class SimpleResult<E: ItemResultEnvironment>(private val item: ItemStack) : ItemResult<E> {
		override fun asItem(): ItemStack = item.clone()
		override fun getResultItem(environment: E): ItemStack = item.clone()
		override fun buildTransaction(
			recipeEnvironment: E,
			resultEnvironment: ResultExecutionEnvironment<E>
		) {
			resultEnvironment.addResult {
				recipeEnvironment.addItem(item)
				RecipeExecutionResult.SuccessExecutionResult
			}
		}
	}
}
