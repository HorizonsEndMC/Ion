package net.horizonsend.ion.server.configuration.starship

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.horizonsend.ion.server.configuration.serializer.SubsystemSerializer
import net.horizonsend.ion.server.configuration.starship.StarshipSounds.SoundInfo
import net.horizonsend.ion.server.configuration.starship.StarshipWeaponBalancing.FireRestrictions
import net.horizonsend.ion.server.configuration.starship.TriTurretBalancing.TriTurretProjectileBalancing
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.damager.EntityDamager
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.horizonsend.ion.server.features.starship.subsystem.StarshipSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.checklist.BargeReactorSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.checklist.BattlecruiserReactorSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.checklist.CruiserReactorSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.checklist.FuelTankSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.checklist.LargeReactorSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.checklist.MediumReactorSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.checklist.SmallReactorSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.checklist.MiniReactorSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.command_burst.AbstractCommandBurstSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.command_burst.CapitalShieldCommandBurstSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.command_burst.CapitalSkirmishCommandBurstSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.command_burst.ShieldCommandBurstSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.command_burst.SkirmishCommandBurstSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.misc.GravityWellSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.misc.JumpBeaconSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.misc.JumpFieldGeneratorSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.BalancedWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.AssaultTurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.GaussCannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.HeavyTurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.IonTurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.Projectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.NeutralizerWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.TriTurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.WebifierWeaponSubsystem
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.damage.DamageSource
import org.bukkit.damage.DamageType
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import java.util.LinkedList
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

