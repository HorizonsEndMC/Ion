package net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile

import fr.skytasul.guardianbeam.Laser
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.HitscanProjectile
import org.bukkit.Location
import org.bukkit.util.Vector

class CthulhuBeamProjectile(
	starship: ActiveStarship?,
	loc: Location,
	dir: Vector,
	shooter: Damager,
) : HitscanProjectile(starship, loc, dir, shooter) {
	override val balancing: StarshipWeapons.ProjectileBalancing = starship?.balancing?.weapons?.cthulhuBeam ?: IonServer.starshipBalancing.nonStarshipFired.cthulhuBeam
	override val range: Double = balancing.range
	override var speed: Double = balancing.speed
	override val shieldDamageMultiplier: Int = balancing.shieldDamageMultiplier
	override val explosionPower: Float = balancing.explosionPower
	override val volume: Int = balancing.volume
	override val pitch: Float = balancing.pitch
	override val soundName: String = balancing.soundName

	override fun drawBeam() {
		val laserEnd = loc.clone().add(dir.clone().multiply(range))
		Laser.CrystalLaser(loc, laserEnd, 5, -1).durationInTicks().apply { start(IonServer) }
	}
}
