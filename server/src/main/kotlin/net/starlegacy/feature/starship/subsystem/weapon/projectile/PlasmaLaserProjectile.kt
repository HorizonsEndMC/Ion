package net.starlegacy.feature.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.IonServer
import net.starlegacy.feature.starship.active.ActiveStarship
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Vector

val flagcolors = arrayOf(
	Color.fromRGB(255, 0, 24),
	Color.fromRGB(255, 165, 44),
	Color.fromRGB(255, 255, 65),
	Color.fromRGB(0, 128, 24),
	Color.fromRGB(0, 0, 249),
	Color.fromRGB(134, 0, 125)
)

// from left to right red - orange - yellow - green - blue - purple
class PlasmaLaserProjectile(
	starship: ActiveStarship,
	loc: Location,
	dir: Vector,
	shooter: Player?
) : LaserProjectile(starship, loc, dir, shooter) {
	var counter = 0
	override val range: Double = IonServer.balancing.starshipWeapons.PlasmaCannon.range
	override val speed: Double = IonServer.balancing.starshipWeapons.PlasmaCannon.speed
	override val shieldDamageMultiplier: Int = IonServer.balancing.starshipWeapons.PlasmaCannon.shieldDamageMultiplier
	override val color: Color
		get() = if (starship!!.rainbowtoggle) flagcolors.random() else starship.weaponColor
	override val thickness: Double = IonServer.balancing.starshipWeapons.PlasmaCannon.thickness
	override val particleThickness: Double = IonServer.balancing.starshipWeapons.PlasmaCannon.particleThickness
	override val explosionPower: Float = IonServer.balancing.starshipWeapons.PlasmaCannon.explosionPower
	override val volume: Int = IonServer.balancing.starshipWeapons.PlasmaCannon.volume
	override val soundName: String = IonServer.balancing.starshipWeapons.PlasmaCannon.soundName
}
