package net.horizonsend.ion.server.features.starship.ai.spawning.alien

import net.horizonsend.ion.server.configuration.AISpawningConfiguration
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.ai.AIControllerFactories.registerFactory
import net.horizonsend.ion.server.features.starship.ai.AIControllerFactory
import net.horizonsend.ion.server.features.starship.ai.module.combat.FrigateCombatModule
import net.horizonsend.ion.server.features.starship.ai.module.movement.CruiseModule
import net.horizonsend.ion.server.features.starship.ai.module.pathfinding.SteeringPathfindingModule
import net.horizonsend.ion.server.features.starship.ai.module.positioning.CirclingPositionModule
import net.horizonsend.ion.server.features.starship.ai.module.targeting.ClosestTargetingModule
import net.horizonsend.ion.server.features.starship.ai.module.targeting.TargetingModule
import net.kyori.adventure.text.format.TextColor

val ALIEN_STANDARD = TextColor.fromHexString("#225C85")!!
val ALIEN_ACCENT = TextColor.fromHexString("#42E6F5")!!

@Suppress("unused")
val alienFrig = registerFactory("ALIEN_FRIGATE") {
	setControllerTypeName("alien_frigate")

	setModuleBuilder {
		val builder = AIControllerFactory.Builder.ModuleBuilder()

		builder.addModule("targeting", ClosestTargetingModule(it, 1500.0, null).apply { sticky = true })
		builder.addModule("combat", FrigateCombatModule(it) { builder.suppliedModule<TargetingModule>("targeting").get().findTarget() })

		val positioning = builder.addModule("positioning", CirclingPositionModule(it, { builder.suppliedModule<TargetingModule>("targeting").get().findTarget() }, 55.0))
		val pathfinding = builder.addModule("pathfinding", SteeringPathfindingModule(it, positioning::findPosition))
		builder.addModule("movement", CruiseModule(it, pathfinding, pathfinding::getDestination, CruiseModule.ShiftFlightType.ALL, 256.0))

		builder
	}

	build()
}

