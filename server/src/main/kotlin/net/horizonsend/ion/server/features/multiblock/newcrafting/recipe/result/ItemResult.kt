package net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.result

import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.multiblock.newcrafting.input.ItemResultEnviornment
import net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.requirement.RequirementHolder
import net.horizonsend.ion.server.features.multiblock.newcrafting.util.SlotModificationWrapper
import net.kyori.adventure.sound.Sound
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
	fun execute(enviornment: E, slotModificationWrapper: SlotModificationWrapper)

	/**
	 * Gets the result item.
	 **/
	fun getResultItem(enviornment: E): ItemStack?

	companion object {
		fun <E: ItemResultEnviornment> simpleResult(itemStack: ItemStack,sound: Sound? = null): SimpleResult<E> = SimpleResult(itemStack, sound)
		fun <E: ItemResultEnviornment> simpleResult(customItem: CustomItem,sound: Sound? = null): SimpleResult<E> = SimpleResult(customItem.constructItemStack(), sound)
		fun <E: ItemResultEnviornment> simpleResult(material: Material,sound: Sound? = null): SimpleResult<E> = SimpleResult(ItemStack(material, 1), sound)
	}

	class SimpleResult<E: ItemResultEnviornment>(private val item: ItemStack, val sound: Sound? = null) : ItemResult<E> {
		override fun getResultItem(enviornment: E): ItemStack? = item
		override fun filterConsumedIngredients(enviornment: E, ingreidents: Collection<RequirementHolder<E, *, *>>): Collection<RequirementHolder<E, *, *>> = ingreidents
		override fun execute(enviornment: E, slotModificationWrapper: SlotModificationWrapper) {
			slotModificationWrapper.addToSlot(item)
			sound?.let { enviornment.playSound(it) }
		}
	}
}
