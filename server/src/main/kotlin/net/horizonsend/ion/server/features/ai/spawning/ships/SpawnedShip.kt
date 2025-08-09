package net.horizonsend.ion.server.features.ai.spawning.ships

import net.horizonsend.ion.server.features.ai.configuration.AITemplate
import net.horizonsend.ion.server.features.ai.spawning.createAIShipFromTemplate
import net.horizonsend.ion.server.features.ai.util.AITarget
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
	var pilotName: Component?

	fun createController(logger: Logger, starship: ActiveStarship, difficulty: Int, targetMode: AITarget.TargetMode): AIController

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

fun SpawnedShip.spawn(
	logger: Logger,
	location: Location,
	difficulty: Int,
	targetMode: AITarget.TargetMode,
	modifyController: AIController.() -> Unit = {}
) {
	createAIShipFromTemplate(
		logger = logger,
		template = template,
		location = location,
		createController = {
			val controller = createController(logger, it, difficulty, targetMode)

			modifyController.invoke(controller)

			controller
		},
		suffix = getSuffix(difficulty)
	)
}
