package net.horizonsend.ion.server.configuration

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.horizonsend.ion.server.configuration.StandardStarshipSounds.SoundInfo
import net.horizonsend.ion.server.configuration.StarshipWeapons.AbyssalGazeBalancing.AbyssalGazeProjectileBalancing
import net.horizonsend.ion.server.configuration.StarshipWeapons.ArsenalRocketBalancing.ArsenalRocketProjectileBalancing
import net.horizonsend.ion.server.configuration.StarshipWeapons.CapitalCannonBalancing.CapitalCannonProjectileBalancing
import net.horizonsend.ion.server.configuration.StarshipWeapons.CycleTurretBalancing.CycleTurretProjectileBalancing
import net.horizonsend.ion.server.configuration.StarshipWeapons.DisintegratorBeamBalancing.DisintegratorBeamProjectileBalancing
import net.horizonsend.ion.server.configuration.StarshipWeapons.DoomsdayDeviceBalancing.DoomsdayDeviceProjectileBalancing
import net.horizonsend.ion.server.configuration.StarshipWeapons.FlamethrowerCannonBalancing.FlamethrowerCannonProjectileBalancing
import net.horizonsend.ion.server.configuration.StarshipWeapons.FlamingSkullCannonBalancing.FlamingSkullCannonProjectileBalancing
import net.horizonsend.ion.server.configuration.StarshipWeapons.HeavyLaserBalancing.HeavyLaserProjectileBalancing
import net.horizonsend.ion.server.configuration.StarshipWeapons.HeavyTurretBalancing.HeavyTurretProjectileBalancing
import net.horizonsend.ion.server.configuration.StarshipWeapons.InterceptorCannonBalancing.IncterceptorCannonProjectileBalancing
import net.horizonsend.ion.server.configuration.StarshipWeapons.IonTurretBalancing.IonTurretProjectileBalancing
import net.horizonsend.ion.server.configuration.StarshipWeapons.LaserCannonBalancing.LaserCannonProjectileBalancing
import net.horizonsend.ion.server.configuration.StarshipWeapons.LightTurretBalancing.LightTurretProjectileBalancing
import net.horizonsend.ion.server.configuration.StarshipWeapons.LogisticsTurretBalancing.LogisticsTurretProjectileBalancing
import net.horizonsend.ion.server.configuration.StarshipWeapons.MiniPhaserBalancing.MiniPhaserProjectileBalancing
import net.horizonsend.ion.server.configuration.StarshipWeapons.PhaserBalancing.PhaserProjectileBalancing
import net.horizonsend.ion.server.configuration.StarshipWeapons.PlasmaCannonBalancing.PlasmaCannonProjectileBalancing
import net.horizonsend.ion.server.configuration.StarshipWeapons.PointDefenseBalancing.PointDefenseProjectileBalancing
import net.horizonsend.ion.server.configuration.StarshipWeapons.ProjectileBalancing
import net.horizonsend.ion.server.configuration.StarshipWeapons.ProjectileBalancing.EntityDamage
import net.horizonsend.ion.server.configuration.StarshipWeapons.ProjectileBalancing.RegularDamage
import net.horizonsend.ion.server.configuration.StarshipWeapons.PulseCannonBalancing.PulseCannonProjectileBalancing
import net.horizonsend.ion.server.configuration.StarshipWeapons.PumpkinCannonBalancing.PumpkinCannonProjectileBalancing
import net.horizonsend.ion.server.configuration.StarshipWeapons.QuadTurretBalancing.QuadTurretProjectileBalancing
import net.horizonsend.ion.server.configuration.StarshipWeapons.RocketBalancing.RocketProjectileBalancing
import net.horizonsend.ion.server.configuration.StarshipWeapons.SonicMissileBalancing.SonicMissileProjectileBalancing
import net.horizonsend.ion.server.configuration.StarshipWeapons.StarshipWeaponBalancing.FireRestrictions
import net.horizonsend.ion.server.configuration.StarshipWeapons.TriTurretBalancing.TriTurretProjectileBalancing
import net.horizonsend.ion.server.configuration.serializer.SubsystemSerializer
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.damager.EntityDamager
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.horizonsend.ion.server.features.starship.subsystem.StarshipSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.checklist.BargeReactorSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.checklist.BattlecruiserReactorSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.checklist.CruiserReactorSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.checklist.FuelTankSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.WeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.AbyssalGazeSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.CapitalBeamWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.FlamethrowerWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.FlamingSkullCannon
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.MiniPhaserWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.PumpkinCannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.SonicMissileWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile.AbyssalGazeProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile.CapitalBeamCannonProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile.FlamethrowerProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile.FlamingSkullProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile.MiniPhaserProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile.PumpkinCannonProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile.SonicMissileProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.CycleTurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.DoomsdayDeviceWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.HeavyTurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.InterceptorCannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.IonTurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.LaserCannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.LightTurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.LogisticTurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.PlasmaCannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.PointDefenseSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.PulseCannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.QuadTurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.ArsenalRocketProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.CycleTurretProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.DoomsdayDeviceProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.HeavyLaserProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.InterceptorCannonProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.IonTurretProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.LaserCannonLaserProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.PhaserProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.PlasmaLaserProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.PointDefenseLaserProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.Projectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.PulseLaserProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.RocketProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.TorpedoProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.TurretLaserProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.ArsenalRocketStarshipWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.HeavyLaserWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.PhaserWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.RocketWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.TorpedoWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.TriTurretWeaponSubsystem
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.damage.DamageSource
import org.bukkit.damage.DamageType
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import java.util.LinkedList
import java.util.concurrent.TimeUnit
import kotlin.math.PI
import kotlin.reflect.KClass

