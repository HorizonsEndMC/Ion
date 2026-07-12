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
import net.horizonsend.ion.server.features.multiblock.type.defense.active.projectile.AntiAirCannonProjectile
import net.horizonsend.ion.server.features.starship.subsystem.command_burst.AbstractCommandBurstSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.command_burst.CapitalShieldCommandBurstSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.command_burst.CapitalSkirmishCommandBurstSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.command_burst.ShieldCommandBurstSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.command_burst.SkirmishCommandBurstSubsystem
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
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.ACAPTurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.ArtilleryWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.AssaultTurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.AutocannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.CycleTurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.DisintegratorBeamWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.DoomsdayDeviceWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.GaussCannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.HeavyLogisticsCannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.HeavyTurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.InterceptorCannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.IonTurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.LaserCannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.LightLogisticsCannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.LightTurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.LogisticTurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.PlasmaCannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.PointDefenseSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.PulseCannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.QuadTurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.LightMissileLauncherStarshipWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.ScramblerWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.TestBoidWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.ArtilleryProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.AutocannonProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.CycleTurretProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.DisintegratorBeamProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.DoomsdayDeviceProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.EMPMissileProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.HeavyLaserProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.HeavyLogisticsProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.HeavyNeutralizerProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.InterceptorCannonProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.IonTurretProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.LaserCannonLaserProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.LightLogisticsProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.LogisticTurretProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.NeutralizerProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.PhaserProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.PlasmaLaserProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.PointDefenseLaserProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.ProbeProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.Projectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.PulseLaserProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.RocketProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.ScramblerProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.SwarmMissileProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.TestBoidProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.TorpedoProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.TrackingMissileProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.TurretLaserProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.WebifierProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.ArsenalRocketStarshipWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.EMPMissileStarshipWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.HeavyLaserWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.HeavyNeutralizerWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.NeutralizerWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.PhaserWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.AdvancedProbeWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.ProbeWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.RapidHeavyMissileLauncherWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.RocketWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.SwarmMissileStarshipWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.ThermonuclearMissileStarshipWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.TorpedoWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.TriTurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.WebifierWeaponSubsystem
import net.kyori.adventure.sound.Sound
import java.util.concurrent.TimeUnit
import kotlin.math.PI
import kotlin.reflect.KClass

@Serializable
data class AntiAirProjectileBalancing (
	override var particleThickness: Double = 2.0,
	override var range: Double = 500.0,
	override var speed: Double = 200.0,
	override var explosionPower: Float = 10.0f,
	override var starshipShieldDamageMultiplier: Double = 4.0,
	override var areaShieldDamageMultiplier: Double = 0.1,
	override val entityDamage: EntityDamage = RegularDamage(15.0),
	override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.torpedo.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
	override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.torpedo.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
	) : StarshipProjectileBalancing, StarshipParticleProjectileBalancing {
	@Transient
	override val clazz: KClass<out Projectile> = AntiAirCannonProjectile::class
}

@Serializable
data class TorpedoBalancing(
	override val fireRestrictions: FireRestrictions = FireRestrictions(),
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(10),
	override var firePowerConsumption: Int = 10000,
	override var isForwardOnly: Boolean = true,
	override var maxPerShot: Int? = null,

	override var convergeDistance: Double = 10.0,
	override var projectileSpawnDistance: Int = 10,
	override var angleRadiansHorizontal: Double = 10.0,
	override var angleRadiansVertical: Double = 10.0,
	override var aimDistance: Int = 3,
	override var applyCooldownToAll: Boolean = true,

	override var boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(10),

	override val projectile: TorpedoProjectileBalancing = TorpedoProjectileBalancing()
): StarshipCannonWeaponBalancing<TorpedoBalancing.TorpedoProjectileBalancing>,
	StarshipHeavyWeaponBalancing<TorpedoBalancing.TorpedoProjectileBalancing>,
	StarshipTrackingWeaponBalancing<TorpedoBalancing.TorpedoProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = TorpedoWeaponSubsystem::class

	@Serializable
	data class TorpedoProjectileBalancing(
        override var range: Double = 240.0,
        override var speed: Double = 70.0,
        override var explosionPower: Float = 7.0f,
        override var starshipShieldDamageMultiplier: Double = 2.0,
        override var areaShieldDamageMultiplier: Double = 2.0,
        override val entityDamage: EntityDamage = RegularDamage(15.0),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.torpedo.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.torpedo.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
        override var particleThickness: Double = 1.0,
        override var maxDegrees: Double = 45.0
	) : StarshipProjectileBalancing, StarshipTrackingProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = TorpedoProjectile::class
	}
}

@Serializable
data class HeavyLaserBalancing(
	override val fireRestrictions: FireRestrictions = FireRestrictions(),
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(250),
	override var firePowerConsumption: Int = 30_000,
	override var isForwardOnly: Boolean = false,
	override var maxPerShot: Int? = null,
	override var applyCooldownToAll: Boolean = false,

	override var convergeDistance: Double = 0.0,
	override var projectileSpawnDistance: Int = 1,
	override var angleRadiansHorizontal: Double = 0.0,
	override var angleRadiansVertical: Double = 0.0,

	override var boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(5),
	override var aimDistance: Int = 10,

	override val projectile: HeavyLaserProjectileBalancing = HeavyLaserProjectileBalancing(),
) : StarshipCannonWeaponBalancing<HeavyLaserProjectileBalancing>,
	StarshipHeavyWeaponBalancing<HeavyLaserProjectileBalancing>,
	StarshipTrackingWeaponBalancing<HeavyLaserProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = HeavyLaserWeaponSubsystem::class

	@Serializable
	data class HeavyLaserProjectileBalancing(
		override var range: Double = 220.0,
		override var speed: Double = 80.0,
		override var explosionPower: Float = 12f,
		override var starshipShieldDamageMultiplier: Double = 2.0,
		override var areaShieldDamageMultiplier: Double = 2.0,
		override val entityDamage: EntityDamage = RegularDamage(10.0),
		override var particleThickness: Double = 1.0,
		override var maxDegrees: Double = 25.0,
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.heavy_laser.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.heavy_laser.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
		override var effectStrength: Double = 0.15,
		override var effectDurationMillis: Long = TimeUnit.SECONDS.toMillis(20L)
	) : StarshipProjectileBalancing, StarshipTrackingProjectileBalancing, StarshipStatusEffectProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = HeavyLaserProjectile::class
	}
}


@Serializable
data class HeavyNeutralizerBalancing(
	override val fireRestrictions: FireRestrictions = FireRestrictions(canFire = false),
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(250),
	override var firePowerConsumption: Int = 30000,
	override var isForwardOnly: Boolean = false,
	override var maxPerShot: Int? = null,
	override var applyCooldownToAll: Boolean = false,

	override var convergeDistance: Double = 0.0,
	override var projectileSpawnDistance: Int = 1,
	override var angleRadiansHorizontal: Double = 45.0,
	override var angleRadiansVertical: Double = 45.0,

	override var boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(7),
	override var aimDistance: Int = 10,

	override val projectile: HeavyNeutralizerProjectileBalancing = HeavyNeutralizerProjectileBalancing(),
) : StarshipCannonWeaponBalancing<HeavyNeutralizerBalancing.HeavyNeutralizerProjectileBalancing>,
	StarshipHeavyWeaponBalancing<HeavyNeutralizerBalancing.HeavyNeutralizerProjectileBalancing>,
	StarshipTrackingWeaponBalancing<HeavyNeutralizerBalancing.HeavyNeutralizerProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = HeavyNeutralizerWeaponSubsystem::class

	@Serializable
	data class HeavyNeutralizerProjectileBalancing(
		override var range: Double = 180.0,
		override var speed: Double = 40.0,
		override var explosionPower: Float = 10f,
		override var starshipShieldDamageMultiplier: Double = 6.0,
		override var areaShieldDamageMultiplier: Double = 2.0,
		override val entityDamage: EntityDamage = RegularDamage(10.0),
		override var particleThickness: Double = 1.0,
		override var maxDegrees: Double = 25.0,
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.neutralizer.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.neutralizer.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
		override var effectStrength: Double = 0.85,
		override var effectDurationMillis: Long = TimeUnit.SECONDS.toMillis(30L)

	) : StarshipProjectileBalancing, StarshipTrackingProjectileBalancing, StarshipStatusEffectProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = HeavyNeutralizerProjectile::class
	}
}

