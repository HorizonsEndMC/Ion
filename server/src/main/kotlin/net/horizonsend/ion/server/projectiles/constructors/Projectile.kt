package net.horizonsend.ion.server.projectiles.constructors

import org.bukkit.Location
import org.bukkit.entity.Entity

abstract class Projectile {
	abstract val location: Location
	abstract val shooter: Entity
	abstract val shouldPassThroughEntities: Boolean
	abstract val size: Double
	var ticks: Int = 0

	abstract fun tick(): Boolean
}