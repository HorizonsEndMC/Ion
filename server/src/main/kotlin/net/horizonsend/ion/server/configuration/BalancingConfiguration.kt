package net.horizonsend.ion.server.configuration

import kotlinx.serialization.Serializable
import kotlin.math.PI

@Serializable
data class BalancingConfiguration(
	val energyWeapons: EnergyWeapons = EnergyWeapons(),
	val starshipWeapons: StarshipWeapons = StarshipWeapons(),
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
			range = 180.0,
			speed = 600.0,
			shieldDamageMultiplier = 2,
			thickness = 0.2,
			particleThickness = 0.4,
			explosionPower = 2.5f,
			volume = 10,
			pitch = 0.5f,
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
			range = 500.0,
			speed = 200.0,
			shieldDamageMultiplier = 2,
			thickness = 0.0,
			particleThickness = 0.3,
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
		val quadTurret: StarshipWeapon = StarshipWeapon(
			range = 650.0,
			speed = 100.0,
			shieldDamageMultiplier = 4,
			thickness = 0.0,
			particleThickness = 0.8,
			explosionPower = 6f,
			volume = 0,
			pitch = 2.0f,
			soundName = "starship.weapon.turbolaser.tri.shoot",
			powerUsage = 4500,
			length = 0,
			angleRadians = 0.0,
			convergeDistance = 0.0,
			extraDistance = 1,
			fireCooldownNanos = 2,
			boostChargeNanos = 3,
			aimDistance = 0,
			inaccuracyRadians = 8.0,
			maxPerShot = 3
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
		),
		val flameThrower: StarshipWeapon = StarshipWeapon(
			range = 340.0,
			speed = 250.0,
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
		val pumpkinCannon: StarshipWeapon = StarshipWeapon(
			range = 500.0,
			speed = 125.0,
			shieldDamageMultiplier = 3,
			thickness = 0.0,
			particleThickness = 0.8,
			explosionPower = 6f,
			volume = 0,
			pitch = 2.0f,
			soundName = "entity.firework_rocket.blast_far",
			powerUsage = 15000,
			length = 4,
			angleRadians = 0.0,
			convergeDistance = 0.0,
			extraDistance = 0,
			fireCooldownNanos = 10,
			boostChargeNanos = 3,
			aimDistance = 0,
			inaccuracyRadians = 3.0
		),
		val plagueCannon: StarshipWeapon = StarshipWeapon(
			range = 200.0,
			speed = 250.0,
			shieldDamageMultiplier = 1,
			thickness = 0.2,
			particleThickness = 0.44,
			explosionPower = 2.0f,
			volume = 10,
			pitch = 2.0f,
			soundName = "entity.firework_rocket.blast_far",
			powerUsage = 5000,
			length = 2,
			angleRadians = 15.0,
			convergeDistance = 20.0,
			extraDistance = 2,
			fireCooldownNanos = 250,
			boostChargeNanos = 0,
			aimDistance = 0
		),
		val capitalBeam: StarshipWeapon = StarshipWeapon(
			range = 500.0,
			speed = PI * 50,
			shieldDamageMultiplier = 0,
			thickness = 0.2,
			particleThickness = 0.44,
			explosionPower = 20.0f,
			volume = 10,
			pitch = 2.0f,
			soundName = "entity.firework_rocket.blast_far",
			powerUsage = 1600,
			length = 2,
			angleRadians = 15.0,
			convergeDistance = 20.0,
			extraDistance = 2,
			fireCooldownNanos = 0,
			boostChargeNanos = 0,
			aimDistance = 0
		),
	) {
		@Serializable
		data class StarshipWeapon(
			override var range: Double,
			override var speed: Double,
			override var shieldDamageMultiplier: Int,
			override var thickness: Double,
			override var particleThickness: Double,
			override var explosionPower: Float,
			override var volume: Int,
			override var pitch: Float,
			override var soundName: String,
			override var powerUsage: Int,
			override var length: Int,
			override var angleRadians: Double,
			override var convergeDistance: Double,
			override var extraDistance: Int,
			override var fireCooldownNanos: Long,
			override var maxPerShot: Int = 0,
			override var fowardOnly: Boolean = false,
			var boostChargeNanos: Long, // Seconds, should only be put for heavyWeapons
			var aimDistance: Int, // should only be put if the weapon in question is target tracking
			var maxDegrees: Double = 0.0,
			override var inaccuracyRadians: Double = 2.0
		) : ProjectileBalancing()

		@Serializable
		abstract class ProjectileBalancing : SubSystem() {
			abstract var range: Double
			abstract var speed: Double
			abstract var shieldDamageMultiplier: Int
			abstract var thickness: Double
			abstract var particleThickness: Double
			abstract var explosionPower: Float
			abstract var volume: Int
			abstract var pitch: Float
			abstract var soundName: String
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
