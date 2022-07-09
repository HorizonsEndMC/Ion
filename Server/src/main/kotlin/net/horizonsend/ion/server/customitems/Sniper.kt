package net.horizonsend.ion.server.customitems

import net.horizonsend.ion.server.BalancingConfiguration.EnergyWeaponBalancing.WeaponBalancing
import net.horizonsend.ion.server.IonServer

@Suppress("Unused")
class Sniper : SingleShotBlaster() {
	override val customModelData: Int = 3

	override val weaponBalancing: WeaponBalancing
		get() = IonServer.balancingConfiguration.energyWeapons.sniper
}