package net.horizonsend.ion.server

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class BalancingConfiguration(
	val energyWeapons: EnergyWeaponBalancing = EnergyWeaponBalancing()
) {
	@ConfigSerializable
	data class EnergyWeaponBalancing(
		val pistol: SingleShotWeaponBalancing = SingleShotWeaponBalancing(
			0.5f,
			2.0,
			15,
			6,
			3.0,
			0.0,
			false,
			2f,
			15,
			true,
			100.0
		),
		val rifle: SingleShotWeaponBalancing = SingleShotWeaponBalancing(
			0.75f,
			2.0,
			15,
			10,
			4.0,
			0.0,
			false,
			1f,
			15,
			true,
			100.0
		),
		val autoRifle: SingleShotWeaponBalancing = SingleShotWeaponBalancing(
			0.5f,
			2.0,
			30,
			1,
			2.0,
			0.0,
			false,
			2f,
			30,
			true,
			100.0
		),
		val sniper: SingleShotWeaponBalancing = SingleShotWeaponBalancing(
			1f,
			2.0,
			120,
			40,
			12.0,
			0.0,
			true,
			0f,
			5,
			false,
			100.0
		),

		val shotGun: MultiShotWeaponBalancing = MultiShotWeaponBalancing(
			0.1f,
			2.0,
			120,
			80,
			5.0,
			0.0,
			false,
			0f,
			5,
			0.05,
			0.0,
			2,
			true,
			100.0
		)
	) {
		@ConfigSerializable
		data class SingleShotWeaponBalancing(
			val shotSize: Float,
			val speed: Double,
			val reload: Int,
			val timeBetweenShots: Int,
			val damage: Double,
			val damageFalloffMultiplier: Double, // Use 0.0 for linear falloff. See https://www.desmos.com/calculator/qfgdwahmdb.
			val shouldPassThroughEntities: Boolean,
			val pitch: Float,
			val magazineSize: Int,
			val shouldBypassHitTicks: Boolean,
			val range: Double
		)

		@ConfigSerializable
		data class MultiShotWeaponBalancing(
			val shotSize: Float,
			val speed: Double,
			val reload: Int,
			val timeBetweenShots: Int,
			val damage: Double,
			val damageFalloffMultiplier: Double,  // Use 0.0 for linear falloff. See https://www.desmos.com/calculator/qfgdwahmdb.
			val shouldPassThroughEntities: Boolean,
			val pitch: Float,
			val shotCount: Int,
			val offsetMax: Double,
			val delay: Double,
			val magazineSize: Int,
			val shouldBypassHitTicks: Boolean,
			val range: Double
		)
	}
}