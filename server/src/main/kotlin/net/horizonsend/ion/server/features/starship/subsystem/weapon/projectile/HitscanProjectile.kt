package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.util.Vector

abstract class HitscanProjectile(
	starship: ActiveStarship?,
	loc: Location,
	dir: Vector,
	shooter: Damager,
) : SimpleProjectile(starship, loc, dir, shooter) {

	override fun tick() {
		val result = loc.world.rayTrace(loc, dir, range, FluidCollisionMode.NEVER, true, 0.1) { true }
		drawBeam()

		if (result != null) {
			result.hitBlock?.let {
				tryImpact(result, it.location)
				return
			}

			result.hitEntity?.let {
				tryImpact(result, it.location)
				return
			}
		}
	}

	override fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double) {}

	abstract fun drawBeam()
}

