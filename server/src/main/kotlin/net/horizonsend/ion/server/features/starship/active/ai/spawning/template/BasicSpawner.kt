package net.horizonsend.ion.server.features.starship.active.ai.spawning.template

import net.horizonsend.ion.common.utils.text.HEColorScheme
import net.horizonsend.ion.common.utils.text.templateMiniMessage
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.AIShipConfiguration
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ai.AIControllerFactories
import net.horizonsend.ion.server.features.starship.active.ai.spawning.AISpawner
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.World
import java.util.function.Supplier

abstract class BasicSpawner(
	identifier: String,
	configurationSupplier: Supplier<AIShipConfiguration.AISpawnerConfiguration>
) : AISpawner(identifier, configurationSupplier) {
	// No specific conditions
	override fun spawningConditionsMet(world: World, x: Int, y: Int, z: Int): Boolean = true

	abstract fun findSpawnLocation(): Location?

	override suspend fun triggerSpawn() {
		val loc = findSpawnLocation() ?: return
		val (x, y, z) = Vec3i(loc)

		if (!spawningConditionsMet(loc.world, x, y, z)) return

		val (template, pilotName) = getStarshipTemplate(loc.world)

		val deferred = spawnAIStarship(template, loc, createController(template, pilotName))

		deferred.invokeOnCompletion {
			IonServer.server.sendMessage(
				templateMiniMessage(
				configuration.miniMessageSpawnMessage,
				paramColor = HEColorScheme.HE_LIGHT_GRAY,
				useQuotesAroundObjects = false,
				template.getName(),
				x,
				y,
				z,
				loc.world.name
			)
			)
		}
	}

	override fun createController(template: AIShipConfiguration.AIStarshipTemplate, pilotName: Component): (ActiveStarship) -> Controller {
		val factory = AIControllerFactories[template.controllerFactory]

		return { starship ->
			factory(
				starship,
				pilotName,
				template.manualWeaponSets,
				template.autoWeaponSets
			)
		}
	}
}
