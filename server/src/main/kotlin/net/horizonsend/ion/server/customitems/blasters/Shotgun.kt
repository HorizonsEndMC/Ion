package net.horizonsend.ion.server.customitems.blasters

import net.horizonsend.ion.server.BalancingConfiguration
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.customitems.CustomItemList
import net.horizonsend.ion.server.customitems.blasters.constructors.AmmoRequiringMultiShotBlaster
import net.horizonsend.ion.server.customitems.blasters.constructors.Magazine

object Shotgun : AmmoRequiringMultiShotBlaster() {
	override val requiredAmmo: Magazine = StandardMagazine
	override val multiShotWeaponBalancing: BalancingConfiguration.EnergyWeaponBalancing.MultiShotWeaponBalancing = IonServer.Ion.balancing.energyWeapons.shotGun
	override val customItemlist: CustomItemList = CustomItemList.SHOTGUN
}