@Serializable
data class NewStarshipBalancing(
	val weaponDefaults: WeaponDefaults = WeaponDefaults(),
	val shipClasses: ShipClasses = ShipClasses(),
	val commandBurstDefaults: CommandBurstDefaults = CommandBurstDefaults(),
) {
	@Serializable
	data class WeaponDefaults(
		val weapons: List<StarshipWeaponBalancing<*>> = listOf(
			TorpedoBalancing(),
			EMPMissileBalancing(),
			HeavyLaserBalancing(),
			PhaserBalancing(),
			ArsenalRocketBalancing(),
			TriTurretBalancing(),
			AssaultTurretBalancing(),
			GaussCannonBalancing(),
			NeutralizerBalancing(),
			HeavyNeutralizerBalancing(),
			WebifierBalancing(),
			ArtilleryBalancing(),
			ACAPTurretBalancing(),
			LightLogisticsCannonBalancing(),
			HeavyLogisticsCannonBalancing(),
			ScramblerBalancing(),
			LightMissileLauncherBalancing(),
			RapidHeavyMissileLauncherBalancing(),
			AutocannonBalancing(),
			LightTurretBalancing(),
			HeavyTurretBalancing(),
			QuadTurretBalancing(),
			IonTurretBalancing(),
			PointDefenseBalancing(),
			PulseCannonBalancing(),
			PlasmaCannonBalancing(),
			LaserCannonBalancing(),
			InterceptorCannonBalancing(),
			AdvancedProbeBalancing(),
			ProbeBalancing(),
			ThermonuclearMissileBalancing(),

			// Event weapons
			DoomsdayDeviceBalancing(),
			RocketBalancing(),
			LogisticsTurretBalancing(),
			DisintegratorBeamBalancing(),
			CycleTurretBalancing(),
			AbyssalGazeBalancing(),
			SonicMissileBalancing(),
			PumpkinCannonBalancing(),
			FlamethrowerCannonBalancing(),
			MiniPhaserBalancing(),
			CthulhuBeamBalancing(),
			CapitalCannonBalancing(),
			TestBoidCannonBalancing(),
			SwarmMissileBalancing()
		)
	)

	@Serializable
	data class CommandBurstDefaults(
		val commandBursts: List<StarshipCommandBurstBalancing> = listOf(
			ShieldCommandBurstBalancing(),
			SkirmishCommandBurstBalancing(),
			CapitalShieldCommandBurstBalancing(),
			CapitalSkirmishCommandBurstBalancing(),
			)
	)

	@Serializable
	data class ShipClasses(
		val speeder: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 3,
			maxSneakFlyAccel = 3,
			warmupTime = 10,
			interdictionRange = 10,
			jumpStrength = 0.0,
			wellStrength = 0.0,
			hyperspaceRangeMultiplier = 0.0,
			shieldPowerMultiplier = 1.0,
			forbiddenMultiblocks = listOf(
				IncompatibleSubsystemInfo(
					GravityWellSubsystem::class.java,
					"Only interdictors can use gravity wells!"
				),
				IncompatibleSubsystemInfo(
					JumpFieldGeneratorSubsystem::class.java,
					"This ship cannot use a jump field generator!"
				),
				IncompatibleSubsystemInfo(
					JumpBeaconSubsystem::class.java,
					"This ship cannot use jump beacons!"
				)
			),
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.fighter.near"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.fighter.far")
			)
		),
		val shuttle: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 5,
			maxSneakFlyAccel = 2,
			warmupTime = 15,
			interdictionRange = 300,
			jumpStrength = 1.0,
			wellStrength = 0.0,
			hyperspaceRangeMultiplier = 1.2,
			maxCruiseSpeed = 35,
			shieldPowerMultiplier = 1.0,
			forbiddenMultiblocks = listOf(
				IncompatibleSubsystemInfo(
					GravityWellSubsystem::class.java,
					"Only interdictors can use gravity wells!"
				),
				IncompatibleSubsystemInfo(
					JumpFieldGeneratorSubsystem::class.java,
					"This ship cannot use a jump field generator!"
				),
				IncompatibleSubsystemInfo(
					JumpBeaconSubsystem::class.java,
					"This ship cannot use jump beacons!"
				)
			),
			weaponOverrides = listOf(
				ProbeBalancing(fireRestrictions = FireRestrictions(canFire = true))
			),
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.fighter.near"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.fighter.far")
			)
		),
		val transport: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 10,
			maxSneakFlyAccel = 3,
			interdictionRange = 600,
			warmupTime = 15,
			jumpStrength = 1.0,
			wellStrength = 1.0,
			hyperspaceRangeMultiplier = 1.25,
			maxCruiseSpeed = 30,
			shieldPowerMultiplier = 1.0,
			forbiddenMultiblocks = listOf(
				IncompatibleSubsystemInfo(
					GravityWellSubsystem::class.java,
					"Only interdictors can use gravity wells!"
				),
				IncompatibleSubsystemInfo(
					JumpFieldGeneratorSubsystem::class.java,
					"This ship cannot use a jump field generator!"
				),
				IncompatibleSubsystemInfo(
					JumpBeaconSubsystem::class.java,
					"This ship cannot use jump beacons!"
				)
			),
			weaponOverrides = listOf(
				ProbeBalancing(fireRestrictions = FireRestrictions(canFire = true))
			),
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.small.near"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.small.far")
			)
		),
		val lightFreighter: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 10,
			maxSneakFlyAccel = 3,
			warmupTime = 30,
			interdictionRange = 900,
			jumpStrength = 1.0,
			wellStrength = 1.0,
			hyperspaceRangeMultiplier = 1.3,
			shieldPowerMultiplier = 1.0,
			forbiddenMultiblocks = listOf(
				IncompatibleSubsystemInfo(
					GravityWellSubsystem::class.java,
					"Only interdictors can use gravity wells!"
				),
				IncompatibleSubsystemInfo(
					JumpFieldGeneratorSubsystem::class.java,
					"This ship cannot use a jump field generator!"
				),
				IncompatibleSubsystemInfo(
					JumpBeaconSubsystem::class.java,
					"This ship cannot use jump beacons!"
				)
			),
			weaponOverrides = listOf(
				ProbeBalancing(fireRestrictions = FireRestrictions(canFire = true))
			),
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.small.near"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.small.far")
			)
		),
		val mediumFreighter: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 10,
			maxSneakFlyAccel = 3,
			warmupTime = 30,
			interdictionRange = 1200,
			jumpStrength = 1.0,
			wellStrength = 1.0,
			hyperspaceRangeMultiplier = 1.35,
			maxCruiseSpeed = 23,
			shieldPowerMultiplier = 1.0,
			forbiddenMultiblocks = listOf(
				IncompatibleSubsystemInfo(
					GravityWellSubsystem::class.java,
					"Only interdictors can use gravity wells!"
				),
				IncompatibleSubsystemInfo(
					JumpFieldGeneratorSubsystem::class.java,
					"This ship cannot use a jump field generator!"
				)
			),
			weaponOverrides = listOf(
				ProbeBalancing(fireRestrictions = FireRestrictions(canFire = true))
			),
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.large.near"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.large.far")
			)
		),
		val blockadeRunner: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 10,
			maxSneakFlyAccel = 3,
			warmupTime = 10,
			interdictionRange = 1200,
			jumpStrength = 3.0,
			wellStrength = 1.0,
			hyperspaceRangeMultiplier = 1.35,
			maxCruiseSpeed = 35,
			shieldPowerMultiplier = 0.5,
			requiredMultiblocks = listOf(
				RequiredSubsystemInfo(
					SmallReactorSubsystem::class.java,
					1,
					"Blockade Runners require a small reactor to pilot!"
				),
				RequiredSubsystemInfo(
					FuelTankSubsystem::class.java,
					1,
					"Blockade Runners require fuel to pilot!"
				)
			),
			forbiddenMultiblocks = listOf(
				IncompatibleSubsystemInfo(
					GravityWellSubsystem::class.java,
					"Only interdictors can use gravity wells!"
				),
				IncompatibleSubsystemInfo(
					JumpFieldGeneratorSubsystem::class.java,
					"This ship cannot use a jump field generator!"
				),
				IncompatibleSubsystemInfo(
					JumpBeaconSubsystem::class.java,
					"This ship cannot use jump beacons!"
				)
			),
			weaponOverrides = listOf(
				ProbeBalancing(fireRestrictions = FireRestrictions(canFire = true))
			),
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.large.near"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.large.far")
			)
		),
		val heavyFreighter: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 10,
			maxSneakFlyAccel = 3,
			warmupTime = 30,
			interdictionRange = 1500,
			jumpStrength = 1.0,
			wellStrength = 1.0,
			hyperspaceRangeMultiplier = 1.4,
			maxCruiseSpeed = 21,
			forbiddenMultiblocks = listOf(
				IncompatibleSubsystemInfo(
					GravityWellSubsystem::class.java,
					"Only interdictors can use gravity wells!"
				),
				IncompatibleSubsystemInfo(
					JumpFieldGeneratorSubsystem::class.java,
					"This ship cannot use a jump field generator!"
				)
			),
			weaponOverrides = listOf(
				ProbeBalancing(fireRestrictions = FireRestrictions(canFire = true))
			),
			shieldPowerMultiplier = 1.0,
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.large.near"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.large.far")
			)
		),
		val barge: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 3,
			maxSneakFlyAccel = 3,
			warmupTime = 60,
			interdictionRange = 4500,
			jumpStrength = 3.0,
			wellStrength = 1.0,
			hyperspaceRangeMultiplier = 3.0,
			cruiseSpeedMultiplier = 0.65,
			maxCruiseSpeed = 14,
			shieldPowerMultiplier = 1.00,
			weaponOverrides = listOf(
				TriTurretBalancing(
					fireRestrictions = FireRestrictions(minBlockCount = 3400),
					projectile = TriTurretProjectileBalancing(speed = 110.0)
				),
				HeavyTurretBalancing(
					fireRestrictions = FireRestrictions(minBlockCount = 16500, maxBlockCount = 20000),
					firePowerConsumption = 3333,
					projectile = HeavyTurretBalancing.HeavyTurretProjectileBalancing(speed = 70.0)
				),
				AdvancedProbeBalancing(
					fireRestrictions = FireRestrictions(canFire = true)
				)
			),
			forbiddenMultiblocks = listOf(
				IncompatibleSubsystemInfo(
					GravityWellSubsystem::class.java,
					"Only interdictors can use gravity wells!"
				)
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
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.battlecruiser"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.battlecruiser"),
			)
		),
		val jumpFreighter: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 3,
			maxSneakFlyAccel = 3,
			warmupTime = 120,
			interdictionRange = 4500,
			jumpStrength = 3.0,
			wellStrength = 1.0,
			hyperspaceRangeMultiplier = 3.0,
			cruiseSpeedMultiplier = 0.45,
			shieldPowerMultiplier = 0.10,
			weaponOverrides = listOf(
				TriTurretBalancing(
					fireRestrictions = FireRestrictions(minBlockCount = 3400),
					projectile = TriTurretProjectileBalancing(speed = 110.0)
				),
				HeavyTurretBalancing(
					fireRestrictions = FireRestrictions(minBlockCount = 16500, maxBlockCount = 20000),
					firePowerConsumption = 3333,
					projectile = HeavyTurretBalancing.HeavyTurretProjectileBalancing(speed = 200.0)
				),
				ProbeBalancing(fireRestrictions = FireRestrictions(canFire = true))
			),
			forbiddenMultiblocks = listOf(
				IncompatibleSubsystemInfo(
					GravityWellSubsystem::class.java,
					"Only interdictors can use gravity wells!"
				),
				IncompatibleSubsystemInfo(
					JumpBeaconSubsystem::class.java,
					"This ship cannot use jump beacons!"
				),
			),
			requiredMultiblocks = listOf(
				RequiredSubsystemInfo(
					LargeReactorSubsystem::class.java,
					1,
					"Jump Freighters require a large reactor to pilot!"
				),
				RequiredSubsystemInfo(
					FuelTankSubsystem::class.java,
					1,
					"Jump Freighters require fuel to pilot!"
				)
			),
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.battlecruiser"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.battlecruiser"),
			)
		),
		val industrialCommandShip: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 3,
			maxSneakFlyAccel = 3,
			warmupTime = 120,
			interdictionRange = 500,
			jumpStrength = 3.0,
			wellStrength = 1.0,
			hyperspaceRangeMultiplier = 3.0,
			cruiseSpeedMultiplier = 0.50,
			maxCruiseSpeed = 14,
			shieldPowerMultiplier = 1.50,
			weaponOverrides = listOf(
				TriTurretBalancing(
					fireRestrictions = FireRestrictions(minBlockCount = 3400),
					projectile = TriTurretProjectileBalancing(speed = 110.0)
				),
				HeavyTurretBalancing(
					fireRestrictions = FireRestrictions(minBlockCount = 16500, maxBlockCount = 20000),
					firePowerConsumption = 3333,
					projectile = HeavyTurretBalancing.HeavyTurretProjectileBalancing(speed = 200.0)
				),
				AdvancedProbeBalancing(fireRestrictions = FireRestrictions(canFire = true))
				),
			forbiddenMultiblocks = listOf(
				IncompatibleSubsystemInfo(
					GravityWellSubsystem::class.java,
					"Only interdictors can use gravity wells!"
				),
			),
			requiredMultiblocks = listOf(
				RequiredSubsystemInfo(
					LargeReactorSubsystem::class.java,
					1,
					"Jump Freighters require a large reactor to pilot!"
				),
				RequiredSubsystemInfo(
					FuelTankSubsystem::class.java,
					1,
					"Jump Freighters require fuel to pilot!"
				)
			),
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.battlecruiser"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.battlecruiser"),
			)
		),
		val tank: StarshipTypeBalancing = GroundStarshipBalancing(
			sneakFlyAccelDistance = 4,
			maxSneakFlyAccel = 4,
			warmupTime = 6767,
			interdictionRange = 10,
			jumpStrength = 0.0,
			wellStrength = 0.0,
			hyperspaceRangeMultiplier = 1.5,
			shieldPowerMultiplier = 1.0,
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.fighter.near"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.fighter.far")
			)
		),
		val starfighter: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 4,
			maxSneakFlyAccel = 4,
			warmupTime = 10,
			interdictionRange = 500,
			jumpStrength = 1.0,
			wellStrength = 0.0,
			hyperspaceRangeMultiplier = 1.5,
			shieldPowerMultiplier = 0.6,
			forbiddenMultiblocks = listOf(
				IncompatibleSubsystemInfo(
					GravityWellSubsystem::class.java,
					"Only interdictors can use gravity wells!"
				),
				IncompatibleSubsystemInfo(
					JumpFieldGeneratorSubsystem::class.java,
					"This ship cannot use a jump field generator!"
				),
				IncompatibleSubsystemInfo(
					MiniReactorSubsystem::class.java,
					"Tech 1 ships cannot house tech 2 reactors!"
				),
				IncompatibleSubsystemInfo(
					JumpBeaconSubsystem::class.java,
					"This ship cannot use jump beacons!"
				)
			),
			weaponOverrides = listOf(
				ProbeBalancing(fireRestrictions = FireRestrictions(canFire = true))
			),
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.fighter.near"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.fighter.far")
			)
		),
		val scramblerStarfighter: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 4,
			maxSneakFlyAccel = 4,
			warmupTime = 10,
			interdictionRange = 350,
			jumpStrength = 1.0,
			wellStrength = 1.0,
			hyperspaceRangeMultiplier = 1.5,
			cruiseSpeedMultiplier = 0.95,
			requiredMultiblocks = listOf(
				RequiredSubsystemInfo(
					MiniReactorSubsystem::class.java,
					1,
					"Tech 2 starfighters require a mini reactor to pilot!"
				)),
			forbiddenMultiblocks = listOf(
				IncompatibleSubsystemInfo(
					GravityWellSubsystem::class.java,
					"This ship cannot use gravity wells!"
				),
				IncompatibleSubsystemInfo(
					JumpFieldGeneratorSubsystem::class.java,
					"This ship cannot use a jump field generator!"
				),
				IncompatibleSubsystemInfo(
					JumpBeaconSubsystem::class.java,
					"This ship cannot use jump beacons!"
				)
			),
			weaponOverrides = listOf(
				PlasmaCannonBalancing(fireRestrictions = FireRestrictions(canFire = false)),
				LaserCannonBalancing(fireRestrictions = FireRestrictions(canFire = false)),
				TorpedoBalancing(fireRestrictions = FireRestrictions(canFire = false)),
				ScramblerBalancing(fireRestrictions = FireRestrictions(canFire = true), maxPerShot = 1),
				AdvancedProbeBalancing(fireRestrictions = FireRestrictions(canFire = true)),
				EMPMissileBalancing(fireRestrictions = FireRestrictions(canFire = true), maxPerShot = 1)
			),
			shieldPowerMultiplier = 0.4,
			shieldRegenMultiplier = 0.7,
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.fighter.near"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.fighter.far")
			)
		),
		val reconStarfighter: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 4,
			maxSneakFlyAccel = 4,
			warmupTime = 8,
			interdictionRange = 350,
			jumpStrength = 1.0,
			wellStrength = 1.0,
			hyperspaceRangeMultiplier = 1.5,
			requiredMultiblocks = listOf(
				RequiredSubsystemInfo(
					MiniReactorSubsystem::class.java,
					1,
					"Tech 2 starfighters require a mini reactor to pilot!"
				)),
			forbiddenMultiblocks = listOf(
				IncompatibleSubsystemInfo(
					GravityWellSubsystem::class.java,
					"This ship cannot use gravity wells!"
				)
			),
			weaponOverrides = listOf(
				PlasmaCannonBalancing(fireRestrictions = FireRestrictions(canFire = false)),
				LaserCannonBalancing(fireRestrictions = FireRestrictions(canFire = false)),
				TorpedoBalancing(fireRestrictions = FireRestrictions(canFire = false)),
				AdvancedProbeBalancing(fireRestrictions = FireRestrictions(canFire = true)),
			),
			shieldPowerMultiplier = 0.35,
			shieldRegenMultiplier = 0.25,
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.fighter.near"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.fighter.far")
			)
		),
		val interceptor: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 4,
			maxSneakFlyAccel = 4,
			warmupTime = 67,
			interdictionRange = 10,
			jumpStrength = 1.0,
			wellStrength = 0.0,
			hyperspaceRangeMultiplier = 0.0,
			shieldPowerMultiplier = 0.01,
			cruiseSpeedMultiplier = 0.01,
			forbiddenMultiblocks = listOf(
				IncompatibleSubsystemInfo(
					GravityWellSubsystem::class.java,
					"Only interdictors can use gravity wells!"
				),
				IncompatibleSubsystemInfo(
					JumpFieldGeneratorSubsystem::class.java,
					"This ship cannot use a jump field generator!"
				),
				IncompatibleSubsystemInfo(
					JumpBeaconSubsystem::class.java,
					"This ship cannot use jump beacons!"
				)
			),
			weaponOverrides = listOf(
				PlasmaCannonBalancing(fireRestrictions = FireRestrictions(canFire = false)),
				LaserCannonBalancing(fireRestrictions = FireRestrictions(canFire = false)),
				InterceptorCannonBalancing(fireRestrictions = FireRestrictions(canFire = true)),
				TorpedoBalancing(fireRestrictions = FireRestrictions(canFire = false)),
			),
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.fighter.near"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.fighter.far")
			)
		),
		val gunship: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 5,
			maxSneakFlyAccel = 2,
			warmupTime = 10,
			interdictionRange = 500,
			jumpStrength = 1.0,
			wellStrength = 1.0,
			hyperspaceRangeMultiplier = 1.6,
			cruiseSpeedMultiplier = 0.8,
			shieldPowerMultiplier = 0.4,
			forbiddenMultiblocks = listOf(
				IncompatibleSubsystemInfo(
					GravityWellSubsystem::class.java,
					"Only interdictors can use gravity wells!"
				),
				IncompatibleSubsystemInfo(
					JumpFieldGeneratorSubsystem::class.java,
					"This ship cannot use a jump field generator!"
				),
				IncompatibleSubsystemInfo(
					MiniReactorSubsystem::class.java,
					"Tech 1 ships cannot house tech 2 reactors!"
				),
				IncompatibleSubsystemInfo(
					JumpBeaconSubsystem::class.java,
					"This ship cannot use jump beacons!"
				)
			),
			weaponOverrides = listOf(
				PulseCannonBalancing(fireRestrictions = FireRestrictions(minBlockCount = 1000, maxBlockCount = 4000)),
				ProbeBalancing(fireRestrictions = FireRestrictions(canFire = true))
			),
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.small.near"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.small.far")
			)
		),
		val assaultGunship: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 5,
			maxSneakFlyAccel = 2,
			interdictionRange = 500,
			warmupTime = 10,
			jumpStrength = 1.0,
			wellStrength = 1.0,
			hyperspaceRangeMultiplier = 1.6,
			cruiseSpeedMultiplier = 0.80,
			shieldPowerMultiplier = 1.18,
			shieldRegenMultiplier = 0.6,
			requiredMultiblocks = listOf(
				RequiredSubsystemInfo(
					MiniReactorSubsystem::class.java,
					1,
					"Tech 2 gunships require a mini reactor to pilot!"
				)),
			weaponOverrides = listOf(
				PulseCannonBalancing(fireRestrictions = FireRestrictions(canFire = false)),
				HeavyLaserBalancing(fireRestrictions = FireRestrictions(canFire = false)),
				TorpedoBalancing(fireRestrictions = FireRestrictions(canFire = false)),
				GaussCannonBalancing(fireRestrictions = FireRestrictions(canFire = true), maxPerShot = 2),
				NeutralizerBalancing(fireRestrictions = FireRestrictions(canFire = true), maxPerShot = 1),
				EMPMissileBalancing(fireRestrictions = FireRestrictions(canFire = true), maxPerShot = 1, boostChargeNanos = TimeUnit.SECONDS.toNanos(10))
				),
			forbiddenMultiblocks = listOf(
				IncompatibleSubsystemInfo(
					JumpFieldGeneratorSubsystem::class.java,
					"This ship cannot use a jump field generator!"
				),
				IncompatibleSubsystemInfo(
					GravityWellSubsystem::class.java,
					"Only interdictors can use gravity wells!"
				),
				IncompatibleSubsystemInfo(
					JumpBeaconSubsystem::class.java,
					"This ship cannot use jump beacons!"
				)
			),
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.small.near"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.small.far")
			)
		),
		val interdictorGunship: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 5,
			maxSneakFlyAccel = 2,
			interdictionRange = 500,
			jumpStrength = 1.0,
			warmupTime = 10,
			wellStrength = 1.0,
			hyperspaceRangeMultiplier = 1.6,
			cruiseSpeedMultiplier = 1.0,
			shieldPowerMultiplier = 0.75,
			shieldRegenMultiplier = 0.5,
			requiredMultiblocks = listOf(
				RequiredSubsystemInfo(
					MiniReactorSubsystem::class.java,
					1,
					"Tech 2 gunships require a mini reactor to pilot!"
				)),
			weaponOverrides = listOf(
				PulseCannonBalancing(fireRestrictions = FireRestrictions(minBlockCount = 1000, maxBlockCount = 4000))
			),
			forbiddenMultiblocks = listOf(
				IncompatibleSubsystemInfo(
					JumpFieldGeneratorSubsystem::class.java,
					"This ship cannot use a jump field generator!"
				),
				IncompatibleSubsystemInfo(
					JumpBeaconSubsystem::class.java,
					"This ship cannot use jump beacons!"
				)
			),
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.small.near"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.small.far")
			)
		),
		val corvette: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 6,
			maxSneakFlyAccel = 2,
			interdictionRange = 650,
			warmupTime = 10,
			jumpStrength = 1.0,
			wellStrength = 1.0,
			hyperspaceRangeMultiplier = 1.7,
			shieldPowerMultiplier = 0.5,
			cruiseSpeedMultiplier = 0.8,
			forbiddenMultiblocks = listOf(
				IncompatibleSubsystemInfo(
					GravityWellSubsystem::class.java,
					"Only interdictors can use gravity wells!"
				),
				IncompatibleSubsystemInfo(
					JumpFieldGeneratorSubsystem::class.java,
					"This ship cannot use a jump field generator!"
				),
				IncompatibleSubsystemInfo(
					SmallReactorSubsystem::class.java,
					"Tech 1 ships cannot house tech 2 reactors!"
				),
				IncompatibleSubsystemInfo(
					JumpBeaconSubsystem::class.java,
					"This ship cannot use jump beacons!"
				)
			),
			weaponOverrides = listOf(
				TriTurretBalancing(
					fireRestrictions = FireRestrictions(canFire = true, minBlockCount = 3400),
					boostChargeNanos = TimeUnit.MILLISECONDS.toNanos(4500)
				),
				PulseCannonBalancing(fireRestrictions = FireRestrictions(canFire = true, minBlockCount = 1000, maxBlockCount = 4000)),
				ProbeBalancing(fireRestrictions = FireRestrictions(canFire = true))
			),
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.small.near"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.small.far")
			)
		),
		val interdictorCorvette: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 6,
			maxSneakFlyAccel = 2,
			interdictionRange = 1000,
			jumpStrength = 1.0,
			warmupTime = 10,
			wellStrength = 2.0,
			cruiseSpeedMultiplier = 1.05,
			hyperspaceRangeMultiplier = 1.7,
			shieldPowerMultiplier = 0.8,
			shieldRegenMultiplier = 0.5,
			requiredMultiblocks = listOf(
				RequiredSubsystemInfo(
					SmallReactorSubsystem::class.java,
					1,
					"Tech 2 corvettes require a small reactor to pilot!"
				),
				RequiredSubsystemInfo(
					FuelTankSubsystem::class.java,
					1,
					"tech 2 ships require a fuel tank to pilot!"
				)),
			weaponOverrides = listOf(
				TriTurretBalancing(
					fireRestrictions = FireRestrictions(canFire = true, minBlockCount = 3400),
					boostChargeNanos = TimeUnit.SECONDS.toNanos(7)
				),
				PulseCannonBalancing(fireRestrictions = FireRestrictions(canFire = true, minBlockCount = 1000, maxBlockCount = 4000), maxPerShot = 3)
			),
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.small.near"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.small.far")
			)
		),
		val stasisCorvette: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 6,
			maxSneakFlyAccel = 2,
			interdictionRange = 650,
			jumpStrength = 1.0,
			warmupTime = 10,
			wellStrength = 1.0,
			cruiseSpeedMultiplier = 0.75,
			hyperspaceRangeMultiplier = 1.7,
			shieldPowerMultiplier = 0.72,
			shieldRegenMultiplier = 0.7,
			requiredMultiblocks = listOf(
				RequiredSubsystemInfo(
					SmallReactorSubsystem::class.java,
					1,
					"Tech 2 corvettes require a small reactor to pilot!"
				),
				RequiredSubsystemInfo(
					FuelTankSubsystem::class.java,
					1,
					"tech 2 ships require a fuel tank to pilot!"
				)),
			weaponOverrides = listOf(
				TriTurretBalancing(
					fireRestrictions = FireRestrictions(canFire = false, minBlockCount = 3400),
					boostChargeNanos = TimeUnit.SECONDS.toNanos(7)
				),
				PulseCannonBalancing(fireRestrictions = FireRestrictions(canFire = false, minBlockCount = 1000, maxBlockCount = 4000)),
				TorpedoBalancing(fireRestrictions = FireRestrictions(canFire = false)),
				WebifierBalancing(fireRestrictions = FireRestrictions(canFire = true), maxPerShot = 1),
				ArtilleryBalancing(fireRestrictions = FireRestrictions(canFire = true), maxPerShot = 1),
				AdvancedProbeBalancing(fireRestrictions = FireRestrictions(canFire = true)),
				AutocannonBalancing(fireRestrictions = FireRestrictions(canFire = true), maxPerShot = 2)
			),
			forbiddenMultiblocks = listOf(
				IncompatibleSubsystemInfo(
					JumpFieldGeneratorSubsystem::class.java,
					"This ship cannot use a jump field generator!"
				),
				IncompatibleSubsystemInfo(
					GravityWellSubsystem::class.java,
					"Only interdictors can use gravity wells!"
				),
				IncompatibleSubsystemInfo(
					JumpBeaconSubsystem::class.java,
					"This ship cannot use jump beacons!"
				)
			),
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.small.near"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.small.far")
			)
		),
		val assaultCorvette: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 6,
			maxSneakFlyAccel = 2,
			interdictionRange = 650,
			jumpStrength = 1.0,
			warmupTime = 10,
			wellStrength = 1.0,
			cruiseSpeedMultiplier = 0.80,
			hyperspaceRangeMultiplier = 1.7,
			shieldPowerMultiplier = 1.20,
			shieldRegenMultiplier = 0.8,
			requiredMultiblocks = listOf(
				RequiredSubsystemInfo(
					SmallReactorSubsystem::class.java,
					1,
					"Tech 2 corvettes require a small reactor to pilot!"
				),
				RequiredSubsystemInfo(
					FuelTankSubsystem::class.java,
					1,
					"tech 2 ships require a fuel tank to pilot!"
				)),
			weaponOverrides = listOf(
				TriTurretBalancing(
					fireRestrictions = FireRestrictions(canFire = true, minBlockCount = 3400),
					boostChargeNanos = TimeUnit.SECONDS.toNanos(7)
				),
				PulseCannonBalancing(fireRestrictions = FireRestrictions(canFire = false, minBlockCount = 1000, maxBlockCount = 4000)),
				TorpedoBalancing(fireRestrictions = FireRestrictions(canFire = false)),
				GaussCannonBalancing(fireRestrictions = FireRestrictions(canFire = true), maxPerShot = 3),
				NeutralizerBalancing(fireRestrictions = FireRestrictions(canFire = true), maxPerShot = 2)
			),
			forbiddenMultiblocks = listOf(
				IncompatibleSubsystemInfo(
					JumpFieldGeneratorSubsystem::class.java,
					"This ship cannot use a jump field generator!"
				),
				IncompatibleSubsystemInfo(
					GravityWellSubsystem::class.java,
					"Only interdictors can use gravity wells!"
				),
				IncompatibleSubsystemInfo(
					JumpBeaconSubsystem::class.java,
					"This ship cannot use jump beacons!"
				)
			),
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.small.near"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.small.far")
			)
		),
		val logisticsCorvette: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 6,
			maxSneakFlyAccel = 2,
			interdictionRange = 650,
			jumpStrength = 1.0,
			warmupTime = 10,
			wellStrength = 0.0,
			cruiseSpeedMultiplier = 0.75,
			hyperspaceRangeMultiplier = 1.7,
			shieldPowerMultiplier = 0.60,
			shieldRegenMultiplier = 1.25,
			requiredMultiblocks = listOf(
				RequiredSubsystemInfo(
					SmallReactorSubsystem::class.java,
					1,
					"Tech 2 corvettes require a small reactor to pilot!"
				),
				RequiredSubsystemInfo(
					FuelTankSubsystem::class.java,
					1,
					"tech 2 ships require a fuel tank to pilot!"
				)),
			commandBurstOverrides = listOf(
				SkirmishCommandBurstBalancing(activateRestrictions = StarshipCommandBurstBalancing.ActivateRestrictions(canActivate = true, incompatibleMultiblocks = listOf(
					IncompatibleSubsystemInfo(
						ShieldCommandBurstSubsystem::class.java,
						"You cannot have more than one type of command burst!"
					)
				))),
				ShieldCommandBurstBalancing(activateRestrictions = StarshipCommandBurstBalancing.ActivateRestrictions(canActivate = true, incompatibleMultiblocks = listOf(
					IncompatibleSubsystemInfo(
						SkirmishCommandBurstSubsystem::class.java,
						"You cannot have more than one type of command burst!"
					)
				)))
			),
			weaponOverrides = listOf(
				TriTurretBalancing(
					fireRestrictions = FireRestrictions(canFire = false, minBlockCount = 3400),
					boostChargeNanos = TimeUnit.SECONDS.toNanos(7)
				),
				PulseCannonBalancing(fireRestrictions = FireRestrictions(canFire = false, minBlockCount = 1000, maxBlockCount = 4000)),
				TorpedoBalancing(fireRestrictions = FireRestrictions(canFire = false)),
				LightLogisticsCannonBalancing(fireRestrictions = FireRestrictions(canFire = true), maxPerShot = 2)
			),
			forbiddenMultiblocks = listOf(
				IncompatibleSubsystemInfo(
					JumpBeaconSubsystem::class.java,
					"This ship cannot use jump beacons!"
				),
				IncompatibleSubsystemInfo(
					AssaultTurretWeaponSubsystem::class.java,
					"Logistics ships cannot have weapons!"
				),
				IncompatibleSubsystemInfo(
					GaussCannonWeaponSubsystem::class.java,
					"Logistics ships cannot have weapons!"
				),
				IncompatibleSubsystemInfo(
					NeutralizerWeaponSubsystem::class.java,
					"Logistics ships cannot have weapons!"
				),
				IncompatibleSubsystemInfo(
					GravityWellSubsystem::class.java,
					"Only interdictors can use gravity wells!"
				),
				IncompatibleSubsystemInfo(
					WebifierWeaponSubsystem::class.java,
					"Logistics ships cannot have weapons!"
				)
			),
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.small.near"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.small.far")
			)
		),
		val frigate: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 6,
			maxSneakFlyAccel = 2,
			interdictionRange = 850,
			warmupTime = 15,
			jumpStrength = 1.0,
			wellStrength = 1.0,
			hyperspaceRangeMultiplier = 1.8,
			cruiseSpeedMultiplier = 0.8,
			shieldPowerMultiplier = 0.5,
			forbiddenMultiblocks = listOf(
				IncompatibleSubsystemInfo(
					GravityWellSubsystem::class.java,
					"Only interdictors can use gravity wells!"
				),
				IncompatibleSubsystemInfo(
					JumpFieldGeneratorSubsystem::class.java,
					"This ship cannot use a jump field generator!"
				),
				IncompatibleSubsystemInfo(
					MediumReactorSubsystem::class.java,
					"Tech 1 ships cannot house tech 2 reactors!"
				),
				IncompatibleSubsystemInfo(
					JumpBeaconSubsystem::class.java,
					"This ship cannot use jump beacons!"
				)
			),
			weaponOverrides = listOf(
				LaserCannonBalancing(fireRestrictions = FireRestrictions(canFire = true), firePowerConsumption = 420),
				PhaserBalancing(fireRestrictions = FireRestrictions(canFire = true), maxPerShot = 1),
				ProbeBalancing(fireRestrictions = FireRestrictions(canFire = true))
			),
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.large.near"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.large.far")
			)
		),
		val assaultFrigate: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 6,
			maxSneakFlyAccel = 2,
			interdictionRange = 850,
			jumpStrength = 1.0,
			warmupTime = 15,
			wellStrength = 1.0,
			hyperspaceRangeMultiplier = 1.8,
			shieldPowerMultiplier = 1.25,
			cruiseSpeedMultiplier = 0.80,
			requiredMultiblocks = listOf(
				RequiredSubsystemInfo(
					MediumReactorSubsystem::class.java,
					1,
					"Tech 2 frigates require a medium reactor to pilot!"
				),
				RequiredSubsystemInfo(
					FuelTankSubsystem::class.java,
					1,
					"tech 2 ships require a fuel tank to pilot!"
				)),
			weaponOverrides = listOf(
				HeavyTurretBalancing(fireRestrictions = FireRestrictions(canFire = false)),
				NeutralizerBalancing(fireRestrictions = FireRestrictions(canFire = true), maxPerShot = 2),
				HeavyNeutralizerBalancing(fireRestrictions = FireRestrictions(canFire = true)),
				AssaultTurretBalancing(fireRestrictions = FireRestrictions(canFire = true, minBlockCount = 6500, ), maxPerShot = 3,),
				LaserCannonBalancing(fireRestrictions = FireRestrictions(canFire = false), firePowerConsumption = 420),
				SwarmMissileBalancing(fireRestrictions = FireRestrictions(canFire = false, minBlockCount = 4500, maxBlockCount = 8000), maxPerShot = 1, boostChargeNanos = TimeUnit.SECONDS.toNanos(6))
			),
			forbiddenMultiblocks = listOf(
				IncompatibleSubsystemInfo(
					JumpFieldGeneratorSubsystem::class.java,
					"This ship cannot use a jump field generator!"
				),
				IncompatibleSubsystemInfo(
					GravityWellSubsystem::class.java,
					"Only interdictors can use gravity wells!"
				),
				IncompatibleSubsystemInfo(
					JumpBeaconSubsystem::class.java,
					"This ship cannot use jump beacons!"
				)
			),
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.large.near"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.large.far")
			)
		),
		val blackOpsFrigate: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 6,
			maxSneakFlyAccel = 2,
			interdictionRange = 850,
			warmupTime = 15,
			jumpStrength = 2.0,
			wellStrength = 1.0,
			hyperspaceRangeMultiplier = 1.8,
			shieldPowerMultiplier = 0.35,
			shieldRegenMultiplier = 0.8,
			cruiseSpeedMultiplier = 1.40,
			requiredMultiblocks = listOf(
				RequiredSubsystemInfo(
					MediumReactorSubsystem::class.java,
					1,
					"Tech 2 frigates require a medium reactor to pilot!"
				),
				RequiredSubsystemInfo(
					FuelTankSubsystem::class.java,
					1,
					"tech 2 ships require a fuel tank to pilot!"
				)),
			weaponOverrides = listOf(
				NeutralizerBalancing(fireRestrictions = FireRestrictions(canFire = true), maxPerShot = 2),
				PhaserBalancing(fireRestrictions = FireRestrictions(canFire = true), maxPerShot = 2, firePowerConsumption = 12500),
				GaussCannonBalancing(fireRestrictions = FireRestrictions(canFire = true), maxPerShot = 3),
				AdvancedProbeBalancing(fireRestrictions = FireRestrictions(canFire = true))
				),
			forbiddenMultiblocks = listOf(
				IncompatibleSubsystemInfo(
					JumpBeaconSubsystem::class.java,
					"This ship cannot use jump beacons!"
				),
				IncompatibleSubsystemInfo(
					GravityWellSubsystem::class.java,
					"Only interdictors can use gravity wells!"
				),
				IncompatibleSubsystemInfo(
					JumpBeaconSubsystem::class.java,
					"This ship cannot use jump beacons!"
				)
			),
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.large.near"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.large.far")
			)
		),
		val missileFrigate: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 6,
			maxSneakFlyAccel = 2,
			interdictionRange = 850,
			warmupTime = 15,
			jumpStrength = 2.0,
			wellStrength = 1.0,
			hyperspaceRangeMultiplier = 1.8,
			shieldPowerMultiplier = 0.77,
			shieldRegenMultiplier = 0.8,
			cruiseSpeedMultiplier = 0.85,
			requiredMultiblocks = listOf(
				RequiredSubsystemInfo(
					MediumReactorSubsystem::class.java,
					1,
					"Tech 2 frigates require a medium reactor to pilot!"
				),
				RequiredSubsystemInfo(
					FuelTankSubsystem::class.java,
					1,
					"tech 2 ships require a fuel tank to pilot!"
				)),
			weaponOverrides = listOf(
				ArsenalRocketBalancing(fireRestrictions = FireRestrictions(canFire = true), maxPerShot = 2, firePowerConsumption = 28500),
				LightMissileLauncherBalancing(fireRestrictions = FireRestrictions(canFire = true), maxPerShot = 4,),
				RapidHeavyMissileLauncherBalancing(fireRestrictions = FireRestrictions(canFire = false), maxPerShot = 2),
				PhaserBalancing(fireRestrictions = FireRestrictions(canFire = false)),
				TriTurretBalancing(fireRestrictions = FireRestrictions(canFire = false)),
				HeavyTurretBalancing(fireRestrictions = FireRestrictions(canFire = false)),
				LaserCannonBalancing(fireRestrictions = FireRestrictions(canFire = false), firePowerConsumption = 420),
				SwarmMissileBalancing(fireRestrictions = FireRestrictions(canFire = true), maxPerShot = 2, boostChargeNanos = TimeUnit.SECONDS.toNanos(6))
			),
			forbiddenMultiblocks = listOf(
				IncompatibleSubsystemInfo(
					JumpFieldGeneratorSubsystem::class.java,
					"This ship cannot use a jump field generator!"
				),
				IncompatibleSubsystemInfo(
					GravityWellSubsystem::class.java,
					"Only interdictors can use gravity wells!"
				),
				IncompatibleSubsystemInfo(
					JumpBeaconSubsystem::class.java,
					"This ship cannot use jump beacons!"
				),
				IncompatibleSubsystemInfo(
					AssaultTurretWeaponSubsystem::class.java,
					"Missile ships cannot have assault turrets!"
				),
				IncompatibleSubsystemInfo(
					HeavyTurretWeaponSubsystem::class.java,
					"Missile ships cannot have heavy turrets!"
				),
				IncompatibleSubsystemInfo(
					TriTurretWeaponSubsystem::class.java,
					"Missile ships cannot have tri turrets!"
				)
			),
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.large.near"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.large.far")
			)
		),
		val destroyer: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 5,
			maxSneakFlyAccel = 3,
			interdictionRange = 1000,
			warmupTime = 15,
			jumpStrength = 1.0,
			wellStrength = 1.0,
			hyperspaceRangeMultiplier = 1.9,
			shieldPowerMultiplier = 0.6,
			cruiseSpeedMultiplier = 0.80,
			weaponOverrides = listOf(
				LaserCannonBalancing(fireRestrictions = FireRestrictions(canFire = true), firePowerConsumption = 360),
				SwarmMissileBalancing(fireRestrictions = FireRestrictions(canFire = true, minBlockCount = 8000, maxBlockCount = 12000), maxPerShot = 2, boostChargeNanos = TimeUnit.SECONDS.toNanos(8)),
				ProbeBalancing(fireRestrictions = FireRestrictions(canFire = true))
			),
			forbiddenMultiblocks = listOf(
				IncompatibleSubsystemInfo(
					GravityWellSubsystem::class.java,
					"Only interdictors can use gravity wells!"
				),
				IncompatibleSubsystemInfo(
					JumpFieldGeneratorSubsystem::class.java,
					"This ship cannot use a jump field generator!"
				),
				IncompatibleSubsystemInfo(
					MediumReactorSubsystem::class.java,
					"Tech 1 ships cannot house tech 2 reactors!"
				),
				IncompatibleSubsystemInfo(
					JumpBeaconSubsystem::class.java,
					"This ship cannot use jump beacons!"
				)
			),
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.large.near"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.large.far")
			)
		),
		val assaultDestroyer: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 5,
			maxSneakFlyAccel = 3,
			interdictionRange = 1000,
			warmupTime = 15,
			jumpStrength = 2.0,
			wellStrength = 1.0,
			cruiseSpeedMultiplier = 0.80,
			hyperspaceRangeMultiplier = 1.9,
			shieldPowerMultiplier = 1.25,
			requiredMultiblocks = listOf(
				RequiredSubsystemInfo(
					MediumReactorSubsystem::class.java,
					1,
					"Tech 2 destroyers require a medium reactor to pilot!"
				),
				RequiredSubsystemInfo(
					FuelTankSubsystem::class.java,
					1,
					"tech 2 ships require a fuel tank to pilot!"
				)),
			forbiddenMultiblocks = listOf(
				IncompatibleSubsystemInfo(
					JumpFieldGeneratorSubsystem::class.java,
					"This ship cannot use a jump field generator!"
				),
				IncompatibleSubsystemInfo(
					GravityWellSubsystem::class.java,
					"Only interdictors can use gravity wells!"
				),
				IncompatibleSubsystemInfo(
					JumpBeaconSubsystem::class.java,
					"This ship cannot use jump beacons!"
				)
			),
			weaponOverrides = listOf(
				NeutralizerBalancing(fireRestrictions = FireRestrictions(canFire = true), maxPerShot = 3),
				HeavyNeutralizerBalancing(fireRestrictions = FireRestrictions(canFire = true), maxPerShot = 3),
				AssaultTurretBalancing(fireRestrictions = FireRestrictions(canFire = true, minBlockCount = 9750, ), maxPerShot = 5,),
				HeavyTurretBalancing(fireRestrictions = FireRestrictions(canFire = false)),
				LaserCannonBalancing(fireRestrictions = FireRestrictions(canFire = false), firePowerConsumption = 360),
				SwarmMissileBalancing(fireRestrictions = FireRestrictions(canFire = false, minBlockCount = 8000, maxBlockCount = 12000), maxPerShot = 2, boostChargeNanos = TimeUnit.SECONDS.toNanos(8))
			),
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.large.near"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.large.far")
			)
		),
		val interdictorDestroyer: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 5,
			maxSneakFlyAccel = 3,
			interdictionRange = 2000,
			warmupTime = 15,
			jumpStrength = 1.0,
			wellStrength = 3.0,
			hyperspaceRangeMultiplier = 1.9,
			cruiseSpeedMultiplier = 0.9,
			shieldPowerMultiplier = 0.7,
			requiredMultiblocks = listOf(
				RequiredSubsystemInfo(
					MediumReactorSubsystem::class.java,
					1,
					"Tech 2 destroyers require a medium reactor to pilot!"
				),
				RequiredSubsystemInfo(
					FuelTankSubsystem::class.java,
					1,
					"tech 2 ships require a fuel tank to pilot!"
				)
			),
			weaponOverrides = listOf(
				PhaserBalancing(fireRestrictions = FireRestrictions(canFire = false)),
				LaserCannonBalancing(fireRestrictions = FireRestrictions(canFire = true), firePowerConsumption = 360),
				SwarmMissileBalancing(fireRestrictions = FireRestrictions(canFire = true, minBlockCount = 8000, maxBlockCount = 12000), maxPerShot = 2, boostChargeNanos = TimeUnit.SECONDS.toNanos(8))
			),
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.large.near"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.large.far")
			)
		),
		val cruiser: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 5,
			maxSneakFlyAccel = 3,
			interdictionRange = 1250,
			warmupTime = 30,
			jumpStrength = 3.0,
			wellStrength = 2.0,
			hyperspaceRangeMultiplier = 1.9,
			cruiseSpeedMultiplier = 0.80,
			shieldPowerMultiplier = 0.85,
			weaponOverrides = listOf(
				IonTurretBalancing(fireRestrictions = FireRestrictions(canFire = true)),
				HeavyTurretBalancing(fireRestrictions = FireRestrictions(canFire = false)),
				ArsenalRocketBalancing(fireRestrictions = FireRestrictions(canFire = false), firePowerConsumption = 17500),
				LaserCannonBalancing(fireRestrictions = FireRestrictions(canFire = false)),
				ProbeBalancing(fireRestrictions = FireRestrictions(canFire = true))
			),
			forbiddenMultiblocks = listOf(
				IncompatibleSubsystemInfo(
					GravityWellSubsystem::class.java,
					"Only interdictors can use gravity wells!"
				),
				IncompatibleSubsystemInfo(
					JumpBeaconSubsystem::class.java,
					"This ship cannot use jump beacons!"
				),
				IncompatibleSubsystemInfo(
					LargeReactorSubsystem::class.java,
					"Tech 1 super-capitals cannot house tech 2 reactors!"
				)
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
			shipSounds = StarshipSounds(
				pilot = SoundInfo("horizonsend:starship.pilot.cruiser", volume = 5f),
				release = SoundInfo("horizonsend:starship.release.cruiser", volume = 5f),
				enterHyperspace = SoundInfo("horizonsend:starship.supercapital.hyperspace_enter"),
				explodeNear = SoundInfo("horizonsend:starship.explosion.cruiser"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.cruiser"),
			)
		),
		val logisticsCruiser: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 5,
			maxSneakFlyAccel = 3,
			interdictionRange = 1250,
			warmupTime = 30,
			jumpStrength = 3.0,
			wellStrength = 0.0,
			hyperspaceRangeMultiplier = 1.9,
			cruiseSpeedMultiplier = 0.85,
			shieldPowerMultiplier = 0.8,
			shieldRegenMultiplier = 4.0,
			commandBurstOverrides = listOf(
				CapitalSkirmishCommandBurstBalancing(activateRestrictions = StarshipCommandBurstBalancing.ActivateRestrictions(canActivate = true, incompatibleMultiblocks = listOf(
					IncompatibleSubsystemInfo(
						CapitalShieldCommandBurstSubsystem::class.java,
						"You cannot have more than one type of command burst!"
					)
				))),
				CapitalShieldCommandBurstBalancing(activateRestrictions = StarshipCommandBurstBalancing.ActivateRestrictions(canActivate = true, incompatibleMultiblocks = listOf(
					IncompatibleSubsystemInfo(
						CapitalSkirmishCommandBurstSubsystem::class.java,
						"You cannot have more than one type of command burst!"
					)
				)))
			),
			weaponOverrides = listOf(
				IonTurretBalancing(fireRestrictions = FireRestrictions(canFire = false)),
				HeavyTurretBalancing(fireRestrictions = FireRestrictions(canFire = false)),
				ArsenalRocketBalancing(fireRestrictions = FireRestrictions(canFire = false)),
				LaserCannonBalancing(fireRestrictions = FireRestrictions(canFire = false)),
				TriTurretBalancing(fireRestrictions = FireRestrictions(canFire = false)),
				HeavyLogisticsCannonBalancing(fireRestrictions = FireRestrictions(canFire = true, minBlockCount = 14500), maxPerShot = 2)
			),
			requiredMultiblocks = listOf(
				RequiredSubsystemInfo(
					FuelTankSubsystem::class.java,
					1,
					"Cruisers require a fuel tank to pilot!"
				),
				RequiredSubsystemInfo(
					LargeReactorSubsystem::class.java,
					1,
					"Tech 2 cruisers require a large reactor to pilot!"
				)
			),
			forbiddenMultiblocks = listOf(
				IncompatibleSubsystemInfo(
					JumpBeaconSubsystem::class.java,
					"This ship cannot use jump beacons!"
				),
				IncompatibleSubsystemInfo(
					GravityWellSubsystem::class.java,
					"Only interdictors can use gravity wells!"
				),
				IncompatibleSubsystemInfo(
					IonTurretWeaponSubsystem::class.java,
					"Logistics ships cannot have weapons!"
				),
				IncompatibleSubsystemInfo(
					TriTurretWeaponSubsystem::class.java,
					"Logistics ships cannot have weapons!"
				)
			),
			shipSounds = StarshipSounds(
				pilot = SoundInfo("horizonsend:starship.pilot.cruiser", volume = 5f),
				release = SoundInfo("horizonsend:starship.release.cruiser", volume = 5f),
				enterHyperspace = SoundInfo("horizonsend:starship.supercapital.hyperspace_enter"),
				explodeNear = SoundInfo("horizonsend:starship.explosion.cruiser"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.cruiser"),
			)
		),
		val missileCruiser: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 5,
			maxSneakFlyAccel = 3,
			interdictionRange = 1250,
			warmupTime = 30,
			jumpStrength = 3.0,
			wellStrength = 2.0,
			hyperspaceRangeMultiplier = 1.9,
			cruiseSpeedMultiplier = 0.85,
			shieldPowerMultiplier = 0.95,
			shieldRegenMultiplier = 0.8,
			weaponOverrides = listOf(
				IonTurretBalancing(fireRestrictions = FireRestrictions(canFire = false)),
				HeavyTurretBalancing(fireRestrictions = FireRestrictions(canFire = false)),
				ArsenalRocketBalancing(fireRestrictions = FireRestrictions(canFire = true), maxPerShot = 4, firePowerConsumption = 18000),
				LaserCannonBalancing(fireRestrictions = FireRestrictions(canFire = false)),
				TriTurretBalancing(fireRestrictions = FireRestrictions(canFire = false)),
				RapidHeavyMissileLauncherBalancing(fireRestrictions = FireRestrictions(canFire = true)),
				LightMissileLauncherBalancing(fireRestrictions = FireRestrictions(canFire = true)),
				AdvancedProbeBalancing(fireRestrictions = FireRestrictions(canFire = true)),
				ThermonuclearMissileBalancing(fireRestrictions = FireRestrictions(canFire = true))
			),
			requiredMultiblocks = listOf(
				RequiredSubsystemInfo(
					FuelTankSubsystem::class.java,
					1,
					"Cruisers require a fuel tank to pilot!"
				),
				RequiredSubsystemInfo(
					LargeReactorSubsystem::class.java,
					1,
					"Tech 2 cruisers require a large reactor to pilot!"
				)
			),
			forbiddenMultiblocks = listOf(
				IncompatibleSubsystemInfo(
					JumpBeaconSubsystem::class.java,
					"This ship cannot use jump beacons!"
				),
				IncompatibleSubsystemInfo(
					GravityWellSubsystem::class.java,
					"Only interdictors can use gravity wells!"
				),
				IncompatibleSubsystemInfo(
					TriTurretWeaponSubsystem::class.java,
					"Missile ships cannot have tri turrets!"
				)
				,
				IncompatibleSubsystemInfo(
					IonTurretWeaponSubsystem::class.java,
					"Logistics ships cannot have ion turrets!"
				)
			),
			shipSounds = StarshipSounds(
				pilot = SoundInfo("horizonsend:starship.pilot.cruiser", volume = 5f),
				release = SoundInfo("horizonsend:starship.release.cruiser", volume = 5f),
				enterHyperspace = SoundInfo("horizonsend:starship.supercapital.hyperspace_enter"),
				explodeNear = SoundInfo("horizonsend:starship.explosion.cruiser"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.cruiser"),
			)
		),
		val battlecruiser: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 3,
			maxSneakFlyAccel = 3,
			interdictionRange = 1500,
			warmupTime = 30,
			jumpStrength = 3.0,
			wellStrength = 3.0,
			hyperspaceRangeMultiplier = 2.5,
			cruiseSpeedMultiplier = 0.9,
			shieldPowerMultiplier = 1.30,
			weaponOverrides = listOf(
				QuadTurretBalancing(fireRestrictions = FireRestrictions(canFire = true, minBlockCount = 17500)),
				TriTurretBalancing(
					fireRestrictions = FireRestrictions(canFire = true),
					projectile = TriTurretProjectileBalancing(speed = 110.0)
				),
				ArsenalRocketBalancing(fireRestrictions = FireRestrictions(canFire = false)),
				LaserCannonBalancing(fireRestrictions = FireRestrictions(canFire = false)),
				ProbeBalancing(fireRestrictions = FireRestrictions(canFire = true))
			),
			forbiddenMultiblocks = listOf(
				IncompatibleSubsystemInfo(
					GravityWellSubsystem::class.java,
					"Only interdictors can use gravity wells!"
				),
				IncompatibleSubsystemInfo(
					JumpBeaconSubsystem::class.java,
				"This ship cannot use jump beacons!"
				),
				IncompatibleSubsystemInfo(
					LargeReactorSubsystem::class.java,
					"Tech 1 super-capitals cannot house tech 2 reactors!"
				)
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
			shipSounds = StarshipSounds(
				pilot = SoundInfo("horizonsend:starship.pilot.battlecruiser", volume = 7f),
				release = SoundInfo("horizonsend:starship.release.battlecruiser", volume = 7f),
				enterHyperspace = SoundInfo("horizonsend:starship.supercapital.hyperspace_enter"),
				explodeNear = SoundInfo("horizonsend:starship.explosion.battlecruiser"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.battlecruiser")
			)
		),
		val lancerBattlecruiser: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 3,
			maxSneakFlyAccel = 3,
			interdictionRange = 1500,
			warmupTime = 30,
			jumpStrength = 6.0,
			wellStrength = 3.0,
			hyperspaceRangeMultiplier = 2.5,
			cruiseSpeedMultiplier = 1.05,
			shieldPowerMultiplier = 1.90,
			weaponOverrides = listOf(
				QuadTurretBalancing(fireRestrictions = FireRestrictions(canFire = false, minBlockCount = 17500)),
				TriTurretBalancing(
					fireRestrictions = FireRestrictions(canFire = true),
					projectile = TriTurretProjectileBalancing(speed = 110.0)
				),
				ArsenalRocketBalancing(fireRestrictions = FireRestrictions(canFire = false)),
				LaserCannonBalancing(fireRestrictions = FireRestrictions(canFire = false)),
				DoomsdayDeviceBalancing(fireRestrictions = FireRestrictions(canFire = true, minBlockCount = 30000), maxPerShot = 1),
				ACAPTurretBalancing(fireRestrictions = FireRestrictions(canFire = true, minBlockCount = 27500), maxPerShot = 2)

			),
			requiredMultiblocks = listOf(
				RequiredSubsystemInfo(
					LargeReactorSubsystem::class.java,
					1,
					"Tech 2 battlecruisers require a large reactor to pilot!"
				),
				RequiredSubsystemInfo(
					FuelTankSubsystem::class.java,
					1,
					"Battlecruisers require fuel to pilot!"
				)
			),
			forbiddenMultiblocks = listOf(
				IncompatibleSubsystemInfo(
					GravityWellSubsystem::class.java,
					"Only interdictors can use gravity wells!"
				),
				IncompatibleSubsystemInfo(
					JumpBeaconSubsystem::class.java,
					"This ship cannot use jump beacons!"
				)
			),
			shipSounds = StarshipSounds(
				pilot = SoundInfo("horizonsend:starship.pilot.battlecruiser", volume = 7f),
				release = SoundInfo("horizonsend:starship.release.battlecruiser", volume = 7f),
				enterHyperspace = SoundInfo("horizonsend:starship.supercapital.hyperspace_enter"),
				explodeNear = SoundInfo("horizonsend:starship.explosion.battlecruiser"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.battlecruiser")
			)
		),
		val battleship: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 3,
			maxSneakFlyAccel = 3,
			interdictionRange = 5200,
			warmupTime = 30,
			jumpStrength = 5.0,
			wellStrength = 5.0,
			hyperspaceRangeMultiplier = 2.75,
			cruiseSpeedMultiplier = 0.80,
			shieldPowerMultiplier = 1.75,
			weaponOverrides = listOf(
				QuadTurretBalancing(
					fireRestrictions = FireRestrictions(canFire = true, minBlockCount = 30000),
					maxPerShot = 5
				)
			),
			shipSounds = StarshipSounds(
				pilot = SoundInfo("horizonsend:starship.pilot.battlecruiser", volume = 7f),
				release = SoundInfo("horizonsend:starship.release.battlecruiser", volume = 7f),
				enterHyperspace = SoundInfo("horizonsend:starship.supercapital.hyperspace_enter"),
				explodeNear = SoundInfo("horizonsend:starship.explosion.battlecruiser"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.battlecruiser")
			)
		),
		val dreadnought: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 3,
			maxSneakFlyAccel = 2,
			interdictionRange = 6000,
			warmupTime = 30,
			jumpStrength = 5.0,
			wellStrength = 5.0,
			hyperspaceRangeMultiplier = 3.0,
			cruiseSpeedMultiplier = 0.70,
			shieldPowerMultiplier = 2.0,
			weaponOverrides = listOf(
				QuadTurretBalancing(
					fireRestrictions = FireRestrictions(canFire = true, minBlockCount = 30000),
					maxPerShot = 5
				)
			),
			shipSounds = StarshipSounds(
				pilot = SoundInfo("horizonsend:starship.pilot.battlecruiser", volume = 7f),
				release = SoundInfo("horizonsend:starship.release.battlecruiser", volume = 7f),
				enterHyperspace = SoundInfo("horizonsend:starship.supercapital.hyperspace_enter"),
				explodeNear = SoundInfo("horizonsend:starship.explosion.battlecruiser"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.battlecruiser")
			)
		),
		val platform: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 0,
			maxSneakFlyAccel = 0,
			interdictionRange = 0,
			warmupTime = 6767,
			jumpStrength = 0.0,
			wellStrength = 0.0,
			hyperspaceRangeMultiplier = 0.0,
		),
		val testing: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 10,
			maxSneakFlyAccel = 3,
			interdictionRange = 2000,
			warmupTime = 5,
			jumpStrength = 5.0,
			weaponOverrides = listOf(
				QuadTurretBalancing(
					fireRestrictions = FireRestrictions(canFire = true)
				),
				ACAPTurretBalancing(
					fireRestrictions = FireRestrictions(canFire = true)
				),
				ThermonuclearMissileBalancing(
					fireRestrictions = FireRestrictions(canFire = true)
				),
				DoomsdayDeviceBalancing(
					fireRestrictions = FireRestrictions(canFire = true),
					boostChargeNanos = TimeUnit.SECONDS.toNanos(5)
				),
				NeutralizerBalancing(
					fireRestrictions = FireRestrictions(canFire = true)
				)
			),
			wellStrength = 5.0,
			hyperspaceRangeMultiplier = 10.0,
			shieldPowerMultiplier = 2.0
		),
		val unidentified: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 10,
			maxSneakFlyAccel = 3,
			warmupTime = 5,
			interdictionRange = 2000,
			jumpStrength = 5.0,
			wellStrength = 5.0,
			hyperspaceRangeMultiplier = 10.0,
			shieldPowerMultiplier = 2.0
		)
	)
}

