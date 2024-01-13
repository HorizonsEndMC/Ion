package net.horizonsend.ion.server.features.starship.ai.spawning.privateer

import net.horizonsend.ion.server.configuration.AISpawningConfiguration
import net.horizonsend.ion.server.configuration.AISpawningConfiguration.AIStarshipTemplate.WeaponSet
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.ai.AIControllerFactories.registerFactory
import net.horizonsend.ion.server.features.starship.ai.AIControllerFactory
import net.horizonsend.ion.server.features.starship.ai.module.combat.StarfighterCombatModule
import net.horizonsend.ion.server.features.starship.ai.module.misc.FleeModule
import net.horizonsend.ion.server.features.starship.ai.module.movement.CruiseModule
import net.horizonsend.ion.server.features.starship.ai.module.pathfinding.SteeringPathfindingModule
import net.horizonsend.ion.server.features.starship.ai.module.positioning.AxisStandoffPositioningModule
import net.horizonsend.ion.server.features.starship.ai.module.positioning.StandoffPositioningModule
import net.horizonsend.ion.server.features.starship.ai.module.targeting.ClosestTargetingModule
import net.horizonsend.ion.server.features.starship.ai.spawning.findSpawnLocationNearPlayer
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Location

val PRIVATEER_LIGHTER_TEAL = TextColor.fromHexString("#48e596")!!
val PRIVATEER_LIGHT_TEAL = TextColor.fromHexString("#5DD097")!!
val PRIVATEER_MEDIUM_TEAL = TextColor.fromHexString("#79B698")!!
val PRIVATEER_DARK_TEAL = TextColor.fromHexString("#639f77")!!

// Privateer controllers passive, only becoming aggressive if fired upon
@Suppress("unused")
val privateerStarfighter = registerFactory("PRIVATEER_STARFIGHTER") {
	setControllerTypeName("Starfighter")
	setModuleBuilder {
		val builder = AIControllerFactory.Builder.ModuleBuilder()

		val targeting = builder.addModule("targeting", ClosestTargetingModule(it, 500.0, null).apply { sticky = false })
		builder.addModule("combat", StarfighterCombatModule(it, targeting::findTarget))

		val positioning = builder.addModule("positioning", AxisStandoffPositioningModule(it, targeting::findTarget, 25.0))
		val pathfinding = builder.addModule("pathfinding", SteeringPathfindingModule(it, positioning::findPosition))
		val flee = builder.addModule("flee", FleeModule(it, pathfinding::getDestination, targeting) { controller, _ -> controller.getMinimumShieldHealth() <= 0.2 }) // Flee if a shield reaches below 10%
		builder.addModule("movement", CruiseModule(it, pathfinding, flee, CruiseModule.ShiftFlightType.ALL, 256.0))

		builder
	}
	build()
}

// Privateer controllers passive, only becoming aggressive if fired upon
@Suppress("unused")
val privateerGunship = registerFactory("PRIVATEER_GUNSHIP") {
	setControllerTypeName("Starfighter")
	setModuleBuilder {
		val builder = AIControllerFactory.Builder.ModuleBuilder()

		val targeting = builder.addModule("targeting", ClosestTargetingModule(it, 500.0, null).apply { sticky = false })
		builder.addModule("combat", StarfighterCombatModule(it, targeting::findTarget))

		val positioning = builder.addModule("positioning", StandoffPositioningModule(it, targeting::findTarget, 55.0))
		val pathfinding = builder.addModule("pathfinding", SteeringPathfindingModule(it, positioning::findPosition))
		val flee = builder.addModule("flee", FleeModule(it, pathfinding::getDestination, targeting) { controller, _ -> controller.getMinimumShieldHealth() <= 0.2 }) // Flee if a shield reaches below 10%
		builder.addModule("movement", CruiseModule(it, pathfinding, flee, CruiseModule.ShiftFlightType.ALL, 256.0))

		builder
	}
	build()
}

// Privateer controllers passive, only becoming aggressive if fired upon
@Suppress("unused")
val privateerCorvette = registerFactory("PRIVATEER_CORVETTE") {
	setControllerTypeName("Corvette")
	setModuleBuilder {
		val builder = AIControllerFactory.Builder.ModuleBuilder()

		val targeting = builder.addModule("targeting", ClosestTargetingModule(it, 500.0, null).apply { sticky = false })
		builder.addModule("combat", StarfighterCombatModule(it, targeting::findTarget))

		val positioning = builder.addModule("positioning", StandoffPositioningModule(it, targeting::findTarget, 55.0))
		val pathfinding = builder.addModule("pathfinding", SteeringPathfindingModule(it, positioning::findPosition))
		val flee = builder.addModule("flee", FleeModule(it, pathfinding::getDestination, targeting) { controller, _ -> controller.getMinimumShieldHealth() <= 0.2 }) // Flee if a shield reaches below 10%
		builder.addModule("movement", CruiseModule(it, pathfinding, flee, CruiseModule.ShiftFlightType.ALL, 256.0))

		builder
	}
	build()
}

private val PRIVATEER_SMACK_PREFIX: String = "<${PRIVATEER_LIGHTER_TEAL.asHexString()}>Receiving transmission from privateer vessel"

