package net.starlegacy.feature.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.IonServer
import net.starlegacy.feature.starship.active.ActiveStarship
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class CannonLaserProjectile(
	starship: ActiveStarship,
	loc: Location,
	dir: Vector,
	shooter: Player?
) : LaserProjectile(starship, loc, dir, shooter) {
	override val range: Double = IonServer.balancing.starshipWeapons.LaserCannon.range
	override val speed: Double = IonServer.balancing.starshipWeapons.LaserCannon.speed
	override val shieldDamageMultiplier: Int = IonServer.balancing.starshipWeapons.LaserCannon.shieldDamageMultiplier
	override val color: Color = Color.YELLOW
	override val thickness: Double = IonServer.balancing.starshipWeapons.LaserCannon.thickness
	override val particleThickness: Double = IonServer.balancing.starshipWeapons.LaserCannon.particleThickness
	override val explosionPower: Float = IonServer.balancing.starshipWeapons.LaserCannon.explosionPower
	override val volume: Int = IonServer.balancing.starshipWeapons.LaserCannon.volume
	override val pitch: Float = IonServer.balancing.starshipWeapons.LaserCannon.pitch
	override val soundName: String = IonServer.balancing.starshipWeapons.LaserCannon.soundName
}