@Serializable
sealed interface StarshipTypeBalancing {
	val canMove: Boolean
	val accelMultiplier: Double
	val maxSpeedMultiplier: Double
	val shipSounds: StarshipSounds
	val warmupTime: Int

	val sneakFlyAccelDistance: Int
	val maxSneakFlyAccel: Int
	val interdictionRange: Int
	val jumpStrength: Double
	val wellStrength: Double
	val hyperspaceRangeMultiplier: Double
	val cruiseSpeedMultiplier: Double
	val maxCruiseSpeed: Int
	val shieldPowerMultiplier: Double
	val shieldRegenMultiplier: Double

	val requiredMultiblocks: List<RequiredSubsystemInfo>
	val forbiddenMultiblocks: List<IncompatibleSubsystemInfo>

	val weaponOverrides: List<StarshipWeaponBalancing<*>>
	val commandBurstOverrides: List<StarshipCommandBurstBalancing>
}

@Serializable
open class StanrdardStarshipTypeBalancing(
	override var canMove: Boolean = true,
	override var accelMultiplier: Double = 1.0,
	override var maxSpeedMultiplier: Double = 1.0,
	override var warmupTime: Int = 10,

	override val shipSounds: StarshipSounds = StarshipSounds(),

	override var sneakFlyAccelDistance: Int,

	override var maxSneakFlyAccel: Int,
	override var interdictionRange: Int,
	override var jumpStrength: Double,
	override var wellStrength: Double,
	override var hyperspaceRangeMultiplier: Double,
	override var cruiseSpeedMultiplier: Double = 1.0,
	override var maxCruiseSpeed: Int = Int.MAX_VALUE,
	override var shieldPowerMultiplier: Double = 1.0,
	override var shieldRegenMultiplier: Double = 1.0,

	override val requiredMultiblocks: List<RequiredSubsystemInfo> = listOf(),
	override val forbiddenMultiblocks: List<IncompatibleSubsystemInfo> = listOf(),

	override val weaponOverrides: List<StarshipWeaponBalancing<*>> = listOf(),
	override val commandBurstOverrides: List<StarshipCommandBurstBalancing> = listOf(),
) : StarshipTypeBalancing

