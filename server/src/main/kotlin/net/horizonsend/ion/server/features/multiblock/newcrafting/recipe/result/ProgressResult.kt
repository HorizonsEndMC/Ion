package net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.result

import net.horizonsend.ion.server.features.multiblock.newcrafting.input.FurnaceEnviornment
import org.bukkit.inventory.ItemStack
import java.time.Duration

class ProgressResult(val duration: Duration, val resultItem: ItemStack) : RecipeResult<FurnaceEnviornment> {
	override fun verifySpace(input: FurnaceEnviornment): Boolean {
		val resultOccupant = input.furnaceInventory.result ?: return true
		if (resultOccupant.isEmpty) return true
		if (!resultOccupant.isSimilar(resultItem)) return false

		val maxStackSize = resultItem.maxStackSize
		return resultItem.amount + resultItem.amount <= maxStackSize
	}

	override fun execute(input: FurnaceEnviornment) {
		val complete = input.progressManager.addProgress(duration)
		if (!complete) return

		val current = input.furnaceInventory.result
		if (current == null) {
			input.furnaceInventory.result = resultItem
			return
		}

		current.amount += resultItem.amount
	}
}
