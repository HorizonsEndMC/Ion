package net.horizonsend.ion.server.features.custom.items.blasters

import net.horizonsend.ion.common.utils.text.ITALIC
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.objects.AmmunitionHoldingItem
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier

class Magazine<T : PVPBalancingConfiguration.EnergyWeapons.AmmoStorageBalancing>(
	identifier: String,

	override val material: Material,
	override val customModelData: Int,
	override val displayName: Component,

	private val balancingSupplier: Supplier<T>
) : CustomItem(identifier), AmmunitionHoldingItem {
	val balancing get() = balancingSupplier.get()
	override fun getMaximumAmmunition(): Int = balancing.capacity
	override fun getTypeRefill(): String = balancing.refillType
	override fun getAmmoPerRefill(): Int = balancing.ammoPerRefill
	override fun getConsumesAmmo(): Boolean = true // dummy value to satisfy the abstract class

	override fun constructItemStack(): ItemStack {
		val base = super.getFullItem()

		// Should always have lore
		val lore = base.itemMeta.lore()!!.toTypedArray()

		val refillTypeComponent = if (getConsumesAmmo()) Component.text()
			.decoration(ITALIC, false)
			.append(Component.text("Refill: ", NamedTextColor.GRAY))
			.append(Component.translatable(Material.matchMaterial(getTypeRefill())!!.translationKey(), NamedTextColor.AQUA))
			.build()
		else null

		return base.updateMeta {
			it.lore(mutableListOf(
				*lore,
				refillTypeComponent
			))
		}
	}
}
