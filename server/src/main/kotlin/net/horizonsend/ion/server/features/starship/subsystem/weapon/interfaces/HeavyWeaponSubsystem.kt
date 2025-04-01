package net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces

import net.horizonsend.ion.server.configuration.StarshipWeapons.StarshipHeavyWeaponBalancing

interface HeavyWeaponSubsystem {
	val balancing: StarshipHeavyWeaponBalancing<*>
	val boostChargeNanos: Long
}
