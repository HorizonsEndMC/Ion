package net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.requirement

import net.horizonsend.ion.server.features.multiblock.newcrafting.input.RecipeEnviornment

class RequirementHolder<T: RecipeEnviornment, V: Any?>(val dataTypeClass: Class<V>, val getter: (T) -> V, val requirement: RecipeRequirement<V>) {
	fun checkRequirement(enviornment: T): Boolean {
		val resourceValue = getter.invoke(enviornment)
		return requirement.ensureAvailable(resourceValue)
	}

	companion object {
		inline fun <T: RecipeEnviornment, reified V: Any?> of(
			noinline getter: (T) -> V,
			requirement: RecipeRequirement<V>
		): RequirementHolder<T, V> = RequirementHolder(V::class.java, getter, requirement)
	}
}
