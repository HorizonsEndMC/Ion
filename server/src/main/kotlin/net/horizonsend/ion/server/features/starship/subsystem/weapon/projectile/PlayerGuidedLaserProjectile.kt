package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.starship.StarshipTrackingProjectileBalancing
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.damage.DamageType
import org.bukkit.util.Vector
import kotlin.random.Random

abstract class PlayerGuidedLaserProjectile<B : StarshipTrackingProjectileBalancing>(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	open val initialDir: Vector,
	override val balancing: B,
	shooter: Damager,
	originalTarget: Vector,
	baseAimDistance: Int
) : TrackingLaserProjectile<B>(source, name, loc, dir, shooter, originalTarget, baseAimDistance, DamageType.GENERIC) {
	val random = Random(System.currentTimeMillis())
	val randomOffset = Vector(random.nextDouble(-1.0, 1.0), random.nextDouble(-1.0, 1.0), random.nextDouble(-1.0, 1.0)).normalize().multiply(Math.random()*3.5)

	override fun calculateTarget(): Vector {
		//shooter should always be PlayerDamager here. However, in the case the player logs out, we continue in the same direction.
		val player = (shooter as? PlayerDamager)?.player ?: return initialDir.add(location.toVector())

		val sphereRadius = player.location.distance(location).plus(speed*delta).coerceIn(30.0,10000.0)
		val intercept = player.location.direction.multiply(sphereRadius).add(player.location.toVector()).add(randomOffset)

		return intercept
	}
}
