package net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces

import org.bukkit.inventory.ItemStack

interface AmmoConsumingWeaponSubsystem {
	fun getRequiredAmmo(): ItemStack
}
