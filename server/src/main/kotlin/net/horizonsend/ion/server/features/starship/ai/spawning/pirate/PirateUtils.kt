package net.horizonsend.ion.server.features.starship.ai.spawning.pirate

import net.horizonsend.ion.server.configuration.AISpawningConfiguration
import net.horizonsend.ion.server.configuration.AISpawningConfiguration.AIStarshipTemplate.WeaponSet
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.ai.AIControllerFactories
import net.horizonsend.ion.server.features.starship.ai.AIControllerFactory
import net.horizonsend.ion.server.features.starship.ai.module.combat.StarfighterCombatModule
import net.horizonsend.ion.server.features.starship.ai.module.misc.FleeModule
import net.horizonsend.ion.server.features.starship.ai.module.misc.RadiusMessageModule
import net.horizonsend.ion.server.features.starship.ai.module.misc.SmackTalkModule
import net.horizonsend.ion.server.features.starship.ai.module.movement.CruiseModule
import net.horizonsend.ion.server.features.starship.ai.module.pathfinding.SteeringPathfindingModule
import net.horizonsend.ion.server.features.starship.ai.module.positioning.AxisStandoffPositioningModule
import net.horizonsend.ion.server.features.starship.ai.module.positioning.StandoffPositioningModule
import net.horizonsend.ion.server.features.starship.ai.module.targeting.ClosestTargetingModule
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor

val PIRATE_LIGHT_RED = TextColor.fromHexString("#A06363")!!
val PIRATE_SATURATED_RED = TextColor.fromHexString("#C63F3F")!!
val PIRATE_DARK_RED = TextColor.fromHexString("#732525")!!

private val smackTalkList = arrayOf<Component>(
	text("Nice day, Nice Ship. I think ill take it!"),
	text("I'll plunder your booty!"),
	text("Scram or we'll blow you to pieces!"),
	text("Someones too curious for their own good."),
	text("Don't say I didn't warn ya, mate.")
)

private val pirateSmackPrefix = text("Receiving transmission from pirate vessel", PIRATE_SATURATED_RED)

// Privateer controllers passive, only becoming aggressive if fired upon
val pirateStarfighter = AIControllerFactories.registerFactory("PIRATE_STARFIGHTER") {
	setControllerTypeName("Starfighter")
	setModuleBuilder {
		val builder = AIControllerFactory.Builder.ModuleBuilder()

		val targeting = builder.addModule("targeting", ClosestTargetingModule(it, 500.0, null).apply { sticky = false })
		builder.addModule("combat", StarfighterCombatModule(it, targeting::findTarget))

		val positioning = builder.addModule("positioning", AxisStandoffPositioningModule(it, targeting::findTarget, 25.0))
		val pathfinding = builder.addModule("pathfinding", SteeringPathfindingModule(it, positioning::findPositionVec3i))
		val flee = builder.addModule("flee", FleeModule(it, pathfinding::getDestination, targeting) { controller, _ -> controller.getMinimumShieldHealth() <= 0.2 }) // Flee if a shield reaches below 10%
		builder.addModule("movement", CruiseModule(it, pathfinding, flee, CruiseModule.ShiftFlightType.ALL, 256.0))

		builder.addModule("smackTalk", SmackTalkModule(it, pirateSmackPrefix, *smackTalkList))
		builder.addModule(
			"warning", RadiusMessageModule(it, mapOf(
				1000.0 to text("Scram! or we'll blow you to pieces!", TextColor.fromHexString("#FFA500")),
				500.0 to text("Now you've pissed us off, scum.", NamedTextColor.RED)
			))
		)

		builder
	}
	build()
}

