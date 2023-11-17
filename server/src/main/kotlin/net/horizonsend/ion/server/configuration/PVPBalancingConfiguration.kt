package net.horizonsend.ion.server.configuration

import kotlinx.serialization.Serializable

@Serializable
data class PVPBalancingConfiguration(
	val energyWeapons: EnergyWeapons = EnergyWeapons(),
	val throwables: Throwables = Throwables()
) {
	@Serializable
	data class Throwables(
		val detonator: ThrowableBalancing = ThrowableBalancing(
			80.0,
			4.0,
			1.0,
			5,
			30,
			1,
			25,
		)
	) {
		@Serializable
		data class ThrowableBalancing(
			val damage: Double,
			val damageRadius: Double,
			val throwVelocityMultiplier: Double,
			val maxHealth: Int,
			val maxTicks: Int,
			val tickInterval: Long,
			val throwCooldownTicks: Int,
		)
	}

	@Serializable
	data class EnergyWeapons(
		val pistol: Singleshot = Singleshot(
			damage = 3.0,
			damageFalloffMultiplier = 0.0,
			magazineSize = 10,
			ammoPerRefill = 20,
			packetsPerShot = 2,
			pitch = 1.0f,
			range = 100.0,
			recoil = 3.0f,
			reload = 15,
			shotSize = 0.5,
			shouldAkimbo = false,
			shouldBypassHitTicks = true,
			shouldHeadshot = false,
			shouldPassThroughEntities = false,
			speed = 7.0,
			timeBetweenShots = 6,
			shotDeviation = 0.0,
			mobDamageMultiplier = 1.0,
			consumesAmmo = false
		),
		val rifle: Singleshot = Singleshot(
			damage = 5.5,
			damageFalloffMultiplier = 0.0,
			magazineSize = 20,
			ammoPerRefill = 20,
			packetsPerShot = 1,
			pitch = 1f,
			range = 100.0,
			recoil = 1.0f,
			reload = 30,
			shotSize = 0.25,
			shouldAkimbo = false,
			shouldBypassHitTicks = true,
			shouldHeadshot = true,
			shouldPassThroughEntities = false,
			speed = 12.0,
			timeBetweenShots = 8,
			shotDeviation = 0.0,
			mobDamageMultiplier = 2.0,
			consumesAmmo = true
		),
		val submachineBlaster: Singleshot = Singleshot(
			damage = 1.5,
			damageFalloffMultiplier = 0.0,
			magazineSize = 45,
			ammoPerRefill = 20,
			packetsPerShot = 1,
			pitch = 2f,
			range = 100.0,
			recoil = 1.0f,
			reload = 45,
			shotSize = 0.125,
			shouldAkimbo = false,
			shouldBypassHitTicks = true,
			shouldHeadshot = false,
			shouldPassThroughEntities = false,
			speed = 8.0,
			timeBetweenShots = 2,
			shotDeviation = 0.025,
			mobDamageMultiplier = 2.0,
			consumesAmmo = true
		),
		val sniper: Singleshot = Singleshot(
			damage = 12.0,
			damageFalloffMultiplier = 30.0,
			magazineSize = 5,
			ammoPerRefill = 20,
			packetsPerShot = 5,
			pitch = 0f,
			range = 160.0,
			recoil = 10.0f,
			reload = 120,
			shotSize = 0.0625,
			shouldAkimbo = false,
			shouldBypassHitTicks = false,
			shouldHeadshot = true,
			shouldPassThroughEntities = true,
			speed = 15.0,
			timeBetweenShots = 40,
			shotDeviation = 0.0,
			mobDamageMultiplier = 2.0,
			consumesAmmo = true
		),
		val shotgun: Multishot = Multishot(
			damage = 1.75,
			damageFalloffMultiplier = 0.25,
			delay = 0,
			magazineSize = 4,
			ammoPerRefill = 20,
			offsetMax = 0.05,
			packetsPerShot = 2,
			pitch = 0.0f,
			range = 25.0,
			recoil = 0.25f,
			reload = 60,
			shotCount = 10,
			shotSize = 0.15,
			shouldAkimbo = false,
			shouldBypassHitTicks = true,
			shouldHeadshot = false,
			shouldPassThroughEntities = false,
			speed = 6.5,
			timeBetweenShots = 20,
			shotDeviation = 0.1,
			mobDamageMultiplier = 2.0,
			consumesAmmo = true
		),

		val cannon: Singleshot = Singleshot(
			damage = 0.5,
			explosionPower = 4.0f,
			damageFalloffMultiplier = 0.0,
			magazineSize = 60,
			ammoPerRefill = 20,
			packetsPerShot = 1,
			pitch = 1f,
			range = 30.0,
			recoil = 1.0f,
			reload = 30,
			shotSize = 0.25,
			shouldAkimbo = false,
			shouldBypassHitTicks = true,
			shouldHeadshot = false,
			shouldPassThroughEntities = false,
			speed = 4.0,
			timeBetweenShots = 12,
			shotDeviation = 0.07,
			mobDamageMultiplier = 2.0,
			consumesAmmo = true
		),

		val standardMagazine: AmmoStorage = AmmoStorage(
			capacity = 60,
			refillType = "minecraft:lapis_lazuli",
			ammoPerRefill = 20
		),
		val specialMagazine: AmmoStorage = AmmoStorage(
			capacity = 20,
			refillType = "minecraft:emerald",
			ammoPerRefill = 20
		)
	) {
		@Serializable
		data class Singleshot(
			override val damage: Double,
			override val explosionPower: Float = 0f,
			override val damageFalloffMultiplier: Double,
			override val magazineSize: Int,
			override val ammoPerRefill: Int,
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
			override val shotDeviation: Double,
			override val mobDamageMultiplier: Double,
			override val consumesAmmo: Boolean
		) : Balancing()

		@Serializable
		data class Multishot(
			val shotCount: Int,
			val offsetMax: Double,
			val delay: Int,

			override val damage: Double,
			override val explosionPower: Float = 0f,
			override val damageFalloffMultiplier: Double,
			override val magazineSize: Int,
			override val ammoPerRefill: Int,
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
			override val shotDeviation: Double,
			override val mobDamageMultiplier: Double,
			override val consumesAmmo: Boolean
		) : Balancing()

		@Serializable
		data class AmmoStorage(
			override val capacity: Int,
			override val refillType: String,
			override val ammoPerRefill: Int
		) : AmmoStorageBalancing

		abstract class Balancing : ProjectileBalancing {
			abstract val magazineSize: Int
			abstract val ammoPerRefill: Int
			abstract val packetsPerShot: Int
			abstract val pitch: Float
			abstract val recoil: Float
			abstract val reload: Int
			abstract val shouldAkimbo: Boolean
			abstract val timeBetweenShots: Int
			abstract val consumesAmmo: Boolean
		}

		interface ProjectileBalancing {
			val speed: Double
			val damage: Double
			val explosionPower: Float
			val damageFalloffMultiplier: Double
			val shouldPassThroughEntities: Boolean
			val shotSize: Double
			val shouldBypassHitTicks: Boolean
			val range: Double
			val shouldHeadshot: Boolean
			val mobDamageMultiplier: Double
			val shotDeviation: Double
		}

		interface AmmoStorageBalancing {
			val capacity: Int
			val refillType: String
			val ammoPerRefill: Int
		}
	}
}
