package net.horizonsend.ion.server.features.starship.ai.spawning.pirate

import net.horizonsend.ion.server.configuration.AISpawningConfiguration
import net.horizonsend.ion.server.features.starship.StarshipType
import net.kyori.adventure.text.format.TextColor

val PIRATE_LIGHT_RED = TextColor.fromHexString("#A06363")!!
val PIRATE_SATURATED_RED = TextColor.fromHexString("#C63F3F")!!
val PIRATE_DARK_RED = TextColor.fromHexString("#732525")!!

val iskat = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "ISKAT",
	schematicName = "Iskat",
	miniMessageName = "<${PIRATE_LIGHT_RED.asHexString()}>Iskat",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "PRIVATEER_STARFIGHTER",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

val voss = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "VOSS",
	schematicName = "Voss",
	miniMessageName = "<${PIRATE_LIGHT_RED.asHexString()}>Voss",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "PRIVATEER_STARFIGHTER",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

val hector = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "HECTOR",
	schematicName = "Hector",
	miniMessageName = "<${PIRATE_LIGHT_RED.asHexString()}>Hector",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "PRIVATEER_STARFIGHTER",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

val hiro = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "HIRO",
	schematicName = "Hiro",
	miniMessageName = "<${PIRATE_LIGHT_RED.asHexString()}>Hiro",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "PRIVATEER_STARFIGHTER",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

val wasp = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "WASP",
	schematicName = "Wasp",
	miniMessageName = "<${PIRATE_LIGHT_RED.asHexString()}>Wasp",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "PRIVATEER_STARFIGHTER",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

val frenz = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "FRENZ",
	schematicName = "Frenz",
	miniMessageName = "<${PIRATE_LIGHT_RED.asHexString()}>Frenz",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "PRIVATEER_STARFIGHTER",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

val tempest = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "TEMPEST",
	schematicName = "Tempest",
	miniMessageName = "<${PIRATE_LIGHT_RED.asHexString()}>Tempest",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "PRIVATEER_STARFIGHTER",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

val velasco = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "VELASCO",
	schematicName = "Velasco",
	miniMessageName = "<${PIRATE_LIGHT_RED.asHexString()}>Velasco",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "PRIVATEER_STARFIGHTER",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

val anaan = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "ANAAN",
	schematicName = "Anaan",
	miniMessageName = "<${PIRATE_SATURATED_RED.asHexString()}>Anaan",
	type = StarshipType.AI_GUNSHIP,
	controllerFactory = "PRIVATEER_GUNSHIP",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

val vendetta = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "VENDETTA",
	schematicName = "Vendetta",
	miniMessageName = "<${PIRATE_SATURATED_RED.asHexString()}>Vendetta",
	type = StarshipType.AI_GUNSHIP,
	controllerFactory = "PRIVATEER_GUNSHIP",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

val cormorant = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "CORMORANT",
	schematicName = "Cormorant",
	miniMessageName = "<${PIRATE_SATURATED_RED.asHexString()}>Cormorant",
	type = StarshipType.AI_GUNSHIP,
	controllerFactory = "PRIVATEER_GUNSHIP",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

val mantis = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "MANTIS",
	schematicName = "Mantis",
	miniMessageName = "<${PIRATE_SATURATED_RED.asHexString()}>Mantis",
	type = StarshipType.AI_GUNSHIP,
	controllerFactory = "PRIVATEER_GUNSHIP",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

val hernstein = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "HERNSTEIN",
	schematicName = "Hernstein",
	miniMessageName = "<${PIRATE_SATURATED_RED.asHexString()}>Hernstein",
	type = StarshipType.AI_GUNSHIP,
	controllerFactory = "PRIVATEER_GUNSHIP",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

val fyr = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "FYR",
	schematicName = "Fyr",
	miniMessageName = "<${PIRATE_SATURATED_RED.asHexString()}>Fyr",
	type = StarshipType.AI_GUNSHIP,
	controllerFactory = "PRIVATEER_GUNSHIP",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

val bloodStar = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "BLOODSTAR",
	schematicName = "Fyr",
	miniMessageName = "<${PIRATE_SATURATED_RED.asHexString()}>Bloodstar",
	type = StarshipType.AI_CORVETTE,
	controllerFactory = "PRIVATEER_CORVETTE",
	xpMultiplier = 0.5,
	creditReward = 100.0
)
