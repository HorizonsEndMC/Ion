package net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement

import net.horizonsend.ion.server.features.multiblock.crafting.input.InventoryResultEnviornment
import net.horizonsend.ion.server.features.multiblock.crafting.input.RecipeEnviornment
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.item.ItemRequirement
import net.horizonsend.ion.server.features.multiblock.crafting.util.SlotModificationWrapper
import org.bukkit.inventory.ItemStack

open class RequirementHolder<T: RecipeEnviornment, V: Any?, out R: RecipeRequirement<V>>(val dataTypeClass: Class<V>, val getter: (T) -> V, val requirement: R) {
	fun checkRequirement(enviornment: T): Boolean {
		val resourceValue = getter.invoke(enviornment)
		return requirement.ensureAvailable(resourceValue)
	}

	open fun consume(environment: T) {
		if (requirement is Consumable<*, *>) {
			@Suppress("UNCHECKED_CAST")
			(requirement as Consumable<V, T>).consume(environment)
		}
	}

	class BundledRequirementHolder<T: RecipeEnviornment, V: Any?, R: RecipeRequirement<V>>(
		dataTypeClass: Class<V>,
		getter: (T) -> V,
		requirement: R,
		private val slotModificationWrapper: (T) -> SlotModificationWrapper
	) : RequirementHolder<T, V, R>(dataTypeClass, getter, requirement) {
		override fun consume(environment: T) {
			val wrapper = slotModificationWrapper.invoke(environment)

			if (requirement is ItemRequirement) { //TODO generalize for new
				wrapper.consume consumer@{ item: ItemStack? ->
					if (item == null) return@consumer
					requirement.consume(item, environment)
				}
			}
		}
	}

	class AnySlotRequirementHolder<T: RecipeEnviornment>(
		requirement: ItemRequirement,
	) : RequirementHolder<T, ItemStack?, ItemRequirement>(
		(ItemStack::class.java as Class<ItemStack?>),
		{ it.getInputItems().firstOrNull { inputItem -> requirement.matches(inputItem) } },
		requirement
	) {
		override fun consume(environment: T) {
			val item = getter.invoke(environment) ?: return
			requirement.consume(item, environment)
		}
	}

	companion object {
		inline fun <T: RecipeEnviornment, reified V: Any?, R: Consumable<V, T>> simpleConsumable(
			noinline getter: (T) -> V,
			requirement: R,
		): RequirementHolder<T, V, R> = RequirementHolder(V::class.java, getter, requirement)

		inline fun <T: RecipeEnviornment, reified V: Any?, R: RecipeRequirement<V>> itemConsumable(
			noinline getter: (T) -> V,
			requirement: R,
			noinline slotModificationWrapper: (T) -> SlotModificationWrapper,
		): RequirementHolder<T, V, R> = BundledRequirementHolder(V::class.java, getter, requirement, slotModificationWrapper)

		fun <T: InventoryResultEnviornment> anySlot(
			requirement: ItemRequirement,
		): AnySlotRequirementHolder<T> = AnySlotRequirementHolder(requirement)
	}
}
