package net.horizonsend.ion.server.features.starship.ai.spawning.miningcorp

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.AISpawningConfiguration
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.ai.module.targeting.SingleTargetingModule
import net.horizonsend.ion.server.features.starship.ai.module.targeting.TargetingModule
import net.horizonsend.ion.server.features.starship.ai.spawning.template.BasicSpawner
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.getLocationNear
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Location
import org.bukkit.World

/**
 * This spawner is not ticked normally, it is not registered.
 *
 * This spawner is constructed on the fly for each ship that would implement reinforcement mechanics.
 **/
class ReinforcementSpawner(
	private val controller: AIController,
	config: AISpawningConfiguration.AISpawnerConfiguration,
) : BasicSpawner("", { config }) {
	override val spawnMessage: Component? = null

	override fun findSpawnLocation(): Location {
		val origin = controller.getCenter().toLocation(controller.getWorld())
		val world = controller.getWorld()

		val planets = Space.getPlanets().filter { it.spaceWorld == world }.map { it.location.toVector() }

		var iterations = 0
		while (iterations <= 15) {
			iterations++

			val loc = origin.getLocationNear(250.0, 500.0)

			if (!world.worldBorder.isInside(loc)) continue

			if (planets.any { it.distanceSquared(loc.toVector()) <= 250000 }) continue

			return loc
		}

		return origin
	}

	override fun getStarshipTemplate(world: World): Pair<AISpawningConfiguration.AIStarshipTemplate, Component> {
		// If the value is null, it is trying to spawn a ship in a world that it is not configured for.=
		val tier = configuration.tiers.random()
		val shipIdentifier = tier.shipsWeightedList.random()
		val name = MiniMessage.miniMessage().deserialize(tier.namesWeightedList.random())

		return IonServer.aiSpawningConfiguration.getShipTemplate(shipIdentifier) to name
	}

	override fun createController(template: AISpawningConfiguration.AIStarshipTemplate, pilotName: Component): (ActiveStarship) -> AIController {
		return { starship ->
			val builtController = super.createController(template, pilotName)(starship)

			(controller.modules["targeting"] as? TargetingModule)?.findTarget()?.let {
				builtController.modules["targeting"] = SingleTargetingModule(builtController, it)
			}

			builtController
		}
	}
}