@Serializable
data class NewStarshipBalancing(
	val weaponDefaults: WeaponDefaults = WeaponDefaults(),
	val shipClasses: ShipClasses = ShipClasses()
) {
	@Serializable
	data class WeaponDefaults(
		val weapons: List<StarshipWeapons.StarshipWeaponBalancing<*>> = listOf(
			StarshipWeapons.TorpedoBalancing(),
			StarshipWeapons.HeavyLaserBalancing(),
			StarshipWeapons.PhaserBalancing(),
			StarshipWeapons.ArsenalRocketBalancing(),
			StarshipWeapons.TriTurretBalancing(),
			StarshipWeapons.LightTurretBalancing(),
			StarshipWeapons.HeavyTurretBalancing(),
			StarshipWeapons.QuadTurretBalancing(),
			StarshipWeapons.IonTurretBalancing(),
			StarshipWeapons.PointDefenseBalancing(),
			StarshipWeapons.PulseCannonBalancing(),
			StarshipWeapons.PlasmaCannonBalancing(),
			StarshipWeapons.LaserCannonBalancing(),
			StarshipWeapons.InterceptorCannonBalancing(),

			// Event weapons
			StarshipWeapons.DoomsdayDeviceBalancing(),
			StarshipWeapons.RocketBalancing(),
			StarshipWeapons.LogisticsTurretBalancing(),
			StarshipWeapons.DisintegratorBeamBalancing(),
			StarshipWeapons.CycleTurretBalancing(),
			StarshipWeapons.AbyssalGazeBalancing(),
			StarshipWeapons.SonicMissileBalancing(),
			StarshipWeapons.PumpkinCannonBalancing(),
			StarshipWeapons.FlamethrowerCannonBalancing(),
			StarshipWeapons.MiniPhaserBalancing(),
			StarshipWeapons.CthulhuBeamBalancing(),
			StarshipWeapons.CapitalCannonBalancing()
		)
	)

	@Serializable
	data class ShipClasses(
		val speeder: NewStarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 3,
			maxSneakFlyAccel = 3,
			interdictionRange = 10,
			jumpStrength = 0.0,
			wellStrength = 0.0,
			hyperspaceRangeMultiplier = 3.0,
			shieldPowerMultiplier = 1.0
		),
		val shuttle: NewStarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 5,
			maxSneakFlyAccel = 2,
			interdictionRange = 300,
			jumpStrength = 1.0,
			wellStrength = 0.0,
			hyperspaceRangeMultiplier = 1.2,
			shieldPowerMultiplier = 1.0
		),
		val transport: NewStarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 10,
			maxSneakFlyAccel = 3,
			interdictionRange = 600,
			jumpStrength = 1.0,
			wellStrength = 1.0,
			hyperspaceRangeMultiplier = 1.25,
			shieldPowerMultiplier = 1.0
		),
		val lightFreighter: NewStarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 10,
			maxSneakFlyAccel = 3,
			interdictionRange = 900,
			jumpStrength = 1.0,
			wellStrength = 1.0,
			hyperspaceRangeMultiplier = 1.3,
			shieldPowerMultiplier = 1.0,
			weaponOverrides = listOf(
				StarshipWeapons.LightTurretBalancing(
					fireRestrictions = FireRestrictions(
						canFire = true,
						minBlockCount = 1750,
						maxBlockCount = 12000
					),
					firePowerConsumption = 5300,
				)
			),
		),
		val mediumFreighter: NewStarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 10,
			maxSneakFlyAccel = 3,
			interdictionRange = 1200,
			jumpStrength = 1.0,
			wellStrength = 1.0,
			hyperspaceRangeMultiplier = 1.35,
			shieldPowerMultiplier = 1.0
		),
		val heavyFreighter: NewStarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 10,
			maxSneakFlyAccel = 3,
			interdictionRange = 1500,
			jumpStrength = 1.0,
			wellStrength = 1.0,
			hyperspaceRangeMultiplier = 1.4,
			shieldPowerMultiplier = 1.0
		),
		val tank: NewStarshipTypeBalancing = GroundStarshipBalancing(
			sneakFlyAccelDistance = 4,
			maxSneakFlyAccel = 4,
			interdictionRange = 10,
			jumpStrength = 1.0,
			wellStrength = 0.0,
			hyperspaceRangeMultiplier = 1.5,
			shieldPowerMultiplier = 1.0
		),
		val starfighter: NewStarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 4,
			maxSneakFlyAccel = 4,
			interdictionRange = 10,
			jumpStrength = 0.0,
			wellStrength = 0.0,
			hyperspaceRangeMultiplier = 1.5,
			shieldPowerMultiplier = 1.0
		),
		val interceptor: NewStarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 4,
			maxSneakFlyAccel = 4,
			interdictionRange = 10,
			jumpStrength = 1.0,
			wellStrength = 0.0,
			hyperspaceRangeMultiplier = 0.0,
			shieldPowerMultiplier = 0.33,
			cruiseSpeedMultiplier = 1.1,
			weaponOverrides = listOf(
				StarshipWeapons.PlasmaCannonBalancing(fireRestrictions = FireRestrictions(canFire = false)),
				StarshipWeapons.LaserCannonBalancing(fireRestrictions = FireRestrictions(canFire = false)),
				StarshipWeapons.InterceptorCannonBalancing(fireRestrictions = FireRestrictions(canFire = true)),
				StarshipWeapons.TorpedoBalancing(fireRestrictions = FireRestrictions(canFire = false)),
			)
		),
		val gunship: NewStarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 5,
			maxSneakFlyAccel = 2,
			interdictionRange = 1200,
			jumpStrength = 1.0,
			wellStrength = 1.0,
			hyperspaceRangeMultiplier = 1.6,
			shieldPowerMultiplier = 1.0,
			weaponOverrides = listOf(
				StarshipWeapons.LightTurretBalancing(fireRestrictions = FireRestrictions(minBlockCount = 1750, maxBlockCount = 12000)),
				StarshipWeapons.PulseCannonBalancing(fireRestrictions = FireRestrictions(minBlockCount = 1000, maxBlockCount = 4000))
			)
		),
		val corvette: NewStarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 6,
			maxSneakFlyAccel = 2,
			interdictionRange = 1800,
			jumpStrength = 1.0,
			wellStrength = 1.0,
			hyperspaceRangeMultiplier = 1.7,
			shieldPowerMultiplier = 1.0,
			weaponOverrides = listOf(
				StarshipWeapons.LightTurretBalancing(fireRestrictions = FireRestrictions(canFire = true, maxBlockCount = 12000)),
				StarshipWeapons.TriTurretBalancing(fireRestrictions = FireRestrictions(canFire = true, minBlockCount = 3400)),
				StarshipWeapons.PulseCannonBalancing(fireRestrictions = FireRestrictions(canFire = true, minBlockCount = 1000, maxBlockCount = 4000)),
			)
		),
		val frigate: NewStarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 6,
			maxSneakFlyAccel = 2,
			interdictionRange = 2400,
			jumpStrength = 1.0,
			wellStrength = 1.0,
			hyperspaceRangeMultiplier = 1.8,
			shieldPowerMultiplier = 1.0,
		),
		val destroyer: NewStarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 5,
			maxSneakFlyAccel = 3,
			interdictionRange = 3000,
			jumpStrength = 1.0,
			wellStrength = 1.0,
			hyperspaceRangeMultiplier = 1.9,
			shieldPowerMultiplier = 1.0
		),
		val cruiser: NewStarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 5,
			maxSneakFlyAccel = 3,
			interdictionRange = 3500,
			jumpStrength = 2.0,
			wellStrength = 2.0,
			hyperspaceRangeMultiplier = 1.9,
			cruiseSpeedMultiplier = 0.98,
			shieldPowerMultiplier = 1.10,
			weaponOverrides = listOf(
				StarshipWeapons.IonTurretBalancing(fireRestrictions = FireRestrictions(canFire = true)),
				StarshipWeapons.HeavyTurretBalancing(fireRestrictions = FireRestrictions(canFire = false)),
				StarshipWeapons.ArsenalRocketBalancing(fireRestrictions = FireRestrictions(canFire = true)),
			),
			requiredMultiblocks = listOf(
				RequiredSubsystemInfo(
					FuelTankSubsystem::class.java,
					1,
					"Cruisers require a fuel tank to pilot!"
				),
				RequiredSubsystemInfo(
					CruiserReactorSubsystem::class.java,
					1,
					"Cruisers require a reactor to pilot!"
				)
			),
			standardSounds = StandardStarshipSounds(
				pilot = SoundInfo("horizonsend:starship.pilot.cruiser", volume = 5f),
				release = SoundInfo("horizonsend:starship.release.cruiser", volume = 5f),
				enterHyperspace = SoundInfo("horizonsend:starship.supercapital.hyperspace_enter"),
				explode = SoundInfo("horizonsend:starship.explosion.cruiser")
			)
		),
		val battlecruiser: NewStarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 3,
			maxSneakFlyAccel = 3,
			interdictionRange = 4500,
			jumpStrength = 3.0,
			wellStrength = 3.0,
			hyperspaceRangeMultiplier = 2.5,
			cruiseSpeedMultiplier = 0.88,
			shieldPowerMultiplier = 1.60,
			weaponOverrides = listOf(
				StarshipWeapons.QuadTurretBalancing(fireRestrictions = FireRestrictions(canFire = true)),
				StarshipWeapons.TriTurretBalancing(
					fireRestrictions = FireRestrictions(canFire = false),
					boostChargeNanos = TimeUnit.SECONDS.toNanos(7),
					projectile = TriTurretProjectileBalancing(speed = 110.0)
				),
				StarshipWeapons.ArsenalRocketBalancing(fireRestrictions = FireRestrictions(canFire = true)),
			),
			requiredMultiblocks = listOf(
				RequiredSubsystemInfo(
					BattlecruiserReactorSubsystem::class.java,
					1,
					"Battlecruisers require a reactor to pilot!"
				),
				RequiredSubsystemInfo(
					FuelTankSubsystem::class.java,
					1,
					"Battlecruisers require fuel to pilot!"
				)
			),
			standardSounds = StandardStarshipSounds(
				pilot = SoundInfo("horizonsend:starship.pilot.battlecruiser", volume = 7f),
				release = SoundInfo("horizonsend:starship.release.battlecruiser", volume = 7f),
				enterHyperspace = SoundInfo("horizonsend:starship.supercapital.hyperspace_enter"),
				explode = SoundInfo("horizonsend:starship.explosion.battlecruiser")
			)
		),
	)
}

@Serializable
sealed interface NewStarshipTypeBalancing {
	val canMove: Boolean
	val accelMultiplier: Double
	val maxSpeedMultiplier: Double

	val standardSounds: StandardStarshipSounds

	val sneakFlyAccelDistance: Int
	val maxSneakFlyAccel: Int
	val interdictionRange: Int
	val hyperspaceRangeMultiplier: Double
	val cruiseSpeedMultiplier: Double
	val shieldPowerMultiplier: Double

	val requiredMultiblocks: List<RequiredSubsystemInfo>

	val weaponOverrides: List<StarshipWeapons.StarshipWeaponBalancing<*>>
}

@Serializable
open class StanrdardStarshipTypeBalancing(
	override val canMove: Boolean = true,
	override val accelMultiplier: Double = 1.0,
	override val maxSpeedMultiplier: Double = 1.0,

	override val standardSounds: StandardStarshipSounds = StandardStarshipSounds(),

	override val sneakFlyAccelDistance: Int,
	override val maxSneakFlyAccel: Int,
	override val interdictionRange: Int,
	override val hyperspaceRangeMultiplier: Double,
	override val cruiseSpeedMultiplier: Double = 1.0,
	override val shieldPowerMultiplier: Double = 1.0,

	override val requiredMultiblocks: List<RequiredSubsystemInfo> = listOf(),

	override val weaponOverrides: List<StarshipWeapons.StarshipWeaponBalancing<*>> = listOf(),
) : NewStarshipTypeBalancing {

}

@Serializable
open class GroundStarshipBalancing(
	override val canMove: Boolean = true,
	override val accelMultiplier: Double = 1.0,
	override val maxSpeedMultiplier: Double = 1.0,

	override val standardSounds: StandardStarshipSounds = StandardStarshipSounds(),

	override val sneakFlyAccelDistance: Int,
	override val maxSneakFlyAccel: Int,
	override val interdictionRange: Int,
	override val hyperspaceRangeMultiplier: Double,
	override val cruiseSpeedMultiplier: Double = 1.0,
	override val shieldPowerMultiplier: Double = 1.0,

	override val requiredMultiblocks: List<RequiredSubsystemInfo> = listOf(),

	override val weaponOverrides: List<StarshipWeapons.StarshipWeaponBalancing<*>> = listOf(),
) : NewStarshipTypeBalancing {

}

