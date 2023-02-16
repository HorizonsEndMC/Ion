package net.horizonsend.ion.server.configuration

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class BalancingConfiguration(
	val energyWeapons: EnergyWeapon = EnergyWeapon(),
	val starshipWeapons: StarshipWeapons = StarshipWeapons()
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
		}

		interface AmmoStorageBalancing {
			val capacity: Int
		}
	}

	@ConfigSerializable
	class StarshipWeapons(
		// Light Weapons
		val PlasmaCannon: StarshipWeapon = StarshipWeapon(
			range = 160.0,
			speed = 400.0,
			shieldDamageMultiplier = 3,
			thickness = 0.3,
			particleThickness = .5,
			explosionPower = 4.0f,
			volume = 10,
			pitch = 1.0f,
			soundName = "starship.weapon.plasma_cannon.shoot",
			powerusage = 2500,
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
		val LaserCannon: StarshipWeapon = StarshipWeapon(
			range = 200.0,
			speed = 250.0,
			shieldDamageMultiplier = 1,
			thickness = 0.2,
			particleThickness = 0.44,
			explosionPower = 2.0f,
			volume = 10,
			pitch = 2.0f,
			soundName = "entity.firework_rocket.blast_far",
			powerusage = 1600,
			length = 2,
			angleRadians = 15.0,
			convergeDistance = 20.0,
			extraDistance = 2,
			fireCooldownNanos = 250,
			boostChargeNanos = 0,
			aimDistance = 0
		),
		val PulseCannon: StarshipWeapon = StarshipWeapon(
			range = 140.0,
			speed = 170.0,
			shieldDamageMultiplier = 1,
			thickness = 0.2,
			particleThickness = 0.4,
			explosionPower = 2.0f,
			volume = 10,
			pitch = 1.5f,
			soundName = "entity.firework_rocket.blast_far",
			powerusage = 1800,
			length = 2,
			angleRadians = 180.0,
			convergeDistance = 16.0,
			extraDistance = 2,
			fireCooldownNanos = 250,
			boostChargeNanos = 0,
			aimDistance = 0
		),
		val MiniPhaser: StarshipWeapon = StarshipWeapon(
			range = 200.0,
			speed = 600.0,
			shieldDamageMultiplier = 1,
			thickness = 0.2,
			particleThickness = 0.0, // not applicable
			explosionPower = 2f,
			volume = 10,
			pitch = -2.0f,
			soundName = "block.conduit.deactivate",
			powerusage = 5000,
			length = 6,
			angleRadians = 30.0,
			convergeDistance = 0.0,
			extraDistance = 0,
			fireCooldownNanos = 500,
			boostChargeNanos = 0,
			aimDistance = 0
		),

		// Heavy Weapons
		val HeavyLaser: StarshipWeapon = StarshipWeapon(
			range = 200.0,
			speed = 50.0,
			shieldDamageMultiplier = 2,
			thickness = 0.35,
			particleThickness = 1.0,
			explosionPower = 12.0f,
			volume = 10,
			pitch = 2.0f,
			soundName = "starship.weapon.heavy_laser.single.shoot",
			powerusage = 30000,
			length = 8,
			angleRadians = 0.0,
			convergeDistance = 0.0,
			extraDistance = 1,
			fireCooldownNanos = 250,
			boostChargeNanos = 5,
			aimDistance = 10,
			maxDegrees = 25.0
		),
		val Phaser: StarshipWeapon = StarshipWeapon(
			range = 140.0,
			speed = 1.0,
			shieldDamageMultiplier = 55,
			thickness = 0.2,
			particleThickness = 0.0,
			explosionPower = 2.0f,
			volume = 10,
			pitch = 2.0f,
			soundName = "starship.weapon.plasma_cannon.shoot",
			powerusage = 50000,
			length = 8,
			angleRadians = 180.0,
			convergeDistance = 0.0,
			extraDistance = 0,
			fireCooldownNanos = 10,
			boostChargeNanos = 3,
			aimDistance = 0
		),
		val ProtonTorpedo: StarshipWeapon = StarshipWeapon(
			range = 100.0,
			speed = 70.0,
			shieldDamageMultiplier = 2,
			thickness = 0.4,
			particleThickness = 1.0,
			explosionPower = 6.0f,
			volume = 10,
			pitch = 0.75f,
			soundName = "entity.firework_rocket.large_blast_far",
			powerusage = 10000,
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
		val Rocket: StarshipWeapon = StarshipWeapon(
			range = 300.0,
			speed = 5.0,
			shieldDamageMultiplier = 5,
			thickness = 1.0,
			particleThickness = 0.0,
			explosionPower = 10.0f,
			volume = 10,
			pitch = 1.0f,
			soundName = "starship.weapon.rocket.shoot",
			powerusage = 50000,
			length = 0,
			angleRadians = 0.0,
			convergeDistance = 0.0,
			extraDistance = 0,
			fireCooldownNanos = 250,
			boostChargeNanos = 7,
			aimDistance = 0
		),
		val SonicMissile: StarshipWeapon = StarshipWeapon(
			range = 300.0,
			speed = 200.0,
			shieldDamageMultiplier = 10,
			thickness = 0.2,
			particleThickness = 0.0,
			explosionPower = 15.0f,
			volume = 10,
			pitch = 2.0f,
			soundName = "entity.warden.sonic_boom",
			powerusage = 70000,
			length = 10,
			angleRadians = 18.0,
			convergeDistance = 0.0,
			extraDistance = 0,
			fireCooldownNanos = 5000,
			boostChargeNanos = 5,
			aimDistance = 0
		),

		// Auto Turret Stuff
		val LightTurret: StarshipWeapon = StarshipWeapon(
			range = 200.0,
			speed = 250.0,
			shieldDamageMultiplier = 2,
			thickness = 0.0,
			particleThickness = 0.3,
			explosionPower = 4.0f,
			volume = 10,
			pitch = 2.0f,
			soundName = "starship.weapon.turbolaser.light.shoot",
			powerusage = 6000,
			length = 0,
			angleRadians = 0.0,
			convergeDistance = 0.0,
			extraDistance = 0,
			fireCooldownNanos = 250,
			boostChargeNanos = 0,
			aimDistance = 0,
			inaccuracyRadians = 2.0
		),
		val HeavyTurret: StarshipWeapon = StarshipWeapon(
			range = 300.0,
			speed = 200.0,
			shieldDamageMultiplier = 2,
			thickness = 0.0,
			particleThickness = 0.5,
			explosionPower = 4.0f,
			volume = 0,
			pitch = 2.0f,
			soundName = "starship.weapon.turbolaser.heavy.shoot",
			powerusage = 8000,
			length = 0,
			angleRadians = 0.0,
			convergeDistance = 0.0,
			extraDistance = 0,
			fireCooldownNanos = 500,
			boostChargeNanos = 0,
			aimDistance = 0
		),
		val TriTurret: StarshipWeapon = StarshipWeapon(
			range = 500.0,
			speed = 125.0,
			shieldDamageMultiplier = 3,
			thickness = 0.0,
			particleThickness = 0.8,
			explosionPower = 6f,
			volume = 0,
			pitch = 2.0f,
			soundName = "starship.weapon.turbolaser.tri.shoot",
			powerusage = 45000,
			length = 0,
			angleRadians = 0.0,
			convergeDistance = 0.0,
			extraDistance = 0,
			fireCooldownNanos = 10,
			boostChargeNanos = 3,
			aimDistance = 0,
			inaccuracyRadians = 3.0
		),
		val PointDefence: StarshipWeapon = StarshipWeapon(
			range = 120.0,
			speed = 150.0,
			shieldDamageMultiplier = 0,
			thickness = 0.2,
			particleThickness = 0.35,
			explosionPower = 0.0f,
			volume = 10,
			pitch = 2.0f,
			soundName = "entity.firework_rocket.large_blast",
			powerusage = 500,
			length = 0,
			angleRadians = 0.0,
			convergeDistance = 0.0,
			extraDistance = 0,
			fireCooldownNanos = 10,
			boostChargeNanos = 0,
			aimDistance = 0
		),
		val CthulhuBeam: StarshipWeapon = StarshipWeapon(
			range = 64.0,
			speed = 1.0,
			shieldDamageMultiplier = 10,
			thickness = 1.0,
			particleThickness = 0.0,
			explosionPower = 1.0f,
			volume = 0,
			pitch = 2.0f,
			soundName = "minecraft:block.beacon.power_select",
			powerusage = 1,
			length = 0,
			angleRadians = 0.0,
			convergeDistance = 0.0,
			extraDistance = 0,
			fireCooldownNanos = 10,
			boostChargeNanos = 0,
			aimDistance = 0
		)
	) {
		@ConfigSerializable
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
			override val powerusage: Int,
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

		@ConfigSerializable
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

		@ConfigSerializable
		abstract class SubSystem {
			abstract val powerusage: Int
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
