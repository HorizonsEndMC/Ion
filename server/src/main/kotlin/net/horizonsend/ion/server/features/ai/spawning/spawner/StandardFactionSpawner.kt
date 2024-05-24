package net.horizonsend.ion.server.features.ai.spawning.spawner

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.ai.AIControllerFactories
import net.horizonsend.ion.server.features.ai.configuration.AITemplate
import net.horizonsend.ion.server.features.ai.configuration.WorldSettings
import net.horizonsend.ion.server.features.ai.faction.AIFaction
import net.horizonsend.ion.server.features.ai.module.misc.FactionManagerModule
import net.horizonsend.ion.server.features.ai.module.targeting.ClosestTargetingModule
import net.horizonsend.ion.server.features.ai.spawning.isSystemOccupied
import net.horizonsend.ion.server.features.misc.NewPlayerProtection.hasProtection
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.space.SpaceWorlds
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getLocationNear
import net.horizonsend.ion.server.miscellaneous.utils.weightedRandom
import net.horizonsend.ion.server.miscellaneous.utils.weightedRandomOrNull
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Player
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * A standard AI spawner, spawns ships one at a time
 **/
class StandardFactionSpawner(
	val faction: AIFaction,

	/** 0: x, 1: y, 2: z, 3: world name, */
	private val spawnMessage: Component,

	override val pointChance: Double,
	override val pointThreshold: Int,

	val worlds: List<WorldSettings>,
) : AISpawner {
	override val identifier = "${faction.identifier}_BASIC_SPAWNER"

	private val logger: Logger = LoggerFactory.getLogger(javaClass)

	override var lastTriggered: Long = System.currentTimeMillis()
	override var points: Int = 0

	override suspend fun triggerSpawn() {
		val (worldSettings, loc) = findSpawnLocationNearPlayer() ?: return logger.warn("Could not find spawn location!")
		val (x, y, z) = Vec3i(loc)

//		spawnMessage?.let {
//			Notify.chatAndGlobal(template(
//				message = it,
//				paramColor = HEColorScheme.HE_LIGHT_GRAY,
//				useQuotesAroundObjects = false,
//				x,
//				y,
//				z,
//				loc.world.name
//			))
//		}

		val (template, pilotName) = getStarshipTemplateForWorld(worldSettings)
		spawnAIStarship(logger, template, loc, createController(template, pilotName)).await()

		IonServer.server.sendMessage(template(
			message = spawnMessage,
			paramColor = HEColorScheme.HE_LIGHT_GRAY,
			useQuotesAroundObjects = false,
			template.starshipInfo.componentName(),
			x,
			y,
			z,
			loc.world.name
		))
	}

	/** Selects a starship template off of the configuration, picks, and serializes a name */
	fun getStarshipTemplateForWorld(worldConfig: WorldSettings): Pair<AITemplate, Component> {
		// If the value is null, it is trying to spawn a ship in a world that it is not configured for.
		val template = worldConfig.templates.weightedRandom { it.probability }.template
		val name = faction.getAvailableName()

		return template to name
	}

	private fun findSpawnLocationNearPlayer(
		playerFilter: (Player) -> Boolean = { !it.hasProtection() && SpaceWorlds.contains(it.world) }
	): Pair<WorldSettings, Location>?  {
		// Get a random world based on the weight in the config
		val occupiedWorlds = worlds.filter { isSystemOccupied(it.getWorld()) }
		val worldConfig = occupiedWorlds.weightedRandomOrNull { it.probability } ?: return null
		val bukkitWorld = worldConfig.getWorld()

		val player = bukkitWorld.players
			.filter { player -> PilotedStarships.isPiloting(player) }
			.filter(playerFilter)
			.randomOrNull() ?: return null

		var iterations = 0

		val border = bukkitWorld.worldBorder

		val planets = Space.getPlanets().filter { it.spaceWorld == bukkitWorld }.map { it.location.toVector() }

		// max 10 iterations
		while (iterations <= 15) {
			iterations++

			val loc = player.location.getLocationNear(worldConfig.minDistanceFromPlayer, worldConfig.maxDistanceFromPlayer)

			if (!border.isInside(loc)) continue
			if (planets.any { it.distanceSquared(loc.toVector()) <= 250000 }) continue

			loc.y = 192.0

			return worldConfig to loc
		}

		return null
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

			controller.setColor(Color.fromRGB(faction.color))
			controller.getModuleByType<ClosestTargetingModule>()?.maxRange = template.behaviorInformation.engagementRange

			template.behaviorInformation.additionalModules.forEach {
				controller.modules[it.name] = it.createModule(controller)
			}

			controller.modules["faction"] = FactionManagerModule(controller, faction)

			controller
		}
	}
}
