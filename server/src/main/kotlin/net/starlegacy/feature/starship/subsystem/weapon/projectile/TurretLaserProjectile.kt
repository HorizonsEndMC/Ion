package net.starlegacy.feature.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.features.starship.controllers.Controller
import net.starlegacy.feature.starship.active.ActiveStarship
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
	override val shieldDamageMultiplier: Double,
	override val soundName: String,
	shooter: Controller?
) : LaserProjectile(ship, loc, dir, shooter) {
	override val thickness: Double = 0.3
	override val volume: Int = (range / 16).toInt()
}
