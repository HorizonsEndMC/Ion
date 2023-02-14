package net.starlegacy.feature.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.IonServer
import net.starlegacy.feature.starship.active.ActiveStarship
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class HeavyLaserProjectile(
	starship: ActiveStarship?,
	loc: Location,
	dir: Vector,
	shooter: Player?,
	originalTarget: Vector,
	baseAimDistance: Int,
	sound: String
) : TrackingLaserProjectile(starship, loc, dir, shooter, originalTarget, baseAimDistance) {
	override val shieldDamageMultiplier = IonServer.Ion.balancing.starshipWeapons.HeavyLaser.shieldDamageMultiplier
	override val maxDegrees: Double = IonServer.Ion.balancing.starshipWeapons.HeavyLaser.maxDegrees
	override val range: Double = IonServer.Ion.balancing.starshipWeapons.HeavyLaser.range
	override val speed: Double = IonServer.Ion.balancing.starshipWeapons.HeavyLaser.speed
	override val color: Color = Color.RED
	override val thickness: Double = IonServer.Ion.balancing.starshipWeapons.HeavyLaser.thickness
	override val particleThickness: Double = IonServer.Ion.balancing.starshipWeapons.HeavyLaser.particleThickness
	override val explosionPower: Float = IonServer.Ion.balancing.starshipWeapons.HeavyLaser.explosionPower
	override val soundName: String = sound
}
