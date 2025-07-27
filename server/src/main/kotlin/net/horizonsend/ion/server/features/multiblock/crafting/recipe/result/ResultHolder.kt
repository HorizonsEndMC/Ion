package net.horizonsend.ion.server.features.multiblock.crafting.recipe.result

import net.horizonsend.ion.server.features.multiblock.crafting.input.ItemResultEnviornment
import net.horizonsend.ion.server.features.multiblock.crafting.input.RecipeEnviornment
import net.horizonsend.ion.server.features.multiblock.entity.type.ProgressMultiblock
import net.horizonsend.ion.server.features.multiblock.entity.type.ProgressMultiblock.Companion.formatProgress
import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.block.Furnace
import kotlin.math.roundToInt

abstract class ResultHolder<E: RecipeEnviornment, R: RecipeResult<E>>(val result: R) {
	fun verifySpace(enviornment: E) = result.verifySpace(enviornment)

	/**
	 * Builds the result of the recipe in the result enviornment. Do not directly set the results in this function, it may result in duplications if errors arise elsewhere
	 **/
	abstract fun buildTransaction(input: E, resultEnviornment: ResultExecutionEnviornment<E>)

	private val callbacks = mutableListOf<(E, RecipeExecutionResult?) -> Unit>()

	fun executeCallbacks(enviornment: E, result: RecipeExecutionResult?) {
		callbacks.forEach { t -> t.invoke(enviornment, result) }
	}

	fun withCallback(callback: (E, RecipeExecutionResult?) -> Unit): ResultHolder<E, R> {
		callbacks.add(callback)
		return this
	}

	fun playSound(sound: Sound, requireSuccess: Boolean): ResultHolder<E, R> {
		callbacks.add { enviornment: E, result: RecipeExecutionResult? ->
			if (requireSuccess && result is RecipeExecutionResult.SuccessExecutionResult) enviornment.playSound(sound)
		}

		return this
	}

	fun updateFurnace(): ResultHolder<E, R> {
		callbacks.add { enviornment: E, executionResult: RecipeExecutionResult? ->
			val furnace = enviornment.multiblock.getInventory(0, 0, 0)?.holder as? Furnace ?: return@add

			// Extinguish the furnace if a failure
			if (executionResult is RecipeExecutionResult.FailureExecutionResult) {
				furnace.burnTime = 0
				furnace.update()

				return@add
			}

			if (executionResult !is RecipeExecutionResult.Success) return@add

			// If progress is made on a recipe, update the furnace arrow to reflect
			if (result is WarmupResult<*> && executionResult is RecipeExecutionResult.ProgressExecutionResult) {
				furnace.burnTime = (200 - (executionResult.newProgress * 200).roundToInt()).toShort()
			} else if (enviornment.multiblock is TickedMultiblockEntityParent) {
				furnace.burnTime = (enviornment.multiblock as TickedMultiblockEntityParent).tickingManager.interval.toShort()
			}

			furnace.update()
		}

		return this
	}

	fun updateProgressText(): ResultHolder<E, R> {
		callbacks.add { enviornment: E, result: RecipeExecutionResult? ->
			if (result !is RecipeExecutionResult.ProgressExecutionResult) return@add

			val entity = enviornment.multiblock
			if (entity !is StatusMultiblockEntity) return@add
			if (entity !is ProgressMultiblock) return@add

			entity.setStatus(formatProgress(NamedTextColor.BLUE, result.newProgress))
		}

		return this
	}

	fun addCooldown(ticks: Int, requireSuccess: Boolean): ResultHolder<E, R> {
		callbacks.add { enviornment: E, result: RecipeExecutionResult? ->
			if (requireSuccess && result !is RecipeExecutionResult.SuccessExecutionResult) return@add
			val entity = enviornment.multiblock
			if (entity !is TickedMultiblockEntityParent) return@add

			entity.tickingManager.sleepForTicks(ticks)
		}

		return this
	}

	companion object {
		fun <T: ItemResultEnviornment> of(result: ItemResult<T>) = ItemResultHolder(result)
		//TODO more results
	}

	class ItemResultHolder<E: ItemResultEnviornment, R: ItemResult<E>>(result: R) : ResultHolder<E, R>(result) {
		override fun buildTransaction(enviornment: E, resultEnviornment: ResultExecutionEnviornment<E>) {
			result.buildTransaction(enviornment, resultEnviornment)
		}
	}
}