@Serializable
data class NeutralizerBalancing(
	override val fireRestrictions: FireRestrictions = FireRestrictions(canFire = false),
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(250),
	override var firePowerConsumption: Int = 30000,
	override var isForwardOnly: Boolean = false,
	override var maxPerShot: Int? = null,
	override var applyCooldownToAll: Boolean = false,

	override var convergeDistance: Double = 0.0,
	override var projectileSpawnDistance: Int = 1,
	override var angleRadiansHorizontal: Double = 15.0,
	override var angleRadiansVertical: Double = 15.0,
	override val aimDistance: Int = 10,

	override var boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(5),

	override val projectile: NeutralizerProjectileBalancing = NeutralizerProjectileBalancing(),
) : StarshipCannonWeaponBalancing<NeutralizerBalancing.NeutralizerProjectileBalancing>,
	StarshipHeavyWeaponBalancing<NeutralizerBalancing.NeutralizerProjectileBalancing>,
	StarshipTrackingWeaponBalancing<NeutralizerBalancing.NeutralizerProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = NeutralizerWeaponSubsystem::class

	@Serializable
	data class NeutralizerProjectileBalancing(
		override var range: Double = 220.0,
		override var speed: Double = 80.0,
		override var explosionPower: Float = 6f,
		override var starshipShieldDamageMultiplier: Double = 2.0,
		override var areaShieldDamageMultiplier: Double = 2.0,
		override val entityDamage: EntityDamage = RegularDamage(10.0),
		override var particleThickness: Double = 4.0,
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.neutralizer.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.neutralizer.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
		override var effectStrength: Double = 0.70,
		override var effectDurationMillis: Long = TimeUnit.SECONDS.toMillis(20L),
		override val maxDegrees: Double = 45.0
	) : StarshipParticleProjectileBalancing, StarshipStatusEffectProjectileBalancing, StarshipTrackingProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = NeutralizerProjectile::class
	}
}

@Serializable
data class AdvancedProbeBalancing(
	override val fireRestrictions: FireRestrictions = FireRestrictions(canFire = false),
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(250),
	override var firePowerConsumption: Int = 1_000,
	override var isForwardOnly: Boolean = false,
	override var maxPerShot: Int? = 1,
	override var applyCooldownToAll: Boolean = false,

	override var convergeDistance: Double = 0.0,
	override var projectileSpawnDistance: Int = 1,
	override var angleRadiansHorizontal: Double = 15.0,
	override var angleRadiansVertical: Double = 15.0,

	override var boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(15),

	override val projectile: ProbeProjectileBalancing = ProbeProjectileBalancing(),
) : StarshipCannonWeaponBalancing<AdvancedProbeBalancing.ProbeProjectileBalancing>,
	StarshipHeavyWeaponBalancing<AdvancedProbeBalancing.ProbeProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = AdvancedProbeWeaponSubsystem::class

	@Serializable
	data class ProbeProjectileBalancing(
		override var range: Double = 30.0,
		override var speed: Double = 40.0,
		override var explosionPower: Float = 0.0f,
		override var starshipShieldDamageMultiplier: Double = 2.0,
		override var areaShieldDamageMultiplier: Double = 2.0,
		override val entityDamage: EntityDamage = RegularDamage(10.0),
		override var particleThickness: Double = 4.0,
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.probe_scan.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.probe_scan.shoot.far", volume = 1f, source = Sound.Source.PLAYER)
	) : StarshipParticleProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = ProbeProjectile::class
	}
}

@Serializable
data class ProbeBalancing(
	override val fireRestrictions: FireRestrictions = FireRestrictions(canFire = false),
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(250),
	override var firePowerConsumption: Int = 1_000,
	override var isForwardOnly: Boolean = false,
	override var maxPerShot: Int? = 1,
	override var applyCooldownToAll: Boolean = false,

	override var convergeDistance: Double = 0.0,
	override var projectileSpawnDistance: Int = 1,
	override var angleRadiansHorizontal: Double = 15.0,
	override var angleRadiansVertical: Double = 15.0,

	override var boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(15),

	override val projectile: ProbeProjectileBalancing = ProbeProjectileBalancing(),
) : StarshipCannonWeaponBalancing<ProbeBalancing.ProbeProjectileBalancing>,
	StarshipHeavyWeaponBalancing<ProbeBalancing.ProbeProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = ProbeWeaponSubsystem::class

	@Serializable
	data class ProbeProjectileBalancing(
		override var range: Double = 30.0,
		override var speed: Double = 40.0,
		override var explosionPower: Float = 0.0f,
		override var starshipShieldDamageMultiplier: Double = 2.0,
		override var areaShieldDamageMultiplier: Double = 2.0,
		override val entityDamage: EntityDamage = RegularDamage(10.0),
		override var particleThickness: Double = 4.0,
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.probe_scan.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.probe_scan.shoot.far", volume = 1f, source = Sound.Source.PLAYER)
	) : StarshipParticleProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = ProbeProjectile::class
	}
}

@Serializable
data class PhaserBalancing(
	override val fireRestrictions: FireRestrictions = FireRestrictions(maxBlockCount = 12000, incompatibleMultiblocks = listOf(
		/*
		IncompatibleSubsystemInfo(
			SwarmMissileStarshipWeaponSubsystem::class.java,
			"Phasers are incompatible with swarm missiles!"
		)
		 */
	)),
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(10),
	override var firePowerConsumption: Int = 50000,
	override var isForwardOnly: Boolean = false,
	override var maxPerShot: Int? = null,
	override var applyCooldownToAll: Boolean = false,

	override var boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(5),

	override var convergeDistance: Double = 0.0,
	override var projectileSpawnDistance: Int = 0,
	override var angleRadiansHorizontal: Double = 180.0,
	override var angleRadiansVertical: Double = 180.0,

	override val projectile: PhaserProjectileBalancing = PhaserProjectileBalancing()
) : StarshipCannonWeaponBalancing<PhaserProjectileBalancing>, StarshipHeavyWeaponBalancing<PhaserProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = PhaserWeaponSubsystem::class

	@Serializable
	data class PhaserProjectileBalancing(
		override var range: Double = 140.0,
		override var speed: Double = 1000.0,
		override var explosionPower: Float = 2f,
		override var starshipShieldDamageMultiplier: Double = 55.0,
		override var areaShieldDamageMultiplier: Double = 5.0,
		override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.phaser.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.phaser.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
	) : StarshipProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = PhaserProjectile::class
	}
}

@Serializable
data class WebifierBalancing(
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(250),
	override val fireRestrictions: FireRestrictions = FireRestrictions(canFire = false, maxBlockCount = 12000, incompatibleMultiblocks = listOf()),
	override var firePowerConsumption: Int = 18000,
	override var isForwardOnly: Boolean = false,
	override var maxPerShot: Int? = null,
	override var applyCooldownToAll: Boolean = false,

	override var boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(9),

	override var convergeDistance: Double = 0.0,
	override var projectileSpawnDistance: Int = 0,
	override var angleRadiansHorizontal: Double = 180.0,
	override var angleRadiansVertical: Double = 180.0,
	override var aimDistance: Int = 10,

	override val projectile: WebifierProjectileBalancing = WebifierProjectileBalancing()
) : StarshipCannonWeaponBalancing<WebifierBalancing.WebifierProjectileBalancing>,
	StarshipHeavyWeaponBalancing<WebifierBalancing.WebifierProjectileBalancing>,
	StarshipTrackingWeaponBalancing<WebifierBalancing.WebifierProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = WebifierWeaponSubsystem::class

	@Serializable
	data class WebifierProjectileBalancing(
		override var range: Double = 230.0,
		override var speed: Double = 135.0,
		override var explosionPower: Float = 2.5f,
		override var starshipShieldDamageMultiplier: Double = 7.0,
		override var areaShieldDamageMultiplier: Double = 5.0,
		override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.webifier.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.webifier.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
		override var particleThickness: Double = 2.0,
		override var effectStrength: Double = 0.45,
		override var effectDurationMillis: Long = TimeUnit.SECONDS.toMillis(5L),
		override val maxDegrees: Double = 45.0,
	) : StarshipParticleProjectileBalancing, StarshipStatusEffectProjectileBalancing, StarshipTrackingProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = WebifierProjectile::class
	}
}

@Serializable
data class ArsenalRocketBalancing(
	override val fireRestrictions: FireRestrictions = FireRestrictions(canFire = false),
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(250),
	override var firePowerConsumption: Int = 8000,
	override var isForwardOnly: Boolean = false,
	override var maxPerShot: Int? = null,
	override var applyCooldownToAll: Boolean = false,

	override var boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(9),

	override val projectile: ArsenalRocketProjectileBalancing = ArsenalRocketProjectileBalancing()
) : StarshipHeavyWeaponBalancing<ArsenalRocketProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = ArsenalRocketStarshipWeaponSubsystem::class

	@Serializable
	data class ArsenalRocketProjectileBalancing(
		override var range: Double = 300.0,
		override var speed: Double = 42.0,
		override var explosionPower: Float = 7.5f,
		override var starshipShieldDamageMultiplier: Double = 7.5,
		override var areaShieldDamageMultiplier: Double = 100.0,
		override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.arsenal_missile.shoot", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.arsenal_missile.shoot", volume = 1f, source = Sound.Source.PLAYER),
		override var maxDegrees: Double = 180.0 ,
		override var particleThickness: Double = 0.1,
	) : StarshipProjectileBalancing, StarshipTrackingProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = TrackingMissileProjectile::class
	}
}

