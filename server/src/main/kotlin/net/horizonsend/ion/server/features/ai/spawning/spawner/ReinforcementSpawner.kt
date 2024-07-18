package net.horizonsend.ion.server.features.ai.spawning.spawner

import net.horizonsend.ion.server.features.ai.AIControllerFactories
import net.horizonsend.ion.server.features.ai.configuration.AITemplate
import net.horizonsend.ion.server.features.ai.module.misc.FactionManagerModule
import net.horizonsend.ion.server.features.ai.module.targeting.ClosestTargetingModule
import net.horizonsend.ion.server.features.ai.spawning.formatLocationSupplier
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.SingleSpawn
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.WeightedShipSupplier
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.getLocationNear
import net.kyori.adventure.text.Component
import org.bukkit.Location
import java.util.function.Supplier

/**
 * This spawner is not ticked normally, it is not registered.
 *
 * This spawner is constructed on the fly for each ship that would implement reinforcement mechanics.
 **/
class ReinforcementSpawner(
	private val reinforced: AIController,
	reinforcementPool: List<AITemplate.SpawningInformationHolder>
) : AISpawner(
	"NULL",
	SingleSpawn(
		WeightedShipSupplier(*reinforcementPool.toTypedArray()),
		Supplier {
			return@Supplier formatLocationSupplier(
				reinforced.getWorld(),
				250.0,
				500.0
			).get()
		}
	)
) {
	override val pointChance: Double = 0.0
	override val pointThreshold: Int = Int.MAX_VALUE

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
