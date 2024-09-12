package net.horizonsend.ion.server.features.ai.spawning.spawner

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.ai.AIControllerFactories
import net.horizonsend.ion.server.features.ai.configuration.AITemplate
import net.horizonsend.ion.server.features.ai.module.misc.FactionManagerModule
import net.horizonsend.ion.server.features.ai.module.targeting.ClosestTargetingModule
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.getLocationNear
import net.horizonsend.ion.server.miscellaneous.utils.weightedRandom
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Location

/**
 * This spawner is not ticked normally, it is not registered.
 *
 * This spawner is constructed on the fly for each ship that would implement reinforcement mechanics.
 **/
class ReinforcementSpawner(
	private val reinforced: AIController,
	private val reinforcementPool: List<AITemplate.SpawningInformationHolder>
) : AISpawner {
	override val identifier: String = "NULL"
	override var lastTriggered: Long = 0L
	override val pointChance: Double = 0.0
	override val pointThreshold: Int = Int.MAX_VALUE
	override var points: Int = 0

	override suspend fun triggerSpawn() {
		val starship = reinforcementPool.weightedRandom { it.probability }.template
		val loc = findSpawnLocation()
		val pilotName = (this.reinforced.modules["faction"] as? FactionManagerModule)?.faction?.getAvailableName() ?: text("Responding Vessel")

		spawnAIStarship(IonServer.slF4JLogger, starship, loc, createController(starship, pilotName)).await()
	}

	private fun findSpawnLocation(): Location {
		val origin = reinforced.getCenter().toLocation(reinforced.getWorld())
		val world = reinforced.getWorld()

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

	/**
	 * This method creates the controller for the spawned ship. It can be used to define the behavior of the vessel.
	 *
	 * @return A function used to create the controller for the starship
	 **/
	fun createController(template: AITemplate, pilotName: Component): (ActiveStarship) -> AIController {
		val factory = AIControllerFactories[template.behaviorInformation.controllerFactory]

		return { starship: ActiveStarship ->
			val controller = factory(starship, pilotName, template.starshipInfo.manualWeaponSets, template.starshipInfo.autoWeaponSets)

			controller.setColor(this.reinforced.getColor())
			controller.getModuleByType<ClosestTargetingModule>()?.maxRange = template.behaviorInformation.engagementRange

			template.behaviorInformation.additionalModules.forEach {
				controller.modules[it.name] = it.createModule(controller)
			}

			(this.reinforced.modules["faction"] as? FactionManagerModule)?.let {
				controller.modules["faction"] = FactionManagerModule(controller, it.faction)
			}

			controller
		}
	}
}
