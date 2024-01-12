package net.horizonsend.ion.server.features.starship.ai.spawning.explorer

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_GRAY
import net.horizonsend.ion.server.configuration.AISpawningConfiguration
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.ai.AIControllerFactories
import net.horizonsend.ion.server.features.starship.ai.AIControllerFactory
import net.horizonsend.ion.server.features.starship.ai.module.combat.DefensiveCombatModule
import net.horizonsend.ion.server.features.starship.ai.module.misc.FleeModule
import net.horizonsend.ion.server.features.starship.ai.module.misc.SmackTalkModule
import net.horizonsend.ion.server.features.starship.ai.module.movement.CruiseModule
import net.horizonsend.ion.server.features.starship.ai.module.pathfinding.SteeringPathfindingModule
import net.horizonsend.ion.server.features.starship.ai.module.positioning.BasicPositioningModule
import net.horizonsend.ion.server.features.starship.ai.module.targeting.HighestDamagerTargetingModule
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.distanceToVector
import net.horizonsend.ion.server.miscellaneous.utils.orNull
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor
import java.util.Optional
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

val smackPrefix = text("Receiving transmission from civilian vessel", EXPLORER_LIGHT_CYAN)

val cruiseEndpoint: (AIController) -> Optional<Vec3i> = lambda@{ controller: AIController ->
	var iterations = 0
	val origin = controller.getCenter()

	val world = controller.getWorld()
	val border = world.worldBorder

	val minX = (border.center.x - border.size).toInt()
	val minZ = (border.center.z - border.size).toInt()
	val maxX = (border.center.x + border.size).toInt()
	val maxZ = (border.center.z + border.size).toInt()

	while (iterations < 15) {
		iterations++

		val endPointX = Random.nextInt(minX, maxX)
		val endPointZ = Random.nextInt(minZ, maxZ)
		val endPoint = Vec3i(endPointX, origin.y, endPointZ)

		val planets = Space.getPlanets().filter { it.spaceWorld == world }.map { it.location.toVector() }

		val minDistance = planets.minOfOrNull {
			val direction = endPoint.minus(origin)

			distanceToVector(origin.toVector(), direction.toVector(), it)
		}

		// If there are planets, and the distance to any of them along the path of travel is less than 500, discard
		if (minDistance != null && minDistance <= 500.0) continue

		return@lambda Optional.of(endPoint)
	}

	Optional.empty()
}

// Privateer controllers passive, only becoming aggressive if fired upon
val explorerCruise = AIControllerFactories.registerFactory("EXPLORER_CRUISE") {
	setControllerTypeName("Starfighter")
	addLocationSupplier(cruiseEndpoint)

	setModuleBuilder {
		val builder = AIControllerFactory.Builder.ModuleBuilder()

		// Combat handling
		val targeting = builder.addModule("targeting", HighestDamagerTargetingModule(it))
		builder.addModule("combat", DefensiveCombatModule(it, targeting::findTarget))

		// Movement handling
		val positioning = builder.addModule("positioning", BasicPositioningModule(it, getLocationSupplier().invoke(it).orNull() ?: Vec3i(0, 0, 0)))
		val pathfinding = builder.addModule("pathfinding", SteeringPathfindingModule(it, positioning::findPosition))
		val flee = builder.addModule("flee", FleeModule(it, positioning::getDestination, targeting) { _, target -> target != null }) // Flee if there is a target found by the highest damage module
		builder.addModule("movement", CruiseModule(it, pathfinding, flee, CruiseModule.ShiftFlightType.ALL, 256.0))

		// Messaging
		builder.addModule("smackTalk", SmackTalkModule(it, smackPrefix, *smackTalkList))

		builder
	}

	build()
}

val wayfinder = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "WAYFINDER",
	schematicName = "Wayfinder",
	miniMessageName = "<${EXPLORER_MEDIUM_CYAN.asHexString()}>Wayfinder",
	type = StarshipType.AI_TRANSPORT,
	controllerFactory = "EXPLORER_CRUISE",
	xpMultiplier = 0.25,
	creditReward = 400.0,
	maxSpeed = 10,
	manualWeaponSets = mutableSetOf(
		AISpawningConfiguration.AIStarshipTemplate.WeaponSet(name = "main", engagementRangeMin = 0.0, engagementRangeMax = 350.0)
	),
	autoWeaponSets = mutableSetOf(
		AISpawningConfiguration.AIStarshipTemplate.WeaponSet(name = "lt1", engagementRangeMin = 0.0, engagementRangeMax = 250.0),
		AISpawningConfiguration.AIStarshipTemplate.WeaponSet(name = "tt1", engagementRangeMin = 250.0, engagementRangeMax = 550.0)
	)
)

val striker = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "STRIKER",
	schematicName = "Striker",
	miniMessageName = "<${EXPLORER_MEDIUM_CYAN.asHexString()}>Striker",
	type = StarshipType.AI_SHUTTLE,
	controllerFactory = "EXPLORER_CRUISE",
	xpMultiplier = 0.25,
	creditReward = 250.0,
	maxSpeed = 10,
	manualWeaponSets = mutableSetOf(
		AISpawningConfiguration.AIStarshipTemplate.WeaponSet(name = "manual", engagementRangeMin = 0.0, engagementRangeMax = 500.0)
	),
	autoWeaponSets = mutableSetOf(
		AISpawningConfiguration.AIStarshipTemplate.WeaponSet(name = "auto", engagementRangeMin = 0.0, engagementRangeMax = 500.0)
	)
)

val nimble = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "NIMBLE",
	schematicName = "Nimble",
	miniMessageName = "<${EXPLORER_MEDIUM_CYAN.asHexString()}>Nimble",
	type = StarshipType.AI_SHUTTLE,
	controllerFactory = "EXPLORER_CRUISE",
	xpMultiplier = 0.25,
	creditReward = 250.0,
	maxSpeed = 10
)

