package net.horizonsend.ion.server.configuration.starship

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.horizonsend.ion.server.configuration.starship.AbyssalGazeBalancing.AbyssalGazeProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.ArsenalRocketBalancing.ArsenalRocketProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.CycleTurretBalancing.CycleTurretProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.DisintegratorBeamBalancing.DisintegratorBeamProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.DoomsdayDeviceBalancing.DoomsdayDeviceProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.FlamethrowerCannonBalancing.FlamethrowerCannonProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.FlamingSkullCannonBalancing.FlamingSkullCannonProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.HeavyLaserBalancing.HeavyLaserProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.HeavyTurretBalancing.HeavyTurretProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.InterceptorCannonBalancing.IncterceptorCannonProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.IonTurretBalancing.IonTurretProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.LaserCannonBalancing.LaserCannonProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.LightTurretBalancing.LightTurretProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.LogisticsTurretBalancing.LogisticsTurretProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.MiniPhaserBalancing.MiniPhaserProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.PhaserBalancing.PhaserProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.PlasmaCannonBalancing.PlasmaCannonProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.PointDefenseBalancing.PointDefenseProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.PulseCannonBalancing.PulseCannonProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.PumpkinCannonBalancing.PumpkinCannonProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.QuadTurretBalancing.QuadTurretProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.RocketBalancing.RocketProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.SonicMissileBalancing.SonicMissileProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.StarshipProjectileBalancing.EntityDamage
import net.horizonsend.ion.server.configuration.starship.StarshipProjectileBalancing.EntityDamage.RegularDamage
import net.horizonsend.ion.server.configuration.starship.StarshipSounds.SoundInfo
import net.horizonsend.ion.server.configuration.starship.StarshipWeaponBalancing.FireRestrictions
import net.horizonsend.ion.server.configuration.starship.TriTurretBalancing.TriTurretProjectileBalancing
import net.horizonsend.ion.server.features.starship.subsystem.weapon.BalancedWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.AbyssalGazeSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.CapitalBeamWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.FlamethrowerWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.FlamingSkullCannonWeaponSubsystem
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
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.DisintegratorBeamWeaponSubsystem
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
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.TestBoidWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.ArsenalRocketProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.CycleTurretProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.DisintegratorBeamProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.DoomsdayDeviceProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.HeavyLaserProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.InterceptorCannonProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.IonTurretProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.LaserCannonLaserProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.LogisticTurretProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.PhaserProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.PlasmaLaserProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.PointDefenseLaserProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.Projectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.PulseLaserProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.RocketProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.TestBoidProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.TorpedoProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.TurretLaserProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.ArsenalRocketStarshipWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.HeavyLaserWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.PhaserWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.RocketWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.TorpedoWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.TriTurretWeaponSubsystem
import net.kyori.adventure.sound.Sound
import java.util.concurrent.TimeUnit
import kotlin.math.PI
import kotlin.reflect.KClass

class AntiAirProjectileBalancing : StarshipParticleProjectileBalancing {
	override val particleThickness: Double = TODO("Not yet implemented")
	override val clazz: KClass<out Projectile> = TODO("Not yet implemented")
	override val range: Double = TODO("Not yet implemented")
	override val speed: Double = TODO("Not yet implemented")
	override val explosionPower: Float = TODO("Not yet implemented")
	override val starshipShieldDamageMultiplier: Double = TODO("Not yet implemented")
	override val areaShieldDamageMultiplier: Double = TODO("Not yet implemented")
	override val entityDamage: EntityDamage = TODO("Not yet implemented")
	override val fireSoundNear: SoundInfo = TODO("Not yet implemented")
	override val fireSoundFar: SoundInfo = TODO("Not yet implemented")
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
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = TorpedoWeaponSubsystem::class

