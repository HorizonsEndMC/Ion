package net.starlegacy.feature.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.IonServer
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.util.mcName
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Sound
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
	override val range: Double = IonServer.Ion.balancing.starshipWeapons.ProtonTorpedo.range
	override val speed: Double = IonServer.Ion.balancing.starshipWeapons.ProtonTorpedo.speed
	override val shieldDamageMultiplier: Int = IonServer.Ion.balancing.starshipWeapons.ProtonTorpedo.shieldDamageMultiplier
	override val color: Color = Color.fromRGB(255, 0, 255)
	override val thickness: Double = IonServer.Ion.balancing.starshipWeapons.ProtonTorpedo.thickness
	override val particleThickness: Double = IonServer.Ion.balancing.starshipWeapons.ProtonTorpedo.particleThickness
	override val explosionPower: Float = IonServer.Ion.balancing.starshipWeapons.ProtonTorpedo.explosionPower
	override val maxDegrees: Double = IonServer.Ion.balancing.starshipWeapons.ProtonTorpedo.maxDegrees
	override val volume: Int = IonServer.Ion.balancing.starshipWeapons.ProtonTorpedo.volume
	override val pitch: Float = IonServer.Ion.balancing.starshipWeapons.ProtonTorpedo.pitch
	override val soundName: String = IonServer.Ion.balancing.starshipWeapons.ProtonTorpedo.soundName
}
