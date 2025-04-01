package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.damage.DamageType
import org.bukkit.util.Vector

abstract class ParticleProjectile<out B : StarshipWeapons.StarshipProjectileBalancing>(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager,
	damageType: DamageType
) : SimpleProjectile<B>(source, name, loc, dir, shooter, damageType) {

	override fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double) {
		for (i in 0 until travel.toInt()) {
			val x = location.x + direction.x * i
			val y = location.y + direction.y * i
			val z = location.z + direction.z * i
			val force = i % 3 == 0
			spawnParticle(x, y, z, force)
		}
	}

	protected abstract fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean)
}
