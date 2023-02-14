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
	override val range: Double = IonServer.Ion.balancing.starshipWeapons.LaserCannon.range
	override val speed: Double = IonServer.Ion.balancing.starshipWeapons.LaserCannon.speed
	override val shieldDamageMultiplier: Int = IonServer.Ion.balancing.starshipWeapons.LaserCannon.shieldDamageMultiplier
	override val color: Color = Color.YELLOW
	override val thickness: Double = IonServer.Ion.balancing.starshipWeapons.LaserCannon.thickness
	override val particleThickness: Double = IonServer.Ion.balancing.starshipWeapons.LaserCannon.particleThickness
	override val explosionPower: Float = IonServer.Ion.balancing.starshipWeapons.LaserCannon.explosionPower
	override val volume: Int = IonServer.Ion.balancing.starshipWeapons.LaserCannon.volume
	override val pitch: Float = IonServer.Ion.balancing.starshipWeapons.LaserCannon.pitch
	override val soundName: String = IonServer.Ion.balancing.starshipWeapons.LaserCannon.soundName
}
