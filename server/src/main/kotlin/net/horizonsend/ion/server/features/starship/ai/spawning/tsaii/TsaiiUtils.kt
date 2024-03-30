package net.horizonsend.ion.server.features.starship.ai.spawning.tsaii

import net.horizonsend.ion.server.configuration.AISpawningConfiguration
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.ai.AIControllerFactories
import net.horizonsend.ion.server.features.starship.ai.AIControllerFactories.registerFactory
import net.horizonsend.ion.server.features.starship.ai.AIControllerFactory
import net.horizonsend.ion.server.features.starship.ai.module.combat.StarfighterCombatModule
import net.horizonsend.ion.server.features.starship.ai.module.movement.CruiseModule
import net.horizonsend.ion.server.features.starship.ai.module.pathfinding.SteeringPathfindingModule
import net.horizonsend.ion.server.features.starship.ai.module.positioning.AxisStandoffPositioningModule
import net.horizonsend.ion.server.features.starship.ai.module.positioning.StandoffPositioningModule
import net.horizonsend.ion.server.features.starship.ai.module.targeting.ClosestTargetingModule
import net.horizonsend.ion.server.features.starship.ai.spawning.pirate.PIRATE_SATURATED_RED
import net.kyori.adventure.text.format.TextColor

val TSAII_LIGHT_ORANGE = TextColor.fromHexString("#F37F58")!!
val TSAII_MEDIUM_ORANGE = TextColor.fromHexString("#E56034")!!
val TSAII_DARK_ORANGE = TextColor.fromHexString("#A1543A")!!
val TSAII_VERY_DARK_ORANGE = TextColor.fromHexString("#9C3614")!!

private val TSAII_SMACK_PREFIX = "<$TSAII_LIGHT_ORANGE>Receiving transmission from Tsaii vessel"

val tsaiiFrigate: AIControllerFactory = registerFactory("TSAII_FRIGATE") {
	AIControllerFactory.Builder(AIControllerFactories.frigate).build()
}

val tsaiiCorvette: AIControllerFactory = registerFactory("TSAII_CORVETTE") {
	AIControllerFactory.Builder(AIControllerFactories.corvette).build()
}

// Tsaii controllers: aggressive, never running.
@Suppress("unused")
val tsaiiStarfighter = registerFactory("TSAII_STARFIGHTER") { // TODO
	setControllerTypeName("Starfighter")
	setModuleBuilder {
		val builder = AIControllerFactory.Builder.ModuleBuilder()

		val targeting = builder.addModule("targeting", ClosestTargetingModule(it, 1500.0, null).apply { sticky = true })
		builder.addModule("combat", StarfighterCombatModule(it, targeting::findTarget))

		val positioning = builder.addModule("positioning", AxisStandoffPositioningModule(it, targeting::findTarget, 25.0))
		val pathfinding = builder.addModule("pathfinding", SteeringPathfindingModule(it, positioning::findPosition))
		builder.addModule("movement", CruiseModule(it, pathfinding, pathfinding::getDestination, CruiseModule.ShiftFlightType.ALL, 256.0))

		builder
	}

	build()
}
//builder.addModule("warning", RadiusMessageModule(it, mapOf(1500.0 to text("You can't run or hide in space, little ship!", TextColor.fromHexString("#FFA500")))))

// Tsaii controllers: aggressive, never running.
@Suppress("unused")
val tsaiiGunship = registerFactory("TSAII_GUNSHIP") { // TODO
	setControllerTypeName("Starfighter")
	setModuleBuilder {
		val builder = AIControllerFactory.Builder.ModuleBuilder()

		val targeting = builder.addModule("targeting", ClosestTargetingModule(it, 1500.0, null).apply { sticky = true })
		builder.addModule("combat", StarfighterCombatModule(it, targeting::findTarget))

		val positioning = builder.addModule("positioning", StandoffPositioningModule(it, targeting::findTarget, 55.0))
		val pathfinding = builder.addModule("pathfinding", SteeringPathfindingModule(it, positioning::findPosition))
		builder.addModule("movement", CruiseModule(it, pathfinding, pathfinding::getDestination, CruiseModule.ShiftFlightType.ALL, 256.0))

		builder
	}
	build()
}

