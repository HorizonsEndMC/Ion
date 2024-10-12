package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.util.Vector

abstract class StickyParticleProjectile(
	starship: ActiveStarship?,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager
) : ParticleProjectile(starship, name, loc, dir, shooter) {
	var embeddedShip: ActiveStarship? = null

	override fun onImpactStarship(starship: ActiveStarship, impactLocation: Location) {
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