val pirateGunshipPulse = AIControllerFactories.registerFactory("PIRATE_GUNSHIP_PULSE") {
	setControllerTypeName("Starfighter")
	setModuleBuilder {
		val builder = AIControllerFactory.Builder.ModuleBuilder()

		val targeting = builder.addModule("targeting", ClosestTargetingModule(it, 500.0, null).apply { sticky = false })
		builder.addModule("combat", StarfighterCombatModule(it, targeting::findTarget))

		val positioning = builder.addModule("positioning", StandoffPositioningModule(it, targeting::findTarget, 55.0))
		val pathfinding = builder.addModule("pathfinding", SteeringPathfindingModule(it, positioning::findPositionVec3i))
		builder.addModule("movement", CruiseModule(it, pathfinding, pathfinding::getDestination, CruiseModule.ShiftFlightType.ALL, 256.0))
		builder.addModule("smackTalk", SmackTalkModule(it, pirateSmackPrefix, *smackTalkList))

		builder.addModule("warning", RadiusMessageModule(it, mapOf(
			1000.0 to text("You are entering restricted airspace. If you hear this transmission, turn away immediately or you will be fired upon.", TextColor.fromHexString("#FFA500")),
			500.0 to text("You have violated restricted airspace. Your vessel will be fired upon.", NamedTextColor.RED)
		)))

		builder
	}
	build()
}

val pirateGunshipPlasma = AIControllerFactories.registerFactory("PIRATE_GUNSHIP_PLASMA") {
	setControllerTypeName("Starfighter")
	setModuleBuilder {
		val builder = AIControllerFactory.Builder.ModuleBuilder()

		val targeting = builder.addModule("targeting", ClosestTargetingModule(it, 500.0, null).apply { sticky = false })
		builder.addModule("combat", StarfighterCombatModule(it, targeting::findTarget))

		val positioning = builder.addModule("positioning", AxisStandoffPositioningModule(it, targeting::findTarget, 55.0))
		val pathfinding = builder.addModule("pathfinding", SteeringPathfindingModule(it, positioning::findPositionVec3i))
		builder.addModule("movement", CruiseModule(it, pathfinding, pathfinding::getDestination, CruiseModule.ShiftFlightType.ALL, 256.0))
		builder.addModule("smackTalk", SmackTalkModule(it, pirateSmackPrefix, *smackTalkList))

		builder.addModule("warning", RadiusMessageModule(it, mapOf(
			1000.0 to text("You are entering restricted airspace. If you hear this transmission, turn away immediately or you will be fired upon.", TextColor.fromHexString("#FFA500")),
			500.0 to text("You have violated restricted airspace. Your vessel will be fired upon.", NamedTextColor.RED)
		)))

		builder
	}
	build()
}

val iskat = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "ISKAT",
	schematicName = "Iskat",
	miniMessageName = "<${PIRATE_LIGHT_RED.asHexString()}>Iskat",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "PIRATE_STARFIGHTER",
	xpMultiplier = 0.6,
	creditReward = 950.0
)

val voss = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "VOSS",
	schematicName = "Voss",
	miniMessageName = "<${PIRATE_LIGHT_RED.asHexString()}>Voss",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "PIRATE_STARFIGHTER",
	xpMultiplier = 0.6,
	creditReward = 950.0
)

val hector = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "HECTOR",
	schematicName = "Hector",
	miniMessageName = "<${PIRATE_LIGHT_RED.asHexString()}>Hector",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "PIRATE_STARFIGHTER",
	xpMultiplier = 0.6,
	creditReward = 950.0
)

val hiro = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "HIRO",
	schematicName = "Hiro",
	miniMessageName = "<${PIRATE_LIGHT_RED.asHexString()}>Hiro",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "PIRATE_STARFIGHTER",
	xpMultiplier = 0.6,
	creditReward = 950.0
)

val wasp = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "WASP",
	schematicName = "Wasp",
	miniMessageName = "<${PIRATE_LIGHT_RED.asHexString()}>Wasp",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "PIRATE_STARFIGHTER",
	xpMultiplier = 0.6,
	creditReward = 950.0
)

val frenz = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "FRENZ",
	schematicName = "Frenz",
	miniMessageName = "<${PIRATE_LIGHT_RED.asHexString()}>Frenz",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "PIRATE_STARFIGHTER",
	xpMultiplier = 0.6,
	creditReward = 950.0
)

val tempest = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "TEMPEST",
	schematicName = "Tempest",
	miniMessageName = "<${PIRATE_LIGHT_RED.asHexString()}>Tempest",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "PIRATE_STARFIGHTER",
	xpMultiplier = 0.6,
	creditReward = 950.0
)

val velasco = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "VELASCO",
	schematicName = "Velasco",
	miniMessageName = "<${PIRATE_LIGHT_RED.asHexString()}>Velasco",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "PIRATE_STARFIGHTER",
	xpMultiplier = 0.6,
	creditReward = 950.0
)

