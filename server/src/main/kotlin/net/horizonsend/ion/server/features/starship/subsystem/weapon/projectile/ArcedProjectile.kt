package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.damage.DamageType
import org.bukkit.util.Vector

abstract class ArcedProjectile(
	starship: ActiveStarship?,
	name: Component,
	loc: Location,
	dir: Vector,
	shooter: Damager,
	damageType: DamageType
) : SimpleProjectile(starship, name, loc, dir, shooter, damageType) {
	abstract val gravityMultiplier: Double
	abstract val decelerationAmount: Double
	abstract override var speed: Double

	override fun tick() {
		speed *= (1.0 - decelerationAmount)

		val oldY = dir.y
		dir.y = oldY - ((GRAVITY_ACCELERATION * gravityMultiplier) * delta)

		super.tick()
	}

	companion object {
		const val GRAVITY_ACCELERATION: Double = 9.81
	}
}
