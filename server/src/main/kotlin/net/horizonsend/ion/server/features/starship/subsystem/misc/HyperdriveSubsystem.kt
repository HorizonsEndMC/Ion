package net.horizonsend.ion.server.features.starship.subsystem.misc

import net.horizonsend.ion.server.features.custom.items.CustomItems.CHETHERITE
import net.horizonsend.ion.server.features.custom.items.CustomItems.customItem
import net.horizonsend.ion.server.features.multiblock.hyperdrive.HyperdriveMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.hyperspace.Hyperspace
import net.horizonsend.ion.server.features.starship.subsystem.AbstractMultiblockSubsystem
import org.bukkit.block.Hopper
import org.bukkit.block.Sign
import org.bukkit.inventory.ItemStack
import kotlin.math.min

class HyperdriveSubsystem(starship: ActiveStarship, sign: Sign, multiblock: HyperdriveMultiblock) :
	AbstractMultiblockSubsystem<HyperdriveMultiblock>(starship, sign, multiblock) {
	private fun getHoppers(): Set<Hopper> {
		return multiblock.getHoppers(starship.world.getBlockAtKey(pos.toBlockKey()).getState(false) as Sign)
	}

	fun hasFuel(): Boolean = getHoppers().all { hopper ->
		hopper.inventory.asSequence()
			.filterNotNull()
			.filter(::isHypermatter)
			.sumOf { it.amount } >= Hyperspace.HYPERMATTER_AMOUNT
	}

	fun useFuel(): Unit = getHoppers().forEach { hopper ->
		var remaining = Hyperspace.HYPERMATTER_AMOUNT
		for (item: ItemStack? in hopper.inventory) {
			if (item == null) {
				continue
			}

			if (!isHypermatter(item)) {
				continue
			}
			val amount = min(item.amount, remaining)
			item.amount -= amount
			remaining -= amount
			if (remaining == 0) {
				break
			}
		}
		check(remaining == 0) { "Hopper at ${hopper.location} did not have ${Hyperspace.HYPERMATTER_AMOUNT} chetherite!" }
	}

	fun restoreFuel(): Unit = getHoppers().forEach { hopper ->
		hopper.inventory.addItem(CHETHERITE.constructItemStack().asQuantity(Hyperspace.HYPERMATTER_AMOUNT))
	}

	private fun isHypermatter(item: ItemStack) = item.customItem == CHETHERITE
}
