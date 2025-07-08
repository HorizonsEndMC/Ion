package net.horizonsend.ion.server.features.multiblock.crafting.recipe.result

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.FluidTypeKeys
import net.horizonsend.ion.server.features.multiblock.crafting.input.FluidMultiblockEnviornment
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.fluids.FluidType
import net.kyori.adventure.text.Component

class FluidResult<E: FluidMultiblockEnviornment>(val storeName: String, val type: IonRegistryKey<FluidType, out FluidType>, val amount: Double) : RecipeResult<E> {
	override fun verifySpace(enviornment: E): Boolean {
		return enviornment.fluidStore.getNamedStorage(storeName)?.canAdd(FluidStack(type.getValue(), amount)) == true
	}

	val resultConsumer: (E) -> RecipeExecutionResult = factory@{
		val store = it.fluidStore.getNamedStorage(storeName) ?: return@factory RecipeExecutionResult.FailureExecutionResult(Component.text("Store $storeName Missing!"))
		store.setFluidType(type.getValue())
		store.addAmount(amount)

		RecipeExecutionResult.SuccessExecutionResult
	}

	companion object {
		fun <E : FluidMultiblockEnviornment> empty(storeName: String): FluidResult<E> = FluidResult(storeName, FluidTypeKeys.EMPTY, 0.0)
	}
}
