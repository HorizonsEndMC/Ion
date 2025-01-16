package net.horizonsend.ion.server.features.multiblock.newcrafting.recipe.result

import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.newcrafting.input.FurnaceEnviornment
import net.horizonsend.ion.server.features.multiblock.newcrafting.util.SlotModificationWrapper
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.inventory.ItemStack
import java.time.Duration

class ProgressResult(val duration: Duration, val resultItem: ItemStack) : ItemResult<FurnaceEnviornment> {
	override fun verifySpace(enviornment: FurnaceEnviornment): Boolean {
		val resultOccupant = enviornment.furnaceInventory.result ?: return true
		if (resultOccupant.isEmpty) return true
		if (!resultOccupant.isSimilar(resultItem)) return false

		val maxStackSize = resultItem.maxStackSize
		return resultItem.amount + resultItem.amount <= maxStackSize
	}

	override fun shouldConsumeIngredients(enviornment: FurnaceEnviornment): Boolean {
		return enviornment.progressManager.wouldComplete(duration)
	}

	override fun execute(enviornment: FurnaceEnviornment, slotModificationWrapper: SlotModificationWrapper) {
		val complete = enviornment.progressManager.addProgress(duration)

		val multiblock = enviornment.multiblock
		if (multiblock is StatusMultiblockEntity) multiblock.setStatus(enviornment.progressManager.formatProgress(NamedTextColor.RED))

		if (!complete) return

		if (multiblock is StatusMultiblockEntity) multiblock.setStatus(enviornment.progressManager.formatProgress(NamedTextColor.GREEN))

		enviornment.progressManager.reset()
		slotModificationWrapper.addToSlot(resultItem)
	}

	override fun getResultItem(enviornment: FurnaceEnviornment): ItemStack? {
		if (!enviornment.progressManager.wouldComplete(duration)) return null
		return resultItem
	}
}