@Serializable
data class EMPMissileBalancing(
	override val fireRestrictions: FireRestrictions = FireRestrictions(canFire = false),
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(250),
	override var firePowerConsumption: Int = 23000,
	override var isForwardOnly: Boolean = false,
	override var maxPerShot: Int? = null,
	override var applyCooldownToAll: Boolean = false,

	override var boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(8),

	override val projectile: EMPMissileProjectileBalancing = EMPMissileProjectileBalancing()
) : StarshipHeavyWeaponBalancing<EMPMissileBalancing.EMPMissileProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = EMPMissileStarshipWeaponSubsystem::class

	@Serializable
	data class EMPMissileProjectileBalancing(
		override var range: Double = 200.0,
		override var speed: Double = 80.0,
		override var explosionPower: Float = 6.5f,
		override var starshipShieldDamageMultiplier: Double = 5.0,
		override var areaShieldDamageMultiplier: Double = 5.0,
		override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.arsenal_missile.shoot", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.arsenal_missile.shoot", volume = 1f, source = Sound.Source.PLAYER),
		override var maxDegrees: Double = 180.0,
		override var particleThickness: Double = 0.1,
		override var effectStrength: Double = 0.20,
		override var effectDurationMillis: Long = TimeUnit.SECONDS.toMillis(30L),
	) : StarshipProjectileBalancing, StarshipTrackingProjectileBalancing, StarshipStatusEffectProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = EMPMissileProjectile::class
	}
}


@Serializable
data class ThermonuclearMissileBalancing(
	override val fireRestrictions: FireRestrictions = FireRestrictions(canFire = false),
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(250),
	override var firePowerConsumption: Int = 37000,
	override var isForwardOnly: Boolean = false,
	override var maxPerShot: Int? = null,
	override var applyCooldownToAll: Boolean = false,

	override var boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(30),

	override val projectile: ThermonuclearMissileProjectileBalancing = ThermonuclearMissileProjectileBalancing()
) : StarshipHeavyWeaponBalancing<ThermonuclearMissileBalancing.ThermonuclearMissileProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = ThermonuclearMissileStarshipWeaponSubsystem::class

	@Serializable
	data class ThermonuclearMissileProjectileBalancing(
		override var range: Double = 245.0,
		override var speed: Double = 37.5,
		override var explosionPower: Float = 20f,
		override var starshipShieldDamageMultiplier: Double = 15.0,
		override var areaShieldDamageMultiplier: Double = 5.0,
		override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.thermonuclear_missile.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.thermonuclear_missile.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
		override var maxDegrees: Double = 180.0 ,
		override var particleThickness: Double = 0.1,
		//override var proximityRange: Double = 75.0,
	) : StarshipProjectileBalancing, StarshipTrackingProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = TrackingMissileProjectile::class
	}
}


@Serializable
data class SwarmMissileBalancing(
	override var boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(10000),
	override val fireRestrictions: FireRestrictions = FireRestrictions(canFire = false, incompatibleMultiblocks = listOf(
		/*
		IncompatibleSubsystemInfo(
			PhaserWeaponSubsystem::class.java,
			"Swarm missiles are incompatible with phasers!"
		)
		 */
	)),
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(250),
	override var firePowerConsumption: Int = 99999999,
	override var isForwardOnly: Boolean = false,
	override var maxPerShot: Int? = 1,
	override var applyCooldownToAll: Boolean = false,

	override val projectile: SwarmMissileProjectileBalancing = SwarmMissileProjectileBalancing(),
) : StarshipHeavyWeaponBalancing<SwarmMissileBalancing.SwarmMissileProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = SwarmMissileStarshipWeaponSubsystem::class

	@Serializable
	data class SwarmMissileProjectileBalancing(
		override var range: Double = 190.0,
		override var speed: Double = 100.0,
		override var explosionPower: Float = 2.0f,
		override var starshipShieldDamageMultiplier: Double = 5.8,
		override var areaShieldDamageMultiplier: Double = 4.0,
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.swarm_missile.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.swarm_missile.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
		override val entityDamage: EntityDamage = RegularDamage(10.0),
		override var separationDistance: Double = 8.0,
		override var separationFactor: Double = 0.20,
		override var visibleDistance: Double = 100.0,
		override var alignFactor: Double = 0.05,
		override var centerFactor: Double = 0.10,
		override var minSpeedFactor: Double = 0.2,
		override var maxSpeedFactor: Double = 1.0,
		override var originalDirectionFactor: Double = 2.0,
		override var proximityRange: Double = 50.0,
	) : StarshipBoidProjectileBalancing, StarshipProximityProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = SwarmMissileProjectile::class
	}
}

@Serializable
data class LightMissileLauncherBalancing(
	override val fireRestrictions: FireRestrictions = FireRestrictions(canFire = false, incompatibleMultiblocks = listOf(
		/*
		IncompatibleSubsystemInfo(
			PhaserWeaponSubsystem::class.java,
			"Swarm missiles are incompatible with phasers!"
		)
		 */
	)),
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(4000),
	override var firePowerConsumption: Int = 2500,
	override var isForwardOnly: Boolean = false,
	override var maxPerShot: Int? = 1,
	override var applyCooldownToAll: Boolean = true,
	override val projectile: LightMissileLauncherProjectileBalancing = LightMissileLauncherProjectileBalancing(),
	override var aimDistance: Int = 5,
	override var convergeDistance: Double = 10.0,
	override var projectileSpawnDistance: Int = 4,
	override var angleRadiansHorizontal: Double = 180.0,
	override var angleRadiansVertical: Double = 180.0,
) : StarshipTrackingWeaponBalancing<LightMissileLauncherBalancing.LightMissileLauncherProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = LightMissileLauncherStarshipWeaponSubsystem::class

	@Serializable
	data class LightMissileLauncherProjectileBalancing(
		override var range: Double = 300.0,
		override var speed: Double = 85.0,
		override var explosionPower: Float = 5.50f,
		override var starshipShieldDamageMultiplier: Double = 7.0,
		override var areaShieldDamageMultiplier: Double = 4.0,
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.light_missile.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.light_missile.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
		override val entityDamage: EntityDamage = RegularDamage(10.0),
		override var maxDegrees: Double = 90.0,
		override var particleThickness: Double = 2.0,
	) : StarshipProjectileBalancing, StarshipTrackingProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = TrackingMissileProjectile::class
	}
}

@Serializable
data class RapidHeavyMissileLauncherBalancing(
	override val fireRestrictions: FireRestrictions = FireRestrictions(canFire = false, incompatibleMultiblocks = listOf(
		/*
		IncompatibleSubsystemInfo(
			PhaserWeaponSubsystem::class.java,
			"Swarm missiles are incompatible with phasers!"
		)
		 */
	)),
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(5000),
	override var firePowerConsumption: Int = 2500,
	override var isForwardOnly: Boolean = false,
	override var maxPerShot: Int? = 2,
	override var applyCooldownToAll: Boolean = true,
	override var aimDistance: Int = 5,
	override var convergeDistance: Double = 10.0,
	override var projectileSpawnDistance: Int = 4,
	override var angleRadiansHorizontal: Double = 180.0,
	override var angleRadiansVertical: Double = 180.0,
	override val projectile: RapidHeavyMissileLauncherProjectileBalancing = RapidHeavyMissileLauncherProjectileBalancing(),
	override var inaccuracyDegrees: Double = 2.0,

	) : StarshipTrackingWeaponBalancing<RapidHeavyMissileLauncherBalancing.RapidHeavyMissileLauncherProjectileBalancing>,
	StarshipTurretWeaponBalancing<RapidHeavyMissileLauncherBalancing.RapidHeavyMissileLauncherProjectileBalancing>{
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = RapidHeavyMissileLauncherWeaponSubsystem::class

	@Serializable
	data class RapidHeavyMissileLauncherProjectileBalancing(
		override var range: Double = 300.0,
		override var speed: Double = 35.0,
		override var explosionPower: Float = 7.0f,
		override var starshipShieldDamageMultiplier: Double = 10.0,
		override var areaShieldDamageMultiplier: Double = 4.0,
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.swarm_missile.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.swarm_missile.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
		override val entityDamage: EntityDamage = RegularDamage(10.0),
		override var maxDegrees: Double = 90.0,
		override var particleThickness: Double = 2.0,
		var delayMillis: Int = 450,
		) : StarshipProjectileBalancing, StarshipTrackingProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = TrackingMissileProjectile::class
	}
}

