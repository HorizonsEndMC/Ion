package net.starlegacy.feature.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.IonServer
import net.starlegacy.feature.starship.active.ActiveStarship
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class PointDefenseLaserProjectile(
	starship: ActiveStarship?,
	loc: Location,
	dir: Vector,
	override val range: Double,
	shooter: Player?
) : LaserProjectile(starship, loc, dir, shooter) {
	override val speed: Double = IonServer.balancing.starshipWeapons.PointDefence.speed
	override val shieldDamageMultiplier: Int = IonServer.balancing.starshipWeapons.PointDefence.shieldDamageMultiplier
	override val color: Color = Color.BLUE
	override val thickness: Double = IonServer.balancing.starshipWeapons.PointDefence.thickness
	override val particleThickness: Double = IonServer.balancing.starshipWeapons.PointDefence.particleThickness
	override val explosionPower: Float = IonServer.balancing.starshipWeapons.PointDefence.explosionPower
	override val volume: Int = IonServer.balancing.starshipWeapons.PointDefence.volume
	override val pitch: Float = IonServer.balancing.starshipWeapons.PointDefence.pitch
	override val soundName: String = IonServer.balancing.starshipWeapons.PointDefence.soundName
}
