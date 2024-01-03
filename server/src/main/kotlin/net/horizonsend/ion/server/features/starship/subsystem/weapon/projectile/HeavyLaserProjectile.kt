package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.util.Vector

class HeavyLaserProjectile(
    starship: ActiveStarship?,
    loc: Location,
    dir: Vector,
    shooter: Damager,
    originalTarget: Vector,
    baseAimDistance: Int,
    sound: String
) : TrackingLaserProjectile(starship, loc, dir, shooter, originalTarget, baseAimDistance) {
	override val shieldDamageMultiplier = IonServer.balancing.starshipWeapons.heavyLaser.shieldDamageMultiplier
	override val maxDegrees: Double = IonServer.balancing.starshipWeapons.heavyLaser.maxDegrees
	override val range: Double = IonServer.balancing.starshipWeapons.heavyLaser.range
	override val speed: Double = IonServer.balancing.starshipWeapons.heavyLaser.speed
	override val color: Color = Color.RED
	override val thickness: Double = IonServer.balancing.starshipWeapons.heavyLaser.thickness
	override val particleThickness: Double = IonServer.balancing.starshipWeapons.heavyLaser.particleThickness
	override val explosionPower: Float = IonServer.balancing.starshipWeapons.heavyLaser.explosionPower
	override val soundName: String = sound
}
