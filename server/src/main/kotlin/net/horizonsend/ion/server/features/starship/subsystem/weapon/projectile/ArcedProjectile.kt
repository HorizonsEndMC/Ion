package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.starship.StarshipArcedProjectileBalancing
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.damage.DamageType
import org.bukkit.util.Vector

abstract class ArcedProjectile<T: StarshipArcedProjectileBalancing>(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager,
	damageType: DamageType
) : SimpleProjectile<T>(source, name, loc, dir, shooter, damageType) {
	val gravityMultiplier: Double get() = balancing.gravityMultiplier
	val decelerationAmount: Double get() = balancing.decelerationAmount
	override var speed: Double = balancing.speed

	override fun tick() {
		speed *= (1.0 - decelerationAmount)

		val oldY = direction.y
		direction.y = oldY - ((GRAVITY_ACCELERATION * gravityMultiplier) * delta)

		super.tick()
	}

	companion object {
		const val GRAVITY_ACCELERATION: Double = 9.81
	}
}
