package net.horizonsend.ion.server

import net.horizonsend.ion.common.annotations.ConfigurationName
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
@ConfigurationName("shared/balancing")
data class BalancingConfiguration(
	val energyWeapons: EnergyWeaponBalancing = EnergyWeaponBalancing()
) {
	@ConfigSerializable
	data class EnergyWeaponBalancing(
		val pistol: WeaponBalancing = WeaponBalancing(),
		val rifle: WeaponBalancing = WeaponBalancing(),
		val sniper: WeaponBalancing = WeaponBalancing()
	) {
		@ConfigSerializable
		data class WeaponBalancing(
			val shotSize: Float = 1f,
			val iterationsPerTick: Int = 4,
			val distancePerIteration: Double = 1.0
		)
	}
}