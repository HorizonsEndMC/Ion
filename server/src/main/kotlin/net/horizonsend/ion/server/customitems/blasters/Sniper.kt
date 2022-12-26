package net.horizonsend.ion.server.customitems.blasters

import net.horizonsend.ion.server.BalancingConfiguration.EnergyWeaponBalancing.SingleShotWeaponBalancing
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.customitems.CustomItemList
import net.horizonsend.ion.server.customitems.blasters.constructors.AmmoRequiringSingleShotBlaster
import net.horizonsend.ion.server.customitems.blasters.constructors.Magazine

@Suppress("Unused")
object Sniper : AmmoRequiringSingleShotBlaster() {
	override val requiredAmmo: Magazine = StandardMagazine
	override val customItemlist: CustomItemList = CustomItemList.SNIPER

	override val singleShotWeaponBalancing: SingleShotWeaponBalancing
		get() = IonServer.Ion.balancing.energyWeapons.sniper
}