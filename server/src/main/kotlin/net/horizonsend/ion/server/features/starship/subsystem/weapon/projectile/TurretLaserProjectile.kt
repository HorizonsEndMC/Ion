package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.damage.DamageType
import org.bukkit.util.Vector

class TurretLaserProjectile(
	ship: ActiveStarship?,
	name: Component,
	loc: Location,
	dir: Vector,
	override val speed: Double,
	override val color: Color,
	override val range: Double,
	override val particleThickness: Double,
	override val explosionPower: Float,
	override val starshipShieldDamageMultiplier: Double,
	override val areaShieldDamageMultiplier: Double,
	override val soundName: String,
	override val balancing: StarshipWeapons.ProjectileBalancing,
	shooter: Damager
) : LaserProjectile(ship, name, loc, dir, shooter, DamageType.GENERIC) {

	override val volume: Int = (range / 16).toInt()
}
