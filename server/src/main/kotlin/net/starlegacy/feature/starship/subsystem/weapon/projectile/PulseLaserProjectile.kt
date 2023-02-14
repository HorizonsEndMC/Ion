package net.starlegacy.feature.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.IonServer
import net.starlegacy.feature.starship.active.ActiveStarship
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class PulseLaserProjectile(
	starship: ActiveStarship,
	loc: Location,
	dir: Vector,
	override val color: Color,
	shooter: Player?
) : LaserProjectile(starship, loc, dir, shooter) {
	override val range: Double = IonServer.Ion.balancing.starshipWeapons.PulseCannon.range
	override val speed: Double = IonServer.Ion.balancing.starshipWeapons.PulseCannon.speed
	override val shieldDamageMultiplier: Int = IonServer.Ion.balancing.starshipWeapons.PulseCannon.shieldDamageMultiplier
	override val thickness: Double = IonServer.Ion.balancing.starshipWeapons.PulseCannon.thickness
	override val particleThickness: Double = IonServer.Ion.balancing.starshipWeapons.PulseCannon.particleThickness
	override val explosionPower: Float = IonServer.Ion.balancing.starshipWeapons.PulseCannon.explosionPower
	override val volume: Int = IonServer.Ion.balancing.starshipWeapons.PulseCannon.volume
	override val pitch: Float = IonServer.Ion.balancing.starshipWeapons.PulseCannon.pitch
	override val soundName: String = IonServer.Ion.balancing.starshipWeapons.PulseCannon.soundName
}
