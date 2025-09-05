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
import net.horizonsend.ion.server.features.starship.subsystem.weapon.BalancedWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.Projectile
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
	val shipClasses: ShipClasses = ShipClasses()
) {
	@Serializable
	data class WeaponDefaults(
		val weapons: List<StarshipWeaponBalancing<*>> = listOf(
			TorpedoBalancing(),
			HeavyLaserBalancing(),
			PhaserBalancing(),
			ArsenalRocketBalancing(),
			TriTurretBalancing(),
			LightTurretBalancing(),
			HeavyTurretBalancing(),
			QuadTurretBalancing(),
			IonTurretBalancing(),
			PointDefenseBalancing(),
			PulseCannonBalancing(),
			PlasmaCannonBalancing(),
			LaserCannonBalancing(),
			InterceptorCannonBalancing(),

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
		)
	)

	@Serializable
	data class ShipClasses(
		val speeder: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 3,
			maxSneakFlyAccel = 3,
			interdictionRange = 10,
			jumpStrength = 0.0,
			wellStrength = 0.0,
			hyperspaceRangeMultiplier = 0.0,
			shieldPowerMultiplier = 1.0,
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.fighter.near"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.fighter.far")
			)
		),
		val shuttle: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 5,
			maxSneakFlyAccel = 2,
			interdictionRange = 300,
			jumpStrength = 1.0,
			wellStrength = 0.0,
			hyperspaceRangeMultiplier = 1.2,
			shieldPowerMultiplier = 1.0,
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.fighter.near"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.fighter.far")
			)
		),
		val transport: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 10,
			maxSneakFlyAccel = 3,
			interdictionRange = 600,
			jumpStrength = 1.0,
			wellStrength = 1.0,
			hyperspaceRangeMultiplier = 1.25,
			shieldPowerMultiplier = 1.0,
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.small.near"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.small.far")
			)
		),
		val lightFreighter: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 10,
			maxSneakFlyAccel = 3,
			interdictionRange = 900,
			jumpStrength = 1.0,
			wellStrength = 1.0,
			hyperspaceRangeMultiplier = 1.3,
			shieldPowerMultiplier = 1.0,
			weaponOverrides = listOf(
				LightTurretBalancing(
					fireRestrictions = FireRestrictions(
						canFire = true,
						minBlockCount = 1750,
						maxBlockCount = 12000
					),
					firePowerConsumption = 5300,
				)
			),
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.small.near"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.small.far")
			)
		),
		val mediumFreighter: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 10,
			maxSneakFlyAccel = 3,
			interdictionRange = 1200,
			jumpStrength = 1.0,
			wellStrength = 1.0,
			hyperspaceRangeMultiplier = 1.35,
			shieldPowerMultiplier = 1.0,
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.large.near"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.large.far")
			)
		),
		val heavyFreighter: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 10,
			maxSneakFlyAccel = 3,
			interdictionRange = 1500,
			jumpStrength = 1.0,
			wellStrength = 1.0,
			hyperspaceRangeMultiplier = 1.4,
			shieldPowerMultiplier = 1.0,
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.large.near"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.large.far")
			)
		),
		val barge: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 3,
			maxSneakFlyAccel = 3,
			interdictionRange = 4500,
			jumpStrength = 2.0,
			wellStrength = 1.0,
			hyperspaceRangeMultiplier = 3.0,
			cruiseSpeedMultiplier = 0.88,
			shieldPowerMultiplier = 1.30,
			weaponOverrides = listOf(
				TriTurretBalancing(
					fireRestrictions = FireRestrictions(minBlockCount = 3400),
					boostChargeNanos = TimeUnit.SECONDS.toNanos(7),
					projectile = TriTurretProjectileBalancing(speed = 110.0)
				),
				HeavyTurretBalancing(
					fireRestrictions = FireRestrictions(minBlockCount = 16500, maxBlockCount = 20000),
					firePowerConsumption = 10000,
					projectile = HeavyTurretBalancing.HeavyTurretProjectileBalancing(speed = 200.0)
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
		val tank: StarshipTypeBalancing = GroundStarshipBalancing(
			sneakFlyAccelDistance = 4,
			maxSneakFlyAccel = 4,
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
			interdictionRange = 10,
			jumpStrength = 1.0,
			wellStrength = 0.0,
			hyperspaceRangeMultiplier = 1.5,
			shieldPowerMultiplier = 1.0,
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.fighter.near"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.fighter.far")
			)
		),
		val interceptor: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 4,
			maxSneakFlyAccel = 4,
			interdictionRange = 10,
			jumpStrength = 1.0,
			wellStrength = 0.0,
			hyperspaceRangeMultiplier = 0.0,
			shieldPowerMultiplier = 0.33,
			cruiseSpeedMultiplier = 1.1,
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
			interdictionRange = 1200,
			jumpStrength = 1.0,
			wellStrength = 1.0,
			hyperspaceRangeMultiplier = 1.6,
			shieldPowerMultiplier = 1.0,
			weaponOverrides = listOf(
				LightTurretBalancing(fireRestrictions = FireRestrictions(minBlockCount = 1750, maxBlockCount = 12000)),
				PulseCannonBalancing(fireRestrictions = FireRestrictions(minBlockCount = 1000, maxBlockCount = 4000))
			),
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.small.near"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.small.far")
			)
		),
		val corvette: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 6,
			maxSneakFlyAccel = 2,
			interdictionRange = 1800,
			jumpStrength = 1.0,
			wellStrength = 1.0,
			hyperspaceRangeMultiplier = 1.7,
			shieldPowerMultiplier = 1.0,
			weaponOverrides = listOf(
				LightTurretBalancing(fireRestrictions = FireRestrictions(canFire = true, maxBlockCount = 12000)),
				TriTurretBalancing(fireRestrictions = FireRestrictions(canFire = true, minBlockCount = 3400)),
				PulseCannonBalancing(fireRestrictions = FireRestrictions(canFire = true, minBlockCount = 1000, maxBlockCount = 4000)),
			),
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.small.near"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.small.far")
			)
		),
		val frigate: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 6,
			maxSneakFlyAccel = 2,
			interdictionRange = 2400,
			jumpStrength = 1.0,
			wellStrength = 1.0,
			hyperspaceRangeMultiplier = 1.8,
			shieldPowerMultiplier = 1.0,
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.large.near"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.large.far")
			)
		),
		val destroyer: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 5,
			maxSneakFlyAccel = 3,
			interdictionRange = 3000,
			jumpStrength = 1.0,
			wellStrength = 1.0,
			hyperspaceRangeMultiplier = 1.9,
			shieldPowerMultiplier = 1.0,
			shipSounds = StarshipSounds(
				explodeNear = SoundInfo("horizonsend:starship.explosion.large.near"),
				explodeFar = SoundInfo("horizonsend:starship.explosion.large.far")
			)
		),
		val cruiser: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 5,
			maxSneakFlyAccel = 3,
			interdictionRange = 3500,
			jumpStrength = 2.0,
			wellStrength = 2.0,
			hyperspaceRangeMultiplier = 1.9,
			cruiseSpeedMultiplier = 0.98,
			shieldPowerMultiplier = 1.10,
			weaponOverrides = listOf(
				IonTurretBalancing(fireRestrictions = FireRestrictions(canFire = true)),
				HeavyTurretBalancing(fireRestrictions = FireRestrictions(canFire = false)),
				ArsenalRocketBalancing(fireRestrictions = FireRestrictions(canFire = true)),
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
		val battlecruiser: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 3,
			maxSneakFlyAccel = 3,
			interdictionRange = 4500,
			jumpStrength = 3.0,
			wellStrength = 3.0,
			hyperspaceRangeMultiplier = 2.5,
			cruiseSpeedMultiplier = 0.88,
			shieldPowerMultiplier = 1.60,
			weaponOverrides = listOf(
				QuadTurretBalancing(fireRestrictions = FireRestrictions(canFire = true)),
				TriTurretBalancing(
					fireRestrictions = FireRestrictions(canFire = false),
					boostChargeNanos = TimeUnit.SECONDS.toNanos(7),
					projectile = TriTurretProjectileBalancing(speed = 110.0)
				),
				ArsenalRocketBalancing(fireRestrictions = FireRestrictions(canFire = true)),
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
		val battleship: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 3,
			maxSneakFlyAccel = 3,
			interdictionRange = 5200,
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
			jumpStrength = 0.0,
			wellStrength = 0.0,
			hyperspaceRangeMultiplier = 0.0,
		),
		val unidentified: StarshipTypeBalancing = StanrdardStarshipTypeBalancing(
			sneakFlyAccelDistance = 10,
			maxSneakFlyAccel = 3,
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

	val sneakFlyAccelDistance: Int
	val maxSneakFlyAccel: Int
	val interdictionRange: Int
	val jumpStrength: Double
	val wellStrength: Double
	val hyperspaceRangeMultiplier: Double
	val cruiseSpeedMultiplier: Double
	val shieldPowerMultiplier: Double

	val requiredMultiblocks: List<RequiredSubsystemInfo>

	val weaponOverrides: List<StarshipWeaponBalancing<*>>
}

@Serializable
open class StanrdardStarshipTypeBalancing(
	override val canMove: Boolean = true,
	override val accelMultiplier: Double = 1.0,
	override val maxSpeedMultiplier: Double = 1.0,

	override val shipSounds: StarshipSounds = StarshipSounds(),

	override val sneakFlyAccelDistance: Int,
	override val maxSneakFlyAccel: Int,
	override val interdictionRange: Int,
	override val jumpStrength: Double,
	override val wellStrength: Double,
	override val hyperspaceRangeMultiplier: Double,
	override val cruiseSpeedMultiplier: Double = 1.0,
	override val shieldPowerMultiplier: Double = 1.0,

	override val requiredMultiblocks: List<RequiredSubsystemInfo> = listOf(),

	override val weaponOverrides: List<StarshipWeaponBalancing<*>> = listOf(),
) : StarshipTypeBalancing

@Serializable
open class GroundStarshipBalancing(
	override val canMove: Boolean = true,
	override val accelMultiplier: Double = 1.0,
	override val maxSpeedMultiplier: Double = 1.0,

	override val shipSounds: StarshipSounds = StarshipSounds(),

	override val sneakFlyAccelDistance: Int,
	override val maxSneakFlyAccel: Int,
	override val interdictionRange: Int,
	override val jumpStrength: Double,
	override val wellStrength: Double,
	override val hyperspaceRangeMultiplier: Double,
	override val cruiseSpeedMultiplier: Double = 1.0,
	override val shieldPowerMultiplier: Double = 1.0,

	override val requiredMultiblocks: List<RequiredSubsystemInfo> = listOf(),

	override val weaponOverrides: List<StarshipWeaponBalancing<*>> = listOf(),
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
data class StarshipSounds(
	val pilot: SoundInfo = SoundInfo("minecraft:block.beacon.activate", volume = 5f, pitch = 0.05f),
	val release: SoundInfo = SoundInfo("minecraft:block.beacon.deactivate", volume = 5f, pitch = 0.05f),
	val enterHyperspace: SoundInfo = SoundInfo("minecraft:entity.elder_guardian.hurt", volume = 5f, pitch = 0.05f),
	val exitHyperspace: SoundInfo = SoundInfo("minecraft:entity.warden.sonic_boom", pitch = 0f),
	val explodeNear: SoundInfo? = null,
	val explodeFar: SoundInfo? = null,
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
sealed interface StarshipArcedProjectileBalancing : StarshipParticleProjectileBalancing {
	val gravityMultiplier: Double
	val decelerationAmount: Double
}

@Serializable
sealed interface StarshipWaveProjectileBalancing : StarshipParticleProjectileBalancing {
	val separation: Double
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
