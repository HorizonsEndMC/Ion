package net.horizonsend.ion.server.configuration

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.configuration.starship.StarshipSounds.SoundInfo
import net.kyori.adventure.sound.Sound

@Serializable
data class PVPBalancingConfiguration(
	val energyWeapons: EnergyWeapons = EnergyWeapons(),
	val meleeWeapons: MeleeWeapons = MeleeWeapons(),
	val throwables: Throwables = Throwables()
) {
	@Serializable
	data class MeleeWeapons(
		var energySwordBalancing: MeleeWeaponBalancing = MeleeWeaponBalancing(
			damage = 6.5,
			speedUp = 0.0,
			attackSpeed = -2.4,
			knockback = 0.0,
			entityInteractionRange = 0.0,
			sneakingSpeed = 0.0,
			knockBackResistance = 0.0,
		)
	){
		@Serializable
		data class MeleeWeaponBalancing(
			var damage: Double, //addition
			var speedUp: Double, //multiplier
			var attackSpeed: Double, //addition
			var knockback: Double, //addition
			var entityInteractionRange: Double,//addition
			var sneakingSpeed: Double,//multiplier
			var knockBackResistance: Double,//addition
		)
	}

	@Serializable
	data class Throwables(
		val detonator: ThrowableBalancing = ThrowableBalancing(
			80.0,
			4.0,
			1.0,
			5,
			30,
			1,
			50,
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
			var damage: Double,
			var damageRadius: Double,
			var throwVelocityMultiplier: Double,
			var maxHealth: Int,
			var maxTicks: Int,
			var tickInterval: Long,
			var throwCooldownTicks: Int,
		)
	}

	@Serializable
	data class EnergyWeapons(
		val pistol: Singleshot = Singleshot(
			damage = 4.25,
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
			damage = 9.0,
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
			damage = 2.25,
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
			damage = 25.0,
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
			damage = 3.0,
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
			damage = 0.1,
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
			override var damage: Double,
			override var explosionPower: Float = 0f,
			override var damageFalloffMultiplier: Double,
			override var capacity: Int,
			override var ammoPerRefill: Int,
			override var packetsPerShot: Int,
			override var pitch: Float,
			override var range: Double,
			override var recoil: Float,
			override var reload: Int,
			override var shotSize: Double,
			override var shouldAkimbo: Boolean,
			override var shouldBypassHitTicks: Boolean,
			override var shouldHeadshot: Boolean,
			override var shouldPassThroughEntities: Boolean,
			override var particleSize: Float,
			override var speed: Double,
			override var timeBetweenShots: Int,
			override var shotDeviation: Double,
			override var mobDamageMultiplier: Double,
			override var consumesAmmo: Boolean,
			override var displayDurability: Boolean = true,
			override var magazineIdentifier: String,
			override var refillType: String,

			override var soundRange: Double,
			override var soundReloadStart: SoundInfo,
			override var soundReloadFinish: SoundInfo,
			override var soundFire: SoundInfo,
			override var soundWhizz: SoundInfo,
			override var soundShell: SoundInfo,

			override var explosiveShot: Boolean = false
		) : Balancing()

		@Serializable
		data class Multishot(
			var shotCount: Int,
			var offsetMax: Double,
			var delay: Int,

			override var damage: Double,
			override var explosionPower: Float = 0f,
			override var damageFalloffMultiplier: Double,
			override var capacity: Int,
			override var ammoPerRefill: Int,
			override var packetsPerShot: Int,
			override var pitch: Float,
			override var range: Double,
			override var recoil: Float,
			override var reload: Int,
			override var shotSize: Double,
			override var shouldAkimbo: Boolean,
			override var shouldBypassHitTicks: Boolean,
			override var shouldHeadshot: Boolean,
			override var shouldPassThroughEntities: Boolean,
			override var particleSize: Float,
			override var speed: Double,
			override var timeBetweenShots: Int,
			override var shotDeviation: Double,
			override var mobDamageMultiplier: Double,
			override var consumesAmmo: Boolean,
			override var displayDurability: Boolean = true,
			override var magazineIdentifier: String,
			override var refillType: String,

			override var soundRange: Double,
			override var soundReloadStart: SoundInfo,
			override var soundReloadFinish: SoundInfo,
			override var soundFire: SoundInfo,
			override var soundWhizz: SoundInfo,
			override var soundShell: SoundInfo,

			override var explosiveShot: Boolean = false
		) : Balancing()

		@Serializable
		data class AmmoStorage(
			override var capacity: Int,
			override var refillType: String,
			override var ammoPerRefill: Int,
			override var displayDurability: Boolean = true
		) : AmmoStorageBalancing, AmmoLoaderUsable

		abstract class Balancing : ProjectileBalancing, AmmoStorageBalancing {
			abstract var magazineIdentifier: String
			abstract var packetsPerShot: Int
			abstract var pitch: Float
			abstract var recoil: Float
			abstract var reload: Int
			abstract var shouldAkimbo: Boolean
			abstract var timeBetweenShots: Int
			abstract var consumesAmmo: Boolean

			abstract var soundRange: Double
			abstract var soundFire: SoundInfo
			abstract var soundWhizz: SoundInfo
			abstract var soundShell: SoundInfo
			abstract var soundReloadStart: SoundInfo
			abstract var soundReloadFinish: SoundInfo
		}

		interface ProjectileBalancing {
			var speed: Double
			var damage: Double
			var explosionPower: Float
			var damageFalloffMultiplier: Double
			var shouldPassThroughEntities: Boolean
			var shotSize: Double
			var shouldBypassHitTicks: Boolean
			var range: Double
			var shouldHeadshot: Boolean
			var mobDamageMultiplier: Double
			var shotDeviation: Double
			var explosiveShot: Boolean
			var particleSize: Float
		}

		interface AmmoStorageBalancing : AmmoLoaderUsable {
			var capacity: Int
			var displayDurability: Boolean
		}

		interface AmmoLoaderUsable {
			var refillType: String
			var ammoPerRefill: Int
		}
	}
}
