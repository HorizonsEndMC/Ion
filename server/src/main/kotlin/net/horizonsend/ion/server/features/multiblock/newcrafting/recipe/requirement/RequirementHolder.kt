package net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.requirement

import net.horizonsend.ion.server.features.multiblock.newcrafting.input.RecipeEnviornment
import net.horizonsend.ion.server.features.multiblock.newcrafting.util.SlotModificationWrapper

open class RequirementHolder<T: RecipeEnviornment, V: Any?, out R: RecipeRequirement<V>>(val dataTypeClass: Class<V>, val getter: (T) -> V, val requirement: R) {
	fun checkRequirement(enviornment: T): Boolean {
		val resourceValue = getter.invoke(enviornment)
		return requirement.ensureAvailable(resourceValue)
	}

	open fun consume(enviornment: T) {
		if (requirement is Consumable<*, *>) {
			@Suppress("UNCHECKED_CAST")
			(requirement as Consumable<V, T>).consume(enviornment)
		}
	}

	class BundledRequirementHolder<T: RecipeEnviornment, V: Any?, R: RecipeRequirement<V>>(
		dataTypeClass: Class<V>,
		getter: (T) -> V,
		requirement: R,
		val slotModificationWrapper: (T) -> SlotModificationWrapper
	) : RequirementHolder<T, V, R>(dataTypeClass, getter, requirement) {
		override fun consume(enviornment: T) {
			val wrapper = slotModificationWrapper.invoke(enviornment)
			wrapper.removeFromSlot(1)
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
	}
}
