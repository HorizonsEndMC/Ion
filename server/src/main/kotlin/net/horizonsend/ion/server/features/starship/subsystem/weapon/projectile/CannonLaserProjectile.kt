package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.Damager
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.util.Vector

class CannonLaserProjectile(
	starship: ActiveStarship,
	loc: Location,
	dir: Vector,
	shooter: Damager
) : LaserProjectile(starship, loc, dir, shooter) {
	override val range: Double = IonServer.balancing.starshipWeapons.laserCannon.range
	override val speed: Double = IonServer.balancing.starshipWeapons.laserCannon.speed
	override val shieldDamageMultiplier: Int = IonServer.balancing.starshipWeapons.laserCannon.shieldDamageMultiplier
	override val color: Color = Color.YELLOW
	override val thickness: Double = IonServer.balancing.starshipWeapons.laserCannon.thickness
	override val particleThickness: Double = IonServer.balancing.starshipWeapons.laserCannon.particleThickness
	override val explosionPower: Float = IonServer.balancing.starshipWeapons.laserCannon.explosionPower
	override val volume: Int = IonServer.balancing.starshipWeapons.laserCannon.volume
	override val pitch: Float = IonServer.balancing.starshipWeapons.laserCannon.pitch
	override val soundName: String = IonServer.balancing.starshipWeapons.laserCannon.soundName
}
