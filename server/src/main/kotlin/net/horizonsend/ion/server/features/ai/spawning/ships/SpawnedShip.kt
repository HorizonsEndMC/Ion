package net.horizonsend.ion.server.features.ai.spawning.ships

import kotlinx.coroutines.Deferred
import net.horizonsend.ion.server.features.ai.configuration.AITemplate
import net.horizonsend.ion.server.features.ai.spawning.spawner.spawnAIStarship
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.getRadialRandomPoint
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.util.Vector
import org.slf4j.Logger

interface SpawnedShip {
	val template: AITemplate
	var offset: Vector?
	var absoluteHeight: Double?

	fun createController(logger: Logger, starship: ActiveStarship): AIController
	fun getName(logger: Logger): Component

	fun spawn(
		logger: Logger,
		location: Location
	): Deferred<ActiveControlledStarship> {
		return spawnAIStarship(
			logger,
			template,
			location,
			{ createController(logger, it) }
		)
	}

	fun withRandomRadialOffset(minDistance: Double, maxDistance: Double, y: Double, absoluteHeight: Double? = null): SpawnedShip {
		val (x, z) = getRadialRandomPoint(minDistance, maxDistance)
		if (absoluteHeight != null) {
			offset = Vector(x, 0.0, z)
			this.absoluteHeight = absoluteHeight
		} else offset = Vector(x, y, z)
		return this
	}

	fun withDirectionalOffset(distance: Double, direction: Vector, absoluteHeight: Double? = null): SpawnedShip {
		if (absoluteHeight != null) {
			offset = direction.clone().normalize().multiply(distance).setY(0.0)
			this.absoluteHeight = absoluteHeight
		} else offset = direction.clone().normalize().multiply(distance)
		return this
	}
}
