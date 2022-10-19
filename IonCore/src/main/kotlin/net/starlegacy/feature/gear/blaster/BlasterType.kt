package net.starlegacy.feature.gear.blaster

import net.starlegacy.feature.misc.CustomItems
import org.bukkit.Sound

enum class BlasterType(
	val item: CustomItems.BlasterItem,
	val speed: Double,
	val range: Int,
	val thickness: Double,
	val cooldown: Long,
	val power: Int,
	val damage: Double,
	val sound: String,
	val pitchBase: Double,
	val pitchRange: Double,
	val explosionPower: Float? = null
) {
	PISTOL(
		CustomItems.BLASTER_PISTOL,
		speed = 2100.0,
		range = 50,
		thickness = 0.1,
		cooldown = 300,
		power = 10,
		damage = 6.0,
		sound = Sound.ENTITY_FIREWORK_ROCKET_BLAST.name,
		pitchBase = 0.7,
		pitchRange = 0.2
	),
	RIFLE(
		CustomItems.BLASTER_RIFLE,
		speed = 1650.0,
		range = 75,
		thickness = 0.1,
		cooldown = 600,
		power = 15,
		damage = 10.0,
		sound = "laser",
		pitchBase = 0.7,
		pitchRange = 0.3
	),
	SNIPER(
		CustomItems.BLASTER_SNIPER,
		speed = 2100.0,
		range = 250,
		thickness = 0.2,
		cooldown = 2000,
		power = 100,
		damage = 20.0,
		sound = "laser",
		pitchBase = 0.6,
		pitchRange = 0.1
	),
	CANNON(
		CustomItems.BLASTER_CANNON,
		speed = 1450.0,
		range = 200,
		thickness = 0.2,
		cooldown = 500,
		power = 50,
		damage = 7.0,
		sound = Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST.name,
		pitchBase = 0.6,
		pitchRange = 0.1,
		explosionPower = 2.0f
	)
}
