package net.starlegacy.feature.starship.subsystem.weapon.projectile

import net.starlegacy.feature.starship.active.ActiveStarship
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class PlasmaLaserProjectile(
	starship: ActiveStarship,
	loc: Location,
	dir: Vector,
	shooter: Player?
) : LaserProjectile(starship, loc, dir, shooter) {
	override val range: Double = 160.0
	override val speed: Double = 400.0
	override val shieldDamageMultiplier: Int = 2
	override val color: Color = starship.weaponColor
	override val thickness: Double = 0.3
	override val particleThickness: Double = .5
	override val explosionPower: Float = 2.5f
	override val volume: Int = 10
	override val soundName: String = "starship.weapon.plasma_cannon.shoot"
}