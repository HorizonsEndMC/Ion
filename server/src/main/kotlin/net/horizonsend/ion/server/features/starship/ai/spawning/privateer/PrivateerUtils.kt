package net.horizonsend.ion.server.features.starship.ai.spawning.privateer

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
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
import net.horizonsend.ion.server.features.starship.ai.module.targeting.TargetingModule
import net.kyori.adventure.text.format.TextColor

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

		val targetingOriginal = builder.addModule("targeting", ClosestTargetingModule(it, 500.0, null).apply { sticky = false })
		builder.addModule("combat", StarfighterCombatModule(it) { builder.suppliedModule<TargetingModule>("targeting").get().findTarget() })

		val positioning = builder.addModule("positioning", AxisStandoffPositioningModule(it, { builder.suppliedModule<TargetingModule>("targeting").get().findTarget() }, 25.0))
		val pathfinding = builder.addModule("pathfinding", SteeringPathfindingModule(it, positioning::findPosition))
		val flee = builder.addModule("flee", FleeModule(it, pathfinding::getDestination, targetingOriginal) { controller, _ -> controller.getMinimumShieldHealth() <= 0.2 }) // Flee if a shield reaches below 10%
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

		builder.addModule("targeting", ClosestTargetingModule(it, 500.0, null).apply { sticky = false })
		builder.addModule("combat", StarfighterCombatModule(it) { builder.suppliedModule<TargetingModule>("targeting").get().findTarget() })

		val positioning = builder.addModule("positioning", StandoffPositioningModule(it, { builder.suppliedModule<TargetingModule>("targeting").get().findTarget() }, 55.0))
		val pathfinding = builder.addModule("pathfinding", SteeringPathfindingModule(it, positioning::findPosition))
		builder.addModule("movement", CruiseModule(it, pathfinding, pathfinding::getDestination, CruiseModule.ShiftFlightType.ALL, 256.0))

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

		builder.addModule("targeting", ClosestTargetingModule(it, 500.0, null).apply { sticky = false })
		builder.addModule("combat", StarfighterCombatModule(it) { builder.suppliedModule<TargetingModule>("targeting").get().findTarget() })

		val positioning = builder.addModule("positioning", StandoffPositioningModule(it, { builder.suppliedModule<TargetingModule>("targeting").get().findTarget() }, 55.0))
		val pathfinding = builder.addModule("pathfinding", SteeringPathfindingModule(it, positioning::findPosition))
		builder.addModule("movement", CruiseModule(it, pathfinding, pathfinding::getDestination, CruiseModule.ShiftFlightType.ALL, 256.0))

		builder
	}
	build()
}

private val PRIVATEER_SMACK_PREFIX: String = "<${HEColorScheme.HE_MEDIUM_GRAY}>Receiving transmission from <$PRIVATEER_LIGHT_TEAL>privateer</$PRIVATEER_LIGHT_TEAL> vessel"

private fun basicPrivateerTemplate(
	identifier: String,
	schematicName: String,
	miniMessageName: String,
	type: StarshipType,
	controllerFactory: String,
	creditReward: Double,
	xpMultiplier: Double,
	engagementRadius: Double = 650.0,
	manualWeaponSets: MutableSet<WeaponSet> = mutableSetOf(),
	autoWeaponSets: MutableSet<WeaponSet> = mutableSetOf(),
): AISpawningConfiguration.AIStarshipTemplate = AISpawningConfiguration.AIStarshipTemplate(
	color = PRIVATEER_LIGHT_TEAL.value(),
	smackInformation = AISpawningConfiguration.AIStarshipTemplate.SmackInformation(
		prefix = PRIVATEER_SMACK_PREFIX,
		messages = listOf(
			"<white>Stand down, we have you outmatched!",
			"<white>Once I breach your shields, there's no going back.",
			"<white>Ha, you call those weapons?",
			"<white>System command, hostile contact is taking severe shield damage.",
			"<white>Flanking right!",
			"<white>Flanking left!"
		)
	),
	radiusMessageInformation = AISpawningConfiguration.AIStarshipTemplate.RadiusMessageInformation(
		prefix = PRIVATEER_SMACK_PREFIX,
		messages = mapOf(
			engagementRadius * 1.5 to "<#FFA500>You are entering restricted airspace. If you hear this transmission, turn away immediately or you will be fired upon.",
			engagementRadius to "<RED>You have violated restricted airspace. Your vessel will be fired upon."
		)
	),
	maxSpeed = -1,

	engagementRange = engagementRadius,
	identifier = identifier,
	schematicName = schematicName,
	miniMessageName = miniMessageName,
	type = type,
	controllerFactory = controllerFactory,
	rewardProviders = listOf(
		AISpawningConfiguration.AIStarshipTemplate.SLXPRewardProviderConfiguration(xpMultiplier),
		AISpawningConfiguration.AIStarshipTemplate.CreditRewardProviderConfiguration(creditReward),
	),
	manualWeaponSets = manualWeaponSets,
	autoWeaponSets = autoWeaponSets
)

