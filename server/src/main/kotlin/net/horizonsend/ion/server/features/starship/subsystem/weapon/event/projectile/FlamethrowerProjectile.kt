package net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.controllers.Controller
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.ArcedParticleProjectile
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.util.Vector

class FlamethrowerProjectile(
	starship: ActiveStarship,
	loc: Location,
	dir: Vector,
	shooter: Controller?
) : ArcedParticleProjectile(starship, loc, dir, shooter) {
	override val range: Double = IonServer.balancing.starshipWeapons.flamethrower.range
	override var speed: Double = IonServer.balancing.starshipWeapons.flamethrower.speed
	override val shieldDamageMultiplier: Int = IonServer.balancing.starshipWeapons.flamethrower.shieldDamageMultiplier
	override val thickness: Double = IonServer.balancing.starshipWeapons.flamethrower.thickness
	override val explosionPower: Float = IonServer.balancing.starshipWeapons.flamethrower.explosionPower
	override val volume: Int = IonServer.balancing.starshipWeapons.flamethrower.volume
	override val pitch: Float = IonServer.balancing.starshipWeapons.flamethrower.pitch
	override val soundName: String = IonServer.balancing.starshipWeapons.flamethrower.soundName

	override val decelerationAmount: Double = 0.05
	override val gravityMultiplier: Double = 0.05

	companion object {
		private val fire = Material.FIRE.createBlockData()
	}

	override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {
		// val entity = loc.world.spawnEntity(Location(loc.world, x, y, z), EntityType.BLOCK_DISPLAY) as Display

		loc.world.spawnParticle(Particle.BLOCK_MARKER, x, y, z, 30, 0.5, 0.5, 0.5, 0.0, fire, force)
	}
}