private fun tsaiiTemplate(
	identifier: String,
	schematicName: String,
	miniMessageName: String,
	type: StarshipType,
	controllerFactory: String,
	creditReward: Double,
	xpMultiplier: Double,
	engagementRadius: Double = 1000.0,
	manualWeaponSets: MutableSet<AISpawningConfiguration.AIStarshipTemplate.WeaponSet> = mutableSetOf(),
	autoWeaponSets: MutableSet<AISpawningConfiguration.AIStarshipTemplate.WeaponSet> = mutableSetOf(),
	reinforcementInformation: AISpawningConfiguration.AIStarshipTemplate.ReinforcementInformation? = null
): AISpawningConfiguration.AIStarshipTemplate {
	return AISpawningConfiguration.AIStarshipTemplate(
		color = TSAII_MEDIUM_ORANGE.value(),
		smackInformation = AISpawningConfiguration.AIStarshipTemplate.SmackInformation(
			prefix = TSAII_SMACK_PREFIX,
			messages = listOf(
				"I'll leave nothing but scrap",
				"I'll cut you to bacon",
				"When I'm done with you, I'll mantle your skull!"
			)
		),
		radiusMessageInformation = AISpawningConfiguration.AIStarshipTemplate.RadiusMessageInformation(
			prefix = TSAII_SMACK_PREFIX,
			messages = mapOf(
				1500.0 to "<#FFA500>You are entering restricted airspace. If you hear this transmission, turn away immediately or you will be fired upon.",
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
}

val swarmer = tsaiiTemplate(
	identifier = "SWARMER",
	schematicName = "Swarmer",
	miniMessageName = "<${TSAII_DARK_ORANGE.asHexString()}>Swarmer",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "TSAII_STARFIGHTER",
	xpMultiplier = 0.7,
	creditReward = 1550.0
)

val scythe = tsaiiTemplate(
	identifier = "SCYTHE",
	schematicName = "Scythe",
	miniMessageName = "<${TSAII_DARK_ORANGE.asHexString()}>Scythe",
	type = StarshipType.AI_STARFIGHTER,
	controllerFactory = "TSAII_STARFIGHTER",
	xpMultiplier = 0.7,
	creditReward = 1550.0
)

val raider = tsaiiTemplate(
	identifier = "RAIDER",
	schematicName = "Raider",
	miniMessageName = "<${TSAII_VERY_DARK_ORANGE.asHexString()}>Raider",
	type = StarshipType.AI_GUNSHIP,
	controllerFactory = "TSAII_GUNSHIP",
	xpMultiplier = 0.7,
	creditReward = 2500.0
)

val reaver = tsaiiTemplate(
	identifier = "REAVER",
	schematicName = "Reaver",
	miniMessageName = "<${TSAII_VERY_DARK_ORANGE.asHexString()}>Reaver",
	type = StarshipType.AI_FRIGATE,
	controllerFactory = "TSAII_FRIGATE",
	xpMultiplier = 0.7,
	creditReward = 6500.0,
	manualWeaponSets = mutableSetOf(
		AISpawningConfiguration.AIStarshipTemplate.WeaponSet(
			name = "manual",
			engagementRangeMin = 0.0,
			engagementRangeMax = 220.0
		)
	),
	autoWeaponSets = mutableSetOf(
		AISpawningConfiguration.AIStarshipTemplate.WeaponSet(
			name = "auto",
			engagementRangeMin = 250.0,
			engagementRangeMax = 550.0
		)
	),
	reinforcementInformation = AISpawningConfiguration.AIStarshipTemplate.ReinforcementInformation(
		activationThreshold = 0.85,
		delay = 100L,
		broadcastMessage = "<italic><red>Did you really think we would risk this ship without an escort fleet? We'll enjoy looting your corpse!",
		configuration = AISpawningConfiguration.AISpawnerConfiguration(
			miniMessageSpawnMessage = "<bold><$PIRATE_SATURATED_RED>How dare you attack the boss’ on his day off. Hand over your ship!",
			pointChance = 0.0,
			pointThreshold = Int.MAX_VALUE,
			minDistanceFromPlayer = 100.0,
			maxDistanceFromPlayer = 150.0,
			tiers = listOf(
				AISpawningConfiguration.AISpawnerTier(
					identifier = "REINFORCEMENTS",
					nameList = mapOf(
						"<$TSAII_DARK_ORANGE>Tsaii Raider" to 2
					),
					ships = mapOf(raider.identifier to 2)
				)
			)
		)
	)
)

val bastion = tsaiiTemplate( // TODO
	identifier = "BASTION",
	schematicName = "Bastion",
	miniMessageName = "<${TSAII_VERY_DARK_ORANGE.asHexString()}>Bastion",
	type = StarshipType.AI_BATTLECRUISER,
	controllerFactory = "TSAII_FRIGATE",
	xpMultiplier = 0.7,
	creditReward = 8000.0
)

val tsaiiTemplates = arrayOf(
	swarmer,
	scythe,
	raider,
	reaver,
//	bastion
)
