package net.horizonsend.ion.server.configuration

import kotlinx.serialization.Serializable
import kotlin.math.PI

@Serializable
data class StarshipTypeBalancing(
	val antiAirCannon: AntiAirCannonBalancing = AntiAirCannonBalancing(),
	val nonStarshipFired: StarshipWeapons = StarshipWeapons(),

	val speeder: StarshipBalancing = StarshipBalancing(
		sneakFlyAccelDistance = 3,
		maxSneakFlyAccel = 3,
		interdictionRange = 10,
		hyperspaceRangeMultiplier = 3.0
	),

	val shuttle: StarshipBalancing = StarshipBalancing(
		sneakFlyAccelDistance = 5,
		maxSneakFlyAccel = 2,
		interdictionRange = 300,
		hyperspaceRangeMultiplier = 1.2,
	),
	val transport: StarshipBalancing = StarshipBalancing(
		sneakFlyAccelDistance = 10,
		maxSneakFlyAccel = 3,
		interdictionRange = 600,
		hyperspaceRangeMultiplier = 1.25,
	),
	val lightFreighter: StarshipBalancing = StarshipBalancing(
		sneakFlyAccelDistance = 10,
		maxSneakFlyAccel = 3,
		interdictionRange = 900,
		hyperspaceRangeMultiplier = 1.3,
	),
	val mediumFreighter: StarshipBalancing = StarshipBalancing(
		sneakFlyAccelDistance = 10,
		maxSneakFlyAccel = 3,
		interdictionRange = 1200,
		hyperspaceRangeMultiplier = 1.35,
	),
	val heavyFreighter: StarshipBalancing = StarshipBalancing(
		sneakFlyAccelDistance = 10,
		maxSneakFlyAccel = 3,
		interdictionRange = 1500,
		hyperspaceRangeMultiplier = 1.4,
	),

	val starfighter: StarshipBalancing = StarshipBalancing(
		sneakFlyAccelDistance = 4,
		maxSneakFlyAccel = 4,
		interdictionRange = 600,
		hyperspaceRangeMultiplier = 1.5
	),
	val gunship: StarshipBalancing = StarshipBalancing(
		sneakFlyAccelDistance = 5,
		maxSneakFlyAccel = 2,
		interdictionRange = 1200,
		hyperspaceRangeMultiplier = 1.6
	),
	val corvette: StarshipBalancing = StarshipBalancing(
		sneakFlyAccelDistance = 6,
		maxSneakFlyAccel = 2,
		interdictionRange = 1800,
		hyperspaceRangeMultiplier = 1.7,
		weapons = StarshipWeapons(
			lightTurret = StarshipWeapons.StarshipWeapon (
				range = 200.0,
				speed = 250.0,
				areaShieldDamageMultiplier = 2.0,
				starshipShieldDamageMultiplier = 2.0,
				particleThickness = 0.3,
				explosionPower = 4.0f,
				volume = 10,
				pitch = 2.0f,
				soundName = "starship.weapon.turbolaser.light.shoot",
				powerUsage = 7500,
				length = 0,
				angleRadians = 0.0,
				convergeDistance = 0.0,
				extraDistance = 0,
				fireCooldownMillis = 250,
				aimDistance = 0,
				inaccuracyRadians = 2.0,
				applyCooldownToAll = true,
				maxBlockCount = 12000
				),
			triTurret = StarshipWeapons.StarshipWeapon (
				range = 500.0,
				speed = 125.0,
				areaShieldDamageMultiplier = 3.0,
				starshipShieldDamageMultiplier = 3.0,
				particleThickness = 0.8,
				explosionPower = 6f,
				volume = 1,
				pitch = 2.0f,
				soundName = "starship.weapon.turbolaser.tri.shoot",
				powerUsage = 45000,
				length = 0,
				angleRadians = 0.0,
				convergeDistance = 0.0,
				extraDistance = 0,
				fireCooldownMillis = 10,
				boostChargeSeconds = 5,
				aimDistance = 0,
				inaccuracyRadians = 3.0,
				applyCooldownToAll = false
	),
	),
	),
	val frigate: StarshipBalancing = StarshipBalancing(
		sneakFlyAccelDistance = 6,
		maxSneakFlyAccel = 2,
		interdictionRange = 2400,
		hyperspaceRangeMultiplier = 1.8,
	),
	val destroyer: StarshipBalancing = StarshipBalancing(
		sneakFlyAccelDistance = 5,
		maxSneakFlyAccel = 3,
		interdictionRange = 3000,
		hyperspaceRangeMultiplier = 1.9,
	),
	val battlecruiser: StarshipBalancing = StarshipBalancing(
		sneakFlyAccelDistance = 3,
		maxSneakFlyAccel = 3,
		interdictionRange = 3200,
		hyperspaceRangeMultiplier = 2.5,
		cruiseSpeedMultiplier = 0.875,
		weapons = StarshipWeapons(
			quadTurret = StarshipWeapons.StarshipWeapon(
				canFire = true,
				range = 500.0,
				speed = 70.0,
				areaShieldDamageMultiplier = 6.0,
				starshipShieldDamageMultiplier = 6.0,
				particleThickness = 0.6,
				explosionPower = 5f,
				volume = 0,
				pitch = 2.0f,
				soundName = "starship.weapon.turbolaser.quad.shoot",
				powerUsage = 3000,
				length = 0,
				angleRadians = 0.0,
				convergeDistance = 0.0,
				extraDistance = 1,
				fireCooldownMillis = 3000,
				aimDistance = 0,
				inaccuracyRadians = 2.0,
				maxPerShot = 3,
				applyCooldownToAll = true
			)
		)
	),
	val battleship: StarshipBalancing = StarshipBalancing(
		sneakFlyAccelDistance = 3,
		maxSneakFlyAccel = 3,
		interdictionRange = 3500,
		hyperspaceRangeMultiplier = 2.6,
		cruiseSpeedMultiplier = 0.8,
		weapons = StarshipWeapons(
			quadTurret = StarshipWeapons.StarshipWeapon(
				canFire = true,
				range = 500.0,
				speed = 70.0,
				areaShieldDamageMultiplier = 6.0,
				starshipShieldDamageMultiplier = 6.0,
				particleThickness = 0.6,
				explosionPower = 5f,
				volume = 0,
				pitch = 2.0f,
				soundName = "starship.weapon.turbolaser.quad.shoot",
				powerUsage = 3000,
				length = 0,
				angleRadians = 0.0,
				convergeDistance = 0.0,
				extraDistance = 1,
				fireCooldownMillis = 3000,
				aimDistance = 0,
				inaccuracyRadians = 2.0,
				maxPerShot = 4,
				applyCooldownToAll = true
			)
		)
	),
	val dreadnought: StarshipBalancing = StarshipBalancing(
		sneakFlyAccelDistance = 3,
		maxSneakFlyAccel = 2,
		interdictionRange = 3800,
		hyperspaceRangeMultiplier = 2.7,
		cruiseSpeedMultiplier = 0.7,
		weapons = StarshipWeapons(
			quadTurret = StarshipWeapons.StarshipWeapon(
				canFire = true,
				range = 500.0,
				speed = 70.0,
				areaShieldDamageMultiplier = 6.0,
				starshipShieldDamageMultiplier = 6.0,
				particleThickness = 0.6,
				explosionPower = 5f,
				volume = 0,
				pitch = 2.0f,
				soundName = "starship.weapon.turbolaser.quad.shoot",
				powerUsage = 3000,
				length = 0,
				angleRadians = 0.0,
				convergeDistance = 0.0,
				extraDistance = 1,
				fireCooldownMillis = 3000,
				aimDistance = 0,
				inaccuracyRadians = 2.0,
				maxPerShot = 6,
				applyCooldownToAll = true
			)
		)
	),

	val aiShuttle: StarshipBalancing = shuttle,
	val aiTransport: StarshipBalancing = transport,
	val aiLightFreighter: StarshipBalancing = lightFreighter,
	val aiMediumFreighter: StarshipBalancing = mediumFreighter,
	val aiHeavyFreighter: StarshipBalancing = heavyFreighter,

	val aiStarfighter: StarshipBalancing = starfighter,
	val aiGunship: StarshipBalancing = gunship,
	val aiCorvette: StarshipBalancing = StarshipBalancing(
		sneakFlyAccelDistance = 20,
		maxSneakFlyAccel = 20,
		interdictionRange = 1800,
		hyperspaceRangeMultiplier = 1.7,
		weapons = corvette.weapons
	),
	val aiFrigate: StarshipBalancing = frigate,
	val aiDestroyer: StarshipBalancing = destroyer,
	val aiBattlecruiser: StarshipBalancing = battlecruiser,
	val aiBattleship: StarshipBalancing = battleship,
	val aiDreadnought: StarshipBalancing = dreadnought,

	val platformBalancing: StarshipBalancing = StarshipBalancing(
		sneakFlyAccelDistance = 0,
		maxSneakFlyAccel = 0,
		interdictionRange = 0,
		hyperspaceRangeMultiplier = 0.0
	),
	val eventShipBalancing: StarshipBalancing = StarshipBalancing(
		sneakFlyAccelDistance = 10,
		maxSneakFlyAccel = 3,
		interdictionRange = 2000,
		hyperspaceRangeMultiplier = 10.0
	)
)

