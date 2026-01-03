package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.common.utils.miscellaneous.randomDouble
import net.horizonsend.ion.server.configuration.starship.ProbeBalancing.ProbeProjectileBalancing
import net.horizonsend.ion.server.features.client.display.modular.ItemDisplayContainer
import net.horizonsend.ion.server.features.client.display.teleportDuration
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.damage.DamageType
import org.bukkit.util.Vector

class ProbeProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager
) : LaserProjectile<ProbeProjectileBalancing>(source, name, loc, dir, shooter, DamageType.GENERIC) {
	override val color: Color = Color.ORANGE

	val item = ItemFactory.unStackableCustomItem("projectile/activated_arsenal_missile").construct()

	private val container = ItemDisplayContainer(
		source.getWorld(),
		4.0F,
		loc.toVector(),
		dir,
		item,
		interpolation = 2
	).apply {
		getEntity().transformationInterpolationDuration = 2
		getEntity().teleportDuration = 2
	}

	override fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double) {
		container.position = location.toVector()
		container.heading = direction.clone()
		container.update()

		/*for (lineLoc in oldLocation.alongVector(newLocation.toVector().subtract(oldLocation.toVector()), 5)) {
            lineLoc.world.spawnParticle(Particle.DUST, lineLoc.x, lineLoc.y, lineLoc.z, 1, 0.0, 0.0, 0.0, 0.0, Particle.DustOptions(color, 2f), true)
        }*/

		(0 until 2).forEach { _ ->
			val angle = Math.PI / 24
			val opposite = direction.clone().multiply(-1)
				.rotateAroundX(randomDouble(-angle, angle))
				.rotateAroundY(randomDouble(-angle, angle))
				.rotateAroundZ(randomDouble(-angle, angle))
			location.world.spawnParticle(
				Particle.LARGE_SMOKE,
				location,
				0,
				opposite.x,
				opposite.y,
				opposite.z,
				1.0,
				null,
				true
			)
		}
	}

	override fun onDespawn() {
		container.remove()
	}
}
