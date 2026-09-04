package net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement

import net.horizonsend.ion.server.features.multiblock.crafting.input.InventoryResultEnvironment
import net.horizonsend.ion.server.features.multiblock.crafting.input.RecipeEnvironment
import net.horizonsend.ion.server.features.multiblock.crafting.input.StatusRecipeEnvironment
import net.horizonsend.ion.server.features.multiblock.crafting.recipe.requirement.item.ItemRequirement
import net.horizonsend.ion.server.features.multiblock.crafting.util.SlotModificationWrapper
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack

open class RequirementHolder<T: RecipeEnvironment, V: Any?, out R: RecipeRequirement<V>>(val dataTypeClass: Class<V>, val getter: (T) -> V, val requirement: R, val failureStatus: Component?) {
	fun checkRequirement(environment: T, displayStatuses: Boolean): Boolean {
		val resourceValue = getter.invoke(environment)
		val result = requirement.ensureAvailable(resourceValue)

		// Only provide statuses if the recipe is locked
		if (failureStatus != null && environment is StatusRecipeEnvironment && displayStatuses) {
			if (!result) {
				environment.setStatus(failureStatus)
			}
		}

		return result
	}

	open fun consume(environment: T) {
		if (requirement is Consumable<*, *>) {
			@Suppress("UNCHECKED_CAST")
			(requirement as Consumable<V, T>).consume(environment)
		}
	}

	class BundledRequirementHolder<T: RecipeEnvironment, V: Any?, R: RecipeRequirement<V>>(
		dataTypeClass: Class<V>,
		getter: (T) -> V,
		requirement: R,
		private val slotModificationWrapper: (T) -> SlotModificationWrapper,
		failureStatus: Component?
	) : RequirementHolder<T, V, R>(dataTypeClass, getter, requirement, failureStatus) {
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

	class AnySlotRequirementHolder<T: RecipeEnvironment>(
		requirement: ItemRequirement,
		failureStatus: Component?
	) : RequirementHolder<T, ItemStack?, ItemRequirement>(
		(ItemStack::class.java as Class<ItemStack?>),
		{ it.getInputItems().firstOrNull { inputItem -> requirement.matches(inputItem) } },
		requirement,
		failureStatus
	) {
		override fun consume(environment: T) {
			val item = getter.invoke(environment) ?: return
			requirement.consume(item, environment)
		}
	}

	companion object {
		inline fun <T: RecipeEnvironment, reified V: Any?, R: Consumable<V, T>> simpleConsumable(
			noinline getter: (T) -> V,
			requirement: R,
			failureStatus: Component? = null
		): RequirementHolder<T, V, R> = RequirementHolder(V::class.java, getter, requirement, failureStatus)

		inline fun <T: RecipeEnvironment, reified V: Any?, R: RecipeRequirement<V>> itemConsumable(
			noinline getter: (T) -> V,
			requirement: R,
			noinline slotModificationWrapper: (T) -> SlotModificationWrapper,
			failureStatus: Component? = null
		): RequirementHolder<T, V, R> = BundledRequirementHolder(V::class.java, getter, requirement, slotModificationWrapper, failureStatus)

		fun <T: InventoryResultEnvironment> anySlot(
			requirement: ItemRequirement,
			failureStatus: Component? = null
		): AnySlotRequirementHolder<T> = AnySlotRequirementHolder(requirement, failureStatus)
	}
}