@Serializable
data class TriTurretBalancing(
	override val fireRestrictions: FireRestrictions = FireRestrictions(),
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(10),
	override var firePowerConsumption: Int = 45000,
	override var isForwardOnly: Boolean = false,
	override var inaccuracyDegrees: Double = 3.0,
	override var range: Double= 500.0,
	override var boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(3),
	override var maxPerShot: Int? = null,
	override var applyCooldownToAll: Boolean = false,

	override val projectile: TriTurretProjectileBalancing = TriTurretProjectileBalancing()
) : StarshipTurretWeaponBalancing<TriTurretProjectileBalancing>, StarshipAutoWeaponBalancing<TriTurretProjectileBalancing>, StarshipHeavyWeaponBalancing<TriTurretProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = TriTurretWeaponSubsystem::class

	@Serializable
	data class TriTurretProjectileBalancing(
        override var range: Double = 500.0,
        override var speed: Double = 125.0,
        override var explosionPower: Float = 4f,
        override var starshipShieldDamageMultiplier: Double = 3.0,
        override var areaShieldDamageMultiplier: Double = 3.0,
        override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.tri_turret.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.tri_turret.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
        override var particleThickness: Double = 0.8
	) : StarshipParticleProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = TurretLaserProjectile::class
	}
}



@Serializable
data class LightTurretBalancing(
	override val fireRestrictions: FireRestrictions = FireRestrictions(
		canFire = false,
		minBlockCount = 0,
		maxBlockCount = 12000
	),
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(250),
	override var firePowerConsumption: Int = 6000,
	override var isForwardOnly: Boolean = false,
	override var inaccuracyDegrees: Double = 2.0,
	override var range: Double = 200.0,
	override var maxPerShot: Int? = null,
	override var applyCooldownToAll: Boolean = true,
	override val projectile: LightTurretProjectileBalancing = LightTurretProjectileBalancing()
) : StarshipTurretWeaponBalancing<LightTurretProjectileBalancing>, StarshipAutoWeaponBalancing<LightTurretProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = LightTurretWeaponSubsystem::class

	@Serializable
	data class LightTurretProjectileBalancing(
        override var range: Double = 200.0,
        override var speed: Double = 250.0,
        override var explosionPower: Float = 2.0f,
        override var starshipShieldDamageMultiplier: Double = 1.0,
        override var areaShieldDamageMultiplier: Double = 2.0,
        override val entityDamage: EntityDamage = RegularDamage(7.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.light_turret.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.light_turret.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
        override var particleThickness: Double = 0.3
	) : StarshipParticleProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = TurretLaserProjectile::class
	}
}

@Serializable
data class HeavyTurretBalancing(
	override val fireRestrictions: FireRestrictions = FireRestrictions(canFire = true, minBlockCount = 6500, maxBlockCount = 12500),
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(750),
	override var firePowerConsumption: Int = 2667,
	override var isForwardOnly: Boolean = false,
	override var inaccuracyDegrees: Double = 2.0,
	override var maxPerShot: Int? = null,
	override var applyCooldownToAll: Boolean = true,

	override val projectile: HeavyTurretProjectileBalancing = HeavyTurretProjectileBalancing()
) : StarshipTurretWeaponBalancing<HeavyTurretProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = HeavyTurretWeaponSubsystem::class

	@Serializable
	data class HeavyTurretProjectileBalancing(
        override var range: Double = 400.0,
        override var speed: Double = 70.0,
        override var explosionPower: Float = 3.5f,
        override var starshipShieldDamageMultiplier: Double = 3.25,
        override var areaShieldDamageMultiplier: Double = 1.0,
        override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.heavy_turret.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.heavy_turret.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
        override var particleThickness: Double = 0.3
	) : StarshipParticleProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = TurretLaserProjectile::class
	}
}

@Serializable
data class AutocannonBalancing(
	override val fireRestrictions: FireRestrictions = FireRestrictions(canFire = false, maxBlockCount = 12000),
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(500),
	override var firePowerConsumption: Int = 1000,
	override var isForwardOnly: Boolean = false,
	override var inaccuracyDegrees: Double = 2.0,
	override var maxPerShot: Int? = null,
	override var applyCooldownToAll: Boolean = true,

	override val projectile: AutocannonProjectileBalancing = AutocannonProjectileBalancing()
) : StarshipTurretWeaponBalancing<AutocannonBalancing.AutocannonProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = AutocannonWeaponSubsystem::class

	@Serializable
	data class AutocannonProjectileBalancing(
		override var range: Double = 200.0,
		override var speed: Double = 450.0,
		override var explosionPower: Float = 3.5f,
		override var starshipShieldDamageMultiplier: Double = 1.0,
		override var areaShieldDamageMultiplier: Double = 1.0,
		override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.autocannon.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.autocannon.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
		override var particleThickness: Double = 0.2,
		var delayMillis: Int = 100,
		) : StarshipParticleProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = AutocannonProjectile::class
	}
}

@Serializable
data class AssaultTurretBalancing(
	override val fireRestrictions: FireRestrictions = FireRestrictions(canFire = false, maxBlockCount = 12000),
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(2500),
	override var firePowerConsumption: Int = 2667,
	override var isForwardOnly: Boolean = false,
	override var inaccuracyDegrees: Double = 2.0,
	override var maxPerShot: Int? = null,
	override var applyCooldownToAll: Boolean = true,

	override val projectile: AssaultTurretProjectileBalancing = AssaultTurretProjectileBalancing()
) : StarshipTurretWeaponBalancing<AssaultTurretBalancing.AssaultTurretProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = AssaultTurretWeaponSubsystem::class

	@Serializable
	data class AssaultTurretProjectileBalancing(
		override var range: Double = 500.0,
		override var speed: Double = 80.0,
		override var explosionPower: Float = 4.5f,
		override var starshipShieldDamageMultiplier: Double = 6.0,
		override var areaShieldDamageMultiplier: Double = 1.0,
		override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.assault_turret.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.assault_turret.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
		override var particleThickness: Double = 0.4
	) : StarshipParticleProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = TurretLaserProjectile::class
	}
}

@Serializable
data class GaussCannonBalancing(
	override val fireRestrictions: FireRestrictions = FireRestrictions(canFire = false, maxBlockCount = 12000),
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(500),
	override var firePowerConsumption: Int = 2667,
	override var isForwardOnly: Boolean = false,
	override var inaccuracyDegrees: Double = 2.0,
	override var maxPerShot: Int? = null,
	override var applyCooldownToAll: Boolean = true,

	override val projectile: GaussCannonProjectileBalancing = GaussCannonProjectileBalancing()
) : StarshipTurretWeaponBalancing<GaussCannonBalancing.GaussCannonProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = GaussCannonWeaponSubsystem::class

	@Serializable
	data class GaussCannonProjectileBalancing(
		override var range: Double = 225.0,
		override var speed: Double = 130.0,
		override var explosionPower: Float = 3.5f,
		override var starshipShieldDamageMultiplier: Double = 4.5,
		override var areaShieldDamageMultiplier: Double = 1.0,
		override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.gauss_cannon.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.gauss_cannon.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
		override var particleThickness: Double = 0.3
	) : StarshipParticleProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = TurretLaserProjectile::class
	}
}

@Serializable
data class QuadTurretBalancing(
	override val fireRestrictions: FireRestrictions = FireRestrictions(canFire = false, minBlockCount = 18500),
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(4000),
	override var firePowerConsumption: Int = 3000,
	override var isForwardOnly: Boolean = false,
	override var inaccuracyDegrees: Double = 2.0,
	override var maxPerShot: Int = 3,
	override var applyCooldownToAll: Boolean = true,

	override val projectile: QuadTurretProjectileBalancing = QuadTurretProjectileBalancing()
) : StarshipTurretWeaponBalancing<QuadTurretProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = QuadTurretWeaponSubsystem::class

	@Serializable
	data class QuadTurretProjectileBalancing(
        override var range: Double = 500.0,
        override var speed: Double = 37.5,
        override var explosionPower: Float = 5f,
        override var starshipShieldDamageMultiplier: Double = 6.9,
        override var areaShieldDamageMultiplier: Double = 10.0,
        override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.quad_turret.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.quad_turret.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
        override var particleThickness: Double = 0.6
	) : StarshipParticleProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = TurretLaserProjectile::class
	}
}

@Serializable
data class ACAPTurretBalancing(
	override val fireRestrictions: FireRestrictions = FireRestrictions(canFire = false, minBlockCount = 18500),
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(4000),
	override var firePowerConsumption: Int = 3000,
	override var isForwardOnly: Boolean = false,
	override var inaccuracyDegrees: Double = 2.0,
	override var maxPerShot: Int = 3,
	override var applyCooldownToAll: Boolean = true,

	override val projectile: ACAPTurretProjectileBalancing = ACAPTurretProjectileBalancing()
) : StarshipTurretWeaponBalancing<ACAPTurretBalancing.ACAPTurretProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = ACAPTurretWeaponSubsystem::class

	@Serializable
	data class ACAPTurretProjectileBalancing(
		override var range: Double = 500.0,
		override var speed: Double = 37.5,
		override var explosionPower: Float = 10f,
		override var starshipShieldDamageMultiplier: Double = 9.5,
		override var areaShieldDamageMultiplier: Double = 6.0,
		override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.acap_turret.shoot", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.acap_turret.shoot", volume = 0f, source = Sound.Source.PLAYER),
		override var particleThickness: Double = 0.6
	) : StarshipParticleProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = TurretLaserProjectile::class
	}
}

