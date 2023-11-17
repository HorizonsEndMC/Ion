package net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.ArcedParticleProjectile
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.util.Vector

class FlamethrowerProjectile(
	starship: ActiveStarship?,
	loc: Location,
	dir: Vector,
	shooter: Damager
) : ArcedParticleProjectile(starship, loc, dir, shooter) {
	override val balancing: StarshipWeapons.ProjectileBalancing = starship?.balancing?.weapons?.flameThrower ?: IonServer.starshipBalancing.nonStarshipFired.flameThrower
	override val range: Double = balancing.range
	override var speed: Double = balancing.speed
	override val shieldDamageMultiplier: Int = balancing.shieldDamageMultiplier
	override val explosionPower: Float = balancing.explosionPower
	override val volume: Int = balancing.volume
	override val pitch: Float = balancing.pitch
	override val soundName: String = balancing.soundName

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