@Serializable
data class StarshipTypeBalancing(

	val barge: StarshipBalancing = StarshipBalancing(
		sneakFlyAccelDistance = 3,
		maxSneakFlyAccel = 3,
		interdictionRange = 4500,
		jumpStrength = 2.0,
		wellStrength = 1.0,
		hyperspaceRangeMultiplier = 3.0,
		cruiseSpeedMultiplier = 0.88,
		shieldPowerMultiplier = 1.30,
		weapons = StarshipWeapons(

			triTurret = StarshipWeapons.StarshipWeapon(
				range = 500.0,
				speed = 110.0,
				areaShieldDamageMultiplier = 3.0,
				starshipShieldDamageMultiplier = 3.0,
				particleThickness = 0.8,
				explosionPower = 6f,
				volume = 1,
				pitch = 2.0f,
				soundName = "horizonsend:starship.weapon.turbolaser.tri.shoot",
				powerUsage = 45000,
				length = 0,
				angleRadiansHorizontal = 0.0,
				angleRadiansVertical = 0.0,
				convergeDistance = 0.0,
				extraDistance = 0,
				fireCooldownMillis = 10,
				boostChargeSeconds = 7,
				aimDistance = 0,
				inaccuracyRadians = 3.0,
				applyCooldownToAll = false,
				minBlockCount = 3400
			),
			heavyTurret = StarshipWeapons.StarshipWeapon(
				canFire = true,
				range = 500.0,
				speed = 200.0,
				areaShieldDamageMultiplier = 1.0,
				starshipShieldDamageMultiplier = 1.0,
				particleThickness = 0.3,
				explosionPower = 3.0f,
				volume = 0,
				pitch = 2.0f,
				soundName = "horizonsend:starship.weapon.turbolaser.heavy.shoot",
				powerUsage = 10000,
				length = 0,
				angleRadiansHorizontal = 0.0,
				angleRadiansVertical = 0.0,
				convergeDistance = 0.0,
				extraDistance = 0,
				fireCooldownMillis = 250,
				boostChargeSeconds = 0,
				applyCooldownToAll = true,
				aimDistance = 0,
				maxBlockCount = 20000,
				minBlockCount = 16500
			),

			),
		requiredMultiblocks = listOf(
			RequiredSubsystemInfo(
				BargeReactorSubsystem::class.java,
				1,
				"Barges require a reactor to pilot!"
			),
			RequiredSubsystemInfo(
				FuelTankSubsystem::class.java,
				1,
				"Barges require fuel to pilot!"
			)
		),
		sounds = StandardStarshipSounds(
			explode = SoundInfo("horizonsend:starship.explosion.battlecruiser")
		)
	),

	val battleship: StarshipBalancing = StarshipBalancing(
		sneakFlyAccelDistance = 3,
		maxSneakFlyAccel = 3,
		interdictionRange = 5200,
		jumpStrength = 5.0,
		wellStrength = 5.0,
		hyperspaceRangeMultiplier = 2.75,
		cruiseSpeedMultiplier = 0.80,
		shieldPowerMultiplier = 1.75,
		weapons = StarshipWeapons(
			quadTurret = StarshipWeapons.StarshipWeapon(
				canFire = true,
				range = 500.0,
				speed = 87.5,
				areaShieldDamageMultiplier = 150.0,
				starshipShieldDamageMultiplier = 7.6,
				particleThickness = 0.6,
				explosionPower = 5.5f,
				volume = 0,
				pitch = 2.0f,
				soundName = "horizonsend:starship.weapon.turbolaser.quad.shoot",
				powerUsage = 4500,
				length = 0,
				angleRadiansHorizontal = 0.0,
				angleRadiansVertical = 0.0,
				convergeDistance = 0.0,
				extraDistance = 1,
				fireCooldownMillis = 2500,
				aimDistance = 0,
				inaccuracyRadians = 2.0,
				maxPerShot = 5,
				applyCooldownToAll = true,
				minBlockCount = 30000
			),
			triTurret = StarshipWeapons.StarshipWeapon(
				range = 500.0,
				speed = 110.0,
				areaShieldDamageMultiplier = 3.0,
				starshipShieldDamageMultiplier = 3.0,
				particleThickness = 0.8,
				explosionPower = 6f,
				volume = 1,
				pitch = 2.0f,
				soundName = "horizonsend:starship.weapon.turbolaser.tri.shoot",
				powerUsage = 45000,
				length = 0,
				angleRadiansHorizontal = 0.0,
				angleRadiansVertical = 0.0,
				convergeDistance = 0.0,
				extraDistance = 0,
				fireCooldownMillis = 10,
				boostChargeSeconds = 7,
				aimDistance = 0,
				inaccuracyRadians = 3.0,
				applyCooldownToAll = false,
				minBlockCount = 3400
			),
			arsenalMissile = StarshipWeapons.StarshipWeapon(
				canFire = true,
				range = 375.0,
				speed = 50.0,
				areaShieldDamageMultiplier = 650.0,
				starshipShieldDamageMultiplier = 1.0,
				particleThickness = 0.5,
				explosionPower = 13.0f,
				volume = 10,
				pitch = 1.0f,
				soundName = "horizonsend:starship.weapon.arsenal_missile.shoot",
				powerUsage = 8000,
				length = 3,
				angleRadiansVertical = 100.0,
				angleRadiansHorizontal = 100.0,
				convergeDistance = 0.0,
				extraDistance = 0,
				fireCooldownMillis = 250,
				boostChargeSeconds = 10,
				aimDistance = 0,
				applyCooldownToAll = false,
				displayEntityCustomModelData = 1101,
				displayEntitySize = 1.0
			)
		)
	),
	val dreadnought: StarshipBalancing = StarshipBalancing(
		sneakFlyAccelDistance = 3,
		maxSneakFlyAccel = 2,
		interdictionRange = 6000,
		jumpStrength = 5.0,
		wellStrength = 5.0,
		hyperspaceRangeMultiplier = 3.0,
		cruiseSpeedMultiplier = 0.70,
		shieldPowerMultiplier = 2.0,
		weapons = StarshipWeapons(
			quadTurret = StarshipWeapons.StarshipWeapon(
				canFire = true,
				range = 500.0,
				speed = 87.5,
				areaShieldDamageMultiplier = 150.0,
				starshipShieldDamageMultiplier = 7.6,
				particleThickness = 0.6,
				explosionPower = 5.5f,
				volume = 0,
				pitch = 2.0f,
				soundName = "horizonsend:starship.weapon.turbolaser.quad.shoot",
				powerUsage = 4500,
				length = 0,
				angleRadiansHorizontal = 0.0,
				angleRadiansVertical = 0.0,
				convergeDistance = 0.0,
				extraDistance = 1,
				fireCooldownMillis = 2500,
				aimDistance = 0,
				inaccuracyRadians = 2.0,
				maxPerShot = 7,
				applyCooldownToAll = true,
				minBlockCount = 45000
			),
			triTurret = StarshipWeapons.StarshipWeapon(
				range = 500.0,
				speed = 110.0,
				areaShieldDamageMultiplier = 3.0,
				starshipShieldDamageMultiplier = 3.0,
				particleThickness = 0.8,
				explosionPower = 6f,
				volume = 1,
				pitch = 2.0f,
				soundName = "horizonsend:starship.weapon.turbolaser.tri.shoot",
				powerUsage = 45000,
				length = 0,
				angleRadiansHorizontal = 0.0,
				angleRadiansVertical = 0.0,
				convergeDistance = 0.0,
				extraDistance = 0,
				fireCooldownMillis = 10,
				boostChargeSeconds = 7,
				aimDistance = 0,
				inaccuracyRadians = 3.0,
				applyCooldownToAll = false,
				minBlockCount = 3400
			),
			arsenalMissile = StarshipWeapons.StarshipWeapon(
				canFire = true,
				range = 375.0,
				speed = 50.0,
				areaShieldDamageMultiplier = 650.0,
				starshipShieldDamageMultiplier = 1.0,
				particleThickness = 0.5,
				explosionPower = 13.0f,
				volume = 10,
				pitch = 1.0f,
				soundName = "horizonsend:starship.weapon.arsenal_missile.shoot",
				powerUsage = 8000,
				length = 3,
				angleRadiansVertical = 100.0,
				angleRadiansHorizontal = 100.0,
				convergeDistance = 0.0,
				extraDistance = 0,
				fireCooldownMillis = 250,
				boostChargeSeconds = 10,
				aimDistance = 0,
				applyCooldownToAll = false,
				displayEntityCustomModelData = 1101,
				displayEntitySize = 1.0
			)
		)
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
	override var soundName: String = "horizonsend:starship.weapon.turbolaser.tri.shoot",
	override var maxDegrees: Double = 360.0,
	override var displayEntityCustomModelData: Int? = null,
	override var displayEntitySize: Double? = null,
	override var delayMillis: Int? = null,
	override val entityDamage: EntityDamage = RegularDamage(10.0),
) : ProjectileBalancing

@Serializable
data class StarshipBalancing(
	var canMove: Boolean = true,
	var accelMultiplier: Double = 1.0,
	var maxSpeedMultiplier: Double = 1.0,
	var weapons: StarshipWeapons = StarshipWeapons(),

	val sneakFlyAccelDistance: Int,
	val maxSneakFlyAccel: Int,

	//interdiction
	val interdictionRange: Int,
	val jumpStrength: Double,
	val wellStrength: Double,

	val hyperspaceRangeMultiplier: Double,
	val cruiseSpeedMultiplier: Double = 1.0,
	val shieldPowerMultiplier: Double = 1.0,

	val requiredMultiblocks: List<RequiredSubsystemInfo> = listOf(),
	val sounds: StandardStarshipSounds = StandardStarshipSounds()
)

@Serializable
data class RequiredSubsystemInfo(
		@Serializable(with = SubsystemSerializer::class) val subsystem: Class<out @Contextual StarshipSubsystem>,
		val requiredAmount: Int,
		val failMessage: String
) {
	/**
	 * Tests whether the starship subsystems contain necessary multiblocks
	 **/
	fun checkRequirements(subsystems: LinkedList<StarshipSubsystem>): Boolean {
		return (subsystems.groupBy { it.javaClass }[subsystem]?.count() ?: 0) >= requiredAmount
	}
}

@Serializable
data class StandardStarshipSounds(
	val pilot: SoundInfo = SoundInfo("minecraft:block.beacon.activate", volume = 5f, pitch = 0.05f),
	val release: SoundInfo = SoundInfo("minecraft:block.beacon.deactivate", volume = 5f, pitch = 0.05f),
	val enterHyperspace: SoundInfo = SoundInfo("minecraft:entity.elder_guardian.hurt", volume = 5f, pitch = 0.05f),
	val exitHyperspace: SoundInfo = SoundInfo("minecraft:entity.warden.sonic_boom", pitch = 0f),
	val explode: SoundInfo? = null,
	val startCruise: SoundInfo = SoundInfo("minecraft:block.note_block.chime", volume = 5f, pitch = 0.53f),
	val stopCruise: SoundInfo = SoundInfo("minecraft:block.note_block.banjo", volume = 5f, pitch = 1.782f),
) {
	@Serializable
	data class SoundInfo(
		val key: String,
		val source: Sound.Source = Sound.Source.AMBIENT,
		val volume: Float = 1f,
		val pitch: Float = 1f
	) {
		@Transient
		val sound = Sound.sound(Key.key(key), source, volume, pitch)
	}
}

@Serializable
class StarshipWeapons(
) {
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

		override var angleRadiansVertical: Double,
		override var angleRadiansHorizontal: Double,
		override var convergeDistance: Double,

		override var fireCooldownMillis: Long,
		override var applyCooldownToAll: Boolean,
		override var maxPerShot: Int = 0,

		override var forwardOnly: Boolean = false,

		var boostChargeSeconds: Long = 0, // Seconds, should only be put for heavyWeapons
		var aimDistance: Int, // should only be put if the weapon in question is target tracking
		override var inaccuracyRadians: Double = 2.0,
		override var maxDegrees: Double = 0.0,
		override var displayEntityCustomModelData: Int? = null,
		override var displayEntitySize: Double? = null,

		override val entityDamage: EntityDamage = RegularDamage(9.0),

		override var delayMillis: Int? = null
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

		var displayEntityCustomModelData: Int?
		var displayEntitySize: Double?

		val entityDamage: EntityDamage

		var delayMillis: Int?

		@Serializable
		sealed interface EntityDamage {
			fun deal(target: LivingEntity, shooter: Damager, type: DamageType)

			fun getCause(source: Entity?, damageType: DamageType): DamageSource {
				val builder = DamageSource.builder(damageType)
				if (source != null) {
					builder.withDirectEntity(source)
					builder.withCausingEntity(source)
				}

				return builder.build()
			}
		}

		@Serializable
		data class RegularDamage(val amount: Double) : EntityDamage {
			override fun deal(target: LivingEntity, shooter: Damager, type: DamageType) {
				when (shooter) {
					is PlayerDamager -> target.damage(amount, getCause(shooter.player, type))
					is EntityDamager -> target.damage(amount, getCause(shooter.entity, type))
					else -> target.damage(amount, getCause(null, type))
				}
			}
		}

		@Serializable
		data object NoDamage : EntityDamage {
			override fun deal(target: LivingEntity, shooter: Damager, type: DamageType) {}
		}
	}

	@Serializable
	sealed interface SubSystem {
		var canFire: Boolean
		var minBlockCount: Int
		var maxBlockCount: Int

		var powerUsage: Int

		var length: Int
		var extraDistance: Int

		var angleRadiansVertical: Double
		var angleRadiansHorizontal: Double
		var convergeDistance: Double

		var fireCooldownMillis: Long
		var applyCooldownToAll: Boolean

		var maxPerShot: Int
		var forwardOnly: Boolean
		var inaccuracyRadians: Double
	}

	@Serializable
	sealed interface StarshipProjectileBalancing {
		val clazz: KClass<out Projectile>

		val range: Double
		val speed: Double

		/** The power of the impact explosion of this projectile. **/
		val explosionPower: Float

		/** The amount the explosion damage is multiplied when damaging a shield. **/
		val starshipShieldDamageMultiplier: Double

		/** The amount the explosion damage is multiplied when damaging a shield. **/
		val areaShieldDamageMultiplier: Double

		val entityDamage: EntityDamage

		val fireSound: SoundInfo
	}

	@Serializable
	sealed interface StarshipParticleProjectileBalancing : StarshipProjectileBalancing {
		val particleThickness: Double
	}

	@Serializable
	sealed interface StarshipTrackingProjectileBalancing : StarshipParticleProjectileBalancing {
		val maxDegrees: Double
	}

	@Serializable
	sealed interface StarshipArcedProjectileBalancing : StarshipParticleProjectileBalancing {
		val gravityMultiplier: Double
		val decelerationAmount: Double
	}

	@Serializable
	sealed interface StarshipWaveProjectileBalancing : StarshipParticleProjectileBalancing {
		val gravityMultiplier: Double
		val decelerationAmount: Double
		val separation: Double
	}

	@Serializable
	sealed interface StarshipWeaponBalancing<T : StarshipProjectileBalancing> {
		val clazz: KClass<out WeaponSubsystem<*>>
		val projectile: T

		/** Controls for which ships can fire this weapon **/
		val fireRestrictions: FireRestrictions

		val fireCooldownNanos: Long

		val firePowerConsumption: Int

		val isForwardOnly: Boolean

		val maxPerShot: Int?

		val applyCooldownToAll: Boolean

		/**
		 * @param canFire Whether this weapon can be fired.
		 * @param minBlockCount The minimum block count of a ship to be able to fire this weapon.
		 * @param maxBlockCount The maximum block count of a ship to be able to fire this weapon.
		 * **/
		@Serializable
		data class FireRestrictions(
			val canFire: Boolean = true,
			val minBlockCount: Int = 0,
			val maxBlockCount: Int = Int.MAX_VALUE
		)
	}

	@Serializable
	sealed interface StarshipCannonWeaponBalancing<T : StarshipProjectileBalancing> : StarshipWeaponBalancing<T> {
		val convergeDistance: Double

		val projectileSpawnDistance: Int

		val angleRadiansHorizontal: Double

		val angleRadiansVertical: Double
	}

	@Serializable
	sealed interface StarshipHeavyWeaponBalancing<T : StarshipProjectileBalancing> : StarshipWeaponBalancing<T> {
		val boostChargeNanos: Long
	}

	@Serializable
	sealed interface StarshipTurretWeaponBalancing<T : StarshipProjectileBalancing> : StarshipWeaponBalancing<T> {
		val inaccuracyRadians: Double
	}

	@Serializable
	sealed interface StarshipAutoWeaponBalancing<T : StarshipProjectileBalancing> : StarshipWeaponBalancing<T> {
		val range: Double
	}

	@Serializable
	sealed interface StarshipTrackingWeaponBalancing<T : StarshipProjectileBalancing> : StarshipCannonWeaponBalancing<T> {
		val aimDistance: Int
	}

	@Serializable
	data class TorpedoBalancing(
		override val fireRestrictions: FireRestrictions = FireRestrictions(),
		override val fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(10),
		override val firePowerConsumption: Int = 10000,
		override val isForwardOnly: Boolean = true,
		override val maxPerShot: Int? = null,

		override val convergeDistance: Double = 10.0,
		override val projectileSpawnDistance: Int = 10,
		override val angleRadiansHorizontal: Double = 10.0,
		override val angleRadiansVertical: Double = 10.0,
		override val aimDistance: Int = 3,
		override val applyCooldownToAll: Boolean = true,

		override val boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(10),

		override val projectile: TorpedoProjectileBalancing = TorpedoProjectileBalancing()
	): StarshipCannonWeaponBalancing<TorpedoBalancing.TorpedoProjectileBalancing>,
		StarshipHeavyWeaponBalancing<TorpedoBalancing.TorpedoProjectileBalancing>,
		StarshipTrackingWeaponBalancing<TorpedoBalancing.TorpedoProjectileBalancing> {
		@Transient
		override val clazz: KClass<out WeaponSubsystem<*>> = TorpedoWeaponSubsystem::class

		@Serializable
		data class TorpedoProjectileBalancing(
			override val range: Double = 300.0,
			override val speed: Double = 70.0,
			override val explosionPower: Float = 7.0f,
			override val starshipShieldDamageMultiplier: Double = 2.0,
			override val areaShieldDamageMultiplier: Double = 2.0,
			override val entityDamage: EntityDamage = RegularDamage(15.0),
			override val fireSound: SoundInfo = SoundInfo(key = "entity.firework_rocket.large_blast_far", volume = 10f, pitch = 0.75f),
			override val particleThickness: Double = 1.0,
			override val maxDegrees: Double = 45.0
		) : StarshipProjectileBalancing, StarshipTrackingProjectileBalancing {
			@Transient
			override val clazz: KClass<out Projectile> = TorpedoProjectile::class
		}
	}

	@Serializable
	data class HeavyLaserBalancing(
		override val fireRestrictions: FireRestrictions = FireRestrictions(),
		override val fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(250),
		override val firePowerConsumption: Int = 30_000,
		override val isForwardOnly: Boolean = false,
		override val maxPerShot: Int? = null,
		override val applyCooldownToAll: Boolean = false,

		override val convergeDistance: Double = 0.0,
		override val projectileSpawnDistance: Int = 1,
		override val angleRadiansHorizontal: Double = 0.0,
		override val angleRadiansVertical: Double = 0.0,

		override val boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(5),
		override val aimDistance: Int = 10,

		override val projectile: HeavyLaserProjectileBalancing = HeavyLaserProjectileBalancing(),
	) : StarshipCannonWeaponBalancing<HeavyLaserProjectileBalancing>,
		StarshipHeavyWeaponBalancing<HeavyLaserProjectileBalancing>,
		StarshipTrackingWeaponBalancing<HeavyLaserProjectileBalancing>  {
		@Transient
		override val clazz: KClass<out WeaponSubsystem<*>> = HeavyLaserWeaponSubsystem::class

		@Serializable
		data class HeavyLaserProjectileBalancing(
			override val range: Double = 200.0,
			override val speed: Double = 80.0,
			override val explosionPower: Float = 12f,
			override val starshipShieldDamageMultiplier: Double = 2.0,
			override val areaShieldDamageMultiplier: Double = 2.0,
			override val entityDamage: EntityDamage = RegularDamage(10.0),
			override val fireSound: SoundInfo = SoundInfo(key = "horizonsend:starship.weapon.heavy_laser.single.shoot", volume = 10f, pitch = 2f),
			override val particleThickness: Double = 1.0,
			override val maxDegrees: Double = 25.0
		) : StarshipProjectileBalancing, StarshipTrackingProjectileBalancing {
			@Transient
			override val clazz: KClass<out Projectile> = HeavyLaserProjectile::class
		}
	}

	@Serializable
	data class PhaserBalancing(
		override val fireRestrictions: FireRestrictions = FireRestrictions(minBlockCount = 12000),
		override val fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(10),
		override val firePowerConsumption: Int = 50000,
		override val isForwardOnly: Boolean = false,
		override val maxPerShot: Int? = null,
		override val applyCooldownToAll: Boolean = false,

		override val boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(3),

		override val convergeDistance: Double = 0.0,
		override val projectileSpawnDistance: Int = 0,
		override val angleRadiansHorizontal: Double = 180.0,
		override val angleRadiansVertical: Double = 180.0,

		override val projectile: PhaserProjectileBalancing = PhaserProjectileBalancing()
	) : StarshipCannonWeaponBalancing<PhaserProjectileBalancing>, StarshipHeavyWeaponBalancing<PhaserProjectileBalancing> {
		@Transient
		override val clazz: KClass<out WeaponSubsystem<*>> = PhaserWeaponSubsystem::class

		@Serializable
		data class PhaserProjectileBalancing(
			override val range: Double = 140.0,
			override val speed: Double = 1.0,
			override val explosionPower: Float = 2f,
			override val starshipShieldDamageMultiplier: Double = 55.0,
			override val areaShieldDamageMultiplier: Double = 5.0,
			override val entityDamage: EntityDamage = RegularDamage(10.0),
			override val fireSound: SoundInfo = SoundInfo(key = "horizonsend:starship.weapon.plasma_cannon.shoot", volume = 10f, pitch = 2.0f)
		) : StarshipProjectileBalancing {
			@Transient
			override val clazz: KClass<out Projectile> = PhaserProjectile::class
		}
	}

	@Serializable
	data class ArsenalRocketBalancing(
		override val fireRestrictions: FireRestrictions = FireRestrictions(canFire = false),
		override val fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(250),
		override val firePowerConsumption: Int = 8000,
		override val isForwardOnly: Boolean = false,
		override val maxPerShot: Int? = null,
		override val applyCooldownToAll: Boolean = false,

		override val boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(7),

		override val projectile: ArsenalRocketProjectileBalancing = ArsenalRocketProjectileBalancing()
	) : StarshipHeavyWeaponBalancing<ArsenalRocketProjectileBalancing> {
		@Transient
		override val clazz: KClass<out WeaponSubsystem<*>> = ArsenalRocketStarshipWeaponSubsystem::class

		@Serializable
		data class ArsenalRocketProjectileBalancing(
			override val range: Double = 700.0,
			override val speed: Double = 50.0,
			override val explosionPower: Float = 3f,
			override val starshipShieldDamageMultiplier: Double = 1.0,
			override val areaShieldDamageMultiplier: Double = 5.0,
			override val entityDamage: EntityDamage = RegularDamage(10.0),
			override val fireSound: SoundInfo = SoundInfo(key = "horizonsend:starship.weapon.arsenal_missile.shoot", volume = 10f, pitch = 1f)
		) : StarshipProjectileBalancing {
			@Transient
			override val clazz: KClass<out Projectile> = ArsenalRocketProjectile::class
		}
	}

	@Serializable
	data class TriTurretBalancing(
		override val fireRestrictions: FireRestrictions = FireRestrictions(),
		override val fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(10),
		override val firePowerConsumption: Int = 45000,
		override val isForwardOnly: Boolean = false,
		override val inaccuracyRadians: Double = 3.0,
		override val range: Double= 500.0,
		override val boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(3),
		override val maxPerShot: Int? = null,
		override val applyCooldownToAll: Boolean = false,

		override val projectile: TriTurretProjectileBalancing = TriTurretProjectileBalancing()
	) : StarshipTurretWeaponBalancing<TriTurretProjectileBalancing>, StarshipAutoWeaponBalancing<TriTurretProjectileBalancing>, StarshipHeavyWeaponBalancing<TriTurretProjectileBalancing> {
		@Transient
		override val clazz: KClass<out WeaponSubsystem<*>> = TriTurretWeaponSubsystem::class

		@Serializable
		data class TriTurretProjectileBalancing(
			override val range: Double = 500.0,
			override val speed: Double = 125.0,
			override val explosionPower: Float = 6f,
			override val starshipShieldDamageMultiplier: Double = 3.0,
			override val areaShieldDamageMultiplier: Double = 3.0,
			override val entityDamage: EntityDamage = RegularDamage(10.0),
			override val fireSound: SoundInfo = SoundInfo(key = "horizonsend:starship.weapon.turbolaser.tri.shoot", volume = 1f, pitch = 2.0f),
			override val particleThickness: Double = 0.8
		) : StarshipParticleProjectileBalancing {
			@Transient
			override val clazz: KClass<out Projectile> = TurretLaserProjectile::class
		}
	}

	@Serializable
	data class LightTurretBalancing(
		override val fireRestrictions: FireRestrictions = FireRestrictions(
			canFire = true,
			minBlockCount = 0,
			maxBlockCount = 12000
		),
		override val fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(250),
		override val firePowerConsumption: Int = 6000,
		override val isForwardOnly: Boolean = false,
		override val inaccuracyRadians: Double = 2.0,
		override val range: Double = 200.0,
		override val maxPerShot: Int? = null,
		override val applyCooldownToAll: Boolean = true,
		override val projectile: LightTurretProjectileBalancing = LightTurretProjectileBalancing()
	) : StarshipTurretWeaponBalancing<LightTurretProjectileBalancing>, StarshipAutoWeaponBalancing<LightTurretProjectileBalancing> {
		@Transient
		override val clazz: KClass<out WeaponSubsystem<*>> = LightTurretWeaponSubsystem::class

		@Serializable
		data class LightTurretProjectileBalancing(
			override val range: Double = 200.0,
			override val speed: Double = 250.0,
			override val explosionPower: Float = 4f,
			override val starshipShieldDamageMultiplier: Double = 2.0,
			override val areaShieldDamageMultiplier: Double = 2.0,
			override val entityDamage: EntityDamage = RegularDamage(7.0),
			override val fireSound: SoundInfo = SoundInfo(key = "horizonsend:starship.weapon.turbolaser.light.shoot", volume = 10f, pitch = 2f),
			override val particleThickness: Double = 0.3
		) : StarshipParticleProjectileBalancing {
			@Transient
			override val clazz: KClass<out Projectile> = TurretLaserProjectile::class
		}
	}

	@Serializable
	data class HeavyTurretBalancing(
		override val fireRestrictions: FireRestrictions = FireRestrictions(canFire = true, minBlockCount = 6500, maxBlockCount = 12000),
		override val fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(250),
		override val firePowerConsumption: Int = 8000,
		override val isForwardOnly: Boolean = false,
		override val inaccuracyRadians: Double = 2.0,
		override val maxPerShot: Int? = null,
		override val applyCooldownToAll: Boolean = true,

		override val projectile: HeavyTurretProjectileBalancing = HeavyTurretProjectileBalancing()
	) : StarshipTurretWeaponBalancing<HeavyTurretProjectileBalancing> {
		@Transient
		override val clazz: KClass<out WeaponSubsystem<*>> = HeavyTurretWeaponSubsystem::class

		@Serializable
		data class HeavyTurretProjectileBalancing(
			override val range: Double = 500.0,
			override val speed: Double = 200.0,
			override val explosionPower: Float = 3f,
			override val starshipShieldDamageMultiplier: Double = 1.0,
			override val areaShieldDamageMultiplier: Double = 1.0,
			override val entityDamage: EntityDamage = RegularDamage(10.0),
			override val fireSound: SoundInfo = SoundInfo(key = "horizonsend:starship.weapon.turbolaser.heavy.shoot", volume = 0f, pitch = 2.0f),
			override val particleThickness: Double = 0.3
		) : StarshipParticleProjectileBalancing {
			@Transient
			override val clazz: KClass<out Projectile> = TurretLaserProjectile::class
		}
	}

	@Serializable
	data class QuadTurretBalancing(
		override val fireRestrictions: FireRestrictions = FireRestrictions(canFire = false, minBlockCount = 18500),
		override val fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(3000),
		override val firePowerConsumption: Int = 3000,
		override val isForwardOnly: Boolean = false,
		override val inaccuracyRadians: Double = 2.0,
		override val maxPerShot: Int = 3,
		override val applyCooldownToAll: Boolean = true,

		override val projectile: QuadTurretProjectileBalancing = QuadTurretProjectileBalancing()
	) : StarshipTurretWeaponBalancing<QuadTurretProjectileBalancing> {
		@Transient
		override val clazz: KClass<out WeaponSubsystem<*>> = QuadTurretWeaponSubsystem::class

		@Serializable
		data class QuadTurretProjectileBalancing(
			override val range: Double = 500.0,
			override val speed: Double = 55.0,
			override val explosionPower: Float = 5f,
			override val starshipShieldDamageMultiplier: Double = 6.3,
			override val areaShieldDamageMultiplier: Double = 6.0,
			override val entityDamage: EntityDamage = RegularDamage(10.0),
			override val fireSound: SoundInfo = SoundInfo(key = "horizonsend:starship.weapon.turbolaser.quad.shoot", volume = 0f, pitch = 2f),
			override val particleThickness: Double = 0.6
		) : StarshipParticleProjectileBalancing {
			@Transient
			override val clazz: KClass<out Projectile> = TurretLaserProjectile::class
		}
	}

	@Serializable
	data class IonTurretBalancing(
		override val fireRestrictions: FireRestrictions = FireRestrictions(canFire = false, minBlockCount = 13500, maxBlockCount = 16000),
		override val fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(1500),
		override val firePowerConsumption: Int = 3000,
		override val isForwardOnly: Boolean = false,
		override val inaccuracyRadians: Double = 1.0,
		override val maxPerShot: Int = 4,
		override val applyCooldownToAll: Boolean = true,

		override val projectile: IonTurretProjectileBalancing = IonTurretProjectileBalancing()
	) : StarshipTurretWeaponBalancing<IonTurretProjectileBalancing> {
		@Transient
		override val clazz: KClass<out WeaponSubsystem<*>> = IonTurretWeaponSubsystem::class

		@Serializable
		data class IonTurretProjectileBalancing(
			override val range: Double = 500.0,
			override val speed: Double = 105.0,
			override val explosionPower: Float = 3f,
			override val starshipShieldDamageMultiplier: Double = 3.7,
			override val areaShieldDamageMultiplier: Double = 60.0,
			override val entityDamage: EntityDamage = RegularDamage(10.0),
			override val fireSound: SoundInfo = SoundInfo(key = "horizonsend:starship.weapon.turbolaser.ion.shoot", volume = 0f, pitch = 2f),
			override val particleThickness: Double = 0.6
		) : StarshipParticleProjectileBalancing {
			@Transient
			override val clazz: KClass<out Projectile> = IonTurretProjectile::class
		}
	}

	@Serializable
	data class PointDefenseBalancing(
		override val fireRestrictions: FireRestrictions = FireRestrictions(),
		override val fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(10),
		override val firePowerConsumption: Int = 500,
		override val isForwardOnly: Boolean = false,
		override val range: Double = 120.0,
		override val maxPerShot: Int? = null,
		override val applyCooldownToAll: Boolean = true,

		override val projectile: PointDefenseProjectileBalancing = PointDefenseProjectileBalancing()
	) : StarshipWeaponBalancing<PointDefenseProjectileBalancing>, StarshipAutoWeaponBalancing<PointDefenseProjectileBalancing>{
		@Transient
		override val clazz: KClass<out WeaponSubsystem<*>> = PointDefenseSubsystem::class

		@Serializable
		data class PointDefenseProjectileBalancing(
			override val range: Double = 120.0,
			override val speed: Double = 150.0,
			override val explosionPower: Float = 0.0f,
			override val starshipShieldDamageMultiplier: Double = 0.0,
			override val areaShieldDamageMultiplier: Double = 2.0,
			override val entityDamage: EntityDamage = RegularDamage(10.0),
			override val fireSound: SoundInfo = SoundInfo(key = "entity.firework_rocket.large_blast", volume = 10f, pitch = 2f),
			override val particleThickness: Double = 0.35
		) : StarshipParticleProjectileBalancing {
			@Transient
			override val clazz: KClass<out Projectile> = PointDefenseLaserProjectile::class
		}
	}

	@Serializable
	data class PulseCannonBalancing(
		override val fireRestrictions: FireRestrictions = FireRestrictions(canFire = false, minBlockCount = 1000, maxBlockCount = 4000),
		override val fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(250),
		override val firePowerConsumption: Int = 2550,
		override val isForwardOnly: Boolean = false,
		override val maxPerShot: Int? = null,
		override val applyCooldownToAll: Boolean = true,

		override val convergeDistance: Double = 16.0,
		override val projectileSpawnDistance: Int = 3,
		override val angleRadiansHorizontal: Double = 180.0,
		override val angleRadiansVertical: Double = 180.0,

		override val projectile: PulseCannonProjectileBalancing = PulseCannonProjectileBalancing()
	) : StarshipCannonWeaponBalancing<PulseCannonProjectileBalancing> {
		@Transient
		override val clazz: KClass<out WeaponSubsystem<*>> = PulseCannonWeaponSubsystem::class

		@Serializable
		data class PulseCannonProjectileBalancing(
			override val range: Double = 180.0,
			override val speed: Double = 400.0,
			override val explosionPower: Float = 1.875f,
			override val starshipShieldDamageMultiplier: Double = 2.0,
			override val areaShieldDamageMultiplier: Double = 2.0,
			override val entityDamage: EntityDamage = RegularDamage(10.0),
			override val fireSound: SoundInfo = SoundInfo(key = "entity.firework_rocket.blast_far", volume = 10f, pitch = 0.5f),
			override val particleThickness: Double = 0.4
		) : StarshipParticleProjectileBalancing {
			@Transient
			override val clazz: KClass<out Projectile> = PulseLaserProjectile::class
		}
	}

	@Serializable
	data class PlasmaCannonBalancing(
		override val fireRestrictions: FireRestrictions = FireRestrictions(),
		override val fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(250),
		override val firePowerConsumption: Int = 2500,
		override val isForwardOnly: Boolean = true,
		override val maxPerShot: Int = 2,
		override val applyCooldownToAll: Boolean = true,

		override val convergeDistance: Double = 10.0,
		override val projectileSpawnDistance: Int = 1,
		override val angleRadiansHorizontal: Double = 15.0,
		override val angleRadiansVertical: Double = 15.0,

		override val projectile: PlasmaCannonProjectileBalancing = PlasmaCannonProjectileBalancing()
	) : StarshipCannonWeaponBalancing<PlasmaCannonProjectileBalancing> {
		@Transient
		override val clazz: KClass<out WeaponSubsystem<*>> = PlasmaCannonWeaponSubsystem::class

		@Serializable
		data class PlasmaCannonProjectileBalancing(
			override val range: Double = 160.0,
			override val speed: Double = 400.0,
			override val explosionPower: Float = 4f,
			override val starshipShieldDamageMultiplier: Double = 3.0,
			override val areaShieldDamageMultiplier: Double = 3.0,
			override val entityDamage: EntityDamage = RegularDamage(10.0),
			override val fireSound: SoundInfo = SoundInfo(key = "horizonsend:starship.weapon.plasma_cannon.shoot", volume = 10f, pitch = 1f),
			override val particleThickness: Double = 0.5
		) : StarshipParticleProjectileBalancing {
			@Transient
			override val clazz: KClass<out Projectile> = PlasmaLaserProjectile::class
		}
	}

	@Serializable
	data class LaserCannonBalancing(
		override val fireRestrictions: FireRestrictions = FireRestrictions(),
		override val fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(250),
		override val firePowerConsumption: Int = 600,
		override val isForwardOnly: Boolean = false,
		override val maxPerShot: Int? = null,
		override val applyCooldownToAll: Boolean = true,

		override val convergeDistance: Double = 20.0,
		override val projectileSpawnDistance: Int = 2,
		override val angleRadiansHorizontal: Double = 17.0,
		override val angleRadiansVertical: Double = 17.0,

		override val projectile: LaserCannonProjectileBalancing = LaserCannonProjectileBalancing()
	) : StarshipCannonWeaponBalancing<LaserCannonProjectileBalancing> {
		@Transient
		override val clazz: KClass<out WeaponSubsystem<*>> = LaserCannonWeaponSubsystem::class

		@Serializable
		data class LaserCannonProjectileBalancing(
			override val range: Double = 200.0,
			override val speed: Double = 250.0,
			override val explosionPower: Float = 2f,
			override val starshipShieldDamageMultiplier: Double = 0.3,
			override val areaShieldDamageMultiplier: Double = 1.0,
			override val entityDamage: EntityDamage = RegularDamage(10.0),
			override val fireSound: SoundInfo = SoundInfo(key = "entity.firework_rocket.blast_far", volume = 10f, pitch = 2f),
			override val particleThickness: Double = 0.44
		) : StarshipParticleProjectileBalancing {
			@Transient
			override val clazz: KClass<out Projectile> = LaserCannonLaserProjectile::class
		}
	}

	@Serializable
	data class InterceptorCannonBalancing(
		override val fireRestrictions: FireRestrictions = FireRestrictions(canFire = false),
		override val fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(250),
		override val firePowerConsumption: Int = 160,
		override val isForwardOnly: Boolean = true,
		override val maxPerShot: Int? = null,
		override val applyCooldownToAll: Boolean = true,

		override val convergeDistance: Double = 20.0,
		override val projectileSpawnDistance: Int = 3,
		override val angleRadiansHorizontal: Double = 180.0,
		override val angleRadiansVertical: Double = 180.0,

		override val projectile: IncterceptorCannonProjectileBalancing = IncterceptorCannonProjectileBalancing()
	) : StarshipCannonWeaponBalancing<IncterceptorCannonProjectileBalancing> {
		@Transient
		override val clazz: KClass<out WeaponSubsystem<*>> = InterceptorCannonWeaponSubsystem::class

		@Serializable
		data class IncterceptorCannonProjectileBalancing(
			override val range: Double = 200.0,
			override val speed: Double = 250.0,
			override val explosionPower: Float = 0.1f,
			override val starshipShieldDamageMultiplier: Double = 1.0,
			override val areaShieldDamageMultiplier: Double = 1.0,
			override val entityDamage: EntityDamage = RegularDamage(10.0),
			override val fireSound: SoundInfo = SoundInfo(key = "entity.firework_rocket.blast_far", volume = 10f, pitch = 2f),
			override val particleThickness: Double = 0.44
		) : StarshipParticleProjectileBalancing {
			@Transient
			override val clazz: KClass<out Projectile> = InterceptorCannonProjectile::class
		}
	}

	// Start Event Weapons
	@Serializable
	data class DoomsdayDeviceBalancing(
		override val fireRestrictions: FireRestrictions = FireRestrictions(),
		override val fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(10000),
		override val firePowerConsumption: Int = 50000,
		override val isForwardOnly: Boolean = false,
		override val maxPerShot: Int = 1,
		override val applyCooldownToAll: Boolean = true,

		override val convergeDistance: Double = 0.0,
		override val projectileSpawnDistance: Int = 1,
		override val angleRadiansHorizontal: Double = 70.0,
		override val angleRadiansVertical: Double = 70.0,
		override val boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(25),

		override val projectile: DoomsdayDeviceProjectileBalancing = DoomsdayDeviceProjectileBalancing(),
	) : StarshipCannonWeaponBalancing<DoomsdayDeviceProjectileBalancing>, StarshipHeavyWeaponBalancing<DoomsdayDeviceProjectileBalancing> {
		@Transient
		override val clazz: KClass<out WeaponSubsystem<*>> = DoomsdayDeviceWeaponSubsystem::class

		@Serializable
		data class DoomsdayDeviceProjectileBalancing(
			override val range: Double = 500.0,
			override val speed: Double = 400.0,
			override val explosionPower: Float = 10f,
			override val starshipShieldDamageMultiplier: Double = 100.0,
			override val areaShieldDamageMultiplier: Double = 100.0,
			override val entityDamage: EntityDamage = RegularDamage(10.0),
			override val fireSound: SoundInfo = SoundInfo(key = "horizonsend:starship.weapon.turbolaser.light.shoot", volume = 0f),
			override val particleThickness: Double = 5.0
		) : StarshipParticleProjectileBalancing {
			@Transient
			override val clazz: KClass<out Projectile> = DoomsdayDeviceProjectile::class
		}
	}

	@Serializable
	data class RocketBalancing(
		override val fireRestrictions: FireRestrictions = FireRestrictions(),
		override val fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(250),
		override val firePowerConsumption: Int = 50000,
		override val isForwardOnly: Boolean = false,
		override val maxPerShot: Int? = null	,
		override val applyCooldownToAll: Boolean = false,

		override val convergeDistance: Double = 0.0,
		override val projectileSpawnDistance: Int = 0,
		override val angleRadiansHorizontal: Double = 0.0,
		override val angleRadiansVertical: Double = 0.0,
		override val boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(7),

		override val projectile: RocketProjectileBalancing = RocketProjectileBalancing(),
	) : StarshipCannonWeaponBalancing<RocketProjectileBalancing>, StarshipHeavyWeaponBalancing<RocketProjectileBalancing> {
		@Transient
		override val clazz: KClass<out WeaponSubsystem<*>> = RocketWeaponSubsystem::class

		@Serializable
		data class RocketProjectileBalancing(
			override val range: Double = 300.0,
			override val speed: Double = 5.0,
			override val explosionPower: Float = 10f,
			override val starshipShieldDamageMultiplier: Double = 5.0,
			override val areaShieldDamageMultiplier: Double = 5.0,
			override val entityDamage: EntityDamage = RegularDamage(10.0),
			override val fireSound: SoundInfo = SoundInfo(key = "horizonsend:starship.weapon.rocket.shoot", volume = 10f),
		) : StarshipProjectileBalancing {
			@Transient
			override val clazz: KClass<out Projectile> = RocketProjectile::class
		}
	}

	@Serializable
	data class LogisticsTurretBalancing(
		override val fireRestrictions: FireRestrictions = FireRestrictions(),
		override val fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(500),
		override val firePowerConsumption: Int = 100,
		override val isForwardOnly: Boolean = false,
		override val maxPerShot: Int = 1,
		override val applyCooldownToAll: Boolean = true,

		override val inaccuracyRadians: Double = 0.5,

		override val projectile: LogisticsTurretProjectileBalancing = LogisticsTurretProjectileBalancing(),
	) : StarshipTurretWeaponBalancing<LogisticsTurretProjectileBalancing> {
		@Transient
		override val clazz: KClass<out WeaponSubsystem<*>> = LogisticTurretWeaponSubsystem::class

		@Serializable
		data class LogisticsTurretProjectileBalancing(
			override val range: Double = 200.0,
			override val speed: Double = 2000.0,
			override val explosionPower: Float = 0f,
			override val starshipShieldDamageMultiplier: Double = 0.0,
			override val areaShieldDamageMultiplier: Double = 0.0,
			override val entityDamage: EntityDamage = RegularDamage(10.0),
			override val fireSound: SoundInfo = SoundInfo(key = "horizonsend:starship.weapon.turbolaser.light.shoot"),
			override val particleThickness: Double = 1.0,
			val shieldBoostFactor: Int = 50000
		) : StarshipParticleProjectileBalancing {
			@Transient
			override val clazz: KClass<out Projectile> = RocketProjectile::class
		}
	}

	@Serializable
	data class DisintegratorBeamBalancing(
		override val fireRestrictions: FireRestrictions = FireRestrictions(),
		override val fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(100),
		override val firePowerConsumption: Int = 100,
		override val isForwardOnly: Boolean = false,
		override val maxPerShot: Int = 6,
		override val applyCooldownToAll: Boolean = true	,

		val inaccuracyRadians: Double = 0.01,

		override val projectile: DisintegratorBeamProjectileBalancing = DisintegratorBeamProjectileBalancing(),
	) : StarshipWeaponBalancing<DisintegratorBeamProjectileBalancing> {
		@Transient
		override val clazz: KClass<out WeaponSubsystem<*>> = RocketWeaponSubsystem::class

		@Serializable
		data class DisintegratorBeamProjectileBalancing(
			override val range: Double = 2000.0,
			override val speed: Double = 100.0,
			override val explosionPower: Float = 1f,
			override val starshipShieldDamageMultiplier: Double = 1.0,
			override val areaShieldDamageMultiplier: Double = 1.0,
			override val entityDamage: EntityDamage = RegularDamage(10.0),
			override val fireSound: SoundInfo = SoundInfo(key = "horizonsend:starship.weapon.turbolaser.light.shoot", volume = 0f, pitch = 1f),
			override val particleThickness: Double = 0.5,
		) : StarshipParticleProjectileBalancing {
			@Transient
			override val clazz: KClass<out Projectile> = RocketProjectile::class
		}
	}

	@Serializable
	data class CycleTurretBalancing(
		override val fireRestrictions: FireRestrictions = FireRestrictions(),
		override val fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(500),
		override val firePowerConsumption: Int = 100,
		override val isForwardOnly: Boolean = false,
		override val maxPerShot: Int = 3,
		override val inaccuracyRadians: Double = 0.5,
		override val applyCooldownToAll: Boolean = true,

		override val projectile: CycleTurretProjectileBalancing = CycleTurretProjectileBalancing()
	) : StarshipTurretWeaponBalancing<CycleTurretProjectileBalancing> {
		@Transient
		override val clazz: KClass<out WeaponSubsystem<*>> = CycleTurretWeaponSubsystem::class

		@Serializable
		data class CycleTurretProjectileBalancing(
			override val range: Double = 275.0,
			override val speed: Double = 1800.0,
			override val explosionPower: Float = 2f,
			override val starshipShieldDamageMultiplier: Double = 0.75,
			override val areaShieldDamageMultiplier: Double = 1.0,
			override val entityDamage: EntityDamage = RegularDamage(10.0),
			override val fireSound: SoundInfo = SoundInfo(key = "horizonsend:starship.weapon.turbolaser.light.shoot", volume = 0f, pitch = 1f),
			override val particleThickness: Double = 0.25,
			val delayMillis: Int = 250,
		) : StarshipParticleProjectileBalancing {
			@Transient
			override val clazz: KClass<out Projectile> = CycleTurretProjectile::class
		}
	}

	@Serializable
	data class AbyssalGazeBalancing(
		override val fireRestrictions: FireRestrictions = FireRestrictions(
			canFire = false,
			minBlockCount = 1,
			maxBlockCount = Int.MAX_VALUE
		),
		override val fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(1000),
		override val firePowerConsumption: Int = 10000,
		override val isForwardOnly: Boolean = false,
		override val maxPerShot: Int? = null,
		override val applyCooldownToAll: Boolean = false,

		override val convergeDistance: Double = 0.0,
		override val projectileSpawnDistance: Int = 3,
		override val angleRadiansHorizontal: Double = 18.0,
		override val angleRadiansVertical: Double = 18.0,

		override val projectile: AbyssalGazeProjectileBalancing = AbyssalGazeProjectileBalancing()
	) : StarshipCannonWeaponBalancing<AbyssalGazeProjectileBalancing> {
		@Transient
		override val clazz: KClass<out WeaponSubsystem<*>> = AbyssalGazeSubsystem::class

		@Serializable
		data class AbyssalGazeProjectileBalancing(
			override val range: Double = 500.0,
			override val speed: Double = 50.0,
			override val explosionPower: Float = 2.5f,
			override val starshipShieldDamageMultiplier: Double =  1.25,
			override val areaShieldDamageMultiplier: Double = 1.0,
			override val entityDamage: EntityDamage = RegularDamage(10.0),
			override val fireSound: SoundInfo = SoundInfo(key = "item.trident.riptide_1", volume = 10f, pitch = 2f),
			override val particleThickness: Double = 0.0,
			override val maxDegrees: Double = 10.0
		) : StarshipTrackingProjectileBalancing {
			@Transient
			override val clazz: KClass<out Projectile> = AbyssalGazeProjectile::class
		}
	}

	@Serializable
	data class SonicMissileBalancing(
		override val fireRestrictions: FireRestrictions = FireRestrictions(
			canFire = false,
			minBlockCount = 1,
			maxBlockCount = Int.MAX_VALUE
		),
		override val fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(5000),
		override val firePowerConsumption: Int = 70000,
		override val isForwardOnly: Boolean = false,
		override val maxPerShot: Int? = null,
		override val applyCooldownToAll: Boolean = false,

		override val convergeDistance: Double = 0.0,
		override val projectileSpawnDistance: Int = 0,
		override val angleRadiansHorizontal: Double = 18.0,
		override val angleRadiansVertical: Double = 18.0,
		override val boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(5),

		override val projectile: SonicMissileProjectileBalancing = SonicMissileProjectileBalancing(),
	) : StarshipCannonWeaponBalancing<SonicMissileProjectileBalancing>, StarshipHeavyWeaponBalancing<SonicMissileProjectileBalancing> {
		@Transient
		override val clazz: KClass<out WeaponSubsystem<*>> = SonicMissileWeaponSubsystem::class

		@Serializable
		data class SonicMissileProjectileBalancing(
			override val range: Double= 300.0,
			override val speed: Double= 200.0,
			override val explosionPower: Float = 15.0f,
			override val starshipShieldDamageMultiplier: Double = 10.0,
			override val areaShieldDamageMultiplier: Double = 10.0,
			override val entityDamage: EntityDamage = RegularDamage(10.0),
			override val fireSound: SoundInfo = SoundInfo(key = "entity.warden.sonic_boom"),
			override val particleThickness: Double = 0.0,
		) : StarshipParticleProjectileBalancing {
			@Transient
			override val clazz: KClass<out Projectile> = SonicMissileProjectile::class
		}
	}

	@Serializable
	data class PumpkinCannonBalancing(
		override val fireRestrictions: FireRestrictions = FireRestrictions(
			canFire = false,
			minBlockCount = 1,
			maxBlockCount = Int.MAX_VALUE
		),
		override val fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(10),
		override val firePowerConsumption: Int = 15000,
		override val isForwardOnly: Boolean = false,
		override val maxPerShot: Int? = null,
		override val applyCooldownToAll: Boolean = false,

		override val projectile: PumpkinCannonProjectileBalancing = PumpkinCannonProjectileBalancing(),
	) : StarshipWeaponBalancing<PumpkinCannonProjectileBalancing> {
		@Transient
		override val clazz: KClass<out WeaponSubsystem<*>> = PumpkinCannonWeaponSubsystem::class

		@Serializable
		data class PumpkinCannonProjectileBalancing(
			override val range: Double = 500.0,
			override val speed: Double = 125.0,
			override val explosionPower: Float = 1.0f,
			override val starshipShieldDamageMultiplier: Double = 1.0,
			override val areaShieldDamageMultiplier: Double = 1.0,
			override val entityDamage: EntityDamage = RegularDamage(10.0),
			override val fireSound: SoundInfo = SoundInfo(key = "entity.firework_rocket.blast_far", volume = 0f, pitch = 2.0f)
		) : StarshipProjectileBalancing {
			@Transient
			override val clazz: KClass<out Projectile> = PumpkinCannonProjectile::class
		}
	}

	@Serializable
	data class FlamingSkullCannonBalancing(
		override val fireRestrictions: FireRestrictions = FireRestrictions(
			canFire = false,
			minBlockCount = 1,
			maxBlockCount = Int.MAX_VALUE
		),
		override val fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(5000),
		override val firePowerConsumption: Int = 70000,
		override val isForwardOnly: Boolean = false,
		override val maxPerShot: Int? = null,
		override val applyCooldownToAll: Boolean = false,

		override val convergeDistance: Double = 0.0,
		override val projectileSpawnDistance: Int = 5,
		override val angleRadiansHorizontal: Double = 18.0,
		override val angleRadiansVertical: Double = 18.0,
		override val boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(5),

		override val projectile: FlamingSkullCannonProjectileBalancing,
	) : StarshipCannonWeaponBalancing<FlamingSkullCannonProjectileBalancing>, StarshipHeavyWeaponBalancing<FlamingSkullCannonProjectileBalancing> {
		@Transient
		override val clazz: KClass<out WeaponSubsystem<*>> = FlamingSkullCannon::class

		@Serializable
		data class FlamingSkullCannonProjectileBalancing(
			override val range: Double = 500.0,
			override val speed: Double = 200.0,
			override val explosionPower: Float = 15f,
			override val starshipShieldDamageMultiplier: Double = 10.0,
			override val areaShieldDamageMultiplier: Double = 10.0,
			override val entityDamage: EntityDamage = RegularDamage(10.0),
			override val fireSound: SoundInfo = SoundInfo(key = "entity.warden.sonic_boom", volume = 10f, pitch = 2f),
			override val particleThickness: Double = 0.0,
			override val maxDegrees: Double = 0.0,
		) : StarshipTrackingProjectileBalancing {
			@Transient
			override val clazz: KClass<out Projectile> = FlamingSkullProjectile::class
		}
	}

	@Serializable
	data class FlamethrowerCannonBalancing(
		override val fireRestrictions: FireRestrictions = FireRestrictions(
			canFire = false,
			minBlockCount = 1,
			maxBlockCount = Int.MAX_VALUE
		),
		override val fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(10),
		override val firePowerConsumption: Int = 50000,
		override val isForwardOnly: Boolean = false,
		override val maxPerShot: Int? = null,
		override val applyCooldownToAll: Boolean = false,

		override val convergeDistance: Double = 0.0,
		override val projectileSpawnDistance: Int = 0,
		override val angleRadiansHorizontal: Double = 180.0,
		override val angleRadiansVertical: Double = 180.0,

		override val projectile: FlamethrowerCannonProjectileBalancing = FlamethrowerCannonProjectileBalancing(),
	) : StarshipCannonWeaponBalancing<FlamethrowerCannonProjectileBalancing> {
		@Transient
		override val clazz: KClass<out WeaponSubsystem<*>> = FlamethrowerWeaponSubsystem::class

		@Serializable
		data class FlamethrowerCannonProjectileBalancing(
			override val range: Double = 340.0,
			override val speed: Double = 350.0,
			override val explosionPower: Float = 2.0f,
			override val starshipShieldDamageMultiplier: Double = 5.0,
			override val areaShieldDamageMultiplier: Double = 5.0,
			override val entityDamage: EntityDamage = RegularDamage(10.0),
			override val fireSound: SoundInfo = SoundInfo(key = "block.fire.ambient", volume = 10f, pitch = 0.5f),
			override val particleThickness: Double = 0.0,
			override val gravityMultiplier: Double = 0.05,
			override val decelerationAmount: Double = 0.05,
		) : StarshipArcedProjectileBalancing {
			@Transient
			override val clazz: KClass<out Projectile> = FlamethrowerProjectile::class
		}
	}

	@Serializable
	data class MiniPhaserBalancing(
		override val fireRestrictions: FireRestrictions = FireRestrictions(
			canFire = false,
			minBlockCount = 1,
			maxBlockCount = Int.MAX_VALUE
		),
		override val fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(500),
		override val firePowerConsumption: Int = 5000,
		override val isForwardOnly: Boolean = false,
		override val maxPerShot: Int? = null,
		override val applyCooldownToAll: Boolean = true,

		override val convergeDistance: Double = 0.0,
		override val projectileSpawnDistance: Int = 0,
		override val angleRadiansHorizontal: Double = 30.0,
		override val angleRadiansVertical: Double = 30.0,

		override val projectile: MiniPhaserProjectileBalancing = MiniPhaserProjectileBalancing(),
	) : StarshipCannonWeaponBalancing<MiniPhaserProjectileBalancing> {
		@Transient
		override val clazz: KClass<out WeaponSubsystem<*>> = MiniPhaserWeaponSubsystem::class

		@Serializable
		data class MiniPhaserProjectileBalancing(
			override val range: Double = 200.0,
			override val speed: Double = 600.0,
			override val explosionPower: Float = 2f,
			override val starshipShieldDamageMultiplier: Double = 1.0,
			override val areaShieldDamageMultiplier: Double = 1.0,
			override val entityDamage: EntityDamage = RegularDamage(10.0),
			override val fireSound: SoundInfo = SoundInfo(key = "block.conduit.deactivate", volume = 10f)
		) : StarshipProjectileBalancing {
			@Transient
			override val clazz: KClass<out Projectile> = MiniPhaserProjectile::class
		}
	}

	@Serializable
	data class CthulhuBeamBalancing(
		override val fireRestrictions: FireRestrictions = FireRestrictions(
			canFire = false,
			minBlockCount = 1,
			maxBlockCount = Int.MAX_VALUE
		),
		override val fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(10),
		override val firePowerConsumption: Int = 1,
		override val isForwardOnly: Boolean = false,
		override val maxPerShot: Int? = null,
		override val applyCooldownToAll: Boolean = false,

		override val range: Double = 128.0,

		override val projectile: CthulhuBeamProjectileBalancing = CthulhuBeamProjectileBalancing(),
	) : StarshipAutoWeaponBalancing<CthulhuBeamBalancing.CthulhuBeamProjectileBalancing> {
		@Transient
		override val clazz: KClass<out WeaponSubsystem<*>> = FlamingSkullCannon::class

		@Serializable
		data class CthulhuBeamProjectileBalancing(
			override val range: Double = 128.0,
			override val speed: Double = 1.0,
			override val explosionPower: Float = 2f,
			override val starshipShieldDamageMultiplier: Double = 1.0,
			override val areaShieldDamageMultiplier: Double = 1.0,
			override val entityDamage: EntityDamage = RegularDamage(10.0),
			override val fireSound: SoundInfo = SoundInfo(key = ""),
		) : StarshipProjectileBalancing {
			@Transient
			override val clazz: KClass<out Projectile> = FlamingSkullProjectile::class
		}
	}

	@Serializable
	data class CapitalCannonBalancing(
		override val fireRestrictions: FireRestrictions = FireRestrictions(
			canFire = false,
			minBlockCount = 1,
			maxBlockCount = Int.MAX_VALUE
		),
		override val fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(3000),
		override val firePowerConsumption: Int = 120000,
		override val isForwardOnly: Boolean = false,
		override val maxPerShot: Int? = null,
		override val boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(10),
		override val applyCooldownToAll: Boolean = true,

		override val projectile: CapitalCannonProjectileBalancing = CapitalCannonProjectileBalancing(),
	) : StarshipHeavyWeaponBalancing<CapitalCannonProjectileBalancing> {
		@Transient
		override val clazz: KClass<out WeaponSubsystem<*>> = CapitalBeamWeaponSubsystem::class

		@Serializable
		data class CapitalCannonProjectileBalancing(
			override val range: Double = 500.0,
			override val speed: Double = PI * 50.0,
			override val explosionPower: Float = 20f,
			override val starshipShieldDamageMultiplier: Double = 2.0,
			override val areaShieldDamageMultiplier: Double = 2.0,
			override val entityDamage: EntityDamage = RegularDamage(20.0),
			override val fireSound: SoundInfo = SoundInfo(key = "entity.zombie_villager.converted",),
			override val particleThickness: Double = 0.44,
		) : StarshipParticleProjectileBalancing {
			@Transient
			override val clazz: KClass<out Projectile> = CapitalBeamCannonProjectile::class
		}
	}
	// End Event Weapons
}
