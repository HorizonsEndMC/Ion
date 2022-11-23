package net.horizonsend.ion.server.customitems.blasters.constructors

import net.horizonsend.ion.server.BalancingConfiguration

abstract class MultiShotBlaster : Blaster() {
	abstract val multiShotWeaponBalancing: BalancingConfiguration.EnergyWeaponBalancing.MultiShotWeaponBalancing
}