@Serializable
data class IonTurretBalancing(
	override val fireRestrictions: FireRestrictions = FireRestrictions(canFire = false, minBlockCount = 13500, maxBlockCount = 16000),
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(1500),
	override var firePowerConsumption: Int = 3000,
	override var isForwardOnly: Boolean = false,
	override var inaccuracyDegrees: Double = 1.0,
	override var maxPerShot: Int = 4,
	override var applyCooldownToAll: Boolean = true,

	override val projectile: IonTurretProjectileBalancing = IonTurretProjectileBalancing()
) : StarshipTurretWeaponBalancing<IonTurretProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = IonTurretWeaponSubsystem::class

	@Serializable
	data class IonTurretProjectileBalancing(
		override var range: Double = 400.0,
		override var speed: Double = 55.0,
		override var explosionPower: Float = 3f,
		override var starshipShieldDamageMultiplier: Double = 3.7,
		override var areaShieldDamageMultiplier: Double = 100.0,
		override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.ion_turret.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.ion_turret.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
		override var particleThickness: Double = 0.6,
		override var effectStrength: Double = 0.65,
		override var effectDurationMillis: Long = TimeUnit.SECONDS.toMillis(7L),
	) : StarshipParticleProjectileBalancing, StarshipStatusEffectProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = IonTurretProjectile::class
	}
}

@Serializable
data class PointDefenseBalancing(
	override val fireRestrictions: FireRestrictions = FireRestrictions(),
	override var fireCooldownNanos: Long = TimeUnit.SECONDS.toMillis(10),
	override var firePowerConsumption: Int = 500,
	override var isForwardOnly: Boolean = false,
	override var range: Double = 120.0,
	override var maxPerShot: Int? = null,
	override var applyCooldownToAll: Boolean = true,

	override val projectile: PointDefenseProjectileBalancing = PointDefenseProjectileBalancing()
) : StarshipWeaponBalancing<PointDefenseProjectileBalancing>, StarshipAutoWeaponBalancing<PointDefenseProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = PointDefenseSubsystem::class

	@Serializable
	data class PointDefenseProjectileBalancing(
        override var range: Double = 150.0,
        override var speed: Double = 170.0,
        override var explosionPower: Float = 0.0f,
        override var starshipShieldDamageMultiplier: Double = 0.0,
        override var areaShieldDamageMultiplier: Double = 0.0,
        override val entityDamage: EntityDamage = RegularDamage(30.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.point_defense.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.point_defense.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
        override var particleThickness: Double = 0.35
	) : StarshipParticleProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = PointDefenseLaserProjectile::class
	}
}

@Serializable
data class PulseCannonBalancing(
	override val fireRestrictions: FireRestrictions = FireRestrictions(canFire = false, minBlockCount = 1000, maxBlockCount = 4000),
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(250),
	override var firePowerConsumption: Int = 2550,
	override var isForwardOnly: Boolean = false,
	override var maxPerShot: Int? = null,
	override var applyCooldownToAll: Boolean = true,

	override var convergeDistance: Double = 16.0,
	override var projectileSpawnDistance: Int = 3,
	override var angleRadiansHorizontal: Double = 180.0,
	override var angleRadiansVertical: Double = 180.0,

	override val projectile: PulseCannonProjectileBalancing = PulseCannonProjectileBalancing()
) : StarshipCannonWeaponBalancing<PulseCannonProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = PulseCannonWeaponSubsystem::class

	@Serializable
	data class PulseCannonProjectileBalancing(
        override var range: Double = 160.0,
        override var speed: Double = 230.0,
        override var explosionPower: Float = 1.5f,
        override var starshipShieldDamageMultiplier: Double = 1.8,
        override var areaShieldDamageMultiplier: Double = 2.0,
        override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.pulse_cannon.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.pulse_cannon.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
        override var particleThickness: Double = 0.4
	) : StarshipParticleProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = PulseLaserProjectile::class
	}
}

@Serializable
data class ScramblerBalancing(
	override val fireRestrictions: FireRestrictions = FireRestrictions(canFire = false, maxBlockCount = 12000),
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(250),
	override var firePowerConsumption: Int = 2550,
	override var isForwardOnly: Boolean = true,
	override var maxPerShot: Int? = null,
	override var applyCooldownToAll: Boolean = true,

	override var convergeDistance: Double = 16.0,
	override var projectileSpawnDistance: Int = 3,
	override var angleRadiansHorizontal: Double = 180.0,
	override var angleRadiansVertical: Double = 180.0,

	override val projectile: ScramblerProjectileBalancing = ScramblerProjectileBalancing()
) : StarshipCannonWeaponBalancing<ScramblerBalancing.ScramblerProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = ScramblerWeaponSubsystem::class

	@Serializable
	data class ScramblerProjectileBalancing(
		override var range: Double = 145.0,
		override var speed: Double = 400.0,
		override var explosionPower: Float = 4.0f,
		override var starshipShieldDamageMultiplier: Double = 3.2,
		override var areaShieldDamageMultiplier: Double = 10.0,
		override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.scrambler.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.scrambler.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
		override var particleThickness: Double = 2.0,
		override var effectStrength: Double = 0.9,
		override var effectDurationMillis: Long = TimeUnit.SECONDS.toMillis(20L)
	) : StarshipParticleProjectileBalancing, StarshipStatusEffectProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = ScramblerProjectile::class
	}
}

@Serializable
data class PlasmaCannonBalancing(
	override val fireRestrictions: FireRestrictions = FireRestrictions(),
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(250),
	override var firePowerConsumption: Int = 2500,
	override var isForwardOnly: Boolean = true,
	override var maxPerShot: Int = 2,
	override var applyCooldownToAll: Boolean = true,

	override var convergeDistance: Double = 10.0,
	override var projectileSpawnDistance: Int = 1,
	override var angleRadiansHorizontal: Double = 15.0,
	override var angleRadiansVertical: Double = 15.0,

	override val projectile: PlasmaCannonProjectileBalancing = PlasmaCannonProjectileBalancing()
) : StarshipCannonWeaponBalancing<PlasmaCannonProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = PlasmaCannonWeaponSubsystem::class

	@Serializable
	data class PlasmaCannonProjectileBalancing(
        override var range: Double = 160.0,
        override var speed: Double = 400.0,
        override var explosionPower: Float = 3f,
        override var starshipShieldDamageMultiplier: Double = 2.0,
        override var areaShieldDamageMultiplier: Double = 3.0,
        override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.plasma_cannon.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.plasma_cannon.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
        override var particleThickness: Double = 0.5
	) : StarshipParticleProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = PlasmaLaserProjectile::class
	}
}

@Serializable
data class ArtilleryBalancing(
	override val fireRestrictions: FireRestrictions = FireRestrictions(canFire = false),
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(2000),
	override var firePowerConsumption: Int = 7200,
	override var isForwardOnly: Boolean = true,
	override var maxPerShot: Int = 1,
	override var applyCooldownToAll: Boolean = true,

	override var convergeDistance: Double = 10.0,
	override var projectileSpawnDistance: Int = 1,
	override var angleRadiansHorizontal: Double = 75.0,
	override var angleRadiansVertical: Double = 75.0,

	override val projectile: ArtilleryProjectileBalancing = ArtilleryProjectileBalancing()
) : StarshipCannonWeaponBalancing<ArtilleryBalancing.ArtilleryProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = ArtilleryWeaponSubsystem::class

	@Serializable
	data class ArtilleryProjectileBalancing(
		override var range: Double = 230.0,
		override var speed: Double = 125.0,
		override var explosionPower: Float = 4f,
		override var starshipShieldDamageMultiplier: Double = 16.7,
		override var areaShieldDamageMultiplier: Double = 3.0,
		override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.artillery.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.artillery.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
		override var particleThickness: Double = 0.5
	) : StarshipParticleProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = ArtilleryProjectile::class
	}
}

