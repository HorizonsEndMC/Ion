package net.horizonsend.ion.server.features.multiblock.crafting.recipe.result

import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.multiblock.crafting.input.ItemResultEnviornment
import net.horizonsend.ion.server.features.multiblock.crafting.util.SlotModificationWrapper
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

interface ItemResult<E: ItemResultEnviornment> : RecipeResult<E> {
	override fun verifySpace(enviornment: E): Boolean {
		val resultOccupant = enviornment.getResultItem() ?: return true
		if (resultOccupant.isEmpty) return true

		val resultItem = getResultItem(enviornment) ?: return true

		if (!resultOccupant.isSimilar(resultItem)) return false

		val maxStackSize = resultItem.maxStackSize
		return resultOccupant.amount + resultItem.amount <= maxStackSize
	}

	/**
	 * Executes the result
	 **/
	fun buildTransaction(
		recipeEnviornment: E,
		resultEnviornment: ResultExecutionEnviornment<E>,
		slotModificationWrapper: SlotModificationWrapper
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
		override fun getResultItem(enviornment: E): ItemStack? = item
		override fun buildTransaction(
			recipeEnviornment: E,
			resultEnviornment: ResultExecutionEnviornment<E>,
			slotModificationWrapper: SlotModificationWrapper
		) {
			resultEnviornment.addResult { e ->
				slotModificationWrapper.addToSlot(item)
				RecipeExecutionResult.SuccessExecutionResult
			}
		}
	}
}
