package net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces

import net.horizonsend.ion.server.configuration.starship.StarshipHeavyWeaponBalancing
import kotlin.time.Duration

interface CommandBurstSubsystem {
	val balancing: StarshipHeavyWeaponBalancing<*>
	val boostChargeNanos: Long
	val range: Double
	val duration: Long
}