@Serializable
data class LaserCannonBalancing(
	override val fireRestrictions: FireRestrictions = FireRestrictions(),
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(250),
	override var firePowerConsumption: Int = 600,
	override var isForwardOnly: Boolean = false,
	override var maxPerShot: Int? = null,
	override var applyCooldownToAll: Boolean = true,

	override var convergeDistance: Double = 20.0,
	override var projectileSpawnDistance: Int = 2,
	override var angleRadiansHorizontal: Double = 17.0,
	override var angleRadiansVertical: Double = 17.0,

	override val projectile: LaserCannonProjectileBalancing = LaserCannonProjectileBalancing()
) : StarshipCannonWeaponBalancing<LaserCannonProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = LaserCannonWeaponSubsystem::class

	@Serializable
	data class LaserCannonProjectileBalancing(
        override var range: Double = 200.0,
        override var speed: Double = 250.0,
        override var explosionPower: Float = 1f,
        override var starshipShieldDamageMultiplier: Double = 0.2,
        override var areaShieldDamageMultiplier: Double = 1.0,
        override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.laser_cannon.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.laser_cannon.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
        override var particleThickness: Double = 0.44
	) : StarshipParticleProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = LaserCannonLaserProjectile::class
	}
}

@Serializable
data class InterceptorCannonBalancing(
	override val fireRestrictions: FireRestrictions = FireRestrictions(canFire = false),
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(250),
	override var firePowerConsumption: Int = 160,
	override var isForwardOnly: Boolean = true,
	override var maxPerShot: Int? = null,
	override var applyCooldownToAll: Boolean = true,

	override var convergeDistance: Double = 20.0,
	override var projectileSpawnDistance: Int = 3,
	override var angleRadiansHorizontal: Double = 180.0,
	override var angleRadiansVertical: Double = 180.0,

	override val projectile: IncterceptorCannonProjectileBalancing = IncterceptorCannonProjectileBalancing()
) : StarshipCannonWeaponBalancing<IncterceptorCannonProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = InterceptorCannonWeaponSubsystem::class

	@Serializable
	data class IncterceptorCannonProjectileBalancing(
        override var range: Double = 200.0,
        override var speed: Double = 250.0,
        override var explosionPower: Float = 0.1f,
        override var starshipShieldDamageMultiplier: Double = 1.0,
        override var areaShieldDamageMultiplier: Double = 1.0,
        override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.laser_cannon.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.laser_cannon.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
        override var particleThickness: Double = 0.44
	) : StarshipParticleProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = InterceptorCannonProjectile::class
	}
}

// Start Event Weapons
@Serializable
data class DoomsdayDeviceBalancing(
	override val fireRestrictions: FireRestrictions = FireRestrictions(canFire = false),
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(250),
	override var firePowerConsumption: Int = 85000,
	override var isForwardOnly: Boolean = false,
	override var maxPerShot: Int = 1,
	override var applyCooldownToAll: Boolean = true,

	override var convergeDistance: Double = 0.0,
	override var projectileSpawnDistance: Int = 10,
	override var angleRadiansHorizontal: Double = 80.0,
	override var angleRadiansVertical: Double = 80.0,
	override var boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(60),

	override val projectile: DoomsdayDeviceProjectileBalancing = DoomsdayDeviceProjectileBalancing(),
) : StarshipCannonWeaponBalancing<DoomsdayDeviceProjectileBalancing>, StarshipHeavyWeaponBalancing<DoomsdayDeviceProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = DoomsdayDeviceWeaponSubsystem::class

	@Serializable
	data class DoomsdayDeviceProjectileBalancing(
        override var range: Double = 500.0,
        override var speed: Double = 175.0,
        override var explosionPower: Float = 18f,
        override var starshipShieldDamageMultiplier: Double = 150.0,
        override var areaShieldDamageMultiplier: Double = 2500.0,
        override val entityDamage: EntityDamage = RegularDamage(100.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.doomsday_device.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.doomsday_device.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
        override var particleThickness: Double = 5.0
	) : StarshipParticleProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = DoomsdayDeviceProjectile::class
	}
}

@Serializable
data class RocketBalancing(
	override val fireRestrictions: FireRestrictions = FireRestrictions(),
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(250),
	override var firePowerConsumption: Int = 50000,
	override var isForwardOnly: Boolean = false,
	override var maxPerShot: Int? = null,
	override var applyCooldownToAll: Boolean = false,

	override var convergeDistance: Double = 0.0,
	override var projectileSpawnDistance: Int = 0,
	override var angleRadiansHorizontal: Double = 0.0,
	override var angleRadiansVertical: Double = 0.0,
	override var boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(7),

	override val projectile: RocketProjectileBalancing = RocketProjectileBalancing(),
) : StarshipCannonWeaponBalancing<RocketProjectileBalancing>, StarshipHeavyWeaponBalancing<RocketProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = RocketWeaponSubsystem::class

	@Serializable
	data class RocketProjectileBalancing(
        override var range: Double = 300.0,
        override var speed: Double = 5.0,
        override var explosionPower: Float = 10f,
        override var starshipShieldDamageMultiplier: Double = 5.0,
        override var areaShieldDamageMultiplier: Double = 5.0,
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
	override val fireRestrictions: FireRestrictions = FireRestrictions(canFire = false),
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(1000),
	override var firePowerConsumption: Int = 5000,
	override var isForwardOnly: Boolean = false,
	override var maxPerShot: Int = 1,
	override var applyCooldownToAll: Boolean = true,

	override var inaccuracyDegrees: Double = 0.5,

	override val projectile: LogisticsTurretProjectileBalancing = LogisticsTurretProjectileBalancing(),
) : StarshipTurretWeaponBalancing<LogisticsTurretProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = LogisticTurretWeaponSubsystem::class

	@Serializable
	data class LogisticsTurretProjectileBalancing(
        override var range: Double = 100.0,
        override var speed: Double = 2000.0,
        override var explosionPower: Float = 0f,
        override var starshipShieldDamageMultiplier: Double = 0.0,
        override var areaShieldDamageMultiplier: Double = 0.0,
        override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.logistics_turret.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.logistics_turret.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
        override var particleThickness: Double = 1.0,
        override var shieldBoostFactor: Int = 50000
	) : StarshipParticleProjectileBalancing, StarshipHealingProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = LogisticTurretProjectile::class
	}
}

@Serializable
data class LightLogisticsCannonBalancing(
	override val fireRestrictions: FireRestrictions = FireRestrictions(canFire = false),
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(1750),
	override var firePowerConsumption: Int = 3000,
	override var isForwardOnly: Boolean = false,
	override var maxPerShot: Int = 2,
	override var applyCooldownToAll: Boolean = true,
	override var convergeDistance: Double = 10.0,
	override var projectileSpawnDistance: Int = 1,
	override var angleRadiansVertical: Double = 180.0,
	override var angleRadiansHorizontal: Double = 180.0,

	override val projectile: LightLogisticsCannonProjectileBalancing = LightLogisticsCannonProjectileBalancing(),
) : StarshipCannonWeaponBalancing<LightLogisticsCannonBalancing.LightLogisticsCannonProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = LightLogisticsCannonWeaponSubsystem::class

	@Serializable
	data class LightLogisticsCannonProjectileBalancing(
		override var range: Double = 140.0,
		override var speed: Double = 2000.0,
		override var explosionPower: Float = 0f,
		override var starshipShieldDamageMultiplier: Double = 0.0,
		override var areaShieldDamageMultiplier: Double = 0.0,
		override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.logistics_turret.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.logistics_turret.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
		override var particleThickness: Double = 1.5,
		override var shieldBoostFactor: Int = 52500
	) : StarshipParticleProjectileBalancing, StarshipHealingProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = LightLogisticsProjectile::class
	}
}

@Serializable
data class HeavyLogisticsCannonBalancing(
	override val fireRestrictions: FireRestrictions = FireRestrictions(canFire = false),
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(2000),
	override var firePowerConsumption: Int = 3750,
	override var isForwardOnly: Boolean = false,
	override var maxPerShot: Int = 3,
	override var applyCooldownToAll: Boolean = true,
	override var convergeDistance: Double = 10.0,
	override var projectileSpawnDistance: Int = 1,
	override var angleRadiansVertical: Double = 180.0,
	override var angleRadiansHorizontal: Double = 180.0,

	override val projectile: HeavyLogisticsCannonProjectileBalancing = HeavyLogisticsCannonProjectileBalancing(),
) : StarshipCannonWeaponBalancing<HeavyLogisticsCannonBalancing.HeavyLogisticsCannonProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = HeavyLogisticsCannonWeaponSubsystem::class

	@Serializable
	data class HeavyLogisticsCannonProjectileBalancing(
		override var range: Double = 200.0,
		override var speed: Double = 2000.0,
		override var explosionPower: Float = 0f,
		override var starshipShieldDamageMultiplier: Double = 0.0,
		override var areaShieldDamageMultiplier: Double = 0.0,
		override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.logistics_turret.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.logistics_turret.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
		override var particleThickness: Double = 2.5,
		override var shieldBoostFactor: Int = 900000
	) : StarshipParticleProjectileBalancing, StarshipHealingProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = HeavyLogisticsProjectile::class
	}
}