val dessle = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "DESSLE",
	schematicName = "Dessle",
	miniMessageName = "<${EXPLORER_MEDIUM_CYAN.asHexString()}>Dessle <${HE_LIGHT_GRAY.asHexString()}>Ore Transporter",
	type = StarshipType.AI_LIGHT_FREIGHTER,
	controllerFactory = "EXPLORER_CRUISE",
	xpMultiplier = 0.25,
	creditReward = 550.0,
	maxSpeed = 10,
	autoWeaponSets = mutableSetOf(
		AISpawningConfiguration.AIStarshipTemplate.WeaponSet(name = "lt", engagementRangeMin = 0.0, engagementRangeMax = 250.0),
	)
)

val minhaulCheth = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "MINHAUL_CHETHERITE",
	schematicName = "Minhaul_chetherite",
	miniMessageName = "<${EXPLORER_MEDIUM_CYAN.asHexString()}>Minhaul <${HE_LIGHT_GRAY.asHexString()}>[<light_purple>Chetherite<${HE_LIGHT_GRAY.asHexString()}>]",
	type = StarshipType.AI_SHUTTLE,
	controllerFactory = "EXPLORER_CRUISE",
	xpMultiplier = 0.25,
	creditReward = 250.0,
	maxSpeed = 10
)

val minhaulRedstone = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "MINHAUL_REDSTONE",
	schematicName = "Minhaul_redstone",
	miniMessageName = "<${EXPLORER_MEDIUM_CYAN.asHexString()}>Minhaul <${HE_LIGHT_GRAY.asHexString()}>[<red>Redstone<${HE_LIGHT_GRAY.asHexString()}>]",
	type = StarshipType.AI_SHUTTLE,
	controllerFactory = "EXPLORER_CRUISE",
	xpMultiplier = 0.25,
	creditReward = 250.0,
	maxSpeed = 10
)

val minhaulTitanium = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "MINHAUL_TITANIUM",
	schematicName = "Minhaul_titanium",
	miniMessageName = "<${EXPLORER_MEDIUM_CYAN.asHexString()}>Minhaul <${HE_LIGHT_GRAY.asHexString()}>[<gray>Titanium<${HE_LIGHT_GRAY.asHexString()}>]",
	type = StarshipType.AI_SHUTTLE,
	controllerFactory = "EXPLORER_CRUISE",
	xpMultiplier = 0.25,
	creditReward = 250.0,
	maxSpeed = 10
)

val exotranTitanium = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "EXOTRAN_TITANIUM",
	schematicName = "Exotran_titanium",
	miniMessageName = "<${EXPLORER_MEDIUM_CYAN.asHexString()}>Exotran <${HE_LIGHT_GRAY.asHexString()}>[<gray>Titanium<${HE_LIGHT_GRAY.asHexString()}>]",
	type = StarshipType.AI_LIGHT_FREIGHTER,
	controllerFactory = "EXPLORER_CRUISE",
	xpMultiplier = 0.25,
	creditReward = 550.0,
	maxSpeed = 10,
	autoWeaponSets = mutableSetOf(
		AISpawningConfiguration.AIStarshipTemplate.WeaponSet(name = "lt", engagementRangeMin = 0.0, engagementRangeMax = 250.0)
	)
)

val exotranChetherite = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "EXOTRAN_CHETHERITE",
	schematicName = "Exotran_chetherite",
	miniMessageName = "<${EXPLORER_MEDIUM_CYAN.asHexString()}>Exotran <${HE_LIGHT_GRAY.asHexString()}>[<light_purple>Chetherite<${HE_LIGHT_GRAY.asHexString()}>]",
	type = StarshipType.AI_LIGHT_FREIGHTER,
	controllerFactory = "EXPLORER_CRUISE",
	xpMultiplier = 0.25,
	creditReward = 550.0,
	maxSpeed = 10,
	autoWeaponSets = mutableSetOf(
		AISpawningConfiguration.AIStarshipTemplate.WeaponSet(name = "lt", engagementRangeMin = 0.0, engagementRangeMax = 250.0)
	)
)

val exotranRedstone = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "EXOTRAN_REDSTONE",
	schematicName = "Exotran_redstone",
	miniMessageName = "<${EXPLORER_MEDIUM_CYAN.asHexString()}>Exotran <${HE_LIGHT_GRAY.asHexString()}>[<red>Redstone<${HE_LIGHT_GRAY.asHexString()}>]",
	type = StarshipType.AI_LIGHT_FREIGHTER,
	controllerFactory = "EXPLORER_CRUISE",
	xpMultiplier = 0.25,
	creditReward = 550.0,
	maxSpeed = 10,
	autoWeaponSets = mutableSetOf(
		AISpawningConfiguration.AIStarshipTemplate.WeaponSet(name = "lt", engagementRangeMin = 0.0, engagementRangeMax = 250.0)
	)
)

val amph = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "AMPH",
	schematicName = "Amph",
	miniMessageName = "<${EXPLORER_MEDIUM_CYAN.asHexString()}>Amph",
	type = StarshipType.AI_TRANSPORT,
	controllerFactory = "EXPLORER_CRUISE",
	xpMultiplier = 0.25,
	creditReward = 400.0,
	maxSpeed = 10,
	autoWeaponSets = mutableSetOf()
)

val explorerTemplates = arrayOf(
	minhaulCheth,
	minhaulRedstone,
	minhaulTitanium,
	exotranChetherite,
	exotranRedstone,
	exotranTitanium,
	dessle,
	nimble,
	wayfinder,
	striker,
	amph,
)

