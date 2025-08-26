package net.horizonsend.ion.server.features.multiblock.crafting.recipe.result

import net.horizonsend.ion.server.core.registration.keys.FluidTypeKeys
import net.horizonsend.ion.server.features.multiblock.crafting.input.FluidMultiblockEnviornment
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.kyori.adventure.text.Component

class FluidResult<E: FluidMultiblockEnviornment>(val storeName: String, val stack: FluidStack) : RecipeResult<E> {
	override fun verifySpace(enviornment: E): Boolean {
		val storage = enviornment.fluidStore.getNamedStorage(storeName) ?: return false
		return storage.canAdd(stack) && storage.hasRoomFor(stack)
	}

	val resultConsumer: (E) -> RecipeExecutionResult = factory@{ env ->
		val store = env.fluidStore.getNamedStorage(storeName) ?: return@factory RecipeExecutionResult.FailureExecutionResult(Component.text("Store $storeName Missing!"))

		store.addFluid(stack.clone(), env.multiblock.location)

		RecipeExecutionResult.SuccessExecutionResult
	}

	companion object {
		fun <E : FluidMultiblockEnviornment> empty(storeName: String): FluidResult<E> = FluidResult(storeName, FluidStack(FluidTypeKeys.EMPTY, 0.0))
	}
}
