package net.starlegacy.feature.starship.subsystem

import net.starlegacy.feature.multiblock.misc.MagazineMultiblock
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.util.rightFace
import org.bukkit.block.Sign
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

class MagazineSubsystem(starship: ActiveStarship, sign: Sign, multiblock: MagazineMultiblock) :
	AbstractMultiblockSubsystem<MagazineMultiblock>(starship, sign, multiblock) {
	fun isAmmoAvailable(itemStack: ItemStack): Boolean {
		val inventory = getInventory()
			?: return false

		return inventory.containsAtLeast(itemStack, itemStack.amount)
	}

	fun tryConsumeAmmo(itemStack: ItemStack): Boolean {
		val inventory = getInventory()
			?: return false

		if (!inventory.containsAtLeast(itemStack, itemStack.amount)) {
			return false
		}

		// clone bc:
		// "It is known that in some implementations this method will also set the
		// inputted argument amount to the number of that item not removed from slots."
		// - javadoc for this method
		inventory.removeItemAnySlot(itemStack.clone())
		return true
	}

	private fun getInventory(): Inventory? {
		if (!isIntact()) {
			return null
		}

		val inventoryHolder = starship.world
			.getBlockAtKey(pos.toBlockKey())
			.getRelative(face)
			.getRelative(face.rightFace)
			.state as? InventoryHolder
			?: return null

		return inventoryHolder.inventory
	}
}
