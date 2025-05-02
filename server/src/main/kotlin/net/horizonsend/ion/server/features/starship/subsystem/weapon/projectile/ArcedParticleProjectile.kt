package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.damage.DamageType
import org.bukkit.util.Vector

abstract class ArcedParticleProjectile(
	starship: ActiveStarship?,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager,
	damageType: DamageType
) : ArcedProjectile(starship, name, loc, dir, shooter, damageType) {
	override fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double) {
		for (i in 0 until travel.toInt()) {
			val x = loc.x + dir.x * i
			val y = loc.y + dir.y * i
			val z = loc.z + dir.z * i
			val force = i % 3 == 0
			spawnParticle(x, y, z, force)
		}
	}

	protected abstract fun spawnParticle(x: Double, y: Double, z: Double, force: Boolean)
}
