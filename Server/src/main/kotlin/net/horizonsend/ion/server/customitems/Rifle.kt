package net.horizonsend.ion.server.customitems

import net.horizonsend.ion.server.BalancingConfiguration.EnergyWeaponBalancing.WeaponBalancing
import net.horizonsend.ion.server.IonServer

@Suppress("Unused")
class Rifle : SingleShotBlaster() {
	override val customModelData: Int = 2

	override val weaponBalancing: WeaponBalancing
		get() = IonServer.balancingConfiguration.energyWeapons.rifle
}