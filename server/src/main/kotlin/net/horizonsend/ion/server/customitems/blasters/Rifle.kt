package net.horizonsend.ion.server.customitems.blasters

import net.horizonsend.ion.server.BalancingConfiguration
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.customitems.CustomItemList
import net.horizonsend.ion.server.customitems.blasters.constructors.AmmoRequiringSingleShotBlaster
import net.horizonsend.ion.server.customitems.blasters.constructors.Magazine

@Suppress("Unused")
object Rifle : AmmoRequiringSingleShotBlaster() {
	override val requiredAmmo: Magazine = StandardMagazine
	override val customItemlist: CustomItemList = CustomItemList.RIFLE

	override val singleShotWeaponBalancing: BalancingConfiguration.EnergyWeaponBalancing.SingleShotWeaponBalancing
		get() = IonServer.Ion.balancing.energyWeapons.rifle
}