val bulwark = basicPrivateerTemplate(
	identifier = "BULWARK",
	schematicName = "Bulwark",
	miniMessageName = "<${PRIVATEER_DARK_TEAL.asHexString()}>Bulwark",
	type = StarshipType.AI_CORVETTE,
	controllerFactory = "PRIVATEER_CORVETTE",
	xpMultiplier = 0.8,
	creditReward = 5750.0,
	engagementRadius = 1250.0,
	manualWeaponSets = mutableSetOf(
		WeaponSet(name = "main", engagementRangeMin = 0.0, engagementRangeMax = 550.0)
	),
	autoWeaponSets = mutableSetOf(
		WeaponSet(name = "auto", engagementRangeMin = 0.0, engagementRangeMax = 550.0)
	)
)

val contractor = basicPrivateerTemplate(
	identifier = "CONTRACTOR",
	schematicName = "Contractor",
	miniMessageName = "<${PRIVATEER_MEDIUM_TEAL.asHexString()}>Contractor",
	type = StarshipType.AI_GUNSHIP,
	controllerFactory = "PRIVATEER_GUNSHIP",
	xpMultiplier = 0.8,
	creditReward = 3750.0,
	engagementRadius = 1250.0,
	manualWeaponSets = mutableSetOf(
		WeaponSet(name = "manual", engagementRangeMin = 0.0, engagementRangeMax = 550.0)
	),
	autoWeaponSets = mutableSetOf(
		WeaponSet(name = "auto", engagementRangeMin = 0.0, engagementRangeMax = 550.0)
	)
)

val dagger = basicPrivateerTemplate(
	identifier = "DAGGER",
	schematicName = "Dagger",
	miniMessageName = "<${PRIVATEER_LIGHT_TEAL.asHexString()}>Dagger",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "PRIVATEER_STARFIGHTER",
	xpMultiplier = 0.8,
	creditReward = 2650.0,
	engagementRadius = 1250.0
)

val daybreak = basicPrivateerTemplate(
	identifier = "DAYBREAK",
	schematicName = "Daybreak",
	miniMessageName = "<${PRIVATEER_LIGHT_TEAL.asHexString()}>Daybreak",
	type = StarshipType.AI_CORVETTE,
	controllerFactory = "PRIVATEER_CORVETTE",
	xpMultiplier = 0.5,
	creditReward = 2650.0
)

val patroller = basicPrivateerTemplate(
	identifier = "PATROLLER",
	schematicName = "Patroller",
	miniMessageName = "<${PRIVATEER_LIGHT_TEAL.asHexString()}>Patroller",
	type = StarshipType.AI_GUNSHIP,
	controllerFactory = "PRIVATEER_GUNSHIP",
	xpMultiplier = 0.8,
	creditReward = 1850.0
)

val protector = basicPrivateerTemplate(
	identifier = "PROTECTOR",
	schematicName = "Protector",
	miniMessageName = "<${PRIVATEER_LIGHT_TEAL.asHexString()}>Protector",
	type = StarshipType.AI_GUNSHIP,
	controllerFactory = "PRIVATEER_GUNSHIP",
	xpMultiplier = 0.8,
	creditReward = 950.0
)

val veteran = basicPrivateerTemplate(
	identifier = "VETERAN",
	schematicName = "Veteran",
	miniMessageName = "<${PRIVATEER_LIGHT_TEAL.asHexString()}>Veteran",
	type = StarshipType.AI_GUNSHIP,
	controllerFactory = "PRIVATEER_GUNSHIP",
	xpMultiplier = 0.6,
	creditReward = 1850.0
)

val teneta = basicPrivateerTemplate(
	identifier = "TENETA",
	schematicName = "Teneta",
	miniMessageName = "<${PRIVATEER_LIGHT_TEAL.asHexString()}>Teneta",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "PRIVATEER_STARFIGHTER",
	xpMultiplier = 0.6,
	creditReward = 950.0
)

val furious = basicPrivateerTemplate(
	identifier = "FURIOUS",
	schematicName = "Furious",
	miniMessageName = "<${PRIVATEER_LIGHT_TEAL.asHexString()}>Furious",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "PRIVATEER_STARFIGHTER",
	xpMultiplier = 0.6,
	creditReward = 950.0
)

val inflict = basicPrivateerTemplate(
	identifier = "INFLICT",
	schematicName = "Inflict",
	miniMessageName = "<${PRIVATEER_LIGHT_TEAL.asHexString()}>Inflict",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "PRIVATEER_STARFIGHTER",
	xpMultiplier = 0.6,
	creditReward = 950.0
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
