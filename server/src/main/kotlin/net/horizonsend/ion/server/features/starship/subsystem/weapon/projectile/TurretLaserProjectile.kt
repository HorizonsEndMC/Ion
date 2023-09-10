package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.util.Vector

class TurretLaserProjectile(
	ship: ActiveStarship?,
	loc: Location,
	dir: Vector,
	override val speed: Double,
	override val color: Color,
	override val range: Double,
	override val particleThickness: Double,
	override val explosionPower: Float,
	override val shieldDamageMultiplier: Int,
	override val soundName: String,
	shooter: Damager
) : LaserProjectile(ship, loc, dir, shooter) {
	override val thickness: Double = 0.3
	override val volume: Int = (range / 16).toInt()
}
