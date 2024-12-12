package net.horizonsend.ion.server.features.starship.subsystem.checklist

import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.GAS_CANISTER_HYDROGEN
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.customItem
import net.horizonsend.ion.server.features.multiblock.type.misc.FuelTankMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.AbstractMultiblockSubsystem
import org.bukkit.block.Sign
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import kotlin.math.min

class FuelTankSubsystem(starship: ActiveStarship, sign: Sign, multiblock: FuelTankMultiblock) :
	AbstractMultiblockSubsystem<FuelTankMultiblock>(starship, sign, multiblock) {

	var fuelAvailable = true; private set
	var ticks = 0

	/**
	 * Searches the inventory and tries to remove the remaining amount of fuel
	 *
	 * Returns the amount of fuel consumed
	 **/
	fun tryConsumeFuel(toConsume: Int): Int {
		var remaining = toConsume

		val inventory = getInventory() ?: return 0
		if (inventory.isEmpty) return 0

		val fuelCanisters = inventory.filter { item: ItemStack? ->
			item?.customItem == GAS_CANISTER_HYDROGEN
		}

		if (fuelCanisters.isEmpty()) return 0

		val byFuel = fuelCanisters.map { it to GAS_CANISTER_HYDROGEN.getFill(it) }.sortedByDescending { it.second }

		for ((itemStack, fuelAmount) in byFuel) {
			val toRemove = min(remaining, fuelAmount)

			GAS_CANISTER_HYDROGEN.setFill(itemStack, fuelAmount - toRemove)
			remaining -= toRemove

			if (remaining == 0) break
		}

		if (remaining >= 0) fuelAvailable = false
		return toConsume - remaining
	}

	private fun getInventory(): Inventory? {
		if (!isIntact()) {
			return null
		}

		val inventoryHolder = starship.world
			.getBlockAtKey(pos.toBlockKey())
			.getRelative(face)
			.getRelative(0, -1, 0)
			.state as? InventoryHolder
			?: return null

		return inventoryHolder.inventory
	}
}
