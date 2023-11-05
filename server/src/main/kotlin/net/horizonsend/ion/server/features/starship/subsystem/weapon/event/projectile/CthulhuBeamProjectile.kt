package net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile

import fr.skytasul.guardianbeam.Laser
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.HitscanProjectile
import org.bukkit.Location
import org.bukkit.util.Vector

class CthulhuBeamProjectile(
	starship: ActiveStarship,
	loc: Location,
	dir: Vector,
	shooter: Damager,
) : HitscanProjectile(starship, loc, dir, shooter) {
	override val range: Double = IonServer.balancing.starshipWeapons.cthulhuBeam.range
	override var speed: Double = IonServer.balancing.starshipWeapons.cthulhuBeam.speed
	override val shieldDamageMultiplier: Int = IonServer.balancing.starshipWeapons.cthulhuBeam.shieldDamageMultiplier
	override val thickness: Double = IonServer.balancing.starshipWeapons.cthulhuBeam.thickness
	override val explosionPower: Float = IonServer.balancing.starshipWeapons.cthulhuBeam.explosionPower
	override val volume: Int = IonServer.balancing.starshipWeapons.cthulhuBeam.volume
	override val pitch: Float = IonServer.balancing.starshipWeapons.cthulhuBeam.pitch
	override val soundName: String = IonServer.balancing.starshipWeapons.cthulhuBeam.soundName

	override fun drawBeam() {
		val laserEnd = loc.clone().add(dir.clone().multiply(range))
		Laser.CrystalLaser(loc, laserEnd, 5, -1).durationInTicks().apply { start(IonServer) }
	}
}
