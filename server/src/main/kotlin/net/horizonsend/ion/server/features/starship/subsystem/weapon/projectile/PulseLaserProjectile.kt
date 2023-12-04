package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.util.Vector

class PulseLaserProjectile(
	starship: ActiveStarship,
	loc: Location,
	dir: Vector,
	override val color: Color,
	shooter: Damager
) : LaserProjectile(starship, loc, dir, shooter) {
	override val range: Double = IonServer.balancing.starshipWeapons.pulseCannon.range
	override val speed: Double = IonServer.balancing.starshipWeapons.pulseCannon.speed
	override val shieldDamageMultiplier: Int = IonServer.balancing.starshipWeapons.pulseCannon.shieldDamageMultiplier
	override val thickness: Double = IonServer.balancing.starshipWeapons.pulseCannon.thickness
	override val particleThickness: Double = IonServer.balancing.starshipWeapons.pulseCannon.particleThickness
	override val explosionPower: Float = IonServer.balancing.starshipWeapons.pulseCannon.explosionPower
	override val volume: Int = IonServer.balancing.starshipWeapons.pulseCannon.volume
	override val pitch: Float = IonServer.balancing.starshipWeapons.pulseCannon.pitch
	override val soundName: String = IonServer.balancing.starshipWeapons.pulseCannon.soundName
}
