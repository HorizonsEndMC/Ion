package net.horizonsend.ion.server.features.starship.ai.spawning.pirate

import net.horizonsend.ion.server.configuration.AISpawningConfiguration
import net.horizonsend.ion.server.configuration.AISpawningConfiguration.AIStarshipTemplate.WeaponSet
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.ai.AIControllerFactories
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

val PIRATE_LIGHT_RED = TextColor.fromHexString("#A06363")!!
val PIRATE_SATURATED_RED = TextColor.fromHexString("#C63F3F")!!
val PIRATE_DARK_RED = TextColor.fromHexString("#732525")!!

private val PIRATE_SMACK_PREFIX = "<$PIRATE_SATURATED_RED>Receiving transmission from pirate vessel"

@Suppress("unused")
val pirateStarfighter = AIControllerFactories.registerFactory("PIRATE_STARFIGHTER") {
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

@Suppress("unused")
val pirateGunshipPulse = AIControllerFactories.registerFactory("PIRATE_GUNSHIP_PULSE") {
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

private fun basicPirateTemplate(
	identifier: String,
	schematicName: String,
	miniMessageName: String,
	type: StarshipType,
	controllerFactory: String,
	creditReward: Double,
	xpMultiplier: Double,
	engagementRadius: Double = 750.0,
	manualWeaponSets: MutableSet<WeaponSet> = mutableSetOf(),
	autoWeaponSets: MutableSet<WeaponSet> = mutableSetOf(),
	reinforcementInformation: AISpawningConfiguration.AIStarshipTemplate.ReinforcementInformation? = null
): AISpawningConfiguration.AIStarshipTemplate = AISpawningConfiguration.AIStarshipTemplate(
	color = PIRATE_SATURATED_RED.value(),
	smackInformation = AISpawningConfiguration.AIStarshipTemplate.SmackInformation(
		prefix = PIRATE_SMACK_PREFIX,
		messages = listOf(
			"Nice day, Nice Ship. I think ill take it!",
			"I'll plunder your booty!",
			"Scram or we'll blow you to pieces!",
			"Someones too curious for their own good.",
			"Don't say I didn't warn ya, mate."
		)
	),
	radiusMessageInformation = AISpawningConfiguration.AIStarshipTemplate.RadiusMessageInformation(
		prefix = PIRATE_SMACK_PREFIX,
		messages = mapOf(
			engagementRadius * 1.5 to "<#FFA500>You are entering restricted airspace. If you hear this transmission, turn away immediately or you will be fired upon.",
			engagementRadius to "<RED>You have violated restricted airspace. Your vessel will be fired upon."
		)
	),
	reinforcementInformation = reinforcementInformation,
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

val iskat = basicPirateTemplate(
	identifier = "ISKAT",
	schematicName = "Iskat",
	miniMessageName = "<${PIRATE_LIGHT_RED.asHexString()}>Iskat",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "PIRATE_STARFIGHTER",
	xpMultiplier = 0.4,
	creditReward = 750.0
)

val voss = basicPirateTemplate(
	identifier = "VOSS",
	schematicName = "Voss",
	miniMessageName = "<${PIRATE_LIGHT_RED.asHexString()}>Voss",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "PIRATE_STARFIGHTER",
	xpMultiplier = 0.4,
	creditReward = 750.0
)

val hector = basicPirateTemplate(
	identifier = "HECTOR",
	schematicName = "Hector",
	miniMessageName = "<${PIRATE_LIGHT_RED.asHexString()}>Hector",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "PIRATE_STARFIGHTER",
	xpMultiplier = 0.4,
	creditReward = 750.0
)

val hiro = basicPirateTemplate(
	identifier = "HIRO",
	schematicName = "Hiro",
	miniMessageName = "<${PIRATE_LIGHT_RED.asHexString()}>Hiro",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "PIRATE_STARFIGHTER",
	xpMultiplier = 0.4,
	creditReward = 750.0
)

val wasp = basicPirateTemplate(
	identifier = "WASP",
	schematicName = "Wasp",
	miniMessageName = "<${PIRATE_LIGHT_RED.asHexString()}>Wasp",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "PIRATE_STARFIGHTER",
	xpMultiplier = 0.4,
	creditReward = 750.0
)

val frenz = basicPirateTemplate(
	identifier = "FRENZ",
	schematicName = "Frenz",
	miniMessageName = "<${PIRATE_LIGHT_RED.asHexString()}>Frenz",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "PIRATE_STARFIGHTER",
	xpMultiplier = 0.4,
	creditReward = 750.0
)

val tempest = basicPirateTemplate(
	identifier = "TEMPEST",
	schematicName = "Tempest",
	miniMessageName = "<${PIRATE_LIGHT_RED.asHexString()}>Tempest",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "PIRATE_STARFIGHTER",
	xpMultiplier = 0.4,
	creditReward = 750.0
)

val velasco = basicPirateTemplate(
	identifier = "VELASCO",
	schematicName = "Velasco",
	miniMessageName = "<${PIRATE_LIGHT_RED.asHexString()}>Velasco",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "PIRATE_STARFIGHTER",
	xpMultiplier = 0.4,
	creditReward = 750.0
)

val anaan = basicPirateTemplate(
	identifier = "ANAAN",
	schematicName = "Anaan",
	miniMessageName = "<${PIRATE_SATURATED_RED.asHexString()}>Anaan",
	type = StarshipType.AI_GUNSHIP,
	controllerFactory = "PIRATE_GUNSHIP_PULSE",
	xpMultiplier = 0.5,
	creditReward = 1250.0,
	autoWeaponSets = mutableSetOf(WeaponSet(name = "auto", engagementRangeMin = 0.0, engagementRangeMax = 500.0))
)

val vendetta = basicPirateTemplate(
	identifier = "VENDETTA",
	schematicName = "Vendetta",
	miniMessageName = "<${PIRATE_SATURATED_RED.asHexString()}>Vendetta",
	type = StarshipType.AI_GUNSHIP,
	controllerFactory = "PIRATE_GUNSHIP_PULSE",
	xpMultiplier = 0.5,
	creditReward = 1250.0,
	manualWeaponSets = mutableSetOf(WeaponSet(name = "main", engagementRangeMin = 0.0, engagementRangeMax = 500.0)),
	autoWeaponSets = mutableSetOf(WeaponSet(name = "auto", engagementRangeMin = 0.0, engagementRangeMax = 500.0))
)

val cormorant = basicPirateTemplate(
	identifier = "CORMORANT",
	schematicName = "Cormorant",
	miniMessageName = "<${PIRATE_SATURATED_RED.asHexString()}>Cormorant",
	type = StarshipType.AI_GUNSHIP,
	controllerFactory = "PIRATE_GUNSHIP_PULSE",
	xpMultiplier = 0.5,
	creditReward = 1250.0,
	manualWeaponSets = mutableSetOf(WeaponSet(name = "main", engagementRangeMin = 0.0, engagementRangeMax = 500.0)),
	autoWeaponSets = mutableSetOf(WeaponSet(name = "auto", engagementRangeMin = 0.0, engagementRangeMax = 500.0))
)

val mantis = basicPirateTemplate(
	identifier = "MANTIS",
	schematicName = "Mantis",
	miniMessageName = "<${PIRATE_SATURATED_RED.asHexString()}>Mantis",
	type = StarshipType.AI_GUNSHIP,
	controllerFactory = "PIRATE_GUNSHIP_PULSE",
	xpMultiplier = 0.5,
	creditReward = 1250.0,
	manualWeaponSets = mutableSetOf(WeaponSet(name = "main", engagementRangeMin = 0.0, engagementRangeMax = 500.0)),
	autoWeaponSets = mutableSetOf(WeaponSet(name = "auto", engagementRangeMin = 0.0, engagementRangeMax = 500.0))
)

val hernstein = basicPirateTemplate(
	identifier = "HERNSTEIN",
	schematicName = "Hernstein",
	miniMessageName = "<${PIRATE_SATURATED_RED.asHexString()}>Hernstein",
	type = StarshipType.AI_GUNSHIP,
	controllerFactory = "PIRATE_GUNSHIP_PULSE",
	xpMultiplier = 0.5,
	creditReward = 1250.0,
	manualWeaponSets = mutableSetOf(WeaponSet(name = "main", engagementRangeMin = 0.0, engagementRangeMax = 500.0)),
	autoWeaponSets = mutableSetOf(WeaponSet(name = "auto", engagementRangeMin = 0.0, engagementRangeMax = 500.0))
)

val fyr = basicPirateTemplate(
	identifier = "FYR",
	schematicName = "Fyr",
	miniMessageName = "<${PIRATE_SATURATED_RED.asHexString()}>Fyr",
	type = StarshipType.AI_GUNSHIP,
	controllerFactory = "PIRATE_GUNSHIP_PULSE",
	xpMultiplier = 0.5,
	creditReward = 1250.0,
	manualWeaponSets = mutableSetOf(WeaponSet(name = "main", engagementRangeMin = 0.0, engagementRangeMax = 500.0)),
	autoWeaponSets = mutableSetOf(WeaponSet(name = "auto", engagementRangeMin = 0.0, engagementRangeMax = 500.0))
)

val bloodStar = basicPirateTemplate(
	identifier = "BLOODSTAR",
	schematicName = "Bloodstar",
	miniMessageName = "<${PIRATE_SATURATED_RED.asHexString()}>Bloodstar",
	type = StarshipType.AI_CORVETTE,
	controllerFactory = "PRIVATEER_CORVETTE",
	xpMultiplier = 0.8,
	creditReward = 2650.0,
	reinforcementInformation = AISpawningConfiguration.AIStarshipTemplate.ReinforcementInformation(
		activationThreshold = 0.85,
		delay = 100L,
		broadcastMessage = "<italic><red>Did you really think we would risk this ship without an escort fleet? We'll enjoy looting your corpse!",
		configuration = AISpawningConfiguration.AISpawnerConfiguration(
			miniMessageSpawnMessage = "<$PIRATE_DARK_RED>We hear ya! {0} comin' to save your booty!.",
			pointChance = 0.0,
			pointThreshold = Int.MAX_VALUE,
			minDistanceFromPlayer = 100.0,
			maxDistanceFromPlayer = 150.0,
			tiers = listOf(
				AISpawningConfiguration.AISpawnerTier(
					identifier = "REINFORCEMENTS",
					nameList = mapOf(
						"<$PIRATE_DARK_RED>Rapscallion" to 2,
						"<$PIRATE_DARK_RED>Swashbuckler" to 2,
						"<$PIRATE_DARK_RED>Corsair Kragan" to 2,
						"<$PIRATE_DARK_RED>Corsair Kavarr" to 2
					),
					ships = mapOf(cormorant.identifier to 2)
				)
			)
		)
	)
)

val pirateShips = arrayOf(
	iskat,
	voss,
	hector,
	hiro,
	wasp,
	frenz,
	tempest,
	velasco,
	anaan,
	vendetta,
	cormorant,
	mantis,
	hernstein,
	fyr,
	bloodStar
)