	@Serializable
	data class TorpedoProjectileBalancing(
        override val range: Double = 300.0,
        override val speed: Double = 70.0,
        override val explosionPower: Float = 7.0f,
        override val starshipShieldDamageMultiplier: Double = 2.0,
        override val areaShieldDamageMultiplier: Double = 2.0,
        override val entityDamage: EntityDamage = RegularDamage(15.0),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.torpedo.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.torpedo.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
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
	StarshipTrackingWeaponBalancing<HeavyLaserProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = HeavyLaserWeaponSubsystem::class

	@Serializable
	data class HeavyLaserProjectileBalancing(
		override val range: Double = 200.0,
		override val speed: Double = 80.0,
		override val explosionPower: Float = 12f,
		override val starshipShieldDamageMultiplier: Double = 2.0,
		override val areaShieldDamageMultiplier: Double = 2.0,
		override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val particleThickness: Double = 1.0,
		override val maxDegrees: Double = 25.0,
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.heavy_laser.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.heavy_laser.shoot.far", volume = 1f, source = Sound.Source.PLAYER)
	) : StarshipProjectileBalancing, StarshipTrackingProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = HeavyLaserProjectile::class
	}
}

@Serializable
data class PhaserBalancing(
	override val fireRestrictions: FireRestrictions = FireRestrictions(maxBlockCount = 12000),
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
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = PhaserWeaponSubsystem::class

	@Serializable
	data class PhaserProjectileBalancing(
		override val range: Double = 140.0,
		override val speed: Double = 1000.0,
		override val explosionPower: Float = 2f,
		override val starshipShieldDamageMultiplier: Double = 55.0,
		override val areaShieldDamageMultiplier: Double = 5.0,
		override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.phaser.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.phaser.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
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
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = ArsenalRocketStarshipWeaponSubsystem::class

	@Serializable
	data class ArsenalRocketProjectileBalancing(
		override val range: Double = 700.0,
		override val speed: Double = 50.0,
		override val explosionPower: Float = 3f,
		override val starshipShieldDamageMultiplier: Double = 1.0,
		override val areaShieldDamageMultiplier: Double = 5.0,
		override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.arsenal_missile.shoot", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.arsenal_missile.shoot", volume = 1f, source = Sound.Source.PLAYER),
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
	override val inaccuracyDegrees: Double = 3.0,
	override val range: Double= 500.0,
	override val boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(3),
	override val maxPerShot: Int? = null,
	override val applyCooldownToAll: Boolean = false,

	override val projectile: TriTurretProjectileBalancing = TriTurretProjectileBalancing()
) : StarshipTurretWeaponBalancing<TriTurretProjectileBalancing>, StarshipAutoWeaponBalancing<TriTurretProjectileBalancing>, StarshipHeavyWeaponBalancing<TriTurretProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = TriTurretWeaponSubsystem::class

	@Serializable
	data class TriTurretProjectileBalancing(
        override val range: Double = 500.0,
        override val speed: Double = 125.0,
        override val explosionPower: Float = 6f,
        override val starshipShieldDamageMultiplier: Double = 3.0,
        override val areaShieldDamageMultiplier: Double = 3.0,
        override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.tri_turret.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.tri_turret.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
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
	override val inaccuracyDegrees: Double = 2.0,
	override val range: Double = 200.0,
	override val maxPerShot: Int? = null,
	override val applyCooldownToAll: Boolean = true,
	override val projectile: LightTurretProjectileBalancing = LightTurretProjectileBalancing()
) : StarshipTurretWeaponBalancing<LightTurretProjectileBalancing>, StarshipAutoWeaponBalancing<LightTurretProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = LightTurretWeaponSubsystem::class

	@Serializable
	data class LightTurretProjectileBalancing(
        override val range: Double = 200.0,
        override val speed: Double = 250.0,
        override val explosionPower: Float = 4f,
        override val starshipShieldDamageMultiplier: Double = 2.0,
        override val areaShieldDamageMultiplier: Double = 2.0,
        override val entityDamage: EntityDamage = RegularDamage(7.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.light_turret.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.light_turret.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
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
	override val firePowerConsumption: Int = 2667,
	override val isForwardOnly: Boolean = false,
	override val inaccuracyDegrees: Double = 2.0,
	override val maxPerShot: Int? = null,
	override val applyCooldownToAll: Boolean = true,

	override val projectile: HeavyTurretProjectileBalancing = HeavyTurretProjectileBalancing()
) : StarshipTurretWeaponBalancing<HeavyTurretProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = HeavyTurretWeaponSubsystem::class

	@Serializable
	data class HeavyTurretProjectileBalancing(
        override val range: Double = 500.0,
        override val speed: Double = 200.0,
        override val explosionPower: Float = 3f,
        override val starshipShieldDamageMultiplier: Double = 1.0,
        override val areaShieldDamageMultiplier: Double = 1.0,
        override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.heavy_turret.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.heavy_turret.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
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
	override val inaccuracyDegrees: Double = 2.0,
	override val maxPerShot: Int = 3,
	override val applyCooldownToAll: Boolean = true,

	override val projectile: QuadTurretProjectileBalancing = QuadTurretProjectileBalancing()
) : StarshipTurretWeaponBalancing<QuadTurretProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = QuadTurretWeaponSubsystem::class

	@Serializable
	data class QuadTurretProjectileBalancing(
        override val range: Double = 500.0,
        override val speed: Double = 55.0,
        override val explosionPower: Float = 5f,
        override val starshipShieldDamageMultiplier: Double = 6.3,
        override val areaShieldDamageMultiplier: Double = 6.0,
        override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.quad_turret.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.quad_turret.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
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
	override val inaccuracyDegrees: Double = 1.0,
	override val maxPerShot: Int = 4,
	override val applyCooldownToAll: Boolean = true,

	override val projectile: IonTurretProjectileBalancing = IonTurretProjectileBalancing()
) : StarshipTurretWeaponBalancing<IonTurretProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = IonTurretWeaponSubsystem::class

	@Serializable
	data class IonTurretProjectileBalancing(
        override val range: Double = 500.0,
        override val speed: Double = 105.0,
        override val explosionPower: Float = 3f,
        override val starshipShieldDamageMultiplier: Double = 3.7,
        override val areaShieldDamageMultiplier: Double = 60.0,
        override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.ion_turret.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.ion_turret.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
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
) : StarshipWeaponBalancing<PointDefenseProjectileBalancing>, StarshipAutoWeaponBalancing<PointDefenseProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = PointDefenseSubsystem::class

	@Serializable
	data class PointDefenseProjectileBalancing(
        override val range: Double = 120.0,
        override val speed: Double = 150.0,
        override val explosionPower: Float = 0.0f,
        override val starshipShieldDamageMultiplier: Double = 0.0,
        override val areaShieldDamageMultiplier: Double = 2.0,
        override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.point_defense.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.point_defense.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
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
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = PulseCannonWeaponSubsystem::class

	@Serializable
	data class PulseCannonProjectileBalancing(
        override val range: Double = 180.0,
        override val speed: Double = 400.0,
        override val explosionPower: Float = 1.875f,
        override val starshipShieldDamageMultiplier: Double = 2.0,
        override val areaShieldDamageMultiplier: Double = 2.0,
        override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.pulse_cannon.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.pulse_cannon.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
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
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = PlasmaCannonWeaponSubsystem::class

	@Serializable
	data class PlasmaCannonProjectileBalancing(
        override val range: Double = 160.0,
        override val speed: Double = 400.0,
        override val explosionPower: Float = 4f,
        override val starshipShieldDamageMultiplier: Double = 3.0,
        override val areaShieldDamageMultiplier: Double = 3.0,
        override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.plasma_cannon.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.plasma_cannon.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
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
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = LaserCannonWeaponSubsystem::class

	@Serializable
	data class LaserCannonProjectileBalancing(
        override val range: Double = 200.0,
        override val speed: Double = 250.0,
        override val explosionPower: Float = 2f,
        override val starshipShieldDamageMultiplier: Double = 0.2,
        override val areaShieldDamageMultiplier: Double = 1.0,
        override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.laser_cannon.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.laser_cannon.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
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
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = InterceptorCannonWeaponSubsystem::class

	@Serializable
	data class IncterceptorCannonProjectileBalancing(
        override val range: Double = 200.0,
        override val speed: Double = 250.0,
        override val explosionPower: Float = 0.1f,
        override val starshipShieldDamageMultiplier: Double = 1.0,
        override val areaShieldDamageMultiplier: Double = 1.0,
        override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.laser_cannon.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.laser_cannon.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
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
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = DoomsdayDeviceWeaponSubsystem::class

	@Serializable
	data class DoomsdayDeviceProjectileBalancing(
        override val range: Double = 500.0,
        override val speed: Double = 400.0,
        override val explosionPower: Float = 10f,
        override val starshipShieldDamageMultiplier: Double = 100.0,
        override val areaShieldDamageMultiplier: Double = 100.0,
        override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.light_turret.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.light_turret.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
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
	override val maxPerShot: Int? = null,
	override val applyCooldownToAll: Boolean = false,

	override val convergeDistance: Double = 0.0,
	override val projectileSpawnDistance: Int = 0,
	override val angleRadiansHorizontal: Double = 0.0,
	override val angleRadiansVertical: Double = 0.0,
	override val boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(7),

	override val projectile: RocketProjectileBalancing = RocketProjectileBalancing(),
) : StarshipCannonWeaponBalancing<RocketProjectileBalancing>, StarshipHeavyWeaponBalancing<RocketProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = RocketWeaponSubsystem::class

	@Serializable
	data class RocketProjectileBalancing(
        override val range: Double = 300.0,
        override val speed: Double = 5.0,
        override val explosionPower: Float = 10f,
        override val starshipShieldDamageMultiplier: Double = 5.0,
        override val areaShieldDamageMultiplier: Double = 5.0,
        override val entityDamage: EntityDamage = RegularDamage(10.0),
        override val fireSoundNear: SoundInfo = SoundInfo(key = "horizonsend:starship.weapon.rocket.shoot", volume = 10f),
        override val fireSoundFar: SoundInfo = SoundInfo(key = "horizonsend:starship.weapon.rocket.shoot", volume = 10f),
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

	override val inaccuracyDegrees: Double = 0.5,

	override val projectile: LogisticsTurretProjectileBalancing = LogisticsTurretProjectileBalancing(),
) : StarshipTurretWeaponBalancing<LogisticsTurretProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = LogisticTurretWeaponSubsystem::class

	@Serializable
	data class LogisticsTurretProjectileBalancing(
        override val range: Double = 200.0,
        override val speed: Double = 2000.0,
        override val explosionPower: Float = 0f,
        override val starshipShieldDamageMultiplier: Double = 0.0,
        override val areaShieldDamageMultiplier: Double = 0.0,
        override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.light_turret.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.light_turret.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
        override val particleThickness: Double = 1.0,
        val shieldBoostFactor: Int = 50000
	) : StarshipParticleProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = LogisticTurretProjectile::class
	}
}

@Serializable
data class DisintegratorBeamBalancing(
	override val fireRestrictions: FireRestrictions = FireRestrictions(),
	override val fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(100),
	override val firePowerConsumption: Int = 100,
	override val isForwardOnly: Boolean = false,
	override val maxPerShot: Int = 6,
	override val applyCooldownToAll: Boolean = true,

	val inaccuracyRadians: Double = 0.01,

	override val projectile: DisintegratorBeamProjectileBalancing = DisintegratorBeamProjectileBalancing(),
) : StarshipWeaponBalancing<DisintegratorBeamProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = DisintegratorBeamWeaponSubsystem::class

	@Serializable
	data class DisintegratorBeamProjectileBalancing(
        override val range: Double = 2000.0,
        override val speed: Double = 100.0,
        override val explosionPower: Float = 1f,
        override val starshipShieldDamageMultiplier: Double = 1.0,
        override val areaShieldDamageMultiplier: Double = 1.0,
        override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.light_turret.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.light_turret.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
        override val particleThickness: Double = 0.5,
	) : StarshipParticleProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = DisintegratorBeamProjectile::class
	}
}

@Serializable
data class CycleTurretBalancing(
	override val fireRestrictions: FireRestrictions = FireRestrictions(),
	override val fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(500),
	override val firePowerConsumption: Int = 100,
	override val isForwardOnly: Boolean = false,
	override val maxPerShot: Int = 3,
	override val inaccuracyDegrees: Double = 0.5,
	override val applyCooldownToAll: Boolean = true,

	override val projectile: CycleTurretProjectileBalancing = CycleTurretProjectileBalancing()
) : StarshipTurretWeaponBalancing<CycleTurretProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = CycleTurretWeaponSubsystem::class

	@Serializable
	data class CycleTurretProjectileBalancing(
        override val range: Double = 275.0,
        override val speed: Double = 1800.0,
        override val explosionPower: Float = 2f,
        override val starshipShieldDamageMultiplier: Double = 0.75,
        override val areaShieldDamageMultiplier: Double = 1.0,
        override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.light_turret.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.light_turret.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
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
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = AbyssalGazeSubsystem::class

	@Serializable
	data class AbyssalGazeProjectileBalancing(
        override val range: Double = 500.0,
        override val speed: Double = 50.0,
        override val explosionPower: Float = 2.5f,
        override val starshipShieldDamageMultiplier: Double =  1.25,
        override val areaShieldDamageMultiplier: Double = 1.0,
        override val entityDamage: EntityDamage = RegularDamage(10.0),
        override val fireSoundNear: SoundInfo = SoundInfo(key = "item.trident.riptide_1", volume = 10f, pitch = 2f),
        override val fireSoundFar: SoundInfo = SoundInfo(key = "item.trident.riptide_1", volume = 10f, pitch = 2f),
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
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = SonicMissileWeaponSubsystem::class

	@Serializable
	data class SonicMissileProjectileBalancing(
        override val range: Double= 300.0,
        override val speed: Double= 200.0,
        override val explosionPower: Float = 15.0f,
        override val starshipShieldDamageMultiplier: Double = 10.0,
        override val areaShieldDamageMultiplier: Double = 10.0,
        override val entityDamage: EntityDamage = RegularDamage(10.0),
        override val fireSoundNear: SoundInfo = SoundInfo(key = "entity.warden.sonic_boom"),
        override val fireSoundFar: SoundInfo = SoundInfo(key = "entity.warden.sonic_boom"),
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
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = PumpkinCannonWeaponSubsystem::class

	@Serializable
	data class PumpkinCannonProjectileBalancing(
		override val range: Double = 500.0,
		override val speed: Double = 125.0,
		override val explosionPower: Float = 1.0f,
		override val starshipShieldDamageMultiplier: Double = 1.0,
		override val areaShieldDamageMultiplier: Double = 1.0,
		override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo(key = "entity.firework_rocket.blast_far", volume = 0f, pitch = 2.0f),
		override val fireSoundFar: SoundInfo = SoundInfo(key = "entity.firework_rocket.blast_far", volume = 0f, pitch = 2.0f)
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
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = FlamingSkullCannonWeaponSubsystem::class

	@Serializable
	data class FlamingSkullCannonProjectileBalancing(
        override val range: Double = 500.0,
        override val speed: Double = 200.0,
        override val explosionPower: Float = 15f,
        override val starshipShieldDamageMultiplier: Double = 10.0,
        override val areaShieldDamageMultiplier: Double = 10.0,
        override val entityDamage: EntityDamage = RegularDamage(10.0),
        override val fireSoundNear: SoundInfo = SoundInfo(key = "entity.warden.sonic_boom", volume = 10f, pitch = 2f),
        override val fireSoundFar: SoundInfo = SoundInfo(key = "entity.warden.sonic_boom", volume = 10f, pitch = 2f),
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
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = FlamethrowerWeaponSubsystem::class

	@Serializable
	data class FlamethrowerCannonProjectileBalancing(
        override val range: Double = 340.0,
        override val speed: Double = 350.0,
        override val explosionPower: Float = 2.0f,
        override val starshipShieldDamageMultiplier: Double = 5.0,
        override val areaShieldDamageMultiplier: Double = 5.0,
        override val entityDamage: EntityDamage = RegularDamage(10.0),
        override val fireSoundNear: SoundInfo = SoundInfo(key = "block.fire.ambient", volume = 10f, pitch = 0.5f),
        override val fireSoundFar: SoundInfo = SoundInfo(key = "block.fire.ambient", volume = 10f, pitch = 0.5f),
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
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = MiniPhaserWeaponSubsystem::class

	@Serializable
	data class MiniPhaserProjectileBalancing(
		override val range: Double = 200.0,
		override val speed: Double = 600.0,
		override val explosionPower: Float = 2f,
		override val starshipShieldDamageMultiplier: Double = 1.0,
		override val areaShieldDamageMultiplier: Double = 1.0,
		override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo(key = "block.conduit.deactivate", volume = 10f),
		override val fireSoundFar: SoundInfo = SoundInfo(key = "block.conduit.deactivate", volume = 10f)
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
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = FlamingSkullCannonWeaponSubsystem::class

	@Serializable
	data class CthulhuBeamProjectileBalancing(
        override val range: Double = 128.0,
        override val speed: Double = 1.0,
        override val explosionPower: Float = 2f,
        override val starshipShieldDamageMultiplier: Double = 1.0,
        override val areaShieldDamageMultiplier: Double = 1.0,
        override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.light_turret.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.light_turret.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
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
) : StarshipHeavyWeaponBalancing<CapitalCannonBalancing.CapitalCannonProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = CapitalBeamWeaponSubsystem::class

	@Serializable
	data class CapitalCannonProjectileBalancing(
        override val range: Double = 500.0,
        override val speed: Double = PI * 50.0,
        override val explosionPower: Float = 20f,
        override val starshipShieldDamageMultiplier: Double = 2.0,
        override val areaShieldDamageMultiplier: Double = 2.0,
        override val entityDamage: EntityDamage = RegularDamage(20.0),
        override val fireSoundNear: SoundInfo = SoundInfo(key = "entity.zombie_villager.converted"),
        override val fireSoundFar: SoundInfo = SoundInfo(key = "entity.zombie_villager.converted"),
        override val particleThickness: Double = 0.44,
	) : StarshipParticleProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = CapitalBeamCannonProjectile::class
	}
}

@Serializable
data class TestBoidCannonBalancing(
	override val projectile: TestBoidCannonProjectileBalancing = TestBoidCannonProjectileBalancing(),
	override val fireRestrictions: FireRestrictions = FireRestrictions(),
	override val fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(3000),
	override val firePowerConsumption: Int = 10000,
	override val isForwardOnly: Boolean = false,
	override val maxPerShot: Int? = 2,
	override val applyCooldownToAll: Boolean = true,
	override val convergeDistance: Double = 10.0,
	override val projectileSpawnDistance: Int = 1,
	override val angleRadiansHorizontal: Double = 30.0,
	override val angleRadiansVertical: Double = 30.0
) : StarshipCannonWeaponBalancing<TestBoidCannonBalancing.TestBoidCannonProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = TestBoidWeaponSubsystem::class

	@Serializable
	data class TestBoidCannonProjectileBalancing(
		override val range: Double = 200.0,
		override val speed: Double = 10.0,
		override val explosionPower: Float = 1.0f,
		override val starshipShieldDamageMultiplier: Double = 1.0,
		override val areaShieldDamageMultiplier: Double = 1.0,
		override val fireSoundNear: SoundInfo = SoundInfo(key = "entity.zombie_villager.converted"),
		override val fireSoundFar: SoundInfo = SoundInfo(key = "entity.zombie_villager.converted"),
		override val entityDamage: EntityDamage = RegularDamage(20.0),
		override val separationDistance: Double = 8.0,
		override val separationFactor: Double = 0.05,
		override val visibleDistance: Double = 40.0,
		override val alignFactor: Double = 0.05,
		override val centerFactor: Double = 0.0005
	) : StarshipBoidProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = TestBoidProjectile::class
	}
}

// End Event Weapons
