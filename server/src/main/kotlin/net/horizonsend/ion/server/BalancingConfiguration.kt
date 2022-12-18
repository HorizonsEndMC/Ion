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
		/**
		 * @param shotSize The type of feedback
		 * @param speed The feedback message, use "{index}" to insert variables into the message
		 * @param reload Variables to insert into the message
		 * @param timeBetweenShots Rate of fire in tick delay between shots
		 * @param damage Base damage before falloff
		 * @param damageFalloffMultiplier Use 0.0 for linear falloff. See https://www.desmos.com/calculator/qfgdwahmdb.
		 * @param shouldPassThroughEntities Will ignore entities and hit blocks.
		 * @param pitch Bullet noise pitch
		 * @param magazineSize Amount of ammo gun can hold
		 * @param shouldBypassHitTicks Will bypass minecraft's invulnerability after damage
		 * @param range Max weapon range, also used in falloff.
		 * @param recoil degrees
		 * @param packetsPerShot Amount of times to apply recoil, provides a smoother recoil
		 * @param shouldHeadshot Will try for headshots
		 * @param shouldAkimbo Allows firing with both hands
		 */
		@ConfigSerializable
		data class SingleShotWeaponBalancing(
			val shotSize: Float,
			val speed: Double,
			val reload: Int,
			val timeBetweenShots: Int,
			val damage: Double,
			val damageFalloffMultiplier: Double,
			val shouldPassThroughEntities: Boolean,
			val pitch: Float,
			val magazineSize: Int,
			val shouldBypassHitTicks: Boolean,
			val range: Double,
			val recoil: Float,
			val packetsPerShot: Int,
			val shouldHeadshot: Boolean,
			val shouldAkimbo: Boolean = false
		)

		/**
		 * @param shotSize The type of feedback
		 * @param shotCount Number of shots to fire
		 * @param offsetMax Spread between the multiple shots
		 * @param delay Delay between the multiple shots fire
		 * @param speed Bullet speed
		 * @param damage Base damage before falloff
		 * @param shouldHeadshot Will try for headshots
		 * @param range Max weapon range, also used in falloff.
		 * @param damageFalloffMultiplier Use 0.0 for linear falloff. See https://www.desmos.com/calculator/qfgdwahmdb.
		 * @param shouldBypassHitTicks Will bypass minecraft's invulnerability after damage
		 * @param shouldPassThroughEntities Will ignore entities and hit blocks.
		 * @param pitch Bullet noise pitch
		 * @param timeBetweenShots Rate of fire in tick delay between shots
		 * @param reload Reload time in ticks
		 * @param magazineSize Amount of ammo gun can hold
		 * @param packetsPerShot Amount of times to apply recoil, provides a smoother recoil
		 * @param recoil degrees
		 * @param shouldAkimbo Allows firing with both hands
		 */
		@ConfigSerializable
		data class MultiShotWeaponBalancing(
			val shotSize: Float,
			val shotCount: Int,
			val offsetMax: Double,
			val delay: Int,
			val speed: Double,
			val damage: Double,
			val shouldHeadshot: Boolean,
			val range: Double,
			val damageFalloffMultiplier: Double,
			val shouldBypassHitTicks: Boolean,
			val shouldPassThroughEntities: Boolean,
			val pitch: Float,
			val timeBetweenShots: Int,
			val reload: Int,
			val magazineSize: Int,
			val packetsPerShot: Int,
			val recoil: Float,
			val shouldAkimbo: Boolean = false
		)
	}
}