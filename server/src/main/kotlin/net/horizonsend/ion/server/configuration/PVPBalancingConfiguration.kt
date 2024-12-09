package net.horizonsend.ion.server.configuration

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.configuration.StarshipSounds.SoundInfo
import net.kyori.adventure.sound.Sound

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
		),

		val smokeGrenade: ThrowableBalancing = ThrowableBalancing(
			damage = 1.0,
			damageRadius = 4.0,
			throwVelocityMultiplier = 1.15,
			maxHealth = 5,
			maxTicks = 50,
			tickInterval = 1,
			throwCooldownTicks = 25
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
			capacity = 10,
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
			consumesAmmo = false,
			soundReloadStart = SoundInfo("horizonsend:blaster.pistol.reload.start", volume = 1f, source = Sound.Source.PLAYER),
			soundReloadFinish = SoundInfo("horizonsend:blaster.pistol.reload.finish", volume = 1f, source = Sound.Source.PLAYER),
			soundFire = SoundInfo("horizonsend:blaster.pistol.shoot", volume = 1f, source = Sound.Source.PLAYER),
			soundWhizz = SoundInfo("horizonsend:blaster.whizz.standard", volume = 1f, source = Sound.Source.PLAYER),
			soundShell = SoundInfo("horizonsend:blaster.pistol.shell", volume = 1f, source = Sound.Source.PLAYER),
			particleSize = 0.25f,
			soundRange = 50.0,
			magazineIdentifier = "STANDARD_MAGAZINE",
			refillType = "minecraft:lapis_lazuli",
		),
		val rifle: Singleshot = Singleshot(
			damage = 5.5,
			damageFalloffMultiplier = 0.0,
			capacity = 20,
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
			consumesAmmo = true,
			soundReloadStart = SoundInfo("horizonsend:blaster.rifle.reload.start", volume = 1f, source = Sound.Source.PLAYER),
			soundReloadFinish = SoundInfo("horizonsend:blaster.rifle.reload.finish", volume = 1f, source = Sound.Source.PLAYER),
			soundFire = SoundInfo("horizonsend:blaster.rifle.shoot", volume = 1f, source = Sound.Source.PLAYER),
			soundWhizz = SoundInfo("horizonsend:blaster.whizz.standard", volume = 1f, source = Sound.Source.PLAYER),
			soundShell = SoundInfo("horizonsend:blaster.rifle.shell", volume = 1f, source = Sound.Source.PLAYER),
			particleSize = 0.25f,
			soundRange = 50.0,
			magazineIdentifier = "STANDARD_MAGAZINE",
			refillType = "minecraft:lapis_lazuli",
		),
		val submachineBlaster: Singleshot = Singleshot(
			damage = 1.5,
			damageFalloffMultiplier = 0.0,
			capacity = 45,
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
			consumesAmmo = true,
			soundReloadStart = SoundInfo("horizonsend:blaster.submachine_blaster.reload.start", volume = 1f, source = Sound.Source.PLAYER),
			soundReloadFinish = SoundInfo("horizonsend:blaster.submachine_blaster.reload.finish", volume = 1f, source = Sound.Source.PLAYER),
			soundFire = SoundInfo("horizonsend:blaster.submachine_blaster.shoot", volume = 1f, source = Sound.Source.PLAYER),
			soundWhizz = SoundInfo("horizonsend:blaster.whizz.standard", volume = 1f, source = Sound.Source.PLAYER),
			soundShell = SoundInfo("horizonsend:blaster.submachine_blaster.shell", volume = 1f, source = Sound.Source.PLAYER),
			particleSize = 0.25f,
			soundRange = 50.0,
			magazineIdentifier = "STANDARD_MAGAZINE",
			refillType = "minecraft:lapis_lazuli",
		),
		val sniper: Singleshot = Singleshot(
			damage = 12.0,
			damageFalloffMultiplier = 30.0,
			capacity = 5,
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
			consumesAmmo = true,
			soundReloadStart = SoundInfo("horizonsend:blaster.sniper.reload.start", volume = 1f, source = Sound.Source.PLAYER),
			soundReloadFinish = SoundInfo("horizonsend:blaster.sniper.reload.finish", volume = 1f, source = Sound.Source.PLAYER),
			soundFire = SoundInfo("horizonsend:blaster.sniper.shoot", volume = 1f, source = Sound.Source.PLAYER),
			soundWhizz = SoundInfo("horizonsend:blaster.whizz.sniper", volume = 1f, source = Sound.Source.PLAYER),
			soundShell = SoundInfo("horizonsend:blaster.sniper.shell", volume = 1f, source = Sound.Source.PLAYER),
			particleSize = 0.5f,
			soundRange = 100.0,
			magazineIdentifier = "SPECIAL_MAGAZINE",
			refillType = "minecraft:emerald",
		),
		val shotgun: Multishot = Multishot(
			damage = 1.75,
			damageFalloffMultiplier = 0.25,
			delay = 0,
			capacity = 4,
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
			consumesAmmo = true,
			soundReloadStart = SoundInfo("horizonsend:blaster.shotgun.reload.start", volume = 1f, source = Sound.Source.PLAYER),
			soundReloadFinish = SoundInfo("horizonsend:blaster.shotgun.reload.finish", volume = 1f, source = Sound.Source.PLAYER),
			soundFire = SoundInfo("horizonsend:blaster.shotgun.shoot", volume = 1f, source = Sound.Source.PLAYER),
			soundWhizz = SoundInfo("horizonsend:blaster.whizz.standard", volume = 1f, source = Sound.Source.PLAYER),
			soundShell = SoundInfo("horizonsend:blaster.shotgun.shell", volume = 1f, source = Sound.Source.PLAYER),
			particleSize = 0.25f,
			soundRange = 50.0,
			magazineIdentifier = "SPECIAL_MAGAZINE",
			refillType = "minecraft:emerald",
		),

		val cannon: Singleshot = Singleshot(
			damage = 0.5,
			explosionPower = 4.0f,
			damageFalloffMultiplier = 0.0,
			capacity = 60,
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
			consumesAmmo = true,
			soundReloadStart = SoundInfo("horizonsend:blaster.cannon.reload.start", volume = 1f, source = Sound.Source.PLAYER),
			soundReloadFinish = SoundInfo("horizonsend:blaster.cannon.reload.finish", volume = 1f, source = Sound.Source.PLAYER),
			soundFire = SoundInfo("horizonsend:blaster.cannon.shoot", volume = 1f, source = Sound.Source.PLAYER),
			soundWhizz = SoundInfo("horizonsend:blaster.whizz.standard", volume = 1f, source = Sound.Source.PLAYER),
			soundShell = SoundInfo("horizonsend:blaster.sniper.shell", volume = 1f, source = Sound.Source.PLAYER),
			soundRange = 50.0,
			particleSize = 0.80f,
			magazineIdentifier = "STANDARD_MAGAZINE",
			explosiveShot = true,
			refillType = "minecraft:lapis_lazuli",
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
			override val capacity: Int,
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
			override val particleSize: Float,
			override val speed: Double,
			override val timeBetweenShots: Int,
			override val shotDeviation: Double,
			override val mobDamageMultiplier: Double,
			override val consumesAmmo: Boolean,
			override val displayDurability: Boolean = true,
			override val magazineIdentifier: String,
			override val refillType: String,

			override val soundRange: Double,
			override val soundReloadStart: SoundInfo,
			override val soundReloadFinish: SoundInfo,
			override val soundFire: SoundInfo,
			override val soundWhizz: SoundInfo,
			override val soundShell: SoundInfo,

			override val explosiveShot: Boolean = false
		) : Balancing()

		@Serializable
		data class Multishot(
			val shotCount: Int,
			val offsetMax: Double,
			val delay: Int,

			override val damage: Double,
			override val explosionPower: Float = 0f,
			override val damageFalloffMultiplier: Double,
			override val capacity: Int,
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
			override val particleSize: Float,
			override val speed: Double,
			override val timeBetweenShots: Int,
			override val shotDeviation: Double,
			override val mobDamageMultiplier: Double,
			override val consumesAmmo: Boolean,
			override val displayDurability: Boolean = true,
			override val magazineIdentifier: String,
			override val refillType: String,

			override val soundRange: Double,
			override val soundReloadStart: SoundInfo,
			override val soundReloadFinish: SoundInfo,
			override val soundFire: SoundInfo,
			override val soundWhizz: SoundInfo,
			override val soundShell: SoundInfo,

			override val explosiveShot: Boolean = false
		) : Balancing()

		@Serializable
		data class AmmoStorage(
			override val capacity: Int,
			override val refillType: String,
			override val ammoPerRefill: Int,
			override val displayDurability: Boolean = true
		) : AmmoStorageBalancing, AmmoLoaderUsable

		abstract class Balancing : ProjectileBalancing, AmmoStorageBalancing {
			abstract val magazineIdentifier: String
			abstract val packetsPerShot: Int
			abstract val pitch: Float
			abstract val recoil: Float
			abstract val reload: Int
			abstract val shouldAkimbo: Boolean
			abstract val timeBetweenShots: Int
			abstract val consumesAmmo: Boolean

			abstract val soundRange: Double
			abstract val soundFire: SoundInfo
			abstract val soundWhizz: SoundInfo
			abstract val soundShell: SoundInfo
			abstract val soundReloadStart: SoundInfo
			abstract val soundReloadFinish: SoundInfo
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
			val explosiveShot: Boolean
			val particleSize: Float
		}

		interface AmmoStorageBalancing : AmmoLoaderUsable {
			val capacity: Int
			val displayDurability: Boolean
		}

		interface AmmoLoaderUsable {
			val refillType: String
			val ammoPerRefill: Int
		}
	}
}
