package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
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
	override val balancing: StarshipWeapons.ProjectileBalancing = starship?.balancing?.weapons?.protonTorpedo ?: IonServer.starshipBalancing.nonStarshipFired.protonTorpedo

	override val range: Double = balancing.range
	override val speed: Double = balancing.speed
	override val shieldDamageMultiplier: Int = balancing.shieldDamageMultiplier
	override val color: Color = Color.fromRGB(255, 0, 255)
	override val particleThickness: Double = balancing.particleThickness
	override val explosionPower: Float = balancing.explosionPower
	override val maxDegrees: Double = balancing.maxDegrees
	override val volume: Int = balancing.volume
	override val pitch: Float = balancing.pitch
	override val soundName: String = balancing.soundName
}
