package net.horizonsend.ion.server.configuration

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.configuration.starship.StarshipSounds.SoundInfo
import net.kyori.adventure.sound.Sound

@Serializable
data class PVPBalancingConfiguration(
	val blasterWeapons: BlasterWeapons = BlasterWeapons(),
	val meleeWeapons: MeleeWeapons = MeleeWeapons(),
	val throwables: Throwables = Throwables(),
	val consumables: Consumables = Consumables(),
	val armour: Armor = Armor()
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
	data class Consumables(
		val healthStim: ConsumableBalancing = ConsumableBalancing(
			modifierValue = 8.0,
			cooldownTicks = 5
		),
		val strengthStim: ConsumableBalancing = ConsumableBalancing(
			modifierValue = 1.0,
			cooldownTicks = 20
		),
		val emptySyringe: ConsumableBalancing = ConsumableBalancing(
			modifierValue = 0.0,
			cooldownTicks = 40
		)
	){
		@Serializable
		data class ConsumableBalancing(
			var modifierValue: Double,
			var cooldownTicks: Int,
		)
	}

	@Serializable
	data class MeleeWeapons(
		val energySwordBalancing: EnergySwordBalancing = EnergySwordBalancing(
			damage = 69.0, //WARNING: THIS NUMBER DOESNT WORK AND THE ATTACK DAMAGE IS DETERMINED IN THE ENERGY SWORD CLASS
			blockAmount = 200,
			blockRechargePerTick= 1.0,
			type = WeaponTypeEnum.MELEE
		),
		val energyGreatswordBalancing: EnergySwordBalancing = EnergySwordBalancing(
			damage = 6.0, //WARNING: THIS NUMBER DOESNT WORK AND THE ATTACK DAMAGE IS DETERMINED IN THE ENERGY GREATSWORD CLASS
			blockAmount = 300,
			blockRechargePerTick= 1.0,
			type = WeaponTypeEnum.MELEE
		)
	){
		@Serializable
		data class EnergySwordBalancing(
			var damage: Double,
			var blockAmount: Int,
			var blockRechargePerTick: Double,
			override val type: WeaponTypeEnum
		): BlasterWeapons.WeaponType
	}

	@Serializable
	data class Armor(
		val heavyPowerArmor: AttributeHolder = AttributeHolder(
			speed = 0.0,
			sneakSpeed = 0.0,
			scale = 0.0,
			entityReach = 0.0,
			blockReach = 0.0,
			armor = 5.25,
			toughness = 3.0,
			knockBackResistance = .1,
			power = 100000,
			powerConsumedPerSecond = 0,
			stepHeight = 0.0,
			maxHealth = 1.0,
			jumpStrength = 0.0,
			flyingSpeed = 0.0,
			canDoubleJump = true,
			canRocketBoot = true,
			gravity = 0.0,
			oxygenBonus = 0.0,
			waterMovementEfficiency = 0.0,
			maxPrimaryModules = 3,
			maxSecondaryModules = 0
		),
		val mediumPowerArmor: AttributeHolder = AttributeHolder(
			speed = 0.0,
			sneakSpeed = 0.0,
			scale = 0.0,
			entityReach = 0.0,
			blockReach = 0.0,
			armor = 5.0,
			toughness = 2.0,
			knockBackResistance = 0.1,
			power = 75000,
			powerConsumedPerSecond = 0,
			stepHeight = 0.0,
			maxHealth = 0.0,
			jumpStrength = 0.0,
			flyingSpeed = 0.0,
			canDoubleJump = true,
			canRocketBoot = true,
			gravity = 0.0,
			oxygenBonus = 0.0,
			waterMovementEfficiency = 0.0,
			maxPrimaryModules = 1,
			maxSecondaryModules = 1
		),
		val lightPowerArmor: AttributeHolder = AttributeHolder(
			speed = 0.0,
			sneakSpeed = 0.0,
			scale = 0.0,
			entityReach = 0.0,
			blockReach = 0.0,
			armor = 5.0,
			toughness = 2.0,
			knockBackResistance = 0.0,
			power = 50000,
			powerConsumedPerSecond = 0,
			stepHeight = 0.0,
			maxHealth = 0.0,
			jumpStrength = 0.0,
			flyingSpeed = 0.0,
			canDoubleJump = true,
			canRocketBoot = true,
			gravity = 0.0,
			oxygenBonus = 0.0,
			waterMovementEfficiency = 0.0,
			maxPrimaryModules = 2,
			maxSecondaryModules = 0
		),
	){
		@Serializable
		data class AttributeHolder(
			var speed: Double, //Percentage increase or decrease
			var sneakSpeed: Double, //Percentage increase or decrease
			var scale: Double, //Scalar add
			var entityReach: Double, //Scalar add
			var blockReach: Double, //Scalar add
			var armor: Double, //Scalar add
			var toughness: Double, //Scalar add
			var knockBackResistance: Double, //Scalar add x10, 0.2 here is 2 ingame
			var power: Int, //Pure number
			var powerConsumedPerSecond: Int,
			var stepHeight: Double,//Scalar add
			var maxHealth: Double,//Scalar add
			var jumpStrength: Double,//Scalar add
			var flyingSpeed: Double,//Scalar add
			var canDoubleJump: Boolean,//Boolean
			var canRocketBoot: Boolean,//Boolean
			var gravity: Double,//Scalar add
			var oxygenBonus: Double,//Scalar add
			var waterMovementEfficiency: Double,//Scalar add
			var maxPrimaryModules: Int,
			var maxSecondaryModules: Int
		)
	}

	@Serializable
	data class BlasterWeapons(
		val pistol: Singleshot = Singleshot(
			damage = 9.375,
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
			type = WeaponTypeEnum.TERTIARY,
			blockbreakAmount = 0.3,
			switchToTimeTicks = 0,
			shouldHaveCameraOverlay = false,
			cameraOverlay = "",
			zoomEffect = 0.0
		),
		val rifle: Singleshot = Singleshot(
			damage = 17.2,
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
			type = WeaponTypeEnum.SECONDARY,
			blockbreakAmount = 20.0,
			switchToTimeTicks = 0,
			shouldHaveCameraOverlay = false,
			cameraOverlay = "",
			zoomEffect = 0.0
		),
		val submachineBlaster: Singleshot = Singleshot(
			damage = 9.375,
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
			type = WeaponTypeEnum.SECONDARY,
			blockbreakAmount = 5.0,
			switchToTimeTicks = 0,
			shouldHaveCameraOverlay = false,
			cameraOverlay = "",
			zoomEffect = 0.0
		),
		val sniper: Singleshot = Singleshot(
			damage = 37.5,
			damageFalloffMultiplier = 30.0,
			capacity = 5,
			ammoPerRefill = 20,
			packetsPerShot = 3,
			pitch = 0f,
			range = 160.0,
			recoil = 2.5f,
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
			type = WeaponTypeEnum.PRIMARY,
			blockbreakAmount = 40.0,
			switchToTimeTicks = 5,
			shouldHaveCameraOverlay = true,
			cameraOverlay = "horizonsend:overlays/sniper_scope",
			zoomEffect = -0.9
		),
		val shotgun: Multishot = Multishot(
			damage = 5.0,
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
			type = WeaponTypeEnum.PRIMARY,
			blockbreakAmount = 10.0,
			switchToTimeTicks = 5,
			shouldHaveCameraOverlay = false,
			cameraOverlay = "",
			zoomEffect = 0.0
		),

		val cannon: Singleshot = Singleshot(
			damage = 1.4,
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
			type = WeaponTypeEnum.TERTIARY,
			blockbreakAmount = 0.0,
			switchToTimeTicks = 0,
			shouldHaveCameraOverlay = false,
			cameraOverlay = "",
			zoomEffect = 0.0
		),
		//KOTH weapons
		val revolver: Singleshot = Singleshot(
			damage = 20.0,
			damageFalloffMultiplier = 0.0,
			capacity = 6,
			ammoPerRefill = 20,
			packetsPerShot = 2,
			pitch = 1.0f,
			range = 100.0,
			recoil = 3.0f,
			reload = 10,
			shotSize = 0.5,
			shouldAkimbo = false,
			shouldBypassHitTicks = true,
			shouldHeadshot = true,
			shouldPassThroughEntities = false,
			speed = 10.0,
			timeBetweenShots = 8,
			shotDeviation = 0.0,
			mobDamageMultiplier = 1.0,
			consumesAmmo = true,
			soundReloadStart = SoundInfo("horizonsend:blaster.pistol.reload.start", volume = 1f, source = Sound.Source.PLAYER),
			soundReloadFinish = SoundInfo("horizonsend:blaster.pistol.reload.finish", volume = 1f, source = Sound.Source.PLAYER),
			soundFire = SoundInfo("horizonsend:blaster.pistol.shoot", volume = 1f, source = Sound.Source.PLAYER),
			soundWhizz = SoundInfo("horizonsend:blaster.whizz.standard", volume = 1f, source = Sound.Source.PLAYER),
			soundShell = SoundInfo("horizonsend:blaster.pistol.shell", volume = 1f, source = Sound.Source.PLAYER),
			particleSize = 0.5f,
			soundRange = 50.0,
			magazineIdentifier = "SPECIAL_MAGAZINE",
			refillType = "minecraft:emerald",
			type = WeaponTypeEnum.TERTIARY,
			blockbreakAmount = 10.0,
			switchToTimeTicks = 5,
			shouldHaveCameraOverlay = false,
			cameraOverlay = "",
			zoomEffect = 0.0
		),
		val lightMachineBlaster: Singleshot = Singleshot(
			damage = 20.0,
			damageFalloffMultiplier = 0.0,
			capacity = 90,
			ammoPerRefill = 20,
			packetsPerShot = 1,
			pitch = 2f,
			range = 150.0,
			recoil = 1.5f,
			reload = 60,
			shotSize = 0.25,
			shouldAkimbo = false,
			shouldBypassHitTicks = true,
			shouldHeadshot = true,
			shouldPassThroughEntities = false,
			speed = 8.0,
			timeBetweenShots = 2,
			shotDeviation = 0.05,
			mobDamageMultiplier = 2.0,
			consumesAmmo = true,
			soundReloadStart = SoundInfo("horizonsend:blaster.submachine_blaster.reload.start", volume = 1f, source = Sound.Source.PLAYER),
			soundReloadFinish = SoundInfo("horizonsend:blaster.submachine_blaster.reload.finish", volume = 1f, source = Sound.Source.PLAYER),
			soundFire = SoundInfo("horizonsend:blaster.submachine_blaster.shoot", volume = 1f, source = Sound.Source.PLAYER),
			soundWhizz = SoundInfo("horizonsend:blaster.whizz.standard", volume = 1f, source = Sound.Source.PLAYER),
			soundShell = SoundInfo("horizonsend:blaster.submachine_blaster.shell", volume = 1f, source = Sound.Source.PLAYER),
			particleSize = 0.5f,
			soundRange = 100.0,
			magazineIdentifier = "SPECIAL_MAGAZINE",
			refillType = "minecraft:emerald",
			type = WeaponTypeEnum.PRIMARY,
			blockbreakAmount = 5.0,
			switchToTimeTicks = 0,
			shouldHaveCameraOverlay = false,
			cameraOverlay = "",
			zoomEffect = 0.0
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
		),
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
			override var blockbreakAmount: Double,
			override var switchToTimeTicks: Int,

			override var soundRange: Double,
			override val soundReloadStart: SoundInfo,
			override val soundReloadFinish: SoundInfo,
			override val soundFire: SoundInfo,
			override val soundWhizz: SoundInfo,
			override val soundShell: SoundInfo,

			override var explosiveShot: Boolean = false,
			override val type: WeaponTypeEnum,


			override var shouldHaveCameraOverlay: Boolean,
			override var cameraOverlay: String,
			override var zoomEffect: Double,
			override var scopedInItemModel: String = "empty"
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
			override var blockbreakAmount: Double,
			override var switchToTimeTicks: Int,

			override var soundRange: Double,
			override val soundReloadStart: SoundInfo,
			override val soundReloadFinish: SoundInfo,
			override val soundFire: SoundInfo,
			override val soundWhizz: SoundInfo,
			override val soundShell: SoundInfo,

			override var explosiveShot: Boolean = false,
			override val type: WeaponTypeEnum,

			override var shouldHaveCameraOverlay: Boolean,
			override var cameraOverlay: String,
			override var zoomEffect: Double,
			override var scopedInItemModel: String = "empty"
		) : Balancing()

		@Serializable
		data class AmmoStorage(
			override var capacity: Int,
			override var refillType: String,
			override var ammoPerRefill: Int,
			override var displayDurability: Boolean = true
		) : AmmoStorageBalancing, AmmoLoaderUsable

		abstract class Balancing : ProjectileBalancing, AmmoStorageBalancing, WeaponType {
			abstract var magazineIdentifier: String
			abstract var packetsPerShot: Int
			abstract var pitch: Float
			abstract var recoil: Float
			abstract var reload: Int
			abstract var shouldAkimbo: Boolean
			abstract var timeBetweenShots: Int
			abstract var consumesAmmo: Boolean
			abstract var switchToTimeTicks: Int

			abstract var shouldHaveCameraOverlay: Boolean
			abstract var cameraOverlay: String
			abstract var zoomEffect: Double //NOTE any speed value change lower then -0.9 will make the player unable to move!
			abstract var scopedInItemModel: String

			abstract var soundRange: Double
			abstract val soundFire: SoundInfo
			abstract val soundWhizz: SoundInfo
			abstract val soundShell: SoundInfo
			abstract val soundReloadStart: SoundInfo
			abstract val soundReloadFinish: SoundInfo
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
			var blockbreakAmount: Double
		}

		interface AmmoStorageBalancing : AmmoLoaderUsable {
			var capacity: Int
			var displayDurability: Boolean
		}

		interface AmmoLoaderUsable {
			var refillType: String
			var ammoPerRefill: Int
		}

		interface WeaponType{
			val type: WeaponTypeEnum
		}
	}

	enum class WeaponTypeEnum{
		PRIMARY,
		SECONDARY,
		TERTIARY,
		MELEE
	}
}
