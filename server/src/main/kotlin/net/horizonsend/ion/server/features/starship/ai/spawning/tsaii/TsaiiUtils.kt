package net.horizonsend.ion.server.features.starship.ai.spawning.tsaii

import net.horizonsend.ion.server.configuration.AISpawningConfiguration
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.ai.AIControllerFactory
import net.kyori.adventure.text.format.TextColor

val TSAII_LIGHT_ORANGE = TextColor.fromHexString("#A1543A")!!
val TSAII_DARK_ORANGE = TextColor.fromHexString("#9C3614")!!

val tsaiiFrigate: AIControllerFactory = TODO()
val tsaiiCorvette: AIControllerFactory = TODO()
val tsaiiGunship: AIControllerFactory = TODO()
val tsaiiStarfighter: AIControllerFactory = TODO()

val bastion = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "BASTION",
	schematicName = "Bastion",
	miniMessageName = "<${TSAII_DARK_ORANGE.asHexString()}>Bastion",
	type = StarshipType.AI_BATTLECRUISER,
	controllerFactory = "TSAII_FRIGATE",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

val reaver = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "REAVER",
	schematicName = "Reaver",
	miniMessageName = "<${TSAII_DARK_ORANGE.asHexString()}>Reaver",
	type = StarshipType.AI_DESTROYER,
	controllerFactory = "AI_FRIGATE",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

val raider = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "RAIDER",
	schematicName = "Raider",
	miniMessageName = "<${TSAII_DARK_ORANGE.asHexString()}>Raider",
	type = StarshipType.AI_GUNSHIP,
	controllerFactory = "TSAII_GUNSHIP",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

val scythe = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "SCYTHE",
	schematicName = "Scythe",
	miniMessageName = "<${TSAII_LIGHT_ORANGE.asHexString()}>Scythe",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "TSAII_STARFIGHTER",
	xpMultiplier = 0.5,
	creditReward = 100.0
)

val swarmer = AISpawningConfiguration.AIStarshipTemplate(
	identifier = "SWARMER",
	schematicName = "Swarmer",
	miniMessageName = "<${TSAII_LIGHT_ORANGE.asHexString()}>Swarmer",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "TSAII_STARFIGHTER",
	xpMultiplier = 0.5,
	creditReward = 100.0
)