@Serializable
open class GroundStarshipBalancing(
	override var canMove: Boolean = true,
	override var accelMultiplier: Double = 1.0,
	override var maxSpeedMultiplier: Double = 1.0,
	override val warmupTime: Int,

	override val shipSounds: StarshipSounds = StarshipSounds(),

	override var sneakFlyAccelDistance: Int,
	override var maxSneakFlyAccel: Int,
	override var interdictionRange: Int,
	override var jumpStrength: Double,
	override var wellStrength: Double,
	override var hyperspaceRangeMultiplier: Double,
	override var cruiseSpeedMultiplier: Double = 1.0,
	override var maxCruiseSpeed: Int = Int.MAX_VALUE,
	override var shieldPowerMultiplier: Double = 1.0,
	override var shieldRegenMultiplier: Double = 1.0,

	override val requiredMultiblocks: List<RequiredSubsystemInfo> = listOf(),
	override val forbiddenMultiblocks: List<IncompatibleSubsystemInfo> = listOf(),

	override val weaponOverrides: List<StarshipWeaponBalancing<*>> = listOf(),
	override val commandBurstOverrides: List<StarshipCommandBurstBalancing> = listOf(),
) : StarshipTypeBalancing

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
data class IncompatibleSubsystemInfo(
	@Serializable(with = SubsystemSerializer::class) val subsystem: Class<out @Contextual StarshipSubsystem>,
	val failMessage: String,
) {
	/**
	 * Tests whether the starship subsystems contain incompatible multiblocks
	 **/
	fun checkRequirements(subsystems: LinkedList<StarshipSubsystem>): Boolean {
		return (subsystems.groupBy { it.javaClass }[subsystem]?.count() ?: 0) <= 0
	}
}

