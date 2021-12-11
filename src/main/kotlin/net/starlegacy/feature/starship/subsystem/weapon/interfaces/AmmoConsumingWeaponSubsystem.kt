package net.starlegacy.feature.starship.subsystem.weapon.interfaces

import org.bukkit.inventory.ItemStack

interface AmmoConsumingWeaponSubsystem {
    fun getRequiredAmmo(): ItemStack
}
