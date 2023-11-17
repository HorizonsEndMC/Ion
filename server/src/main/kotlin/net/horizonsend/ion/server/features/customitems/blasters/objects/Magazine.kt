package net.horizonsend.ion.server.features.customitems.blasters.objects

import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration
import net.kyori.adventure.text.Component
import org.bukkit.Material
import java.util.function.Supplier

abstract class Magazine<T : PVPBalancingConfiguration.EnergyWeapons.AmmoStorageBalancing>(
	identifier: String,

	material: Material,
	customModelData: Int,
	displayName: Component,

	private val balancingSupplier: Supplier<T>
) : AmmunitionHoldingItem(identifier, material, customModelData, displayName) {
	val balancing get() = balancingSupplier.get()
	override fun getMaximumAmmunition(): Int = balancing.capacity
	override fun getTypeRefill(): String = balancing.refillType
	override fun getAmmoPerRefill(): Int = balancing.ammoPerRefill
	override fun getConsumesAmmo(): Boolean = true // dummy value to satisfy the abstract class
}
