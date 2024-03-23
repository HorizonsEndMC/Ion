package net.horizonsend.ion.server.features.starship.subsystem

import net.horizonsend.ion.server.features.customitems.CustomItems.GAS_CANISTER_HYDROGEN
import net.horizonsend.ion.server.features.customitems.GasCanister
import net.horizonsend.ion.server.features.gas.Gasses.EMPTY_CANISTER
import net.horizonsend.ion.server.features.multiblock.misc.FuelTankMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import org.bukkit.block.Sign
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

class FuelTankSubsystem(starship: ActiveStarship, sign: Sign, multiblock: FuelTankMultiblock) :
		AbstractMultiblockSubsystem<FuelTankMultiblock>(starship, sign, multiblock) {
      
      
	fun isFuelAvailable(): Boolean {
		val inventory = getInventory()
				?: return false
    
		return inventory.containsAtLeast(GAS_CANISTER_HYDROGEN.constructItemStack(), 1)
	}

	fun tryConsumeFuel(
	fuelType : GasCanister
	): Boolean {
		val inventory = getInventory()
				?: return false

		if (!inventory.containsAtLeast(GAS_CANISTER_HYDROGEN.constructItemStack(), 1)) {
			return false
		}
		else {
			for (item in inventory) {
				if (item == GAS_CANISTER_HYDROGEN) {
					val fuelFill = fuelType.getFill(item)
					if (fuelFill <= 0) {
						item.subtract()
						inventory.addItem(EMPTY_CANISTER)
					}
					if (fuelFill - 60 <= 0) {
						item.subtract()
						inventory.addItem(EMPTY_CANISTER)
					}
					else {
						fuelType.setFill(item, fuelFill - 60)
						return true
					}
				}
			}
		}


		return false
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
