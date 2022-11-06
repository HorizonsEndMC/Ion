package net.horizonsend.ion.server

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class BalancingConfiguration(
	val energyWeapons: EnergyWeaponBalancing = EnergyWeaponBalancing()
) {
	@ConfigSerializable
	data class EnergyWeaponBalancing(
		val pistol: SingleShotWeaponBalancing = SingleShotWeaponBalancing(0.5f, 2, 2.0, 15, 6, 3.0, false, 2f, 15),
		val rifle: SingleShotWeaponBalancing = SingleShotWeaponBalancing(0.75f, 3, 2.0, 30, 12, 4.0, false, 1f, 30),
		val sniper: SingleShotWeaponBalancing = SingleShotWeaponBalancing(1f, 4, 2.0, 120, 40, 12.0, true, 0f, 5),

		val shotGun: MultiShotWeaponBalancing = MultiShotWeaponBalancing(
			0.1f,
			4,
			2.0,
			120,
			80,
			5.0,
			false,
			0f,
			5,
			0.75,
			0.0,
			2
		)

	) {
		@ConfigSerializable
		data class SingleShotWeaponBalancing(
			val shotSize: Float,
			val iterationsPerTick: Int,
			val distancePerIteration: Double,
			val reload: Int,
			val timeBetweenShots: Int,
			val damage: Double,
			val shouldPassThroughEntities: Boolean,
			val pitch: Float,
			val magazineSize: Int
		)

		@ConfigSerializable
		data class MultiShotWeaponBalancing(
			val shotSize: Float,
			val iterationsPerTick: Int,
			val distancePerIteration: Double,
			val reload: Int,
			val timeBetweenShots: Int,
			val damage: Double,
			val shouldPassThroughEntities: Boolean,
			val pitch: Float,
			val shotCount: Int,
			val offsetmax: Double,
			val delay: Double,
			val magazineSize: Int
		)
	}

}