@Serializable
data class DisintegratorBeamBalancing(
	override val fireRestrictions: FireRestrictions = FireRestrictions(),
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(100),
	override var firePowerConsumption: Int = 100,
	override var isForwardOnly: Boolean = false,
	override var maxPerShot: Int = 6,
	override var applyCooldownToAll: Boolean = true,

	var inaccuracyRadians: Double = 0.01,

	override val projectile: DisintegratorBeamProjectileBalancing = DisintegratorBeamProjectileBalancing(),
) : StarshipWeaponBalancing<DisintegratorBeamProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = DisintegratorBeamWeaponSubsystem::class

	@Serializable
	data class DisintegratorBeamProjectileBalancing(
        override var range: Double = 2000.0,
        override var speed: Double = 100.0,
        override var explosionPower: Float = 1f,
        override var starshipShieldDamageMultiplier: Double = 1.0,
        override var areaShieldDamageMultiplier: Double = 1.0,
        override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.light_turret.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.light_turret.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
        override var particleThickness: Double = 0.5,
	) : StarshipParticleProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = DisintegratorBeamProjectile::class
	}
}

@Serializable
data class CycleTurretBalancing(
	override val fireRestrictions: FireRestrictions = FireRestrictions(),
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(500),
	override var firePowerConsumption: Int = 100,
	override var isForwardOnly: Boolean = false,
	override var maxPerShot: Int = 3,
	override var inaccuracyDegrees: Double = 0.5,
	override var applyCooldownToAll: Boolean = true,

	override val projectile: CycleTurretProjectileBalancing = CycleTurretProjectileBalancing()
) : StarshipTurretWeaponBalancing<CycleTurretProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = CycleTurretWeaponSubsystem::class

	@Serializable
	data class CycleTurretProjectileBalancing(
        override var range: Double = 275.0,
        override var speed: Double = 1800.0,
        override var explosionPower: Float = 2f,
        override var starshipShieldDamageMultiplier: Double = 0.75,
        override var areaShieldDamageMultiplier: Double = 1.0,
        override val entityDamage: EntityDamage = RegularDamage(10.0),
		override val fireSoundNear: SoundInfo = SoundInfo("horizonsend:starship.weapon.light_turret.shoot.near", volume = 1f, source = Sound.Source.PLAYER),
		override val fireSoundFar: SoundInfo = SoundInfo("horizonsend:starship.weapon.light_turret.shoot.far", volume = 1f, source = Sound.Source.PLAYER),
        override var particleThickness: Double = 0.25,
        var delayMillis: Int = 250,
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
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(1000),
	override var firePowerConsumption: Int = 10000,
	override var isForwardOnly: Boolean = false,
	override var maxPerShot: Int? = null,
	override var applyCooldownToAll: Boolean = false,

	override var convergeDistance: Double = 0.0,
	override var projectileSpawnDistance: Int = 3,
	override var angleRadiansHorizontal: Double = 18.0,
	override var angleRadiansVertical: Double = 18.0,

	override val projectile: AbyssalGazeProjectileBalancing = AbyssalGazeProjectileBalancing()
) : StarshipCannonWeaponBalancing<AbyssalGazeProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = AbyssalGazeSubsystem::class

	@Serializable
	data class AbyssalGazeProjectileBalancing(
        override var range: Double = 500.0,
        override var speed: Double = 50.0,
        override var explosionPower: Float = 2.5f,
        override var starshipShieldDamageMultiplier: Double =  1.25,
        override var areaShieldDamageMultiplier: Double = 1.0,
        override val entityDamage: EntityDamage = RegularDamage(10.0),
        override val fireSoundNear: SoundInfo = SoundInfo(key = "item.trident.riptide_1", volume = 10f, pitch = 2f),
        override val fireSoundFar: SoundInfo = SoundInfo(key = "item.trident.riptide_1", volume = 10f, pitch = 2f),
        override var particleThickness: Double = 0.0,
        override var maxDegrees: Double = 10.0
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
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(5000),
	override var firePowerConsumption: Int = 70000,
	override var isForwardOnly: Boolean = false,
	override var maxPerShot: Int? = null,
	override var applyCooldownToAll: Boolean = false,

	override var convergeDistance: Double = 0.0,
	override var projectileSpawnDistance: Int = 0,
	override var angleRadiansHorizontal: Double = 18.0,
	override var angleRadiansVertical: Double = 18.0,
	override var boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(5),

	override val projectile: SonicMissileProjectileBalancing = SonicMissileProjectileBalancing(),
) : StarshipCannonWeaponBalancing<SonicMissileProjectileBalancing>, StarshipHeavyWeaponBalancing<SonicMissileProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = SonicMissileWeaponSubsystem::class

	@Serializable
	data class SonicMissileProjectileBalancing(
        override var range: Double= 300.0,
        override var speed: Double= 200.0,
        override var explosionPower: Float = 15.0f,
        override var starshipShieldDamageMultiplier: Double = 10.0,
        override var areaShieldDamageMultiplier: Double = 10.0,
        override val entityDamage: EntityDamage = RegularDamage(10.0),
        override val fireSoundNear: SoundInfo = SoundInfo(key = "entity.warden.sonic_boom"),
        override val fireSoundFar: SoundInfo = SoundInfo(key = "entity.warden.sonic_boom"),
        override var particleThickness: Double = 0.0,
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
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(10),
	override var firePowerConsumption: Int = 15000,
	override var isForwardOnly: Boolean = false,
	override var maxPerShot: Int? = null,
	override var applyCooldownToAll: Boolean = false,

	override val projectile: PumpkinCannonProjectileBalancing = PumpkinCannonProjectileBalancing(),
) : StarshipWeaponBalancing<PumpkinCannonProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = PumpkinCannonWeaponSubsystem::class

	@Serializable
	data class PumpkinCannonProjectileBalancing(
		override var range: Double = 500.0,
		override var speed: Double = 125.0,
		override var explosionPower: Float = 1.0f,
		override var starshipShieldDamageMultiplier: Double = 1.0,
		override var areaShieldDamageMultiplier: Double = 1.0,
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
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(5000),
	override var firePowerConsumption: Int = 70000,
	override var isForwardOnly: Boolean = false,
	override var maxPerShot: Int? = null,
	override var applyCooldownToAll: Boolean = false,

	override var convergeDistance: Double = 0.0,
	override var projectileSpawnDistance: Int = 5,
	override var angleRadiansHorizontal: Double = 18.0,
	override var angleRadiansVertical: Double = 18.0,
	override var boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(5),

	override val projectile: FlamingSkullCannonProjectileBalancing,
) : StarshipCannonWeaponBalancing<FlamingSkullCannonProjectileBalancing>, StarshipHeavyWeaponBalancing<FlamingSkullCannonProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = FlamingSkullCannonWeaponSubsystem::class

	@Serializable
	data class FlamingSkullCannonProjectileBalancing(
        override var range: Double = 500.0,
        override var speed: Double = 200.0,
        override var explosionPower: Float = 15f,
        override var starshipShieldDamageMultiplier: Double = 10.0,
        override var areaShieldDamageMultiplier: Double = 10.0,
        override val entityDamage: EntityDamage = RegularDamage(10.0),
        override val fireSoundNear: SoundInfo = SoundInfo(key = "entity.warden.sonic_boom", volume = 10f, pitch = 2f),
        override val fireSoundFar: SoundInfo = SoundInfo(key = "entity.warden.sonic_boom", volume = 10f, pitch = 2f),
        override var particleThickness: Double = 0.0,
        override var maxDegrees: Double = 0.0,
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
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(10),
	override var firePowerConsumption: Int = 50000,
	override var isForwardOnly: Boolean = false,
	override var maxPerShot: Int? = null,
	override var applyCooldownToAll: Boolean = false,

	override var convergeDistance: Double = 0.0,
	override var projectileSpawnDistance: Int = 0,
	override var angleRadiansHorizontal: Double = 180.0,
	override var angleRadiansVertical: Double = 180.0,

	override val projectile: FlamethrowerCannonProjectileBalancing = FlamethrowerCannonProjectileBalancing(),
) : StarshipCannonWeaponBalancing<FlamethrowerCannonProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = FlamethrowerWeaponSubsystem::class

	@Serializable
	data class FlamethrowerCannonProjectileBalancing(
        override var range: Double = 340.0,
        override var speed: Double = 350.0,
        override var explosionPower: Float = 2.0f,
        override var starshipShieldDamageMultiplier: Double = 5.0,
        override var areaShieldDamageMultiplier: Double = 5.0,
        override val entityDamage: EntityDamage = RegularDamage(10.0),
        override val fireSoundNear: SoundInfo = SoundInfo(key = "block.fire.ambient", volume = 10f, pitch = 0.5f),
        override val fireSoundFar: SoundInfo = SoundInfo(key = "block.fire.ambient", volume = 10f, pitch = 0.5f),
        override var particleThickness: Double = 0.0,
        override var gravityMultiplier: Double = 0.05,
        override var decelerationAmount: Double = 0.05,
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
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(500),
	override var firePowerConsumption: Int = 5000,
	override var isForwardOnly: Boolean = false,
	override var maxPerShot: Int? = null,
	override var applyCooldownToAll: Boolean = true,

	override var convergeDistance: Double = 0.0,
	override var projectileSpawnDistance: Int = 0,
	override var angleRadiansHorizontal: Double = 30.0,
	override var angleRadiansVertical: Double = 30.0,

	override val projectile: MiniPhaserProjectileBalancing = MiniPhaserProjectileBalancing(),
) : StarshipCannonWeaponBalancing<MiniPhaserProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = MiniPhaserWeaponSubsystem::class

	@Serializable
	data class MiniPhaserProjectileBalancing(
		override var range: Double = 200.0,
		override var speed: Double = 600.0,
		override var explosionPower: Float = 2f,
		override var starshipShieldDamageMultiplier: Double = 1.0,
		override var areaShieldDamageMultiplier: Double = 1.0,
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
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(10),
	override var firePowerConsumption: Int = 1,
	override var isForwardOnly: Boolean = false,
	override var maxPerShot: Int? = null,
	override var applyCooldownToAll: Boolean = false,

	override var range: Double = 128.0,

	override val projectile: CthulhuBeamProjectileBalancing = CthulhuBeamProjectileBalancing(),
) : StarshipAutoWeaponBalancing<CthulhuBeamBalancing.CthulhuBeamProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = FlamingSkullCannonWeaponSubsystem::class

	@Serializable
	data class CthulhuBeamProjectileBalancing(
        override var range: Double = 128.0,
        override var speed: Double = 1.0,
        override var explosionPower: Float = 2f,
        override var starshipShieldDamageMultiplier: Double = 1.0,
        override var areaShieldDamageMultiplier: Double = 1.0,
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
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(3000),
	override var firePowerConsumption: Int = 120000,
	override var isForwardOnly: Boolean = false,
	override var maxPerShot: Int? = null,
	override var boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(10),
	override var applyCooldownToAll: Boolean = true,

	override val projectile: CapitalCannonProjectileBalancing = CapitalCannonProjectileBalancing(),
) : StarshipHeavyWeaponBalancing<CapitalCannonBalancing.CapitalCannonProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = CapitalBeamWeaponSubsystem::class

	@Serializable
	data class CapitalCannonProjectileBalancing(
        override var range: Double = 500.0,
        override var speed: Double = PI * 50.0,
        override var explosionPower: Float = 20f,
        override var starshipShieldDamageMultiplier: Double = 2.0,
        override var areaShieldDamageMultiplier: Double = 2.0,
        override val entityDamage: EntityDamage = RegularDamage(20.0),
        override val fireSoundNear: SoundInfo = SoundInfo(key = "entity.zombie_villager.converted"),
        override val fireSoundFar: SoundInfo = SoundInfo(key = "entity.zombie_villager.converted"),
        override var particleThickness: Double = 0.44,
	) : StarshipParticleProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = CapitalBeamCannonProjectile::class
	}
}

