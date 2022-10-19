package net.starlegacy.feature.starship.subsystem.weapon.projectile

import net.starlegacy.feature.starship.active.ActiveStarship
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Vector

val flagcolors = arrayOf(
	Color.fromRGB(255, 0, 24),
	Color.fromRGB(255, 165, 44),
	Color.fromRGB(255, 255, 65),
	Color.fromRGB(0, 128, 24),
	Color.fromRGB(0, 0, 249),
	Color.fromRGB(134, 0, 125)
)

//from left to right red - orange - yellow - green - blue - purple
class PlasmaLaserProjectile(
	starship: ActiveStarship,
	loc: Location,
	dir: Vector,
	shooter: Player?
) : LaserProjectile(starship, loc, dir, shooter) {
	var counter = 0
	override val range: Double = 160.0
	override val speed: Double = 400.0
	override val shieldDamageMultiplier: Int = 3
	override val color: Color
		get() = if (starship!!.rainbowtoggle) flagcolors.random() else starship.weaponColor
	override val thickness: Double = 0.3
	override val particleThickness: Double = .5
	override val explosionPower: Float = 4.0f
	override val volume: Int = 10
	override val soundName: String = "starship.weapon.plasma_cannon.shoot"
}
