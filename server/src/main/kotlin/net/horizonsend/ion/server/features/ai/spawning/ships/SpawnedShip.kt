package net.horizonsend.ion.server.features.ai.spawning.ships

import kotlinx.coroutines.currentCoroutineContext
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.features.ai.configuration.AITemplate
import net.horizonsend.ion.server.features.ai.spawning.PostSpawnBehaviorContext
import net.horizonsend.ion.server.features.ai.spawning.createAIShipFromTemplate
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRadialRandomPoint
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.util.Vector
import org.slf4j.Logger
import java.util.function.Supplier

interface SpawnedShip {
	val template: AITemplate
	val offsets: MutableList<Supplier<Vector>>
	var absoluteHeight: Double?
	var pilotName : Component?

	fun createController(logger: Logger, starship: ActiveStarship, difficulty: Int): AIController

	fun getName(difficulty: Int): Component

	fun getSuffix(difficulty: Int): String

	fun withRandomRadialOffset(minDistance: Double, maxDistance: Double, y: Double, absoluteHeight: Double? = null): SpawnedShip {
		this.absoluteHeight = absoluteHeight

		offsets.add(Supplier {
			val (x, z) = getRadialRandomPoint(minDistance, maxDistance)
			if (absoluteHeight != null) Vector(x, 0.0, z) else Vector(x, y, z)
		})

		return this
	}

	fun withDirectionalOffset(distance: Double, direction: Vector, absoluteHeight: Double? = null): SpawnedShip {
		this.absoluteHeight = absoluteHeight

		offsets.add(Supplier {
			if (absoluteHeight != null) direction.clone().normalize().multiply(distance).setY(0.0) else direction.clone().normalize().multiply(distance)
		})

		return this
	}
}

suspend fun SpawnedShip.spawn(
	logger: Logger,
	location: Location,
	difficulty: Int,
	modifyController: AIController.() -> Unit = {}
) {
	val context = currentCoroutineContext()
	createAIShipFromTemplate(
		logger,
		template,
		location,
		{
			val controller = createController(logger, it, difficulty)

			modifyController.invoke(controller)
			context[PostSpawnBehaviorContext]?.hook?.invoke(controller)

			controller
		},
		getSuffix(difficulty)
	)
}
