package net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.controllers.Controller
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.StickyParticleProjectile
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.util.Vector

class PlagueCannonProjectile(
	starship: ActiveStarship?,
	loc: Location,
	dir: Vector,
	shooter: Controller?
) : StickyParticleProjectile(starship, loc, dir, shooter) {
	override val range: Double = IonServer.balancing.starshipWeapons.plagueCannon.range
	override var speed: Double = IonServer.balancing.starshipWeapons.flamethrower.speed
	override val shieldDamageMultiplier: Int = IonServer.balancing.starshipWeapons.plagueCannon.shieldDamageMultiplier
	override val thickness: Double = IonServer.balancing.starshipWeapons.plagueCannon.thickness
	override val explosionPower: Float = IonServer.balancing.starshipWeapons.plagueCannon.explosionPower
	override val volume: Int = IonServer.balancing.starshipWeapons.plagueCannon.volume
	override val pitch: Float = IonServer.balancing.starshipWeapons.plagueCannon.pitch
	override val soundName: String = IonServer.balancing.starshipWeapons.plagueCannon.soundName

	override fun tickEmbedded() {
		val embeddedShip = embeddedShip ?: return

		// ill finish later
	}

	override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {
		val particle = Particle.REDSTONE
		val dustOptions = DustOptions(Color.GREEN, thickness.toFloat())
		loc.world.spawnParticle(particle, x, y, z, 1, 0.0, 0.0, 0.0, 0.0, dustOptions, force)
	}
}
