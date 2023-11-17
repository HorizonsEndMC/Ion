package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.util.Vector

class PointDefenseLaserProjectile(
	starship: ActiveStarship?,
	loc: Location,
	dir: Vector,
	override val range: Double,
	shooter: Damager
) : LaserProjectile(starship, loc, dir, shooter) {
	override val balancing: StarshipWeapons.ProjectileBalancing = starship?.balancing?.weapons?.pointDefence ?: IonServer.starshipBalancing.nonStarshipFired.pointDefence
	override val speed: Double = balancing.speed
	override val shieldDamageMultiplier: Int = balancing.shieldDamageMultiplier
	override val color: Color = Color.BLUE
	override val particleThickness: Double = balancing.particleThickness
	override val explosionPower: Float = balancing.explosionPower
	override val volume: Int = balancing.volume
	override val pitch: Float = balancing.pitch
	override val soundName: String = balancing.soundName
}
