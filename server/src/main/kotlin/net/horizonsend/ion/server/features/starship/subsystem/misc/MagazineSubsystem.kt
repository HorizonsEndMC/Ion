package net.horizonsend.ion.server.features.starship.subsystem.misc

import net.horizonsend.ion.server.features.multiblock.type.misc.AbstractMagazineMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.features.starship.subsystem.AbstractMultiblockSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AmmoConsumingWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.leftFace
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import org.bukkit.block.Sign
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

class MagazineSubsystem(starship: ActiveStarship, sign: Sign, multiblock: AbstractMagazineMultiblock) : AbstractMultiblockSubsystem<AbstractMagazineMultiblock>(starship, sign, multiblock) {
	fun isAmmoAvailable(subsystem: AmmoConsumingWeaponSubsystem): Boolean {
		if (starship.controller is AIController) return true
		val inventory = getInventory() ?: return false
		return inventory.filterNotNull().any(subsystem::isRequiredAmmo)
	}

	fun tryConsumeAmmo(subsystem: AmmoConsumingWeaponSubsystem): Boolean {
		if (starship.controller is AIController) return true
		val inventory = getInventory()
			?: return false

		val ammoItem = inventory.filterNotNull().firstOrNull(subsystem::isRequiredAmmo) ?: return false
		subsystem.consumeAmmo(ammoItem)

		return true
	}

	private fun getInventory(): Inventory? {
		if (!isIntact()) {
			return null
		}

		val inventoryHolder = if (!multiblock.mirrored) {
			starship.world
				.getBlockAtKey(pos.toBlockKey())
				.getRelative(face)
				.getRelative(face.rightFace)
				.state as? InventoryHolder
				?: return null
		} else {
			starship.world
				.getBlockAtKey(pos.toBlockKey())
				.getRelative(face)
				.getRelative(face.leftFace)
				.state as? InventoryHolder
				?: return null
		}

		return inventoryHolder.inventory
	}
}
