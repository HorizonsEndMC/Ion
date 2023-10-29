package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.controllers.Controller
import org.bukkit.Location
import org.bukkit.util.Vector

abstract class StickyProjectile(
	starship: ActiveStarship?,
	loc: Location,
	dir: Vector,
	shooter: Controller?
) : SimpleProjectile(starship, loc, dir, shooter) {
	var embeddedShip: ActiveStarship? = null

	override fun onImpactStarship(starship: ActiveStarship) {
		if (embeddedShip != null) return

		embeddedShip = starship
	}

	override fun tick() {
		if (embeddedShip == null) return super.tick()

		tickEmbedded()
		lastTick = System.nanoTime()
		reschedule()
	}

	abstract fun tickEmbedded()
}
