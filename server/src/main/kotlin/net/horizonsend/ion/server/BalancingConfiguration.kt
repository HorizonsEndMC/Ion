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
			false,
			true
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
			0.25f,
			10,
			0.05,
			0,
			2.0,
			4.0,
			false,
			25.0,
			0.25,
			true,
			false,
			0.0f,
			20,
			120,
			4,
			2,
			0.25f
		)
	) {
		@ConfigSerializable
		data class SingleShotWeaponBalancing(
			val shotSize: Float, // Bullet hit box size, also effects particle size.
			val speed: Double, // Bullet speed
			val reload: Int, // Reload time in ticks
			val timeBetweenShots: Int, // Rate of fire in tick delay between shots
			val damage: Double, // Base damage before falloff
			val damageFalloffMultiplier: Double, // Use 0.0 for linear falloff. See https://www.desmos.com/calculator/qfgdwahmdb.
			val shouldPassThroughEntities: Boolean, // Will ignore entities and hit blocks.
			val pitch: Float, // Bullet noise pitch
			val magazineSize: Int, // Amount of ammo gun can hold
			val shouldBypassHitTicks: Boolean, // Will bypass minecraft's invulnerability after damage
			val range: Double, // Max weapon range, also used in falloff.
			val recoil: Float, // degrees
			val packetsPerShot: Int, // Amount of times to apply recoil, provides a smoother recoil
			val shouldHeadshot: Boolean, // Will try for headshots
			val shouldAkimbo: Boolean = false // Allows firing with both hands
		)

		@ConfigSerializable
		data class MultiShotWeaponBalancing(
			val shotSize: Float, // Bullet hit box size, also effects particle size.
			val shotCount: Int, // Number of shots to fire
			val offsetMax: Double, // Spread between the multiple shots
			val delay: Int, // Delay between the multiple shots fire
			val speed: Double, // Bullet speed
			val damage: Double, // Base damage before falloff
			val shouldHeadshot: Boolean, // Will try for headshots
			val range: Double, // Max weapon range, also used in falloff.
			val damageFalloffMultiplier: Double, // Use 0.0 for linear falloff. See https://www.desmos.com/calculator/qfgdwahmdb.
			val shouldBypassHitTicks: Boolean, // Will bypass minecraft's invulnerability after damage
			val shouldPassThroughEntities: Boolean, // Will ignore entities and hit blocks.
			val pitch: Float, // Bullet noise pitch
			val timeBetweenShots: Int, // Rate of fire in tick delay between shots
			val reload: Int, // Reload time in ticks
			val magazineSize: Int, // Amount of ammo gun can hold
			val packetsPerShot: Int, // Amount of times to apply recoil, provides a smoother recoil
			val recoil: Float, // degrees
			val shouldAkimbo: Boolean = false // Allows firing with both hands
		)
	}
}