private fun basicPrivateerTemplate(
	identifier: String,
	schematicName: String,
	type: StarshipType,
	controllerFactory: String,
	manualWeaponSets: MutableSet<WeaponSet> = mutableSetOf(),
	autoWeaponSets: MutableSet<WeaponSet> = mutableSetOf(),
	engagementRadius: Double = 500.0,
): AISpawningConfiguration.AIStarshipTemplate {
	val creditRewards = when (type) {
		StarshipType.AI_CORVETTE -> 2650.0
		StarshipType.AI_GUNSHIP -> 1850.0
		StarshipType.AI_STARFIGHTER -> 950.0
		else -> 0.0
	}

	return AISpawningConfiguration.AIStarshipTemplate(
		identifier = identifier,
		schematicName = schematicName,

		miniMessageName = "<${PRIVATEER_DARK_TEAL.asHexString()}>$schematicName",
		color = PRIVATEER_LIGHT_TEAL.value(),

		type = type,

		controllerFactory = controllerFactory,

		xpMultiplier = 0.6,
		creditReward = creditRewards,

		maxSpeed = 20,
		engagementRange = engagementRadius,

		manualWeaponSets = manualWeaponSets,
		autoWeaponSets = autoWeaponSets,

		mobs = mutableSetOf(),
		smackInformation = AISpawningConfiguration.AIStarshipTemplate.SmackInformation(
			prefix = PRIVATEER_SMACK_PREFIX,
			messages = listOf()
		),
		radiusMessageInformation = AISpawningConfiguration.AIStarshipTemplate.RadiusMessageInformation(
			prefix = PRIVATEER_SMACK_PREFIX,
			messages = mapOf(
				engagementRadius * 1.5 to "<#FFA500>You are entering restricted airspace. If you hear this transmission, turn away immediately or you will be fired upon.",
				engagementRadius to "<RED>You have violated restricted airspace. Your vessel will be fired upon."
			)
		)
	)
}

val bulwark = basicPrivateerTemplate(
	identifier = "BULWARK",
	schematicName = "Bulwark",
	type = StarshipType.AI_GUNSHIP,
	controllerFactory = "PRIVATEER_GUNSHIP",
	manualWeaponSets = mutableSetOf(
		WeaponSet(name = "main", engagementRangeMin = 0.0, engagementRangeMax = 550.0)
	),
	autoWeaponSets = mutableSetOf(
		WeaponSet(name = "auto", engagementRangeMin = 0.0, engagementRangeMax = 250.0),
		WeaponSet(name = "tt", engagementRangeMin = 250.0, engagementRangeMax = 550.0)
	)
)

val contractor = basicPrivateerTemplate(
	identifier = "CONTRACTOR",
	schematicName = "Contractor",
	type = StarshipType.AI_GUNSHIP,
	controllerFactory = "PRIVATEER_GUNSHIP",
	manualWeaponSets = mutableSetOf(
		WeaponSet(name = "manual", engagementRangeMin = 0.0, engagementRangeMax = 500.0)
	),
	autoWeaponSets = mutableSetOf(
		WeaponSet(name = "auto", engagementRangeMin = 0.0, engagementRangeMax = 500.0)
	)
)

val dagger = basicPrivateerTemplate(
	identifier = "DAGGER",
	schematicName = "Dagger",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "PRIVATEER_STARFIGHTER"
)

val daybreak = basicPrivateerTemplate(
	identifier = "DAYBREAK",
	schematicName = "Daybreak",
	type = StarshipType.AI_CORVETTE,
	controllerFactory = "PRIVATEER_CORVETTE"
)

val patroller = basicPrivateerTemplate(
	identifier = "PATROLLER",
	schematicName = "Patroller",
	type = StarshipType.AI_GUNSHIP,
	controllerFactory = "PRIVATEER_GUNSHIP"
)

val protector = basicPrivateerTemplate(
	identifier = "PROTECTOR",
	schematicName = "Protector",
	type = StarshipType.AI_GUNSHIP,
	controllerFactory = "PRIVATEER_GUNSHIP"
)

val veteran = basicPrivateerTemplate(
	identifier = "VETERAN",
	schematicName = "Veteran",
	type = StarshipType.AI_GUNSHIP,
	controllerFactory = "PRIVATEER_GUNSHIP"
)

val teneta = basicPrivateerTemplate(
	identifier = "TENETA",
	schematicName = "Teneta",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "PRIVATEER_STARFIGHTER"
)

val furious = basicPrivateerTemplate(
	identifier = "FURIOUS",
	schematicName = "Furious",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "PRIVATEER_STARFIGHTER"
)

val inflict = basicPrivateerTemplate(
	identifier = "INFLICT",
	schematicName = "Inflict",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "PRIVATEER_STARFIGHTER"
)

val privateerTemplates = arrayOf(
	bulwark,
	contractor,
	dagger,
	daybreak,
	patroller,
	protector,
	veteran,
	teneta,
	furious,
	inflict
)

fun findPrivateerSpawnLocation(configuration: AISpawningConfiguration.AISpawnerConfiguration): Location?  {
	val nearPlayer = findSpawnLocationNearPlayer(configuration) ?: return null

//	val world: World = nearPlayer.world

	return nearPlayer
}
