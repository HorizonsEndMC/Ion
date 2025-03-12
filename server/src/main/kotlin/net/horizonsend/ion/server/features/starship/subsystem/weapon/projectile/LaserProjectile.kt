package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.damage.DamageType
import org.bukkit.util.Vector

abstract class LaserProjectile(
	starship: ActiveStarship?,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager,
	damageType: DamageType
) : ParticleProjectile(starship, name, loc, dir, shooter, damageType) {
	abstract val color: Color
	abstract val particleThickness: Double

	override fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean) {
		// val entity = loc.world.spawnEntity(Location(loc.world, x, y, z), EntityType.BLOCK_DISPLAY) as Display

		val particle = Particle.DUST
		val dustOptions = Particle.DustOptions(color, particleThickness.toFloat() * 4f)
		loc.world.spawnParticle(particle, x, y, z, 1, 0.0, 0.0, 0.0, 0.0, dustOptions, force)
	}
}
