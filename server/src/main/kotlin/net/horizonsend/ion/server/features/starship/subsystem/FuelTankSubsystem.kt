package net.horizonsend.ion.server.features.starship.subsystem

import net.horizonsend.ion.server.features.customitems.CustomItems.REFINED_FUEL
import net.horizonsend.ion.server.features.multiblock.misc.FuelTankMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import org.bukkit.block.Sign
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

class FuelTankSubsystem(starship: ActiveStarship, sign: Sign, multiblock: FuelTankMultiblock) :
		AbstractMultiblockSubsystem<FuelTankMultiblock>(starship, sign, multiblock) {
	fun isFuelAvailable(): Boolean {
		val inventory = getInventory()
				?: return false

		return inventory.containsAtLeast(REFINED_FUEL.constructItemStack(), 1)
	}

	fun tryConsumeFuel(): Boolean {

		val inventory = getInventory()
				?: return false

		if (!inventory.containsAtLeast(REFINED_FUEL.constructItemStack(), 1)) {
			return false
		}


		inventory.removeItemAnySlot(REFINED_FUEL.constructItemStack().asQuantity(1))
		return true
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
