package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.miscellaneous.utils.gayColors
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.util.Vector

// from left to right red - orange - yellow - green - blue - purple
class PlasmaLaserProjectile(
	starship: ActiveStarship,
	loc: Location,
	dir: Vector,
	shooter: Damager
) : LaserProjectile(starship, loc, dir, shooter) {
	override val range: Double = IonServer.balancing.starshipWeapons.plasmaCannon.range
	override val speed: Double = IonServer.balancing.starshipWeapons.plasmaCannon.speed
	override val shieldDamageMultiplier: Int = IonServer.balancing.starshipWeapons.plasmaCannon.shieldDamageMultiplier
	override val color: Color get() = if (starship!!.rainbowToggle) gayColors.random() else starship.controller.color
	override val thickness: Double = IonServer.balancing.starshipWeapons.plasmaCannon.thickness
	override val particleThickness: Double = IonServer.balancing.starshipWeapons.plasmaCannon.particleThickness
	override val explosionPower: Float = IonServer.balancing.starshipWeapons.plasmaCannon.explosionPower
	override val volume: Int = IonServer.balancing.starshipWeapons.plasmaCannon.volume
	override val soundName: String = IonServer.balancing.starshipWeapons.plasmaCannon.soundName
}
