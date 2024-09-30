package net.horizonsend.ion.server.features.ai.starship

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.colors.WATCHER_STANDARD
import net.horizonsend.ion.common.utils.text.colors.吃饭人_STANDARD
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.features.ai.AIControllerFactories
import net.horizonsend.ion.server.features.ai.AIControllerFactory
import net.horizonsend.ion.server.features.ai.configuration.AITemplate
import net.horizonsend.ion.server.features.ai.faction.AIFaction
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.PIRATES
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.SYSTEM_DEFENSE_FORCES
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.WATCHERS
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.miningGuildMini
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.吃饭人
import net.horizonsend.ion.server.features.ai.spawning.ships.SpawnedShip

/**
 * Fully realized, spawnable, AI templates
 *
 * These rely on a starship template (defined elsewhere), behavior information, and module information
 *
 * These are applied to the starship once it is spawned using info from the starship template
 **/
object AITemplateRegistry {
	private val TEMPLATE_DIRECTORY = IonServer.configurationFolder.resolve("ai_templates").apply { mkdirs() }

	private val templates = mutableMapOf<String, AITemplate>()

	// START_TEST_FACTION
	val TEST_JAMMER = registerTemplate(builder(
		identifier = "TEST_JAMMER",
		template = StarshipTemplateRegistry.TEST_JAMMER,
		controllerFactory = AIControllerFactories.jammingGunship,
		engagementRange = 2500.0
	).build())

	val TEST_LOGISTIC = registerTemplate(builder(
		identifier = "TEST_LOGISTIC",
		template = StarshipTemplateRegistry.TEST_LOGISTIC,
		controllerFactory = AIControllerFactories.logisticCorvette,
		engagementRange = 2500.0
	).build())

	val TEST_BATTLECRUISER = registerTemplate(builder(
		identifier = "TEST_BATTLECRUISER",
		template = StarshipTemplateRegistry.TEST_BATTLECRUISER,
		controllerFactory = AIControllerFactories.battlecruiser,
		engagementRange = 5000.0
	).build())

	val TEST_DISINTEGRATOR = registerTemplate(builder(
		identifier = "TEST_DISINTEGRATOR",
		template = StarshipTemplateRegistry.TEST_DISINTEGRATOR,
		controllerFactory = AIControllerFactories.advancedDestroyer,
		engagementRange = 5000.0
	).build())

	val TEST_CYCLE = registerTemplate(builder(
		identifier = "TEST_CYCLE",
		template = StarshipTemplateRegistry.TEST_CYCLE,
		controllerFactory = AIControllerFactories.advancedFrigate,
		engagementRange = 2500.0
	).build())
	// END_TEST_FACTION

