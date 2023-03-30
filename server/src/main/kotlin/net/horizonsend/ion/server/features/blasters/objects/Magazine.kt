package net.horizonsend.ion.server.features.blasters.objects

import net.horizonsend.ion.server.configuration.BalancingConfiguration
import net.kyori.adventure.text.Component
import org.bukkit.Material
import java.util.function.Supplier

abstract class Magazine<T : BalancingConfiguration.EnergyWeapon.AmmoStorageBalancing>(
	identifier: String,

	material: Material,
	customModelData: Int,
	displayName: Component,

	private val balancingSupplier: Supplier<T>
) : AmmunitionHoldingItem(identifier, material, customModelData, displayName) {
	val balancing get() = balancingSupplier.get()
	override fun getMaximumAmmunition(): Int = balancing.capacity
	override fun getTypeMagazine(): String = ""
	override fun getTypeRefill(): String = balancing.refillType
	override fun getAmmoPerRefill(): Int = balancing.ammoPerRefill
}