val anaan = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "ANAAN",
	schematicName = "Anaan",
	miniMessageName = "<${PIRATE_SATURATED_RED.asHexString()}>Anaan",
	type = StarshipType.AI_GUNSHIP,
	controllerFactory = "PIRATE_GUNSHIP_PLASMA",
	xpMultiplier = 0.6,
	creditReward = 1850.0,
	autoWeaponSets = mutableSetOf(WeaponSet(name = "auto", engagementRangeMin = 0.0, engagementRangeMax = 500.0))
)

val vendetta = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "VENDETTA",
	schematicName = "Vendetta",
	miniMessageName = "<${PIRATE_SATURATED_RED.asHexString()}>Vendetta",
	type = StarshipType.AI_GUNSHIP,
	controllerFactory = "PIRATE_GUNSHIP_PLASMA",
	xpMultiplier = 0.6,
	creditReward = 1850.0,
	manualWeaponSets = mutableSetOf(WeaponSet(name = "main", engagementRangeMin = 0.0, engagementRangeMax = 500.0)),
	autoWeaponSets = mutableSetOf(WeaponSet(name = "auto", engagementRangeMin = 0.0, engagementRangeMax = 500.0))
)

val cormorant = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "CORMORANT",
	schematicName = "Cormorant",
	miniMessageName = "<${PIRATE_SATURATED_RED.asHexString()}>Cormorant",
	type = StarshipType.AI_GUNSHIP,
	controllerFactory = "PIRATE_GUNSHIP_PLASMA",
	xpMultiplier = 0.6,
	creditReward = 1850.0,
	manualWeaponSets = mutableSetOf(WeaponSet(name = "main", engagementRangeMin = 0.0, engagementRangeMax = 500.0)),
	autoWeaponSets = mutableSetOf(WeaponSet(name = "auto", engagementRangeMin = 0.0, engagementRangeMax = 500.0))
)

val mantis = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "MANTIS",
	schematicName = "Mantis",
	miniMessageName = "<${PIRATE_SATURATED_RED.asHexString()}>Mantis",
	type = StarshipType.AI_GUNSHIP,
	controllerFactory = "PIRATE_GUNSHIP_PULSE",
	xpMultiplier = 0.6,
	creditReward = 1850.0,
	manualWeaponSets = mutableSetOf(WeaponSet(name = "main", engagementRangeMin = 0.0, engagementRangeMax = 500.0)),
	autoWeaponSets = mutableSetOf(WeaponSet(name = "auto", engagementRangeMin = 0.0, engagementRangeMax = 500.0))
)

val hernstein = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "HERNSTEIN",
	schematicName = "Hernstein",
	miniMessageName = "<${PIRATE_SATURATED_RED.asHexString()}>Hernstein",
	type = StarshipType.AI_GUNSHIP,
	controllerFactory = "PIRATE_GUNSHIP_PLASMA",
	xpMultiplier = 0.6,
	creditReward = 1850.0,
	manualWeaponSets = mutableSetOf(WeaponSet(name = "main", engagementRangeMin = 0.0, engagementRangeMax = 500.0)),
	autoWeaponSets = mutableSetOf(WeaponSet(name = "auto", engagementRangeMin = 0.0, engagementRangeMax = 500.0))
)

val fyr = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "FYR",
	schematicName = "Fyr",
	miniMessageName = "<${PIRATE_SATURATED_RED.asHexString()}>Fyr",
	type = StarshipType.AI_GUNSHIP,
	controllerFactory = "PIRATE_GUNSHIP_PLASMA",
	xpMultiplier = 0.6,
	creditReward = 1850.0,
	manualWeaponSets = mutableSetOf(WeaponSet(name = "main", engagementRangeMin = 0.0, engagementRangeMax = 500.0)),
	autoWeaponSets = mutableSetOf(WeaponSet(name = "auto", engagementRangeMin = 0.0, engagementRangeMax = 500.0))
)

val bloodStar = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "BLOODSTAR",
	schematicName = "Fyr",
	miniMessageName = "<${PIRATE_SATURATED_RED.asHexString()}>Bloodstar",
	type = StarshipType.AI_CORVETTE,
	controllerFactory = "PRIVATEER_CORVETTE",
	xpMultiplier = 0.6,
	creditReward = 2650.0
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
