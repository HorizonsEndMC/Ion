package net.starlegacy.feature.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.IonServer
import net.starlegacy.feature.starship.active.ActiveStarship
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class TorpedoProjectile(
	starship: ActiveStarship?,
	loc: Location,
	dir: Vector,
	shooter: Player?,
	originalTarget: Vector,
	baseAimDistance: Int
) : TrackingLaserProjectile(starship, loc, dir, shooter, originalTarget, baseAimDistance) {
	override val range: Double = IonServer.balancing.starshipWeapons.ProtonTorpedo.range
	override val speed: Double = IonServer.balancing.starshipWeapons.ProtonTorpedo.speed
	override val shieldDamageMultiplier: Int = IonServer.balancing.starshipWeapons.ProtonTorpedo.shieldDamageMultiplier
	override val color: Color = Color.fromRGB(255, 0, 255)
	override val thickness: Double = IonServer.balancing.starshipWeapons.ProtonTorpedo.thickness
	override val particleThickness: Double = IonServer.balancing.starshipWeapons.ProtonTorpedo.particleThickness
	override val explosionPower: Float = IonServer.balancing.starshipWeapons.ProtonTorpedo.explosionPower
	override val maxDegrees: Double = IonServer.balancing.starshipWeapons.ProtonTorpedo.maxDegrees
	override val volume: Int = IonServer.balancing.starshipWeapons.ProtonTorpedo.volume
	override val pitch: Float = IonServer.balancing.starshipWeapons.ProtonTorpedo.pitch
	override val soundName: String = IonServer.balancing.starshipWeapons.ProtonTorpedo.soundName
}
