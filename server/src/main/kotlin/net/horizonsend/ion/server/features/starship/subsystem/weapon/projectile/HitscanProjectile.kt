package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.kyori.adventure.text.Component
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.damage.DamageType
import org.bukkit.util.Vector

abstract class HitscanProjectile<out B : StarshipWeapons.StarshipProjectileBalancing>(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager,
	damageType: DamageType
) : SimpleProjectile<B>(source, name, loc, dir, shooter, damageType) {

	override fun tick() {
		val result = location.world.rayTrace(location, direction, range, FluidCollisionMode.NEVER, true, 0.1) { true }
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

