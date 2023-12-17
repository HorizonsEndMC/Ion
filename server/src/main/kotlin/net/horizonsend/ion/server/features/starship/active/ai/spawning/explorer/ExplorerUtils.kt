package net.horizonsend.ion.server.features.starship.active.ai.spawning.explorer

import net.horizonsend.ion.server.configuration.AIShipConfiguration
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.active.ai.AIControllerFactories
import net.horizonsend.ion.server.features.starship.active.ai.AIControllerFactory
import net.horizonsend.ion.server.features.starship.active.ai.module.misc.SmackTalkModule
import net.horizonsend.ion.server.features.starship.active.ai.module.movement.CruiseModule
import net.horizonsend.ion.server.features.starship.active.ai.module.pathfinding.SteeringPathfindingModule
import net.horizonsend.ion.server.features.starship.active.ai.module.positioning.BasicPositioningModule
import net.horizonsend.ion.server.features.starship.active.ai.spawning.getLocationNear
import net.horizonsend.ion.server.features.starship.active.ai.spawning.getNonProtectedPlayer
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.component1
import net.horizonsend.ion.server.miscellaneous.utils.component2
import net.horizonsend.ion.server.miscellaneous.utils.component3
import net.horizonsend.ion.server.miscellaneous.utils.component4
import net.horizonsend.ion.server.miscellaneous.utils.distanceToVector
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Location
import org.bukkit.util.Vector
import kotlin.random.Random

val EXPLORER_LIGHT_CYAN = TextColor.fromHexString("#59E3D7")!!
val EXPLORER_MEDIUM_CYAN = TextColor.fromHexString("#3AA198")!!
val EXPLORER_DARK_CYAN = TextColor.fromHexString("#1F5651")!!

private val smackTalkList = arrayOf(
	text(""),
	text(""),
	text(""),
	text(""),
	text(""),
	text(""),
	text("")
)

val smackPrefix = text("Receiving transmission from civilian vessel: ", EXPLORER_LIGHT_CYAN)

// Privateer controllers passive, only becoming aggressive if fired upon
val explorerCruise = AIControllerFactories.registerFactory("EXPLORER_CRUISE") {
	setControllerTypeName("Starfighter")
	addLocationSupplier(cruiseEndpoint)

	setModuleBuilder {
		val builder = AIControllerFactory.Builder.ModuleBuilder()

		val positioning = builder.addModule("positioning", BasicPositioningModule(it, getLocationSupplier().invoke(it) ?: Location(it.getWorld(), 0.0, 0.0, 0.0)))
		val pathfinding = builder.addModule("pathfinding", SteeringPathfindingModule(it, positioning::findPositionVec3i))
		builder.addModule("movement", CruiseModule(it, pathfinding, pathfinding::getDestination, CruiseModule.ShiftFlightType.ALL, 256.0))
		builder.addModule("smackTalk", SmackTalkModule(it, smackPrefix, *smackTalkList))
			builder
	}

	build()
}

val bulwark = AIShipConfiguration.AIStarshipTemplate(
	identifier = "WAYFINDER",
	schematicName = "Wayfinder",
	miniMessageName = "<${EXPLORER_DARK_CYAN.asHexString()}>Wayfinder",
	type = StarshipType.AI_CORVETTE,
	controllerFactory = "EXPLORER_CRUISE",
	xpMultiplier = 0.5,
	creditReward = 100.0,
	manualWeaponSets = mutableSetOf(
		AIShipConfiguration.AIStarshipTemplate.WeaponSet(name = "main", engagementRangeMin = 0.0, engagementRangeMax = 350.0)
	),
	autoWeaponSets = mutableSetOf(
		AIShipConfiguration.AIStarshipTemplate.WeaponSet(name = "lt1", engagementRangeMin = 0.0, engagementRangeMax = 250.0),
		AIShipConfiguration.AIStarshipTemplate.WeaponSet(name = "tt1", engagementRangeMin = 250.0, engagementRangeMax = 550.0)
	)
)

val sriker = AIShipConfiguration.AIStarshipTemplate(
	identifier = "SRIKER",
	schematicName = "Sriker",
	miniMessageName = "<${EXPLORER_MEDIUM_CYAN.asHexString()}>Sriker",
	type = StarshipType.AI_GUNSHIP,
	controllerFactory = "EXPLORER_CRUISE",
	xpMultiplier = 0.5,
	creditReward = 100.0,
	manualWeaponSets = mutableSetOf(
		AIShipConfiguration.AIStarshipTemplate.WeaponSet(name = "manual", engagementRangeMin = 0.0, engagementRangeMax = 500.0)
	),
	autoWeaponSets = mutableSetOf(
		AIShipConfiguration.AIStarshipTemplate.WeaponSet(name = "auto", engagementRangeMin = 0.0, engagementRangeMax = 500.0)
	)
)

val nimble = AIShipConfiguration.AIStarshipTemplate(
	identifier = "NIMBLE",
	schematicName = "Nimble",
	miniMessageName = "<${EXPLORER_LIGHT_CYAN.asHexString()}>Nimble",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "EXPLORER_CRUISE",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

val cruiseEndpoint: (AIController) -> Location? = lambda@{ controller: AIController ->
	var iterations = 0
	val origin = controller.getCenter()
	val (world, originX, originY, originZ) = origin

	while (iterations < 15) {
		iterations++

		val endPointX = if (originX > 0) Random.nextDouble(-originX, 0.0) else Random.nextDouble(0.0, -originX)
		val endPointZ = if (originZ > 0) Random.nextDouble(-originZ, 0.0) else Random.nextDouble(0.0, -originZ)
		val endPoint = Vector(endPointX, originY, endPointZ)

		val planets = Space.getPlanets().filter { it.spaceWorld == world }.map { it.location.toVector() }

		val minDistance = planets.minOfOrNull {
			val direction = endPoint.clone().subtract(origin.toVector())

			distanceToVector(origin.toVector(), direction, it)
		}

		// If there are planets, and the distance to any of them along the path of travel is less than 500, discard
		if (minDistance != null && minDistance <= 500.0) continue

		return@lambda Location(world, endPointX, originY, endPointZ)
	}

	null
}

fun findExplorerSpawnLocation(configuration: AIShipConfiguration.AISpawnerConfiguration): Location? {
	// Get a random world based on the weight in the config
	val worldConfig = configuration.worldWeightedRandomList.random()
	val world = worldConfig.getWorld()

	val player = getNonProtectedPlayer(world) ?: return null

	var iterations = 0

	val border = world.worldBorder

	val planets = Space.getPlanets().filter { it.spaceWorld == world }.map { it.location.toVector() }

	// max 10 iterations
	while (iterations <= 15) {
		iterations++

		val loc = player.getLocationNear(configuration.minDistanceFromPlayer, configuration.maxDistanceFromPlayer)

		if (!border.isInside(loc)) continue

		if (planets.any { it.distanceSquared(loc.toVector()) <= 250000 }) continue

		return loc
	}

	return null
}

