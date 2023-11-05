package net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.controllers.Controller
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.ArcedParticleProjectile
import org.bukkit.Color
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
	override val range: Double = IonServer.balancing.starshipWeapons.flameThrower.range
	override var speed: Double = IonServer.balancing.starshipWeapons.flameThrower.speed
	override val shieldDamageMultiplier: Int = IonServer.balancing.starshipWeapons.flameThrower.shieldDamageMultiplier
	override val thickness: Double = IonServer.balancing.starshipWeapons.flameThrower.thickness
	override val explosionPower: Float = IonServer.balancing.starshipWeapons.flameThrower.explosionPower
	override val volume: Int = IonServer.balancing.starshipWeapons.flameThrower.volume
	override val pitch: Float = IonServer.balancing.starshipWeapons.flameThrower.pitch
	override val soundName: String = IonServer.balancing.starshipWeapons.flameThrower.soundName

	override val decelerationAmount: Double = 0.05
	override val gravityMultiplier: Double = 0.05

	companion object {
		val fire = Material.FIRE.createBlockData()
	}

	override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {
		// val entity = loc.world.spawnEntity(Location(loc.world, x, y, z), EntityType.BLOCK_DISPLAY) as Display

//		loc.world.spawnParticle(Particle.BLOCK_MARKER, x, y, z, 30, 0.5, 0.5, 0.5, 0.0, fire, force)

		val particle = Particle.REDSTONE
		val dustOptions = Particle.DustOptions(Color.GREEN, 100f)
		loc.world.spawnParticle(particle, x, y, z, 20, 1.0, 1.0, 1.0, 0.0, dustOptions, force)
	}
}
