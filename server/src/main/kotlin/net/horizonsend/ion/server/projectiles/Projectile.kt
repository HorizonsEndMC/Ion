package net.horizonsend.ion.server.projectiles

import org.bukkit.Location
import org.bukkit.entity.Entity

abstract class Projectile {
	abstract val location: Location
	abstract val iterationsPerTick: Int
	abstract val shooter: Entity
	abstract val damage: Double
	abstract val shouldPassThroughEntities: Boolean
	abstract val size: Double
	abstract val shouldBypassHitTicks: Boolean
	abstract val distancePerIteration: Double

	abstract fun tick(): Boolean
}