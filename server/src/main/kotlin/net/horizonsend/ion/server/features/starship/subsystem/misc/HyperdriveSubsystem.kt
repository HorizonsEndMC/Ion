package net.horizonsend.ion.server.features.starship.subsystem.misc

import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.CHETHERITE
import net.horizonsend.ion.server.core.registration.registries.CustomItemRegistry.Companion.customItem
import net.horizonsend.ion.server.data.migrator.DataMigrators
import net.horizonsend.ion.server.data.migrator.DataMigrators.migrateInventory
import net.horizonsend.ion.server.features.multiblock.type.starship.hyperdrive.HyperdriveMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.hyperspace.Hyperspace
import net.horizonsend.ion.server.features.starship.subsystem.AbstractMultiblockSubsystem
import org.bukkit.block.Sign
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import kotlin.math.min

open class HyperdriveSubsystem(starship: ActiveStarship, sign: Sign, multiblock: HyperdriveMultiblock) :
	AbstractMultiblockSubsystem<HyperdriveMultiblock>(starship, sign, multiblock) {
	fun getFuelInventories(): Set<InventoryHolder> {
		val sign = starship.world.getBlockAtKey(pos.toBlockKey()).getState(false) as? Sign ?: return emptySet()
		return multiblock.getFuelInventories(sign)
	}

	open fun hasFuel(): Boolean = getFuelInventories().all { inventory ->
		inventory.inventory.asSequence()
			.filterNotNull()
			.filter(::isHypermatter)
			.sumOf { it.amount } >= Hyperspace.getHyperMatterAmount(starship)
	}

	open fun useFuel(): Unit = getFuelInventories().forEach { inventory ->
		var remaining = Hyperspace.getHyperMatterAmount(starship)
		migrateInventory(inventory.inventory, DataMigrators.getVersions(0))

		for (item: ItemStack? in inventory.inventory) {
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
		check(remaining == 0) { "Inventory at ${inventory.inventory.location} did not have ${Hyperspace.getHyperMatterAmount(starship)} chetherite!" }
	}

	private fun isHypermatter(item: ItemStack) = item.customItem?.key == CHETHERITE
}
