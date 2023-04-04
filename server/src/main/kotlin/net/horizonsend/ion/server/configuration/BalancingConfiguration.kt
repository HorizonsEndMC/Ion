package net.horizonsend.ion.server.configuration

import kotlinx.serialization.Serializable

@Serializable
data class BalancingConfiguration(
	val energyWeapons: EnergyWeapon = EnergyWeapon(),
	val starshipWeapons: StarshipWeapons = StarshipWeapons()
) {
	@Serializable
	data class EnergyWeapon(
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
			mobDamageMultiplier = 2.0
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
			mobDamageMultiplier = 2.0
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
			mobDamageMultiplier = 2.0
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
			mobDamageMultiplier = 2.0
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
			mobDamageMultiplier = 2.0
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
			override val mobDamageMultiplier: Double
		) : Balancing()

		@Serializable
		data class Multishot(
			val shotCount: Int,
			val offsetMax: Double,
			val delay: Int,

			override val damage: Double,
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
			override val mobDamageMultiplier: Double
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
			val shouldHeadshot: Boolean
			val mobDamageMultiplier: Double
		}

		interface AmmoStorageBalancing {
			val capacity: Int
			val refillType: String
			val ammoPerRefill: Int
		}
	}

	@Serializable
	class StarshipWeapons(
		// Light Weapons
		val plasmaCannon: StarshipWeapon = StarshipWeapon(
			range = 160.0,
			speed = 400.0,
			shieldDamageMultiplier = 3,
			thickness = 0.3,
			particleThickness = .5,
			explosionPower = 4.0f,
			volume = 10,
			pitch = 1.0f,
			soundName = "starship.weapon.plasma_cannon.shoot",
			powerUsage = 2500,
			length = 3,
			angleRadians = 15.0,
			convergeDistance = 10.0,
			extraDistance = 1,
			fireCooldownNanos = 250, // not overriden for Plasma Cannons
			boostChargeNanos = 0,
			aimDistance = 0,
			fowardOnly = true,
			maxPerShot = 2
		),
		val laserCannon: StarshipWeapon = StarshipWeapon(
			range = 200.0,
			speed = 250.0,
			shieldDamageMultiplier = 1,
			thickness = 0.2,
			particleThickness = 0.44,
			explosionPower = 2.0f,
			volume = 10,
			pitch = 2.0f,
			soundName = "entity.firework_rocket.blast_far",
			powerUsage = 1600,
			length = 2,
			angleRadians = 15.0,
			convergeDistance = 20.0,
			extraDistance = 2,
			fireCooldownNanos = 250,
			boostChargeNanos = 0,
			aimDistance = 0
		),
		val pulseCannon: StarshipWeapon = StarshipWeapon(
			range = 140.0,
			speed = 170.0,
			shieldDamageMultiplier = 1,
			thickness = 0.2,
			particleThickness = 0.4,
			explosionPower = 2.0f,
			volume = 10,
			pitch = 1.5f,
			soundName = "entity.firework_rocket.blast_far",
			powerUsage = 1800,
			length = 2,
			angleRadians = 180.0,
			convergeDistance = 16.0,
			extraDistance = 2,
			fireCooldownNanos = 250,
			boostChargeNanos = 0,
			aimDistance = 0
		),
		val miniPhaser: StarshipWeapon = StarshipWeapon(
			range = 200.0,
			speed = 600.0,
			shieldDamageMultiplier = 1,
			thickness = 0.2,
			particleThickness = 0.0, // not applicable
			explosionPower = 2f,
			volume = 10,
			pitch = -2.0f,
			soundName = "block.conduit.deactivate",
			powerUsage = 5000,
			length = 6,
			angleRadians = 30.0,
			convergeDistance = 0.0,
			extraDistance = 0,
			fireCooldownNanos = 500,
			boostChargeNanos = 0,
			aimDistance = 0
		),

		// Heavy Weapons
		val heavyLaser: StarshipWeapon = StarshipWeapon(
			range = 200.0,
			speed = 50.0,
			shieldDamageMultiplier = 2,
			thickness = 0.35,
			particleThickness = 1.0,
			explosionPower = 12.0f,
			volume = 10,
			pitch = 2.0f,
			soundName = "starship.weapon.heavy_laser.single.shoot",
			powerUsage = 30000,
			length = 8,
			angleRadians = 0.0,
			convergeDistance = 0.0,
			extraDistance = 1,
			fireCooldownNanos = 250,
			boostChargeNanos = 5,
			aimDistance = 10,
			maxDegrees = 25.0
		),
		val phaser: StarshipWeapon = StarshipWeapon(
			range = 140.0,
			speed = 1.0,
			shieldDamageMultiplier = 55,
			thickness = 0.2,
			particleThickness = 0.0,
			explosionPower = 2.0f,
			volume = 10,
			pitch = 2.0f,
			soundName = "starship.weapon.plasma_cannon.shoot",
			powerUsage = 50000,
			length = 8,
			angleRadians = 180.0,
			convergeDistance = 0.0,
			extraDistance = 0,
			fireCooldownNanos = 10,
			boostChargeNanos = 3,
			aimDistance = 0
		),
		val protonTorpedo: StarshipWeapon = StarshipWeapon(
			range = 100.0,
			speed = 70.0,
			shieldDamageMultiplier = 2,
			thickness = 0.4,
			particleThickness = 1.0,
			explosionPower = 6.0f,
			volume = 10,
			pitch = 0.75f,
			soundName = "entity.firework_rocket.large_blast_far",
			powerUsage = 10000,
			length = 3,
			angleRadians = 10.0,
			convergeDistance = 10.0,
			extraDistance = 10,
			maxDegrees = 45.0,
			fireCooldownNanos = 10,
			boostChargeNanos = 10,
			aimDistance = 3,
			fowardOnly = true,
			maxPerShot = 2
		),
		val rocket: StarshipWeapon = StarshipWeapon(
			range = 300.0,
			speed = 5.0,
			shieldDamageMultiplier = 5,
			thickness = 1.0,
			particleThickness = 0.0,
			explosionPower = 10.0f,
			volume = 10,
			pitch = 1.0f,
			soundName = "starship.weapon.rocket.shoot",
			powerUsage = 50000,
			length = 0,
			angleRadians = 0.0,
			convergeDistance = 0.0,
			extraDistance = 0,
			fireCooldownNanos = 250,
			boostChargeNanos = 7,
			aimDistance = 0
		),
		val sonicMissile: StarshipWeapon = StarshipWeapon(
			range = 300.0,
			speed = 200.0,
			shieldDamageMultiplier = 10,
			thickness = 0.2,
			particleThickness = 0.0,
			explosionPower = 15.0f,
			volume = 10,
			pitch = 2.0f,
			soundName = "entity.warden.sonic_boom",
			powerUsage = 70000,
			length = 10,
			angleRadians = 18.0,
			convergeDistance = 0.0,
			extraDistance = 0,
			fireCooldownNanos = 5000,
			boostChargeNanos = 5,
			aimDistance = 0
		),

		// Auto Turret Stuff
		val lightTurret: StarshipWeapon = StarshipWeapon(
			range = 200.0,
			speed = 250.0,
			shieldDamageMultiplier = 2,
			thickness = 0.0,
			particleThickness = 0.3,
			explosionPower = 4.0f,
			volume = 10,
			pitch = 2.0f,
			soundName = "starship.weapon.turbolaser.light.shoot",
			powerUsage = 6000,
			length = 0,
			angleRadians = 0.0,
			convergeDistance = 0.0,
			extraDistance = 0,
			fireCooldownNanos = 250,
			boostChargeNanos = 0,
			aimDistance = 0,
			inaccuracyRadians = 2.0
		),
		val heavyTurret: StarshipWeapon = StarshipWeapon(
			range = 300.0,
			speed = 200.0,
			shieldDamageMultiplier = 2,
			thickness = 0.0,
			particleThickness = 0.5,
			explosionPower = 4.0f,
			volume = 0,
			pitch = 2.0f,
			soundName = "starship.weapon.turbolaser.heavy.shoot",
			powerUsage = 8000,
			length = 0,
			angleRadians = 0.0,
			convergeDistance = 0.0,
			extraDistance = 0,
			fireCooldownNanos = 500,
			boostChargeNanos = 0,
			aimDistance = 0
		),
		val triTurret: StarshipWeapon = StarshipWeapon(
			range = 500.0,
			speed = 125.0,
			shieldDamageMultiplier = 3,
			thickness = 0.0,
			particleThickness = 0.8,
			explosionPower = 6f,
			volume = 0,
			pitch = 2.0f,
			soundName = "starship.weapon.turbolaser.tri.shoot",
			powerUsage = 45000,
			length = 0,
			angleRadians = 0.0,
			convergeDistance = 0.0,
			extraDistance = 0,
			fireCooldownNanos = 10,
			boostChargeNanos = 3,
			aimDistance = 0,
			inaccuracyRadians = 3.0
		),
		val pointDefence: StarshipWeapon = StarshipWeapon(
			range = 120.0,
			speed = 150.0,
			shieldDamageMultiplier = 0,
			thickness = 0.2,
			particleThickness = 0.35,
			explosionPower = 0.0f,
			volume = 10,
			pitch = 2.0f,
			soundName = "entity.firework_rocket.large_blast",
			powerUsage = 500,
			length = 0,
			angleRadians = 0.0,
			convergeDistance = 0.0,
			extraDistance = 0,
			fireCooldownNanos = 10,
			boostChargeNanos = 0,
			aimDistance = 0
		),
		val cthulhuBeam: StarshipWeapon = StarshipWeapon(
			range = 64.0,
			speed = 1.0,
			shieldDamageMultiplier = 10,
			thickness = 1.0,
			particleThickness = 0.0,
			explosionPower = 1.0f,
			volume = 0,
			pitch = 2.0f,
			soundName = "minecraft:block.beacon.power_select",
			powerUsage = 1,
			length = 0,
			angleRadians = 0.0,
			convergeDistance = 0.0,
			extraDistance = 0,
			fireCooldownNanos = 10,
			boostChargeNanos = 0,
			aimDistance = 0
		)
	) {
		@Serializable
		data class StarshipWeapon(
			override val range: Double,
			override val speed: Double,
			override val shieldDamageMultiplier: Int,
			override val thickness: Double,
			override val particleThickness: Double,
			override val explosionPower: Float,
			override val volume: Int,
			override val pitch: Float,
			override val soundName: String,
			override val powerUsage: Int,
			override val length: Int,
			override val angleRadians: Double,
			override val convergeDistance: Double,
			override val extraDistance: Int,
			override val fireCooldownNanos: Long,
			override val maxPerShot: Int = 0,
			override val fowardOnly: Boolean = false,
			val boostChargeNanos: Long, // Seconds, should only be put for heavyWeapons
			val aimDistance: Int, // should only be put if the weapon in question is target tracking
			val maxDegrees: Double = 0.0,
			override val inaccuracyRadians: Double = 2.0
		) : ProjectileBalancing()

		@Serializable
		abstract class ProjectileBalancing : SubSystem() {
			abstract val range: Double
			abstract val speed: Double
			abstract val shieldDamageMultiplier: Int
			abstract val thickness: Double
			abstract val particleThickness: Double
			abstract val explosionPower: Float
			abstract val volume: Int
			abstract val pitch: Float
			abstract val soundName: String
		}

		@Serializable
		abstract class SubSystem {
			abstract val powerUsage: Int
			abstract val length: Int
			abstract val angleRadians: Double
			abstract val convergeDistance: Double
			abstract val extraDistance: Int
			abstract val fireCooldownNanos: Long
			abstract val maxPerShot: Int
			abstract val fowardOnly: Boolean
			abstract val inaccuracyRadians: Double
		}
	}
}
