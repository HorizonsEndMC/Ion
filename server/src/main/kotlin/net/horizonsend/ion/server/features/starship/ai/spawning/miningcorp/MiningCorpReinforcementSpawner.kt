package net.horizonsend.ion.server.features.starship.ai.spawning.miningcorp

import net.horizonsend.ion.common.utils.text.HEColorScheme
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.AISpawningConfiguration
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.ai.AIControllerFactories
import net.horizonsend.ion.server.features.starship.ai.AIControllerFactory
import net.horizonsend.ion.server.features.starship.ai.module.combat.StarfighterCombatModule
import net.horizonsend.ion.server.features.starship.ai.module.movement.CruiseModule
import net.horizonsend.ion.server.features.starship.ai.module.pathfinding.SteeringPathfindingModule
import net.horizonsend.ion.server.features.starship.ai.module.positioning.AxisStandoffPositioningModule
import net.horizonsend.ion.server.features.starship.ai.module.targeting.HighestDamagerTargetingModule
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
class MiningCorpReinforcementSpawner(
	private val controller: AIController
) : BasicSpawner("", IonServer.aiSpawningConfiguration.spawners::miningCorpReinforcementSpawner) {
	override val spawnMessage: Component? = null

	override fun findSpawnLocation(): Location {
		val origin = controller.getCenter()
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

	fun getHighestDamager() = controller.starship.damagers.maxBy { it.value.points.get() }.key

	override fun getStarshipTemplate(world: World): Pair<AISpawningConfiguration.AIStarshipTemplate, Component> {
		// If the value is null, it is trying to spawn a ship in a world that it is not configured for.=
		val tier = configuration.tiers.random()
		val shipIdentifier = tier.shipsWeightedList.random()
		val name = MiniMessage.miniMessage().deserialize(tier.namesWeightedList.random())

		return IonServer.aiSpawningConfiguration.getShipTemplate(shipIdentifier) to name
	}

	companion object {
		@Suppress("unused")
		val targetController = AIControllerFactories.registerFactory("MINING_CORP_REINFORCEMENTS") {
			setControllerTypeName("Starfighter")

			setModuleBuilder {
				val builder = AIControllerFactory.Builder.ModuleBuilder()

				val targeting = builder.addModule("targeting", HighestDamagerTargetingModule(it).apply { sticky = true })
				builder.addModule("combat", StarfighterCombatModule(it, targeting::findTarget))
				val positioning = builder.addModule("positioning", AxisStandoffPositioningModule(it, targeting::findTarget, 25.0))
				val pathfinding = builder.addModule("pathfinding", SteeringPathfindingModule(it, positioning::findPositionVec3i))
				builder.addModule(
					"movement",
					CruiseModule(
						it,
						pathfinding,
						pathfinding::getDestination,
						CruiseModule.ShiftFlightType.ALL,
						256.0
					)
				)

				builder
			}

			build()
		}

		val fighter1 = AISpawningConfiguration.AIStarshipTemplate(
			identifier = "REINFORCEMENTS",
			controllerFactory = "MINING_CORP_REINFORCEMENTS"
		) // Vestas for now

		val defaultConfiguration = AISpawningConfiguration.AISpawnerConfiguration(
			miniMessageSpawnMessage = "${MiningCorpSpawner.miningGuild}<${HEColorScheme.HE_MEDIUM_GRAY}> backup request acknowledged. {0} responding at {1}, {3}, in {4}",
			pointChance = 0.0,
			pointThreshold = Int.MAX_VALUE,
			minDistanceFromPlayer = 50.0,
			maxDistanceFromPlayer = 100.0,
			tiers = listOf(
				AISpawningConfiguration.AISpawnerTier(
					identifier = "REINFORCEMENTS",
					nameList = mapOf(
						"Pilot" to 2,
					),
					ships = mapOf(
						fighter1.identifier to 2,
					)
				)
			)
		)
	}
}
