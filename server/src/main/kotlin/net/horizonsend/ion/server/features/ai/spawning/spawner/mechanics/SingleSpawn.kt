package net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics

import net.horizonsend.ion.server.features.ai.AIControllerFactories
import net.horizonsend.ion.server.features.ai.spawning.spawner.spawnAIStarship
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.slf4j.Logger
import java.util.function.Supplier

class SingleSpawn(
	logger: Logger,
	private val shipPool: ShipSupplier,
	private val locationProvider: Supplier<Location?>,
	private val controllerModifier: AIController.() -> Unit = {},
	private val nameProvider: Supplier<Component>,
	private val callback: (ActiveControlledStarship) -> Unit = {}
) : SpawnerMechanic(logger) {
	override suspend fun trigger() {
		val template = shipPool.get()
		val spawnPoint = locationProvider.get() ?: return

		val factory = AIControllerFactories[template.behaviorInformation.controllerFactory]

		@Suppress("DeferredResultUnused")
		spawnAIStarship(
			logger,
			template,
			spawnPoint,
			{
				val controller = factory.invoke(it, nameProvider.get())
				controllerModifier.invoke(controller)

				controller
			},
			callback
		)
	}
}
