package net.horizonsend.ion.server.customitems.blasters.constructors

import net.horizonsend.ion.server.BalancingConfiguration

abstract class SingleShotBlaster : Blaster() {
	abstract val singleShotWeaponBalancing: BalancingConfiguration.EnergyWeaponBalancing.SingleShotWeaponBalancing
}