@Serializable
data class TestBoidCannonBalancing(
	override val projectile: TestBoidCannonProjectileBalancing = TestBoidCannonProjectileBalancing(),
	override val fireRestrictions: FireRestrictions = FireRestrictions(),
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(3000),
	override var firePowerConsumption: Int = 10000,
	override var isForwardOnly: Boolean = false,
	override var maxPerShot: Int? = 2,
	override var applyCooldownToAll: Boolean = true,
	override var convergeDistance: Double = 10.0,
	override var projectileSpawnDistance: Int = 1,
	override var angleRadiansHorizontal: Double = 30.0,
	override var angleRadiansVertical: Double = 30.0
) : StarshipCannonWeaponBalancing<TestBoidCannonBalancing.TestBoidCannonProjectileBalancing> {
	@Transient
	override val clazz: KClass<out BalancedWeaponSubsystem<*>> = TestBoidWeaponSubsystem::class

	@Serializable
	data class TestBoidCannonProjectileBalancing(
		override var range: Double = 170.0,
		override var speed: Double = 100.0,
		override var explosionPower: Float = 1.0f,
		override var starshipShieldDamageMultiplier: Double = 1.0,
		override var areaShieldDamageMultiplier: Double = 1.0,
		override val fireSoundNear: SoundInfo = SoundInfo(key = "entity.zombie_villager.converted"),
		override val fireSoundFar: SoundInfo = SoundInfo(key = "entity.zombie_villager.converted"),
		override val entityDamage: EntityDamage = RegularDamage(20.0),
		override var separationDistance: Double = 8.0,
		override var separationFactor: Double = 0.20,
		override var visibleDistance: Double = 100.0,
		override var alignFactor: Double = 0.05,
		override var centerFactor: Double = 0.10,
		override var minSpeedFactor: Double = 0.2,
		override var maxSpeedFactor: Double = 1.0,
		override var originalDirectionFactor: Double = 2.0
	) : StarshipBoidProjectileBalancing {
		@Transient
		override val clazz: KClass<out Projectile> = TestBoidProjectile::class
	}
}

// End Event Weapons

// Begin Command Bursts
@Serializable
data class ShieldCommandBurstBalancing(
	@Transient
	override val clazz: KClass<out AbstractCommandBurstSubsystem<*>> = ShieldCommandBurstSubsystem::class,
	override val activateRestrictions: StarshipCommandBurstBalancing.ActivateRestrictions = StarshipCommandBurstBalancing.ActivateRestrictions(canActivate = false, incompatibleMultiblocks = listOf(
		IncompatibleSubsystemInfo(
			SkirmishCommandBurstSubsystem::class.java,
			"You cannot have more than one type of command burst!"
		)
	)),
	override val activateCooldownMillis: Long = TimeUnit.SECONDS.toMillis(45),
	override val range: Double = 200.0,
	override val effectDurationMillis: Long = TimeUnit.SECONDS.toMillis(25),

	override val effectStrength: Double = 0.10,
) : StarshipMultiplierCommandBurstBalancing

@Serializable
data class SkirmishCommandBurstBalancing(
	@Transient
	override val clazz: KClass<out AbstractCommandBurstSubsystem<*>> = SkirmishCommandBurstSubsystem::class,
	override val activateRestrictions: StarshipCommandBurstBalancing.ActivateRestrictions = StarshipCommandBurstBalancing.ActivateRestrictions(canActivate = false, incompatibleMultiblocks = listOf(
		IncompatibleSubsystemInfo(
			ShieldCommandBurstSubsystem::class.java,
			"You cannot have more than one type of command burst!"
		)
	)),
	override val activateCooldownMillis: Long = TimeUnit.SECONDS.toMillis(45),
	override val range: Double = 200.0,
	override val effectDurationMillis: Long = TimeUnit.SECONDS.toMillis(25),
	override val effectStrength: Double = 0.10,
) : StarshipMultiplierCommandBurstBalancing

@Serializable
data class CapitalSkirmishCommandBurstBalancing(
	@Transient
	override val clazz: KClass<out AbstractCommandBurstSubsystem<*>> = CapitalSkirmishCommandBurstSubsystem::class,
	override val activateRestrictions: StarshipCommandBurstBalancing.ActivateRestrictions = StarshipCommandBurstBalancing.ActivateRestrictions(canActivate = false, incompatibleMultiblocks = listOf(
		IncompatibleSubsystemInfo(
			CapitalShieldCommandBurstSubsystem::class.java,
			"You cannot have more than one type of command burst!"
		)
	)),
	override val activateCooldownMillis: Long = TimeUnit.SECONDS.toMillis(45),
	override val range: Double = 500.0,
	override val effectDurationMillis: Long = TimeUnit.SECONDS.toMillis(45),
	override val effectStrength: Double = 0.20,
) : StarshipMultiplierCommandBurstBalancing

@Serializable
data class CapitalShieldCommandBurstBalancing(
	@Transient
	override val clazz: KClass<out AbstractCommandBurstSubsystem<*>> = CapitalShieldCommandBurstSubsystem::class,
	override val activateRestrictions: StarshipCommandBurstBalancing.ActivateRestrictions = StarshipCommandBurstBalancing.ActivateRestrictions(canActivate = false, incompatibleMultiblocks = listOf(
		IncompatibleSubsystemInfo(
			CapitalSkirmishCommandBurstSubsystem::class.java,
			"You cannot have more than one type of command burst!"
		)
	)),
	override val activateCooldownMillis: Long = TimeUnit.SECONDS.toMillis(45),
	override val range: Double = 500.0,
	override val effectDurationMillis: Long = TimeUnit.SECONDS.toMillis(45),
	override val effectStrength: Double = 0.20,
) : StarshipMultiplierCommandBurstBalancing

// End Command Bursts
