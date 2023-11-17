package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.miscellaneous.utils.gayColors
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.util.Vector

// from left to right red - orange - yellow - green - blue - purple
class PlasmaLaserProjectile(
	starship: ActiveStarship?,
	loc: Location,
	dir: Vector,
	shooter: Damager
) : LaserProjectile(starship, loc, dir, shooter) {
	override val balancing: StarshipWeapons.ProjectileBalancing = starship?.balancing?.weapons?.plasmaCannon ?: IonServer.starshipBalancing.nonStarshipFired.plasmaCannon
	override val range: Double = balancing.range
	override val speed: Double = balancing.speed
	override val shieldDamageMultiplier: Int = balancing.shieldDamageMultiplier
	override val color: Color get() = if (starship?.rainbowToggle == true) gayColors.random() else shooter.color
	override val particleThickness: Double = balancing.particleThickness
	override val explosionPower: Float = balancing.explosionPower
	override val volume: Int = balancing.volume
	override val soundName: String = balancing.soundName
}
