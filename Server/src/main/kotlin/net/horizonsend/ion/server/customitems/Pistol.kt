package net.horizonsend.ion.server.customitems

import net.horizonsend.ion.server.BalancingConfiguration.EnergyWeaponBalancing.WeaponBalancing
import net.horizonsend.ion.server.IonServer

@Suppress("Unused")
class Pistol : SingleShotBlaster() {
	override val customModelData: Int = 1

	override val weaponBalancing: WeaponBalancing
		get() = IonServer.balancingConfiguration.energyWeapons.pistol
}