package net.horizonsend.ion.server.customitems.blasters

import net.horizonsend.ion.server.BalancingConfiguration
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.customitems.CustomItemList
import net.horizonsend.ion.server.customitems.blasters.constructors.AmmoRequiringMultiShotBlaster
import net.starlegacy.feature.misc.CustomItems
import org.bukkit.inventory.ItemStack

object Shotgun : AmmoRequiringMultiShotBlaster() {
	override val requiredAmmo: ItemStack = CustomItems.BATTERY_MEDIUM.singleItem()
	override val multiShotWeaponBalancing: BalancingConfiguration.EnergyWeaponBalancing.MultiShotWeaponBalancing = IonServer.Ion.balancing.energyWeapons.shotGun
	override val customItemlist: CustomItemList = CustomItemList.SHOTGUN
}