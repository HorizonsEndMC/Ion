package net.starlegacy.feature.starship.subsystem.weapon.projectile

import net.starlegacy.feature.starship.active.ActiveStarship
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Vector

abstract class ParticleProjectile(
	starship: ActiveStarship?,
	loc: Location,
	dir: Vector,
	shooter: Player?
) : SimpleProjectile(starship, loc, dir, shooter) {
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
