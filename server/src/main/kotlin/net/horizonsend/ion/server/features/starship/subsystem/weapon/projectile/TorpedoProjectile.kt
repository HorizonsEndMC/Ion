package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.Damager
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.util.Vector

class TorpedoProjectile(
    starship: ActiveStarship?,
    loc: Location,
    dir: Vector,
    shooter: Damager,
    originalTarget: Vector,
    baseAimDistance: Int
) : TrackingLaserProjectile(starship, loc, dir, shooter, originalTarget, baseAimDistance) {
	override val range: Double = IonServer.balancing.starshipWeapons.protonTorpedo.range
	override val speed: Double = IonServer.balancing.starshipWeapons.protonTorpedo.speed
	override val shieldDamageMultiplier: Int = IonServer.balancing.starshipWeapons.protonTorpedo.shieldDamageMultiplier
	override val color: Color = Color.fromRGB(255, 0, 255)
	override val thickness: Double = IonServer.balancing.starshipWeapons.protonTorpedo.thickness
	override val particleThickness: Double = IonServer.balancing.starshipWeapons.protonTorpedo.particleThickness
	override val explosionPower: Float = IonServer.balancing.starshipWeapons.protonTorpedo.explosionPower
	override val maxDegrees: Double = IonServer.balancing.starshipWeapons.protonTorpedo.maxDegrees
	override val volume: Int = IonServer.balancing.starshipWeapons.protonTorpedo.volume
	override val pitch: Float = IonServer.balancing.starshipWeapons.protonTorpedo.pitch
	override val soundName: String = IonServer.balancing.starshipWeapons.protonTorpedo.soundName
}
