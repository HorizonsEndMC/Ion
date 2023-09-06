package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.Damager
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
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
	override val speed: Double = IonServer.balancing.starshipWeapons.pointDefence.speed
	override val shieldDamageMultiplier: Int = IonServer.balancing.starshipWeapons.pointDefence.shieldDamageMultiplier
	override val color: Color = Color.BLUE
	override val thickness: Double = IonServer.balancing.starshipWeapons.pointDefence.thickness
	override val particleThickness: Double = IonServer.balancing.starshipWeapons.pointDefence.particleThickness
	override val explosionPower: Float = IonServer.balancing.starshipWeapons.pointDefence.explosionPower
	override val volume: Int = IonServer.balancing.starshipWeapons.pointDefence.volume
	override val pitch: Float = IonServer.balancing.starshipWeapons.pointDefence.pitch
	override val soundName: String = IonServer.balancing.starshipWeapons.pointDefence.soundName
}