@Serializable
data class AntiAirCannonBalancing(
	override var range: Double = 500.0,
	override var speed: Double = 125.0,
	override var areaShieldDamageMultiplier: Double = 1.0,
	override var starshipShieldDamageMultiplier: Double = 3.0,
	override var particleThickness: Double = 0.8,
	override var explosionPower: Float = 6f,
	override var volume: Int = 0,
	override var pitch: Float = 2.0f,
	override var soundName: String = "starship.weapon.turbolaser.tri.shoot",
	override var maxDegrees: Double = 360.0
) : StarshipWeapons.ProjectileBalancing

@Serializable
data class StarshipBalancing(
	var canMove: Boolean = true,
	var accelMultiplier: Double = 1.0,
	var maxSpeedMultiplier: Double = 1.0,
	var weapons: StarshipWeapons = StarshipWeapons(),

	val sneakFlyAccelDistance: Int,
	val maxSneakFlyAccel: Int,
	val interdictionRange: Int,
	val hyperspaceRangeMultiplier: Double,
	val cruiseSpeedMultiplier: Double = 1.0,
)

@Serializable
class StarshipWeapons(
	// Light Weapons
	val plasmaCannon: StarshipWeapon = StarshipWeapon(
		range = 160.0,
		speed = 400.0,
		areaShieldDamageMultiplier = 3.0,
		starshipShieldDamageMultiplier = 3.0,
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
		fireCooldownMillis = 250, // not overriden for Plasma Cannons
		aimDistance = 0,
		forwardOnly = true,
		maxPerShot = 2,
		applyCooldownToAll = true
	),

	val laserCannon: StarshipWeapon = StarshipWeapon(
		range = 200.0,
		speed = 250.0,
		areaShieldDamageMultiplier = 1.0,
		starshipShieldDamageMultiplier = 1.0,
		particleThickness = 0.44,
		explosionPower = 2.0f,
		volume = 10,
		pitch = 2.0f,
		soundName = "entity.firework_rocket.blast_far",
		powerUsage = 1600,
		length = 2,
		angleRadians = 17.0,
		convergeDistance = 20.0,
		extraDistance = 2,
		fireCooldownMillis = 250,
		aimDistance = 0,
		applyCooldownToAll = true
	),

	val pulseCannon: StarshipWeapon = StarshipWeapon(
		range = 180.0,
		speed = 600.0,
		areaShieldDamageMultiplier = 2.0,
		starshipShieldDamageMultiplier = 2.0,
		particleThickness = 0.4,
		explosionPower = 1.875f,
		volume = 10,
		pitch = 0.5f,
		soundName = "entity.firework_rocket.blast_far",
		powerUsage = 1984,
		length = 2,
		angleRadians = 180.0,
		convergeDistance = 16.0,
		extraDistance = 2,
		fireCooldownMillis = 250,
		aimDistance = 0,
		applyCooldownToAll = true,
		minBlockCount = 1000,
		maxBlockCount = 4000
	),

	// Heavy Weapons
	val heavyLaser: StarshipWeapon = StarshipWeapon(
		range = 200.0,
		speed = 50.0,
		areaShieldDamageMultiplier = 2.0,
		starshipShieldDamageMultiplier = 2.0,
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
		fireCooldownMillis = 250,
		boostChargeSeconds = 5,
		aimDistance = 10,
		maxDegrees = 25.0,
		applyCooldownToAll = false
	),

	val phaser: StarshipWeapon = StarshipWeapon(
		range = 140.0,
		speed = 1.0,
		areaShieldDamageMultiplier = 5.0,
		starshipShieldDamageMultiplier = 55.0,
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
		fireCooldownMillis = 10,
		boostChargeSeconds = 3,
		aimDistance = 0,
		applyCooldownToAll = false,
		maxBlockCount = 12000
	),

	val protonTorpedo: StarshipWeapon = StarshipWeapon(
		range = 135.0,
		speed = 70.0,
		areaShieldDamageMultiplier = 2.0,
		starshipShieldDamageMultiplier = 2.0,
		particleThickness = 1.0,
		explosionPower = 7.0f,
		volume = 10,
		pitch = 0.75f,
		soundName = "entity.firework_rocket.large_blast_far",
		powerUsage = 10000,
		length = 3,
		angleRadians = 10.0,
		convergeDistance = 10.0,
		extraDistance = 10,
		maxDegrees = 45.0,
		fireCooldownMillis = 10,
		boostChargeSeconds = 10,
		aimDistance = 3,
		forwardOnly = true,
		maxPerShot = 2,
		applyCooldownToAll = false
	),

	val rocket: StarshipWeapon = StarshipWeapon(
		range = 300.0,
		speed = 5.0,
		areaShieldDamageMultiplier = 5.0,
		starshipShieldDamageMultiplier = 5.0,
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
		fireCooldownMillis = 250,
		boostChargeSeconds = 7,
		aimDistance = 0,
		applyCooldownToAll = false
	),

	// Auto Turret Stuff
	val lightTurret: StarshipWeapon = StarshipWeapon(
		range = 200.0,
		speed = 250.0,
		areaShieldDamageMultiplier = 2.0,
		starshipShieldDamageMultiplier = 2.0,
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
		fireCooldownMillis = 250,
		aimDistance = 0,
		inaccuracyRadians = 2.0,
		applyCooldownToAll = true,
		maxBlockCount = 12000
	),

	val heavyTurret: StarshipWeapon = StarshipWeapon(
		range = 500.0,
		speed = 200.0,
		areaShieldDamageMultiplier = 1.0,
		starshipShieldDamageMultiplier = 1.0,
		particleThickness = 0.3,
		explosionPower = 3.0f,
		volume = 0,
		pitch = 2.0f,
		soundName = "starship.weapon.turbolaser.heavy.shoot",
		powerUsage = 8000,
		length = 0,
		angleRadians = 0.0,
		convergeDistance = 0.0,
		extraDistance = 0,
		fireCooldownMillis = 500,
		boostChargeSeconds = 0,
		applyCooldownToAll = true,
		aimDistance = 0,
		maxBlockCount = 12000,
		minBlockCount = 6500
	),

	val triTurret: StarshipWeapon = StarshipWeapon(
		range = 500.0,
		speed = 125.0,
		areaShieldDamageMultiplier = 3.0,
		starshipShieldDamageMultiplier = 3.0,
		particleThickness = 0.8,
		explosionPower = 6f,
		volume = 1,
		pitch = 2.0f,
		soundName = "starship.weapon.turbolaser.tri.shoot",
		powerUsage = 45000,
		length = 0,
		angleRadians = 0.0,
		convergeDistance = 0.0,
		extraDistance = 0,
		fireCooldownMillis = 10,
		boostChargeSeconds = 3,
		aimDistance = 0,
		inaccuracyRadians = 3.0,
		applyCooldownToAll = false
	),

	val quadTurret: StarshipWeapon = StarshipWeapon(
		canFire = false,
		range = 500.0,
		speed = 70.0,
		areaShieldDamageMultiplier = 6.0,
		starshipShieldDamageMultiplier = 6.0,
		particleThickness = 0.6,
		explosionPower = 5f,
		volume = 0,
		pitch = 2.0f,
		soundName = "starship.weapon.turbolaser.quad.shoot",
		powerUsage = 3000,
		length = 0,
		angleRadians = 0.0,
		convergeDistance = 0.0,
		extraDistance = 1,
		fireCooldownMillis = 3000,
		aimDistance = 0,
		inaccuracyRadians = 2.0,
		maxPerShot = 3,
		applyCooldownToAll = true,
		minBlockCount = 18500
	),

	val pointDefence: StarshipWeapon = StarshipWeapon(
		range = 120.0,
		speed = 150.0,
		areaShieldDamageMultiplier = 2.0,
		starshipShieldDamageMultiplier = 0.0,
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
		fireCooldownMillis = 10,
		aimDistance = 0,
		applyCooldownToAll = true
	),

	// Event weapons
	// Event auto weapons
	val cthulhuBeam: StarshipWeapon = StarshipWeapon(
		canFire = false,
		range = 64.0,
		speed = 1.0,
		areaShieldDamageMultiplier = 10.0,
		starshipShieldDamageMultiplier = 10.0,
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
		fireCooldownMillis = 10,
		aimDistance = 0,
		applyCooldownToAll = false
	),

	// Event manual weapons
	val flameThrower: StarshipWeapon = StarshipWeapon(
		canFire = false,
		range = 340.0,
		speed = 250.0,
		areaShieldDamageMultiplier = 5.0,
		starshipShieldDamageMultiplier = 5.0,
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
		fireCooldownMillis = 10,
		aimDistance = 0,
		applyCooldownToAll = false
	),

	val pumpkinCannon: StarshipWeapon = StarshipWeapon(
		canFire = false,
		range = 500.0,
		speed = 125.0,
		areaShieldDamageMultiplier = 3.0,
		starshipShieldDamageMultiplier = 3.0,
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
		fireCooldownMillis = 10,
		aimDistance = 0,
		inaccuracyRadians = 3.0,
		applyCooldownToAll = false
	),

	val plagueCannon: StarshipWeapon = StarshipWeapon(
		canFire = false,
		range = 200.0,
		speed = 250.0,
		areaShieldDamageMultiplier = 1.0,
		starshipShieldDamageMultiplier = 1.0,
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
		fireCooldownMillis = 250,
		aimDistance = 0,
		applyCooldownToAll = false
	),

	val miniPhaser: StarshipWeapon = StarshipWeapon(
		canFire = false,
		range = 200.0,
		speed = 600.0,
		areaShieldDamageMultiplier = 1.0,
		starshipShieldDamageMultiplier = 1.0,
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
		fireCooldownMillis = 500,
		aimDistance = 0,
		applyCooldownToAll = true
	),

	// Event heavy weapons
	val capitalBeam: StarshipWeapon = StarshipWeapon(
		canFire = false,
		range = 500.0,
		speed = PI * 50,
		areaShieldDamageMultiplier = 0.0,
		starshipShieldDamageMultiplier = 0.0,
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
		fireCooldownMillis = 0,
		boostChargeSeconds = 0,
		aimDistance = 0,
		applyCooldownToAll = false
	),

	val sonicMissile: StarshipWeapon = StarshipWeapon(
		canFire = false,
		range = 300.0,
		speed = 200.0,
		areaShieldDamageMultiplier = 10.0,
		starshipShieldDamageMultiplier = 10.0,
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
		fireCooldownMillis = 5000,
		boostChargeSeconds = 5,
		aimDistance = 0,
		applyCooldownToAll = false
	),
) {
	/**
	 * @param canFire Whether this weapon can be fired.
	 * @param minBlockCount The minimum block count of a ship to be able to fire this weapon.
	 * @param maxBlockCount The maximum block count of a ship to be able to fire this weapon.
	 *
	 * @param range Amount of travel before the projectile de-spawns.
	 * @param speed The speed of this weapon's projectile.
	 *
	 * @param explosionPower The power of the impact explosion of this projectile.
	 * @param areaShieldDamageMultiplier The amount the explosion damage is multiplied when damaging a shield.
	 *
	 * @param particleThickness The thickness of the particles. Not always used.
	 *
	 * @param soundName Controls the sound played.
	 * @param volume Controls the volume of the sound played.
	 * @param pitch Controls the pitch of the sound played.
	 *
	 * @param powerUsage The power taken from the ship's weapon capacitor.
	 *
	 * @param length The length of the weapon, from the origin of the multiblock.
	 * @param extraDistance Extra distance between the multiblock and the fire point to spawn the projectile.
	 *
	 * @param fireCooldownMillis The cooldown between firing this weapon. Later converted to nanos.
	 * @param applyCooldownToAll Whether to apply the cooldown to all of this weapon type upon firing.
	 *
	 * @param maxPerShot The max number of this weapon that can be fired at once.
	 *
	 * @param forwardOnly Whether this weapon can only fire in the direction the starship is facing.
	 *
	 * Cannon specific
	 * @param angleRadians For cannon type weapons. Controls the aiming distance.
	 * @param convergeDistance For cannon type weapons. Controls the distance at which the firing arcs converge on a point.
	 *
	 * Heavy weapons
	 * @param boostChargeSeconds
	 *
	 * Tracking projectiles
	 * @param aimDistance
	 * @param maxDegrees
	 *
	 * Turrets
	 * @param inaccuracyRadians
	 **/
	@Serializable
	data class StarshipWeapon(
		override var canFire: Boolean = true,
		override var minBlockCount: Int = 0,
		override var maxBlockCount: Int = Int.MAX_VALUE,

		override var range: Double,
		override var speed: Double,

		override var explosionPower: Float,

		override var starshipShieldDamageMultiplier: Double,
		override var areaShieldDamageMultiplier: Double,

		override var particleThickness: Double,

		override var soundName: String,
		override var volume: Int,
		override var pitch: Float,

		override var powerUsage: Int,

		override var length: Int,
		override var extraDistance: Int,

		override var angleRadians: Double,
		override var convergeDistance: Double,

		override var fireCooldownMillis: Long,
		override var applyCooldownToAll: Boolean,
		override var maxPerShot: Int = 0,

		override var forwardOnly: Boolean = false,

		var boostChargeSeconds: Long = 0, // Seconds, should only be put for heavyWeapons
		var aimDistance: Int, // should only be put if the weapon in question is target tracking
		override var inaccuracyRadians: Double = 2.0,
		override var maxDegrees: Double = 0.0
	) : ProjectileBalancing, SubSystem

	@Serializable
	sealed interface ProjectileBalancing {
		var range: Double
		var speed: Double

		var starshipShieldDamageMultiplier: Double
		var areaShieldDamageMultiplier: Double

		var particleThickness: Double
		var explosionPower: Float
		var volume: Int
		var pitch: Float
		var soundName: String
		var maxDegrees: Double
	}

	@Serializable
	sealed interface SubSystem {
		var canFire: Boolean
		var minBlockCount: Int
		var maxBlockCount: Int

		var powerUsage: Int

		var length: Int
		var extraDistance: Int

		var angleRadians: Double
		var convergeDistance: Double

		var fireCooldownMillis: Long
		var applyCooldownToAll: Boolean

		var maxPerShot: Int
		var forwardOnly: Boolean
		var inaccuracyRadians: Double
	}
}

