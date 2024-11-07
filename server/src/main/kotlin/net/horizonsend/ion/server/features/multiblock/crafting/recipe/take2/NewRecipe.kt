package net.horizonsend.ion.server.features.multiblock.crafting.recipe.take2

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.minecraft.commands.execution.ExecutionContext
import kotlin.reflect.KClass

abstract class NewRecipe(val multiblockEntity: KClass<out MultiblockEntity>) {
	abstract fun checkSpaceAvailable(context: ExecutionContext): Boolean

	abstract fun checkResourcesAvailable(context: ExecutionContext): Boolean

	fun checkExecution(context: ExecutionContext): Boolean {
		if (!checkResourcesAvailable(context)) return false
		if (!checkSpaceAvailable(context)) return false
		//TODO
		return true
	}

	abstract fun execute(context: ExecutionContext)

	class ExecutionContext(val entity: MultiblockEntity)
}