@Serializable
data class StarshipSounds(
	val pilot: SoundInfo = SoundInfo("minecraft:block.beacon.activate", volume = 5f, pitch = 0.05f),
	val release: SoundInfo = SoundInfo("minecraft:block.beacon.deactivate", volume = 5f, pitch = 0.05f),
	val enterHyperspace: SoundInfo = SoundInfo("minecraft:entity.elder_guardian.hurt", volume = 5f, pitch = 0.05f),
	val exitHyperspace: SoundInfo = SoundInfo("minecraft:entity.warden.sonic_boom", pitch = 0f),
	val explodeNear: SoundInfo? = null,
	val explodeFar: SoundInfo? = null,
	val startCruise: SoundInfo = SoundInfo("minecraft:block.note_block.chime", volume = 5f, pitch = 0.53f),
	val stopCruise: SoundInfo = SoundInfo("minecraft:block.note_block.banjo", volume = 5f, pitch = 1.782f),
	val jumpChargeNear: SoundInfo = SoundInfo("horizonsend:starship.jump.charge.near"),
	val jumpChargeFar: SoundInfo = SoundInfo("horizonsend:starship.jump.charge.far"),
	val jumpCompleteNear: SoundInfo = SoundInfo("horizonsend:starship.jump.complete.near"),
	val jumpCompleteFar: SoundInfo = SoundInfo("horizonsend:starship.jump.complete.far"),
	val horn: SoundInfo = SoundInfo("minecraft:item.goat_horn.sound.6", pitch = 2f)
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

	val fireSoundNear: SoundInfo

	val fireSoundFar: SoundInfo

	val entityDamage: EntityDamage

	@Suppress("UnstableApiUsage")
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
		data class TrueDamage(val amount: Double) : EntityDamage {
			override fun deal(target: LivingEntity, shooter: Damager, type: DamageType) {
				fun damage(amount: Double, sourceEntity: Entity?) {
					if (target.isDead || (target is Player && !target.gameMode.isInvulnerable))
						target.damage(0.0, getCause(sourceEntity, type))
					target.health -= minOf(target.health, amount)
				}

				when (shooter) {
					is PlayerDamager -> damage(amount, shooter.player)
					is EntityDamager -> damage(amount, shooter.entity)
					else -> damage(amount, null)
				}
			}
		}

		@Serializable
		data object NoDamage : EntityDamage {
			override fun deal(target: LivingEntity, shooter: Damager, type: DamageType) {}
		}
	}
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
sealed interface StarshipProximityProjectileBalancing : StarshipProjectileBalancing {
	val proximityRange: Double
}

