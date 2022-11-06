package net.horizonsend.ion.server.customitems.blasters

import net.horizonsend.ion.server.BalancingConfiguration.EnergyWeaponBalancing.SingleShotWeaponBalancing
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.customitems.CustomItemList
import net.starlegacy.feature.misc.CustomItems
import org.bukkit.inventory.ItemStack

@Suppress("Unused")
object Sniper : AmmoRequiringSingleShotBlaster() {
	override val requiredAmmo: ItemStack = CustomItems.BATTERY_LARGE.singleItem()
	override val customItemlist: CustomItemList = CustomItemList.SNIPER

	override val singleShotWeaponBalancing: SingleShotWeaponBalancing
		get() = IonServer.Ion.balancing.energyWeapons.sniper
}