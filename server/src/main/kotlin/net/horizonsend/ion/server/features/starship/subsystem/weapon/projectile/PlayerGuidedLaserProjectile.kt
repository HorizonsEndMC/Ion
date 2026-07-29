package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.starship.StarshipTrackingProjectileBalancing
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.damage.DamageType
import org.bukkit.util.Vector

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

	override fun calculateTarget(): Vector {
		//shooter should always be PlayerDamager here. However, in the case the player logs out, we continue in the same direction.
		val player = (shooter as? PlayerDamager)?.player ?: return initialDir.add(location.toVector())
		val sphereRadius = ((System.nanoTime() - firedAtNanos)/1_000_000_000.0)*speed
		return player.location.direction.multiply(sphereRadius)
	}
}