@Serializable
sealed interface StarshipHealingProjectileBalancing : StarshipProjectileBalancing {
	val shieldBoostFactor: Int
}

sealed interface StarshipShieldDrainingProjectileBalancing : StarshipProjectileBalancing {
	val shieldDrainFactor: Int
}

@Serializable
sealed interface StarshipStatusEffectProjectileBalancing : StarshipProjectileBalancing {
	val effectStrength: Double
	val effectDurationMillis: Long
}

@Serializable
sealed interface StarshipArcedProjectileBalancing : StarshipParticleProjectileBalancing {
	val gravityMultiplier: Double
	val decelerationAmount: Double
}

@Serializable
sealed interface StarshipWaveProjectileBalancing : StarshipParticleProjectileBalancing {
	val separation: Double
}

@Serializable
sealed interface StarshipBoidProjectileBalancing : StarshipProjectileBalancing {
	val separationDistance: Double
	val separationFactor: Double
	val visibleDistance: Double
	val alignFactor: Double
	val centerFactor: Double
	val minSpeedFactor: Double
	val maxSpeedFactor: Double
	val originalDirectionFactor: Double
}

@Serializable
sealed interface StarshipWeaponBalancing<T : StarshipProjectileBalancing> {
	val clazz: KClass<out BalancedWeaponSubsystem<*>>
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
		val maxBlockCount: Int = Int.MAX_VALUE,
		val incompatibleMultiblocks: List<IncompatibleSubsystemInfo> = listOf(),
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
	val inaccuracyDegrees: Double
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
sealed interface StarshipCommandBurstBalancing {
	val clazz: KClass<out AbstractCommandBurstSubsystem<*>>

	val activateRestrictions: ActivateRestrictions

	val activateCooldownMillis: Long
	val range: Double
	val effectDurationMillis: Long

	/**
	 * @param canActivate Whether this weapon can be fired.
	 * @param minBlockCount The minimum block count of a ship to be able to fire this weapon.
	 * @param maxBlockCount The maximum block count of a ship to be able to fire this weapon.
	 * **/
	@Serializable
	data class ActivateRestrictions(
		val canActivate: Boolean = true,
		val minBlockCount: Int = 0,
		val maxBlockCount: Int = Int.MAX_VALUE,
		val incompatibleMultiblocks: List<IncompatibleSubsystemInfo> = listOf(),
	)
}

@Serializable
sealed interface StarshipMultiplierCommandBurstBalancing : StarshipCommandBurstBalancing {
	val effectStrength: Double
}
