package net.horizonsend.ion.server

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class BalancingConfiguration(
	val energyWeapons: EnergyWeapon = EnergyWeapon()
) {
	@ConfigSerializable
	data class EnergyWeapon(
		val pistol: Singleshot = Singleshot(
			damage = 3.0,
			damageFalloffMultiplier = 0.0,
			magazineSize = 15,
			packetsPerShot = 5,
			pitch = 2f,
			range = 100.0,
			recoil = 3.0f,
			reload = 15,
			shotSize = 0.9,
			shouldAkimbo = true,
			shouldBypassHitTicks = true,
			shouldHeadshot = false,
			shouldPassThroughEntities = false,
			speed = 2.0,
			timeBetweenShots = 6,
			shotDeviation = 0.0
		),
		val rifle: Singleshot = Singleshot(
			damage = 2.0,
			damageFalloffMultiplier = 0.0,
			magazineSize = 30,
			packetsPerShot = 1,
			pitch = 2f,
			range = 100.0,
			recoil = 1.0f,
			reload = 30,
			shotSize = 0.5,
			shouldAkimbo = false,
			shouldBypassHitTicks = true,
			shouldHeadshot = false,
			shouldPassThroughEntities = false,
			speed = 2.0,
			timeBetweenShots = 1,
			shotDeviation = 0.0
		),
		val submachineBlaster: Singleshot = Singleshot(
			damage = 2.0,
			damageFalloffMultiplier = 0.0,
			magazineSize = 30,
			packetsPerShot = 1,
			pitch = 2f,
			range = 100.0,
			recoil = 1.0f,
			reload = 30,
			shotSize = 0.5,
			shouldAkimbo = false,
			shouldBypassHitTicks = true,
			shouldHeadshot = false,
			shouldPassThroughEntities = false,
			speed = 2.0,
			timeBetweenShots = 1,
			shotDeviation = 0.05
		),
		val sniper: Singleshot = Singleshot(
			damage = 12.0,
			damageFalloffMultiplier = 0.0,
			magazineSize = 5,
			packetsPerShot = 5,
			pitch = 0f,
			range = 100.0,
			recoil = 10.0f,
			reload = 120,
			shotSize = 1.0,
			shouldAkimbo = false,
			shouldBypassHitTicks = false,
			shouldHeadshot = true,
			shouldPassThroughEntities = true,
			speed = 2.0,
			timeBetweenShots = 40,
			shotDeviation = 0.0
		),
		val shotgun: Multishot = Multishot(
			damage = 4.0,
			damageFalloffMultiplier = 0.25,
			delay = 0,
			magazineSize = 4,
			offsetMax = 0.05,
			packetsPerShot = 2,
			pitch = 0.0f,
			range = 25.0,
			recoil = 0.25f,
			reload = 120,
			shotCount = 10,
			shotSize = 0.25,
			shouldAkimbo = false,
			shouldBypassHitTicks = true,
			shouldHeadshot = false,
			shouldPassThroughEntities = false,
			speed = 2.0,
			timeBetweenShots = 20,
			shotDeviation = 0.05
		),
		val standardMagazine: AmmoStorage = AmmoStorage(
			capacity = 60
		)
	) {
		@ConfigSerializable
		data class Singleshot(
			override val damage: Double,
			override val damageFalloffMultiplier: Double,
			override val magazineSize: Int,
			override val packetsPerShot: Int,
			override val pitch: Float,
			override val range: Double,
			override val recoil: Float,
			override val reload: Int,
			override val shotSize: Double,
			override val shouldAkimbo: Boolean,
			override val shouldBypassHitTicks: Boolean,
			override val shouldHeadshot: Boolean,
			override val shouldPassThroughEntities: Boolean,
			override val speed: Double,
			override val timeBetweenShots: Int,
			override val shotDeviation: Double
		) : Balancing()

		@ConfigSerializable
		data class Multishot(
			val shotCount: Int,
			val offsetMax: Double,
			val delay: Int,

			override val damage: Double,
			override val damageFalloffMultiplier: Double,
			override val magazineSize: Int,
			override val packetsPerShot: Int,
			override val pitch: Float,
			override val range: Double,
			override val recoil: Float,
			override val reload: Int,
			override val shotSize: Double,
			override val shouldAkimbo: Boolean,
			override val shouldBypassHitTicks: Boolean,
			override val shouldHeadshot: Boolean,
			override val shouldPassThroughEntities: Boolean,
			override val speed: Double,
			override val timeBetweenShots: Int,
			override val shotDeviation: Double
		) : Balancing()

		@ConfigSerializable
		data class AmmoStorage(
			override val capacity: Int
		) : AmmoStorageBalancing

		abstract class Balancing : ProjectileBalancing {
			abstract val magazineSize: Int
			abstract val packetsPerShot: Int
			abstract val pitch: Float
			abstract val recoil: Float
			abstract val reload: Int
			abstract val shouldAkimbo: Boolean
			abstract val shouldHeadshot: Boolean
			abstract val timeBetweenShots: Int
			abstract val shotDeviation: Double
		}

		interface ProjectileBalancing {
			val speed: Double
			val damage: Double
			val damageFalloffMultiplier: Double
			val shouldPassThroughEntities: Boolean
			val shotSize: Double
			val shouldBypassHitTicks: Boolean
			val range: Double
		}

		interface AmmoStorageBalancing {
			val capacity: Int
		}
	}
}