private fun alienTemplateFormat(
	identifier: String,
	schematicName: String,
	miniMessageName: String,
	type: StarshipType,
	controllerFactory: String,
	creditReward: Double,
	xpMultiplier: Double,
	engagementRadius: Double = 2500.0,
	manualWeaponSets: MutableSet<AISpawningConfiguration.AIStarshipTemplate.WeaponSet> = mutableSetOf(),
	autoWeaponSets: MutableSet<AISpawningConfiguration.AIStarshipTemplate.WeaponSet> = mutableSetOf(),
	reinforcementInformation: AISpawningConfiguration.AIStarshipTemplate.ReinforcementInformation? = null,
	smackInformation: AISpawningConfiguration.AIStarshipTemplate.SmackInformation? = null
): AISpawningConfiguration.AIStarshipTemplate {
	return AISpawningConfiguration.AIStarshipTemplate(
		color = ALIEN_STANDARD.value(),
		smackInformation = smackInformation,
		maxSpeed = -1,
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

val mianbaoUnreinforced = alienTemplateFormat(
	identifier = "MIANBAO_UNREINFORCED",
	schematicName = "Mianbao",
	miniMessageName = "<#FCBA03>Mianbao",
	type = StarshipType.AI_CORVETTE,
	controllerFactory = "ALIEN_FRIGATE",
	xpMultiplier = 0.9,
	creditReward = 6750.0,
	manualWeaponSets = mutableSetOf(AISpawningConfiguration.AIStarshipTemplate.WeaponSet(
		name = "manual",
		engagementRangeMin = 0.0,
		engagementRangeMax = 550.0
	)),
	autoWeaponSets = mutableSetOf(AISpawningConfiguration.AIStarshipTemplate.WeaponSet(
		name = "auto",
		engagementRangeMin = 250.0,
		engagementRangeMax = 550.0
	)),
	reinforcementInformation = null
)

val malingshuUnreinforced = alienTemplateFormat(
	identifier = "MALINGSHU_UNREINFORCED",
	schematicName = "Malingshu",
	miniMessageName = "<#FCBA03>Malingshu",
	type = StarshipType.AI_FRIGATE,
	controllerFactory = "ALIEN_FRIGATE",
	xpMultiplier = 0.9,
	creditReward = 8250.0,
	manualWeaponSets = mutableSetOf(AISpawningConfiguration.AIStarshipTemplate.WeaponSet(
		name = "manual",
		engagementRangeMin = 0.0,
		engagementRangeMax = 550.0
	)),
	autoWeaponSets = mutableSetOf(
		AISpawningConfiguration.AIStarshipTemplate.WeaponSet(
			name = "auto",
			engagementRangeMin = 0.0,
			engagementRangeMax = 250.0
		),
		AISpawningConfiguration.AIStarshipTemplate.WeaponSet(
			name = "tt",
			engagementRangeMin = 250.0,
			engagementRangeMax = 550.0
		)
	),
	reinforcementInformation = null
)

val malingshuReinforced = alienTemplateFormat(
	identifier = "MALINGSHU_REINFORCED",
	schematicName = "Malingshu",
	miniMessageName = "<#FCBA03>Malingshu",
	type = StarshipType.AI_FRIGATE,
	controllerFactory = "ALIEN_FRIGATE",
	xpMultiplier = 0.9,
	creditReward = 8250.0,
	manualWeaponSets = mutableSetOf(AISpawningConfiguration.AIStarshipTemplate.WeaponSet(
		name = "manual",
		engagementRangeMin = 0.0,
		engagementRangeMax = 550.0
	)),
	autoWeaponSets = mutableSetOf(
		AISpawningConfiguration.AIStarshipTemplate.WeaponSet(
			name = "auto",
			engagementRangeMin = 0.0,
			engagementRangeMax = 250.0
		),
		AISpawningConfiguration.AIStarshipTemplate.WeaponSet(
			name = "tt",
			engagementRangeMin = 250.0,
			engagementRangeMax = 550.0
		)
	),
	reinforcementInformation = AISpawningConfiguration.AIStarshipTemplate.ReinforcementInformation(
		activationThreshold = 0.95,
		delay = 100L,
		broadcastMessage = null,
		configuration = AISpawningConfiguration.AISpawnerConfiguration(
			miniMessageSpawnMessage = "<italic><$ALIEN_STANDARD>You cannot decipher the transmission from the incoming alien ship",
			pointChance = 0.0,
			pointThreshold = Int.MAX_VALUE,
			minDistanceFromPlayer = 100.0,
			maxDistanceFromPlayer = 150.0,
			tiers = listOf(
				AISpawningConfiguration.AISpawnerTier(
					identifier = "REINFORCEMENTS",
					nameList = mapOf(
						"<$ALIEN_ACCENT><obfuscated>飞行员" to 5,
					),
					ships = mapOf(mianbaoUnreinforced.identifier to 2)
				)
			)
		)
	)
)

val mianbaoReinforced = alienTemplateFormat(
	identifier = "MIANBAO_REINFORCED",
	schematicName = "Mianbao",
	miniMessageName = "<#FCBA03>Mianbao",
	type = StarshipType.AI_CORVETTE,
	controllerFactory = "ALIEN_FRIGATE",
	xpMultiplier = 0.9,
	creditReward = 6750.0,
	manualWeaponSets = mutableSetOf(AISpawningConfiguration.AIStarshipTemplate.WeaponSet(
		name = "manual",
		engagementRangeMin = 0.0,
		engagementRangeMax = 550.0
	)),
	autoWeaponSets = mutableSetOf(AISpawningConfiguration.AIStarshipTemplate.WeaponSet(
		name = "auto",
		engagementRangeMin = 250.0,
		engagementRangeMax = 550.0
	)),
	reinforcementInformation = AISpawningConfiguration.AIStarshipTemplate.ReinforcementInformation(
		activationThreshold = 0.95,
		delay = 100L,
		broadcastMessage = null,
		configuration = AISpawningConfiguration.AISpawnerConfiguration(
			miniMessageSpawnMessage = "<italic><$ALIEN_STANDARD>You cannot decipher the transmission from the incoming alien ship",
			pointChance = 0.0,
			pointThreshold = Int.MAX_VALUE,
			minDistanceFromPlayer = 100.0,
			maxDistanceFromPlayer = 150.0,
			tiers = listOf(
				AISpawningConfiguration.AISpawnerTier(
					identifier = "REINFORCEMENTS",
					nameList = mapOf(
						"<$ALIEN_ACCENT><obfuscated>飞行员" to 5,
					),
					ships = mapOf(malingshuUnreinforced.identifier to 2)
				)
			)
		)
	)
)

val verdolithReinforcement = alienTemplateFormat(
	identifier = "VERDOLITH_UNREINFROCED",
	schematicName = "Verdolith",
	miniMessageName = "<#013220>Verdolith",
	type = StarshipType.AI_FRIGATE,
	controllerFactory = "ALIEN_FRIGATE",
	xpMultiplier = 0.9,
	creditReward = 9000.0,
	manualWeaponSets = mutableSetOf(
		AISpawningConfiguration.AIStarshipTemplate.WeaponSet(
			name = "phaser",
			engagementRangeMin = 0.0,
			engagementRangeMax = 220.0
		),
		AISpawningConfiguration.AIStarshipTemplate.WeaponSet(
			name = "manual",
			engagementRangeMin = 220.0,
			engagementRangeMax = 550.0
		),
	),
	autoWeaponSets = mutableSetOf(AISpawningConfiguration.AIStarshipTemplate.WeaponSet(
		name = "auto",
		engagementRangeMin = 250.0,
		engagementRangeMax = 550.0
	)),
)

val verdolithReinforced = alienTemplateFormat(
	identifier = "VERDOLITH_REINFORCED",
	schematicName = "Verdolith",
	miniMessageName = "<#013220>Verdolith",
	type = StarshipType.AI_FRIGATE,
	controllerFactory = "ALIEN_FRIGATE",
	xpMultiplier = 0.9,
	creditReward = 9000.0,
	manualWeaponSets = mutableSetOf(
		AISpawningConfiguration.AIStarshipTemplate.WeaponSet(
			name = "phaser",
			engagementRangeMin = 0.0,
			engagementRangeMax = 220.0
		),
		AISpawningConfiguration.AIStarshipTemplate.WeaponSet(
			name = "manual",
			engagementRangeMin = 220.0,
			engagementRangeMax = 550.0
		),
	),
	autoWeaponSets = mutableSetOf(AISpawningConfiguration.AIStarshipTemplate.WeaponSet(
		name = "auto",
		engagementRangeMin = 250.0,
		engagementRangeMax = 550.0
	)),
	reinforcementInformation = AISpawningConfiguration.AIStarshipTemplate.ReinforcementInformation(
		activationThreshold = 0.85,
		delay = 100L,
		broadcastMessage = null,
		configuration = AISpawningConfiguration.AISpawnerConfiguration(
			miniMessageSpawnMessage = "<italic><$ALIEN_STANDARD>You cannot decipher the transmission from the incoming alien ship",
			pointChance = 0.0,
			pointThreshold = Int.MAX_VALUE,
			minDistanceFromPlayer = 100.0,
			maxDistanceFromPlayer = 150.0,
			tiers = listOf(
				AISpawningConfiguration.AISpawnerTier(
					identifier = "REINFORCEMENTS",
					nameList = mapOf(
						"<$ALIEN_ACCENT><obfuscated>飞行员" to 5
					),
					ships = mapOf(verdolithReinforcement.identifier to 2)
				)
			)
		)
	),
	smackInformation = AISpawningConfiguration.AIStarshipTemplate.SmackInformation(
		prefix = "",
		messages = listOf(
			"<$ALIEN_STANDARD>Intercepting hostile transmissions. Adapting swarm behavior to disrupt enemy communications.",
			"<$ALIEN_STANDARD>Evasive maneuvers engaged; navigating hostile terrain.",
			"<$ALIEN_STANDARD>Near-field barrier corroded under hostile fire. Re-routing aortal flow to priority organs.",
			"<$ALIEN_STANDARD>Deploying attack swarm.",
			"<$ALIEN_STANDARD>Hostile vessel subsystem lock-on confirmed. Firing.",
			"<$ALIEN_STANDARD>Combat pattern analysis transmitted to nearest Hive.",
			"<$ALIEN_STANDARD>Hostile vessel damaged.",
			"<$ALIEN_STANDARD>Hive directive received, switching designation: Hunter-Seeker.",
			"<$ALIEN_STANDARD>Releasing attack swarm.",
			"<$ALIEN_STANDARD>Attack vector plotted.",
			"<$ALIEN_STANDARD>Engaging defensive maneuvers.",
			"<$ALIEN_STANDARD>Re-routing aortal flow to drone locomotion systems."
		)
	)
)

val alienTemplates = arrayOf(
	verdolithReinforcement,
	verdolithReinforced,
	mianbaoReinforced,
	mianbaoUnreinforced,
	malingshuReinforced,
	malingshuUnreinforced
)