	// START_WATCHER
	val VERDOLITH_REINFORCEMENT = registerTemplate(builder(
		identifier = "VERDOLITH_REINFORCEMENT",
		template = StarshipTemplateRegistry.VERDOLITH,
		controllerFactory = AIControllerFactories.frigate,
		engagementRange = 2500.0
	)
		.addFactionConfiguration(AIFaction.WATCHERS)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.9))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(9000.0))
		.addRewardProvider(AITemplate.ItemRewardProviderConfiguration(listOf(ServerConfiguration.PlanetSpawnConfig.DroppedItem(
			itemString = net.horizonsend.ion.server.features.custom.items.CustomItems.SUPERCONDUCTOR.identifier,
			dropChance = 1.0f,
			amount = 1,
		))))
		.build()
	)

	val VERDOLITH_REINFORCED = registerTemplate(builder(
			identifier = "VERDOLITH_REINFORCED",
			template = StarshipTemplateRegistry.VERDOLITH,
			controllerFactory = AIControllerFactories.frigate,
			engagementRange = 2500.0
		)
		.addFactionConfiguration(AIFaction.WATCHERS)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.9))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(9000.0))
		.addRewardProvider(AITemplate.ItemRewardProviderConfiguration(listOf(ServerConfiguration.PlanetSpawnConfig.DroppedItem(
			itemString = net.horizonsend.ion.server.features.custom.items.CustomItems.SUPERCONDUCTOR.identifier,
			dropChance = 1.0f,
			amount = 1,
		))))
		.addAdditionalModule(BehaviorConfiguration.ReinforcementInformation(
			activationThreshold = 0.85,
			delay = 100L,
			broadcastMessage = "<italic><$WATCHER_STANDARD>You cannot decipher the transmission from the incoming alien ship",
			reinforcementShips = listOf(spawnChance(WATCHERS.asSpawnedShip(VERDOLITH_REINFORCEMENT), 1.0))
		))
		.build()
	)

	val TERALITH = registerTemplate(builder(
			identifier = "TERALITH",
			template = StarshipTemplateRegistry.TERALITH,
			controllerFactory = AIControllerFactories.frigate,
			engagementRange = 2500.0
		)
		.addFactionConfiguration(AIFaction.WATCHERS)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.9))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(18000.0))
		.addRewardProvider(AITemplate.ItemRewardProviderConfiguration(listOf(ServerConfiguration.PlanetSpawnConfig.DroppedItem(
			itemString = net.horizonsend.ion.server.features.custom.items.CustomItems.SUPERCONDUCTOR.identifier,
			dropChance = 1.0f,
			amount = 1,
		))))
		.addAdditionalModule(BehaviorConfiguration.ReinforcementInformation(
			activationThreshold = 0.75,
			delay = 100L,
			broadcastMessage = "<italic><$WATCHER_STANDARD>You cannot decipher the transmission from the incoming alien ship",
			reinforcementShips = listOf(spawnChance(WATCHERS.asSpawnedShip(VERDOLITH_REINFORCEMENT), 1.0))
		))
		.addAdditionalModule(BehaviorConfiguration.ReinforcementInformation(
			activationThreshold = 0.25,
			delay = 100L,
			broadcastMessage = "<italic><$WATCHER_STANDARD>You cannot decipher the transmission from the incoming alien ship",
			reinforcementShips = listOf(spawnChance(WATCHERS.asSpawnedShip(VERDOLITH_REINFORCEMENT), 1.0))
		))
		.build()
	)
	// END_WATCHER
	// START_吃饭人
	val MALINGSHU_REINFORCEMENT = registerTemplate(builder(
		identifier = "MALINGSHU_REINFORCEMENT",
		template = StarshipTemplateRegistry.MALINGSHU,
		controllerFactory = AIControllerFactories.frigate,
		engagementRange = 2500.0
	)
		.addFactionConfiguration(吃饭人)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.9))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(9000.0))
		.addRewardProvider(AITemplate.ItemRewardProviderConfiguration(listOf(ServerConfiguration.PlanetSpawnConfig.DroppedItem(
			itemString = net.horizonsend.ion.server.features.custom.items.CustomItems.SUPERCONDUCTOR.identifier,
			dropChance = 1.0f,
			amount = 1,
		))))
		.build()
	)

	val MIANBAO_REINFORCEMENT = registerTemplate(builder(
		identifier = "MIANBAO_REINFORCEMENT",
		template = StarshipTemplateRegistry.MIANBAO,
		controllerFactory = AIControllerFactories.frigate,
		engagementRange = 2500.0
	)
		.addFactionConfiguration(吃饭人)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.9))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(9000.0))
		.addRewardProvider(AITemplate.ItemRewardProviderConfiguration(listOf(ServerConfiguration.PlanetSpawnConfig.DroppedItem(
			itemString = net.horizonsend.ion.server.features.custom.items.CustomItems.SUPERCONDUCTOR.identifier,
			dropChance = 1.0f,
			amount = 1,
		))))
		.build()
	)

	val MALINGSHU_REINFORCED = registerTemplate(builder(
		identifier = "MALINGSHU_REINFORCED",
		template = StarshipTemplateRegistry.MALINGSHU,
		controllerFactory = AIControllerFactories.frigate,
		engagementRange = 2500.0
	)
		.addFactionConfiguration(吃饭人)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.9))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(9000.0))
		.addAdditionalModule(BehaviorConfiguration.ReinforcementInformation(
			activationThreshold = 0.85,
			delay = 100L,
			broadcastMessage = "<italic><$吃饭人_STANDARD>You cannot decipher the transmission from the incoming alien ship",
			reinforcementShips = listOf(spawnChance(吃饭人.asSpawnedShip(MIANBAO_REINFORCEMENT), 1.0))
		))
		.build()
	)

	val MIANBAO_REINFORCED = registerTemplate(builder(
		identifier = "MIANBAO_REINFORCED",
		template = StarshipTemplateRegistry.MIANBAO,
		controllerFactory = AIControllerFactories.frigate,
		engagementRange = 2500.0
	)
		.addFactionConfiguration(吃饭人)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.9))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(9000.0))
		.addAdditionalModule(BehaviorConfiguration.ReinforcementInformation(
			activationThreshold = 0.85,
			delay = 100L,
			broadcastMessage = "<italic><$吃饭人_STANDARD>You cannot decipher the transmission from the incoming alien ship",
			reinforcementShips = listOf(spawnChance(吃饭人.asSpawnedShip(MALINGSHU_REINFORCEMENT), 1.0))
		))
		.build()
	)
	// END_吃饭人
	// START_PIRATE

	val ISKAT = registerTemplate(builder(
		identifier = "ISKAT",
		template = StarshipTemplateRegistry.ISKAT,
		controllerFactory = AIControllerFactories.starfighter,
		engagementRange = 750.0
	)
		.addFactionConfiguration(PIRATES)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.4))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(750.0))
		.build()
	)

	val VOSS = registerTemplate(builder(
		identifier = "VOSS",
		template = StarshipTemplateRegistry.VOSS,
		controllerFactory = AIControllerFactories.starfighter,
		engagementRange = 750.0
	)
		.addFactionConfiguration(PIRATES)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.4))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(750.0))
		.build()
	)

	val HECTOR = registerTemplate(builder(
		identifier = "HECTOR",
		template = StarshipTemplateRegistry.HECTOR,
		controllerFactory = AIControllerFactories.starfighter,
		engagementRange = 750.0
	)
		.addFactionConfiguration(PIRATES)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.4))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(750.0))
		.build()
	)

	val HIRO = registerTemplate(builder(
		identifier = "HIRO",
		template = StarshipTemplateRegistry.HIRO,
		controllerFactory = AIControllerFactories.starfighter,
		engagementRange = 750.0
	)
		.addFactionConfiguration(PIRATES)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.4))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(750.0))
		.build()
	)

	val WASP = registerTemplate(builder(
		identifier = "WASP",
		template = StarshipTemplateRegistry.WASP,
		controllerFactory = AIControllerFactories.starfighter,
		engagementRange = 750.0
	)
		.addFactionConfiguration(PIRATES)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.4))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(750.0))
		.build()
	)

	val FRENZ = registerTemplate(builder(
		identifier = "FRENZ",
		template = StarshipTemplateRegistry.FRENZ,
		controllerFactory = AIControllerFactories.starfighter,
		engagementRange = 750.0
	)
		.addFactionConfiguration(PIRATES)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.4))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(750.0))
		.build()
	)

	val TEMPEST = registerTemplate(builder(
		identifier = "ISKAT",
		template = StarshipTemplateRegistry.TEMPEST,
		controllerFactory = AIControllerFactories.starfighter,
		engagementRange = 750.0
	)
		.addFactionConfiguration(PIRATES)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.4))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(750.0))
		.build()
	)

	val VELASCO = registerTemplate(builder(
		identifier = "VELASCO",
		template = StarshipTemplateRegistry.VELASCO,
		controllerFactory = AIControllerFactories.starfighter,
		engagementRange = 750.0
	)
		.addFactionConfiguration(PIRATES)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.4))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(750.0))
		.build()
	)

	val ANAAN = registerTemplate(builder(
		identifier = "ANAAN",
		template = StarshipTemplateRegistry.ANAAN,
		controllerFactory = AIControllerFactories.gunship_pulse,
		engagementRange = 750.0
	)
		.addFactionConfiguration(PIRATES)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.5))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(1250.0))
		.build()
	)

	val VENDETTA = registerTemplate(builder(
		identifier = "VENDETTA",
		template = StarshipTemplateRegistry.VENDETTA,
		controllerFactory = AIControllerFactories.gunship_pulse,
		engagementRange = 750.0
	)
		.addFactionConfiguration(PIRATES)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.5))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(1250.0))
		.build()
	)

	val CORMORANT = registerTemplate(builder(
		identifier = "CORMORANT",
		template = StarshipTemplateRegistry.CORMORANT,
		controllerFactory = AIControllerFactories.gunship_pulse,
		engagementRange = 750.0
	)
		.addFactionConfiguration(PIRATES)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.5))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(1250.0))
		.build()
	)

	val MANTIS = registerTemplate(builder(
		identifier = "MANTIS",
		template = StarshipTemplateRegistry.MANTIS,
		controllerFactory = AIControllerFactories.gunship_pulse,
		engagementRange = 750.0
	)
		.addFactionConfiguration(PIRATES)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.5))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(1250.0))
		.build()
	)

	val HERNSTEIN = registerTemplate(builder(
		identifier = "HERNSTEIN",
		template = StarshipTemplateRegistry.HERNSTEIN,
		controllerFactory = AIControllerFactories.gunship_pulse,
		engagementRange = 750.0
	)
		.addFactionConfiguration(PIRATES)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.5))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(1250.0))
		.build()
	)

	val FYR = registerTemplate(builder(
		identifier = "FYR",
		template = StarshipTemplateRegistry.FYR,
		controllerFactory = AIControllerFactories.gunship_pulse,
		engagementRange = 750.0
	)
		.addFactionConfiguration(PIRATES)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.5))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(1250.0))
		.build()
	)

	val BLOODSTAR = registerTemplate(builder(
		identifier = "BLOODSTAR",
		template = StarshipTemplateRegistry.BLOODSTAR,
		controllerFactory = AIControllerFactories.corvette,
		engagementRange = 750.0
	)
		.addFactionConfiguration(PIRATES)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.8))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(2650.0))
		.addAdditionalModule(BehaviorConfiguration.ReinforcementInformation(
			activationThreshold = 0.85,
			delay = 100L,
			broadcastMessage = "<italic><red>Did you really think we would risk this ship without an escort fleet? We'll enjoy looting your corpse!",
			reinforcementShips = listOf(
				spawnChance(PIRATES.asSpawnedShip(CORMORANT), 1.0)
			)
		))
		.build()
	)

	// END_PIRATE
	// START_PRIVATEER

	val BULWARK = registerTemplate(builder(
		identifier = "BULWARK",
		template = StarshipTemplateRegistry.BULWARK,
		controllerFactory = AIControllerFactories.corvette,
		engagementRange = 1250.0,
	)
		.addFactionConfiguration(AIFaction.SYSTEM_DEFENSE_FORCES)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.8))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(5750.0))
		.build()
	)

	val CONTRACTOR = registerTemplate(builder(
		identifier = "CONTRACTOR",
		template = StarshipTemplateRegistry.CONTRACTOR,
		controllerFactory = AIControllerFactories.gunship_pulse,
		engagementRange = 1250.0,
	)
		.addFactionConfiguration(AIFaction.SYSTEM_DEFENSE_FORCES)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.8))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(3750.0))
		.build()
	)

	val DAGGER = registerTemplate(builder(
		identifier = "DAGGER",
		template = StarshipTemplateRegistry.DAGGER,
		controllerFactory = AIControllerFactories.starfighter,
		engagementRange = 1250.0,
	)
		.addFactionConfiguration(AIFaction.SYSTEM_DEFENSE_FORCES)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.8))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(2650.0))
		.build()
	)

	val DAYBREAK = registerTemplate(builder(
		identifier = "DAYBREAK",
		template = StarshipTemplateRegistry.DAYBREAK,
		controllerFactory = AIControllerFactories.corvette,
		engagementRange = 1250.0,
	)
		.addFactionConfiguration(AIFaction.SYSTEM_DEFENSE_FORCES)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.8))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(2650.0))
		.build()
	)

	val PATROLLER = registerTemplate(builder(
		identifier = "PATROLLER",
		template = StarshipTemplateRegistry.PATROLLER,
		controllerFactory = AIControllerFactories.gunship_pulse,
		engagementRange = 650.0,
	)
		.addFactionConfiguration(AIFaction.SYSTEM_DEFENSE_FORCES)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.8))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(1850.0))
		.build()
	)

	val PROTECTOR = registerTemplate(builder(
		identifier = "PROTECTOR",
		template = StarshipTemplateRegistry.PROTECTOR,
		controllerFactory = AIControllerFactories.gunship_pulse,
		engagementRange = 650.0,
	)
		.addFactionConfiguration(AIFaction.SYSTEM_DEFENSE_FORCES)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.8))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(950.0))
		.build()
	)

	val VETERAN = registerTemplate(builder(
		identifier = "VETERAN",
		template = StarshipTemplateRegistry.VETERAN,
		controllerFactory = AIControllerFactories.gunship_pulse,
		engagementRange = 650.0,
	)
		.addFactionConfiguration(AIFaction.SYSTEM_DEFENSE_FORCES)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.8))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(1850.0))
		.build()
	)

	val TENETA = registerTemplate(builder(
		identifier = "TENETA",
		template = StarshipTemplateRegistry.TENETA,
		controllerFactory = AIControllerFactories.starfighter,
		engagementRange = 650.0,
	)
		.addFactionConfiguration(AIFaction.SYSTEM_DEFENSE_FORCES)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.6))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(950.0))
		.build()
	)

	val FURIOUS = registerTemplate(builder(
		identifier = "FURIOUS",
		template = StarshipTemplateRegistry.FURIOUS,
		controllerFactory = AIControllerFactories.starfighter,
		engagementRange = 650.0,
	)
		.addFactionConfiguration(AIFaction.SYSTEM_DEFENSE_FORCES)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.6))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(950.0))
		.build()
	)

	val INFLICT = registerTemplate(builder(
		identifier = "INFLICT",
		template = StarshipTemplateRegistry.INFLICT,
		controllerFactory = AIControllerFactories.starfighter,
		engagementRange = 650.0,
	)
		.addFactionConfiguration(AIFaction.SYSTEM_DEFENSE_FORCES)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.6))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(950.0))
		.build()
	)

	// END_PRIVATEER
	// START_EXPLORER

	val WAYFINDER = registerTemplate(builder(
		identifier = "WAYFINDER",
		template = StarshipTemplateRegistry.WAYFINDER,
		controllerFactory = AIControllerFactories.passive_cruise,
		engagementRange = 750.0
	)
		.addFactionConfiguration(AIFaction.PERSEUS_EXPLORERS)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.25))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(400.0))
		.build()
	)

	val STRIKER = registerTemplate(builder(
		identifier = "STRIKER",
		template = StarshipTemplateRegistry.STRIKER,
		controllerFactory = AIControllerFactories.passive_cruise,
		engagementRange = 750.0
	)
		.addFactionConfiguration(AIFaction.PERSEUS_EXPLORERS)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.25))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(250.0))
		.build()
	)

	val NIMBLE = registerTemplate(builder(
		identifier = "NIMBLE",
		template = StarshipTemplateRegistry.NIMBLE,
		controllerFactory = AIControllerFactories.passive_cruise,
		engagementRange = 750.0
	)
		.addFactionConfiguration(AIFaction.PERSEUS_EXPLORERS)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.25))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(250.0))
		.build()
	)

	val DESSLE = registerTemplate(builder(
		identifier = "DESSLE",
		template = StarshipTemplateRegistry.DESSLE,
		controllerFactory = AIControllerFactories.passive_cruise,
		engagementRange = 750.0
	)
		.addFactionConfiguration(AIFaction.PERSEUS_EXPLORERS)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.25))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(550.0))
		.build()
	)

	val MINHAUL_CHETHERITE = registerTemplate(builder(
		identifier = "MINHAUL_CHETHERITE",
		template = StarshipTemplateRegistry.MINHAUL_CHETHERITE,
		controllerFactory = AIControllerFactories.passive_cruise,
		engagementRange = 750.0
	)
		.addFactionConfiguration(AIFaction.PERSEUS_EXPLORERS)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.25))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(250.0))
		.build()
	)

	val MINHAUL_REDSTONE = registerTemplate(builder(
		identifier = "MINHAUL_CHETHERITE",
		template = StarshipTemplateRegistry.MINHAUL_REDSTONE,
		controllerFactory = AIControllerFactories.passive_cruise,
		engagementRange = 750.0
	)
		.addFactionConfiguration(AIFaction.PERSEUS_EXPLORERS)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.25))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(250.0))
		.build()
	)

	val MINHAUL_TITANIUM = registerTemplate(builder(
		identifier = "MINHAUL_TITANIUM",
		template = StarshipTemplateRegistry.MINHAUL_TITANIUM,
		controllerFactory = AIControllerFactories.passive_cruise,
		engagementRange = 750.0
	)
		.addFactionConfiguration(AIFaction.PERSEUS_EXPLORERS)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.25))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(250.0))
		.build()
	)

	val EXOTRAN_TITANIUM = registerTemplate(builder(
		identifier = "EXOTRAN_TITANIUM",
		template = StarshipTemplateRegistry.EXOTRAN_TITANIUM,
		controllerFactory = AIControllerFactories.passive_cruise,
		engagementRange = 750.0
	)
		.addFactionConfiguration(AIFaction.PERSEUS_EXPLORERS)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.25))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(550.0))
		.build()
	)

	val EXOTRAN_CHETHERITE = registerTemplate(builder(
		identifier = "EXOTRAN_CHETHERITE",
		template = StarshipTemplateRegistry.EXOTRAN_CHETHERITE,
		controllerFactory = AIControllerFactories.passive_cruise,
		engagementRange = 750.0
	)
		.addFactionConfiguration(AIFaction.PERSEUS_EXPLORERS)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.25))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(550.0))
		.build()
	)

	val EXOTRAN_REDSTONE = registerTemplate(builder(
		identifier = "EXOTRAN_REDSTONE",
		template = StarshipTemplateRegistry.EXOTRAN_REDSTONE,
		controllerFactory = AIControllerFactories.passive_cruise,
		engagementRange = 750.0
	)
		.addFactionConfiguration(AIFaction.PERSEUS_EXPLORERS)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.25))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(550.0))
		.build()
	)

	val AMPH = registerTemplate(builder(
		identifier = "AMPH",
		template = StarshipTemplateRegistry.AMPH,
		controllerFactory = AIControllerFactories.passive_cruise,
		engagementRange = 750.0
	)
		.addFactionConfiguration(AIFaction.PERSEUS_EXPLORERS)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.25))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(400.0))
		.build()
	)

	// END_EXPLORER
	// START_MINING_GUILD

	val OSTRICH = registerTemplate(builder(
		identifier = "OSTRICH",
		template = StarshipTemplateRegistry.OSTRICH,
		controllerFactory = AIControllerFactories.starfighter,
		engagementRange = 500.0
	)
		.addFactionConfiguration(AIFaction.MINING_GUILD)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.6))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(2650.0))
		.addAdditionalModule(BehaviorConfiguration.ReinforcementInformation(
			activationThreshold = 0.75,
			delay = 100L,
			broadcastMessage = "$miningGuildMini<${HEColorScheme.HE_MEDIUM_GRAY}> backup request acknowledged. {0} responding at {1}, {3}, in {4}",
			reinforcementShips = listOf(spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(BULWARK), 1.0))
		))
		.build()
	)

	val WOODPECKER = registerTemplate(builder(
		identifier = "WOODPECKER",
		template = StarshipTemplateRegistry.WOODPECKER,
		controllerFactory = AIControllerFactories.starfighter,
		engagementRange = 500.0
	)
		.addFactionConfiguration(AIFaction.MINING_GUILD)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.6))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(650.0))
		.addAdditionalModule(BehaviorConfiguration.ReinforcementInformation(
			activationThreshold = 0.65,
			delay = 100L,
			broadcastMessage = "$miningGuildMini<${HEColorScheme.HE_MEDIUM_GRAY}> backup request acknowledged. {0} responding at {1}, {3}, in {4}",
			reinforcementShips = listOf(spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(DAGGER), 1.0))
		))
		.build()
	)

	val BEAVER = registerTemplate(builder(
		identifier = "BEAVER",
		template = StarshipTemplateRegistry.BEAVER,
		controllerFactory = AIControllerFactories.starfighter,
		engagementRange = 500.0
	)
		.addFactionConfiguration(AIFaction.MINING_GUILD)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.6))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(1850.0))
		.addAdditionalModule(BehaviorConfiguration.ReinforcementInformation(
			activationThreshold = 0.5,
			delay = 100L,
			broadcastMessage = "$miningGuildMini<${HEColorScheme.HE_MEDIUM_GRAY}> backup request acknowledged. {0} responding at {1}, {3}, in {4}",
			reinforcementShips = listOf(spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(DAGGER), 1.0))
		))
		.build()
	)

	val BADGER = registerTemplate(builder(
		identifier = "BADGER",
		template = StarshipTemplateRegistry.BADGER,
		controllerFactory = AIControllerFactories.starfighter,
		engagementRange = 500.0
	)
		.addFactionConfiguration(AIFaction.MINING_GUILD)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.6))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(2560.0))
		.addAdditionalModule(BehaviorConfiguration.ReinforcementInformation(
			activationThreshold = 0.75,
			delay = 100L,
			broadcastMessage = "$miningGuildMini<${HEColorScheme.HE_MEDIUM_GRAY}> backup request acknowledged. {0} responding at {1}, {3}, in {4}",
			reinforcementShips = listOf(spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(BULWARK), 1.0))
		))
		.build()
	)

	val TYPE_V11 = registerTemplate(builder(
		identifier = "TYPE_V11",
		template = StarshipTemplateRegistry.TYPE_V11,
		controllerFactory = AIControllerFactories.starfighter,
		engagementRange = 500.0
	)
		.addFactionConfiguration(AIFaction.MINING_GUILD)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.6))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(650.0))
		.addAdditionalModule(BehaviorConfiguration.ReinforcementInformation(
			activationThreshold = 0.55,
			delay = 100L,
			broadcastMessage = "$miningGuildMini<${HEColorScheme.HE_MEDIUM_GRAY}> backup request acknowledged. {0} responding at {1}, {3}, in {4}",
			reinforcementShips = listOf(spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(DAGGER), 1.0))
		))
		.build()
	)

	val TYPEA21B = registerTemplate(builder(
		identifier = "TYPEA21B",
		template = StarshipTemplateRegistry.TYPEA21B,
		controllerFactory = AIControllerFactories.starfighter,
		engagementRange = 500.0
	)
		.addFactionConfiguration(AIFaction.MINING_GUILD)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.6))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(650.0))
		.addAdditionalModule(BehaviorConfiguration.ReinforcementInformation(
			activationThreshold = 0.55,
			delay = 100L,
			broadcastMessage = "$miningGuildMini<${HEColorScheme.HE_MEDIUM_GRAY}> backup request acknowledged. {0} responding at {1}, {3}, in {4}",
			reinforcementShips = listOf(spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(DAGGER), 1.0))
		))
		.build()
	)

	val TYPEI41 = registerTemplate(builder(
		identifier = "TYPEI41",
		template = StarshipTemplateRegistry.TYPEI41,
		controllerFactory = AIControllerFactories.starfighter,
		engagementRange = 500.0
	)
		.addFactionConfiguration(AIFaction.MINING_GUILD)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.6))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(650.0))
		.addAdditionalModule(BehaviorConfiguration.ReinforcementInformation(
			activationThreshold = 0.55,
			delay = 100L,
			broadcastMessage = "$miningGuildMini<${HEColorScheme.HE_MEDIUM_GRAY}> backup request acknowledged. {0} responding at {1}, {3}, in {4}",
			reinforcementShips = listOf(spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(DAGGER), 1.0))
		))
		.build()
	)

	// END_MINING_GUILD
	// START_TSAII

	val SWARMER = registerTemplate(builder(
		identifier = "SWARMER",
		template = StarshipTemplateRegistry.SWARMER,
		controllerFactory = AIControllerFactories.starfighter,
		engagementRange = 1000.0
	)
		.addFactionConfiguration(AIFaction.TSAII_RAIDERS)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.7))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(1550.0))
		.build()
	)

	val SCYTHE = registerTemplate(builder(
		identifier = "SCYTHE",
		template = StarshipTemplateRegistry.SCYTHE,
		controllerFactory = AIControllerFactories.starfighter,
		engagementRange = 1000.0
	)
		.addFactionConfiguration(AIFaction.TSAII_RAIDERS)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.7))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(1550.0))
		.build()
	)

	val RAIDER = registerTemplate(builder(
		identifier = "RAIDER",
		template = StarshipTemplateRegistry.RAIDER,
		controllerFactory = AIControllerFactories.gunship_pulse,
		engagementRange = 1000.0
	)
		.addFactionConfiguration(AIFaction.TSAII_RAIDERS)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.7))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(2500.0))
		.build()
	)

	val REAVER = registerTemplate(builder(
		identifier = "REAVER",
		template = StarshipTemplateRegistry.REAVER,
		controllerFactory = AIControllerFactories.frigate,
		engagementRange = 1000.0
	)
		.addFactionConfiguration(AIFaction.TSAII_RAIDERS)
		.addRewardProvider(AITemplate.SLXPRewardProviderConfiguration(0.7))
		.addRewardProvider(AITemplate.CreditRewardProviderConfiguration(6500.0))
		.addAdditionalModule(BehaviorConfiguration.ReinforcementInformation(
			activationThreshold = 0.85,
			delay = 100L,
			broadcastMessage = "<italic><red>Did you really think we would risk this ship without an escort fleet? We'll enjoy looting your corpse!",
			reinforcementShips = listOf(spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(RAIDER), 1.0))
		))
		.build()
	)

	// END_TSAII
	fun registerTemplate(template: AITemplate): AITemplate {
		templates[template.identifier] = template

		return template
	}

	private fun builder(identifier: String, template: StarshipTemplate, controllerFactory: AIControllerFactory, engagementRange: Double): Builder = Builder(identifier, controllerFactory, template, engagementRange)

	class Builder(val identifier: String, val controllerFactory: AIControllerFactory, val template: StarshipTemplate, val engagementRange: Double) {
		private val additionalModules: MutableList<BehaviorConfiguration.AdditionalModule> = mutableListOf()
		private val rewardProviders: MutableList<AITemplate.AIRewardsProviderConfiguration> = mutableListOf()

		fun addRewardProvider(provider: AITemplate.AIRewardsProviderConfiguration): Builder {
			rewardProviders += provider
			return this
		}

		fun addAdditionalModule(module: BehaviorConfiguration.AdditionalModule): Builder {
			additionalModules += module
			return this
		}

		fun addFactionConfiguration(faction: AIFaction): Builder {
			faction.processTemplate(this)

			return this
		}

		fun build(): AITemplate {
			return AITemplate(
				identifier = this.identifier,
				starshipInfo = this.template,
				behaviorInformation = BehaviorConfiguration(
					controllerFactory = this.controllerFactory.identifier,
					engagementRange = this.engagementRange,
					additionalModules = this.additionalModules
				),
				rewardProviders = this.rewardProviders
			)
		}
	}

	/**
	 * Formats a SpawningInformationHolder for the specified template and probability
	 **/
	fun spawnChance(template: SpawnedShip, probability: Double): AITemplate.SpawningInformationHolder = AITemplate.SpawningInformationHolder(template, probability)
}
