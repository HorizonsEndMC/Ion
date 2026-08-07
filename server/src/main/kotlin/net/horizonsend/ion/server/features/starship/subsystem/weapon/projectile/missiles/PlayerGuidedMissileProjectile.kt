package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.missiles

import net.horizonsend.ion.server.configuration.starship.StarshipTrackingProjectileBalancing
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import kotlin.random.Random

abstract class PlayerGuidedMissileProjectile<B : StarshipTrackingProjectileBalancing>(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	initialDir: Vector,
	override val balancing: B,
	shooter: Damager,
	face: BlockFace,
	originalTarget: Vector,
	baseAimDistance: Int
) : TrackingMissileProjectile<B>(source, name, loc, dir, initialDir, balancing, shooter, face, originalTarget, baseAimDistance) {
	val random = Random(System.currentTimeMillis())
	val randomOffset = Vector(random.nextDouble(-1.0, 1.0), random.nextDouble(-1.0, 1.0), random.nextDouble(-1.0, 1.0)).normalize().multiply(Math.random()*10.0)

	open val minimumSphereRadius = 15.0

	override fun calculateTarget(): Vector {
		//shooter should always be PlayerDamager here. However, in the case the player logs out, we continue in the same direction.
		val player = (shooter as? PlayerDamager)?.player ?: return initialDir.add(location.toVector())
		val sphereRadius = player.location.distance(location).plus(speed * delta).coerceIn(minimumSphereRadius,10000.0)

		val intercept = player.location.direction.multiply(sphereRadius).add(player.location.toVector()).add(randomOffset)

		return intercept
	}
}
