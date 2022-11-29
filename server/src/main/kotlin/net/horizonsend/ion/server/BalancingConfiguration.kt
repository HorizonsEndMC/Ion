package net.horizonsend.ion.server

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class BalancingConfiguration(
	val energyWeapons: EnergyWeaponBalancing = EnergyWeaponBalancing()
) {
	@ConfigSerializable
	data class EnergyWeaponBalancing(
		val pistol: SingleShotWeaponBalancing = SingleShotWeaponBalancing(
			0.9f,
			2.0,
			15,
			6,
			3.0,
			0.0,
			false,
			2f,
			15,
			true,
			100.0,
			3.0f,
			5,
			false
		),
		val rifle: SingleShotWeaponBalancing = SingleShotWeaponBalancing(
			1f,
			2.0,
			15,
			10,
			4.0,
			0.0,
			false,
			1f,
			15,
			true,
			100.0,
			2.0f,
			5,
			true
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
			100.0,
			1.0f,
			1,
			false
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
			100.0,
			10.0f,
			5,
			true
		),

		val shotGun: MultiShotWeaponBalancing = MultiShotWeaponBalancing(
			1f,
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
			4,
			true,
			100.0,
			5.0f,
			10,
			false
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
			val range: Double,
			val recoil: Float, //degrees
			val packetsPerShot: Int,
			val shouldHeadshot: Boolean
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
			val range: Double,
			val recoil: Float, //degrees
			val packetsPerShot: Int,
			val shouldHeadshot: Boolean
		)
	}
}