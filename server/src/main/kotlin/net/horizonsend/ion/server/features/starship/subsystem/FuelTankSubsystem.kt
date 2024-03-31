package net.horizonsend.ion.server.features.starship.subsystem

import net.horizonsend.ion.server.features.customitems.CustomItems.GAS_CANISTER_HYDROGEN
import net.horizonsend.ion.server.features.customitems.CustomItems.customItem
import net.horizonsend.ion.server.features.multiblock.misc.FuelTankMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import org.bukkit.block.Sign
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import kotlin.math.min

class FuelTankSubsystem(starship: ActiveStarship, sign: Sign, multiblock: FuelTankMultiblock) :
	AbstractMultiblockSubsystem<FuelTankMultiblock>(starship, sign, multiblock) {

	var fuelAvailable = true; private set
	var ticks = 0

	override fun tick() {
		ticks++;

		if (ticks % 200 != 0) return

		fuelAvailable = tryConsumeFuel()
	}

	/**
	 * Searches the inventory and tries to remove the remaining amount of fuel
	 *
	 * Returns whether the fuel could be removed
	 **/
	fun tryConsumeFuel(): Boolean {
		val inventory = getInventory() ?: return false
		if (inventory.isEmpty) return false

		val fuelCanisters = inventory.filter { it.customItem == GAS_CANISTER_HYDROGEN }

		if (fuelCanisters.isEmpty()) return false;

		val byFuel = fuelCanisters.map { it to GAS_CANISTER_HYDROGEN.getFill(it) }.sortedByDescending { it.second }

		var remaining = FUEL_CONSUMPTION

		for ((itemStack, fuelAmount) in byFuel) {
			val toRemove = min(remaining, fuelAmount)

			GAS_CANISTER_HYDROGEN.setFill(itemStack, fuelAmount - toRemove)
			remaining -= toRemove

			if (remaining == 0) break
		}

		return remaining <= 0
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

	companion object {
		private const val FUEL_CONSUMPTION = 60
	}
}
