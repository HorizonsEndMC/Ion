package net.horizonsend.ion.server.features.ai.spawning.spawner

import com.google.common.collect.Multimap
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.configuration.StaticIntegerAmount
import net.horizonsend.ion.server.features.ai.configuration.WorldSettings
import net.horizonsend.ion.server.features.ai.faction.AIFaction
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.EXPLORER_LIGHT_CYAN
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.MINING_GUILD
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.PERSEUS_EXPLORERS
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.PIRATES
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.PIRATE_SATURATED_RED
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.PRIVATEER_LIGHT_TEAL
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.SYSTEM_DEFENSE_FORCES
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.TSAII_DARK_ORANGE
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.TSAII_MEDIUM_ORANGE
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.TSAII_RAIDERS
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.WATCHERS
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.miningGuildMini
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.吃饭人_STANDARD
import net.horizonsend.ion.server.features.ai.spawning.formatLocationSupplier
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.BagSpawner
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.MultiSpawner
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.SingleSpawn
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.WeightedShipSupplier
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.DAGGER
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.MALINGSHU_REINFORCED
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.MIANBAO_REINFORCED
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.TERALITH
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.VERDOLITH_REINFORCED
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.VERDOLITH_REINFORCEMENT
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.spawnChance
import net.horizonsend.ion.server.miscellaneous.utils.multimapOf
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.world.WorldInitEvent

object AISpawners : IonServerComponent(true) {
	/**
	 * For variety, the spawners are defined in the code, but they get their ship configuration and spawn rates, etc. from configuration files.
	 **/
	private val spawners = mutableListOf<AISpawner>()

	fun getAllSpawners(): List<AISpawner> = spawners

	/**
	 * Registers a spawner that has a single instance for every world
	 *
	 *
	 **/
	private fun <T: AISpawner> registerGlobalSpawner(spawner: T): T {
		spawners += spawner

		return spawner
	}

	private val perWorldSpawners: MutableList<(World) -> AISpawner> = mutableListOf()

	/**
	 * Registers a spawner that has an instance for every world
	 **/
	fun registerPerWorldSpawner(create: (World) -> AISpawner) {
		perWorldSpawners += create
	}

	private val singleWorldSpawners: Multimap<String, (World) -> AISpawner> = multimapOf()

	/**
	 * Registers a spawner that has a single instance for a single world
	 **/
	fun registerSingleWorldSpawner(worldName: String, create: (World) -> AISpawner) {
		singleWorldSpawners[worldName].add(create)
	}

	/**
	 * Registers a spawner that has a single instance for a single world
	 **/
	fun registerSingleWorldSpawner(vararg worldName: String, create: (World) -> AISpawner) {
		for (world in worldName) {
			singleWorldSpawners[world].add(create)
		}
	}

	// Initialize all the spawners individual to the world when the world loads
	@EventHandler
	fun onWorldInitialize(event: WorldInitEvent) {
		val name = event.world.name

		spawners.addAll(singleWorldSpawners[name].map { it.invoke(event.world) })
	}

	// Run after tick is true
	override fun onEnable() {
		// Initialize all the per world spawners, after the worlds have all initialzed
		for (world in IonServer.server.worlds) {
			spawners.addAll(perWorldSpawners.map { it.invoke(world) })
		}
	}

	val WATCHER_SPAWNER = registerSingleWorldSpawner("Trench", "AU-0821") {
		SingleWorldSpawner(
			"WATCHER_SPAWNER",
			it,
			pointChance = 0.5,
			pointThreshold = 20 * 60 * 7,
			log,
			SingleSpawn(
				log,
				WeightedShipSupplier(
					spawnChance(VERDOLITH_REINFORCED, 0.75),
					spawnChance(TERALITH, 0.25)
				),
				formatLocationSupplier(it, 2500.0, 4500.0),
				WATCHERS.controllerModifier,
				WATCHERS::getAvailableName,
			)
		)
	}

	val WATCHER_BAG_SPAWNER = registerSingleWorldSpawner("Trench", "AU-0821") {
		SingleWorldSpawner(
			"WATCHER_BAG_SPAWNER",
			it,
			pointChance = 0.5,
			pointThreshold = 20 * 60 * 7,
			log,
			BagSpawner(
				log,
				formatLocationSupplier(it, 2500.0, 4500.0),
				StaticIntegerAmount(100),
				listOf(
					BagSpawner.BagSpawnShip(MultiSpawner.GroupSpawnedShip(
						VERDOLITH_REINFORCEMENT,
						WATCHERS::getAvailableName,
						WATCHERS.controllerModifier,
					), 5),
					BagSpawner.BagSpawnShip(MultiSpawner.GroupSpawnedShip(
						TERALITH,
						WATCHERS::getAvailableName,
						WATCHERS.controllerModifier,
					), 10)
				)
			)
		)
	}

	val DAGGER_SWARM = registerSingleWorldSpawner("Trench", "AU-0821") {
		SingleWorldSpawner(
			"DAGGER_SWARM",
			it,
			pointChance = 0.5,
			pointThreshold = 20 * 60 * 7,
			log,
			BagSpawner(
				log,
				formatLocationSupplier(it, 2500.0, 4500.0),
				StaticIntegerAmount(100),
				listOf(
					BagSpawner.BagSpawnShip(MultiSpawner.GroupSpawnedShip(
						DAGGER,
						WATCHERS::getAvailableName,
						WATCHERS.controllerModifier,
					), 5))
			)
		)
	}

	val 吃饭人_BASIC = registerGlobalSpawner(StandardFactionSpawner(
		"吃饭人_BASIC",
		logger = log,
		faction = AIFaction.吃饭人,
		spawnMessage = "<${吃饭人_STANDARD}>An unknown starship signature is being broadcast in {4} at {1}, {3}".miniMessage(),
		pointChance = 0.5,
		pointThreshold = 20 * 60 * 7,
		worlds = listOf(
			WorldSettings(
				worldName = "Trench",
				probability = 0.5,
				minDistanceFromPlayer = 2500.0,
				maxDistanceFromPlayer = 4500.0,
				templates = listOf(
					spawnChance(MIANBAO_REINFORCED, 0.5),
					spawnChance(MALINGSHU_REINFORCED, 0.5)
				)
			),
			WorldSettings(
				worldName = "AU-0821",
				probability = 0.5,
				minDistanceFromPlayer = 2500.0,
				maxDistanceFromPlayer = 4500.0,
				templates = listOf(
					spawnChance(MIANBAO_REINFORCED, 0.5),
					spawnChance(MALINGSHU_REINFORCED, 0.5)
				)
			)
		)
	))

	val PIRATE_BASIC = registerGlobalSpawner(StandardFactionSpawner(
		"PIRATE_BASIC",
		logger = log,
		faction = PIRATES,
		spawnMessage = "<${HE_MEDIUM_GRAY}>A pirate {0} has been identified in the area of {1}, {3}, in {4}. <$PIRATE_SATURATED_RED>Please avoid the sector until the threat has been cleared.".miniMessage(),
		pointChance = 0.5,
		pointThreshold = 10000,
		worlds = listOf(
			WorldSettings(
				worldName = "Asteri",
				probability = 0.15,
				minDistanceFromPlayer = 2000.0,
				maxDistanceFromPlayer = 4000.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.ISKAT, 0.2),
					spawnChance(AITemplateRegistry.VOSS, 0.2),
					spawnChance(AITemplateRegistry.HECTOR, 0.4),
					spawnChance(AITemplateRegistry.HIRO, 0.4),
					spawnChance(AITemplateRegistry.WASP, 0.4)
				)
			),
			WorldSettings(
				worldName = "Regulus",
				probability = 0.25,
				minDistanceFromPlayer = 2000.0,
				maxDistanceFromPlayer = 4000.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.ISKAT, 0.2),
					spawnChance(AITemplateRegistry.VOSS, 0.2),
					spawnChance(AITemplateRegistry.HECTOR, 0.4),
					spawnChance(AITemplateRegistry.HIRO, 0.4),
					spawnChance(AITemplateRegistry.WASP, 0.4),
					spawnChance(AITemplateRegistry.FRENZ, 0.4),
					spawnChance(AITemplateRegistry.TEMPEST, 0.2),
					spawnChance(AITemplateRegistry.VELASCO, 0.2),
					spawnChance(AITemplateRegistry.VENDETTA, 0.2),
					spawnChance(AITemplateRegistry.ANAAN, 0.2),
					spawnChance(AITemplateRegistry.CORMORANT, 0.2),
				)
			),
			WorldSettings(
				worldName = "Sirius",
				probability = 0.15,
				minDistanceFromPlayer = 2000.0,
				maxDistanceFromPlayer = 4000.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.ISKAT, 0.2),
					spawnChance(AITemplateRegistry.VOSS, 0.2),
					spawnChance(AITemplateRegistry.HECTOR, 0.4),
					spawnChance(AITemplateRegistry.HIRO, 0.4),
					spawnChance(AITemplateRegistry.WASP, 0.4),
					spawnChance(AITemplateRegistry.FRENZ, 0.4),
					spawnChance(AITemplateRegistry.TEMPEST, 0.2),
					spawnChance(AITemplateRegistry.VELASCO, 0.2)
				)
			),
			WorldSettings(
				worldName = "Ilios",
				probability = 0.15,
				minDistanceFromPlayer = 2000.0,
				maxDistanceFromPlayer = 4000.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.ISKAT, 0.2),
					spawnChance(AITemplateRegistry.VOSS, 0.2),
					spawnChance(AITemplateRegistry.HECTOR, 0.4),
					spawnChance(AITemplateRegistry.HIRO, 0.4),
					spawnChance(AITemplateRegistry.WASP, 0.4),
					spawnChance(AITemplateRegistry.FRENZ, 0.4),
					spawnChance(AITemplateRegistry.TEMPEST, 0.2),
					spawnChance(AITemplateRegistry.VELASCO, 0.2),
					spawnChance(AITemplateRegistry.VENDETTA, 0.2),
					spawnChance(AITemplateRegistry.ANAAN, 0.2),
					spawnChance(AITemplateRegistry.CORMORANT, 0.2)
				)
			),
			WorldSettings(
				worldName = "Horizon",
				probability = 0.15,
				minDistanceFromPlayer = 2000.0,
				maxDistanceFromPlayer = 4000.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.VENDETTA, 0.2),
					spawnChance(AITemplateRegistry.ANAAN, 0.2),
					spawnChance(AITemplateRegistry.CORMORANT, 0.2),
					spawnChance(AITemplateRegistry.MANTIS, 0.2),
					spawnChance(AITemplateRegistry.HERNSTEIN, 0.2),
					spawnChance(AITemplateRegistry.FYR, 0.2),
					spawnChance(AITemplateRegistry.BLOODSTAR, 0.2)
				)
			),
			WorldSettings(
				worldName = "Trench",
				probability = 0.15,
				minDistanceFromPlayer = 2000.0,
				maxDistanceFromPlayer = 4000.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.VENDETTA, 0.2),
					spawnChance(AITemplateRegistry.ANAAN, 0.2),
					spawnChance(AITemplateRegistry.CORMORANT, 0.2),
					spawnChance(AITemplateRegistry.MANTIS, 0.2),
					spawnChance(AITemplateRegistry.HERNSTEIN, 0.2),
					spawnChance(AITemplateRegistry.FYR, 0.2),
					spawnChance(AITemplateRegistry.BLOODSTAR, 0.2)
				)
			),
			WorldSettings(
				worldName = "AU-0821",
				probability = 0.15,
				minDistanceFromPlayer = 2000.0,
				maxDistanceFromPlayer = 4000.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.VENDETTA, 0.2),
					spawnChance(AITemplateRegistry.ANAAN, 0.2),
					spawnChance(AITemplateRegistry.CORMORANT, 0.2),
					spawnChance(AITemplateRegistry.MANTIS, 0.2),
					spawnChance(AITemplateRegistry.HERNSTEIN, 0.2),
					spawnChance(AITemplateRegistry.FYR, 0.2),
					spawnChance(AITemplateRegistry.BLOODSTAR, 0.2)
				)
			)
		)
	))

	private fun explorerWorld(worldName: String, probability: Double): WorldSettings = WorldSettings(
		worldName = worldName,
		probability = probability,
		minDistanceFromPlayer = 1500.0,
		maxDistanceFromPlayer = 3500.0,
		templates = listOf(
			spawnChance(AITemplateRegistry.WAYFINDER, 0.35),
			spawnChance(AITemplateRegistry.STRIKER, 0.3),
			spawnChance(AITemplateRegistry.NIMBLE, 0.2),
			spawnChance(AITemplateRegistry.DESSLE, 0.2),
			spawnChance(AITemplateRegistry.MINHAUL_CHETHERITE, 0.15),
			spawnChance(AITemplateRegistry.MINHAUL_REDSTONE, 0.1),
			spawnChance(AITemplateRegistry.MINHAUL_TITANIUM, 0.1),
			spawnChance(AITemplateRegistry.EXOTRAN_CHETHERITE, 0.15),
			spawnChance(AITemplateRegistry.EXOTRAN_REDSTONE, 0.1),
			spawnChance(AITemplateRegistry.EXOTRAN_TITANIUM, 0.1),
			spawnChance(AITemplateRegistry.AMPH, 0.35)
		)
	)

	val EXPLORER_BASIC = registerGlobalSpawner(StandardFactionSpawner(
		"EXPLORER_BASIC",
		logger = log,
		PERSEUS_EXPLORERS,
		spawnMessage = "<$EXPLORER_LIGHT_CYAN>Horizon Transit Lines<${HE_MEDIUM_GRAY}> {0} spawned at {1}, {3}, in {4}".miniMessage(),
		pointChance = 0.75,
		pointThreshold = 20 * 60 * 10,
		worlds = listOf(
			explorerWorld("Asteri", 0.2),
			explorerWorld("Sirius", 0.11),
			explorerWorld("Regulus", 0.2),
			explorerWorld("Ilios", 0.135),
			explorerWorld("Horizon", 0.27),
			explorerWorld("Trench", 0.055),
			explorerWorld("AU-0821", 0.055),
		)
	))

	val MINING_GUILD_BASIC = registerGlobalSpawner(StandardFactionSpawner(
		"MINING_GUILD_BASIC",
		logger = log,
		MINING_GUILD,
		spawnMessage = "$miningGuildMini <${HE_MEDIUM_GRAY}>extraction vessel {0} spawned at {1}, {3}, in {4}".miniMessage(),
		pointChance = 0.8,
		pointThreshold = 8400,
		worlds = listOf(
			WorldSettings(
				worldName = "Asteri",
				probability = 0.2,
				minDistanceFromPlayer = 1000.0,
				maxDistanceFromPlayer = 2500.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.WOODPECKER, 0.22),
					spawnChance(AITemplateRegistry.TYPE_V11, 0.22),
					spawnChance(AITemplateRegistry.TYPEA21B, 0.22),
					spawnChance(AITemplateRegistry.TYPEI41, 0.22),
					spawnChance(AITemplateRegistry.BEAVER, 0.12)
				)
			),
			WorldSettings(
				worldName = "Sirius",
				probability = 0.11,
				minDistanceFromPlayer = 1000.0,
				maxDistanceFromPlayer = 2500.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.WOODPECKER, 0.22),
					spawnChance(AITemplateRegistry.TYPE_V11, 0.22),
					spawnChance(AITemplateRegistry.TYPEA21B, 0.22),
					spawnChance(AITemplateRegistry.TYPEI41, 0.22),
					spawnChance(AITemplateRegistry.BEAVER, 0.12)
				)
			),
			WorldSettings(
				worldName = "Regulus",
				probability = 0.2,
				minDistanceFromPlayer = 1000.0,
				maxDistanceFromPlayer = 2500.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.WOODPECKER, 0.22),
					spawnChance(AITemplateRegistry.TYPE_V11, 0.22),
					spawnChance(AITemplateRegistry.TYPEA21B, 0.22),
					spawnChance(AITemplateRegistry.TYPEI41, 0.22),
					spawnChance(AITemplateRegistry.BEAVER, 0.12),
					spawnChance(AITemplateRegistry.OSTRICH, 0.05),
					spawnChance(AITemplateRegistry.BADGER, 0.05),
				)
			),
			WorldSettings(
				worldName = "Ilios",
				probability = 0.13,
				minDistanceFromPlayer = 1000.0,
				maxDistanceFromPlayer = 2500.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.WOODPECKER, 0.22),
					spawnChance(AITemplateRegistry.TYPE_V11, 0.22),
					spawnChance(AITemplateRegistry.TYPEA21B, 0.22),
					spawnChance(AITemplateRegistry.TYPEI41, 0.22),
					spawnChance(AITemplateRegistry.BEAVER, 0.12)
				)
			),
			WorldSettings(
				worldName = "Horizon",
				probability = 0.27,
				minDistanceFromPlayer = 1000.0,
				maxDistanceFromPlayer = 2500.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.WOODPECKER, 0.12),
					spawnChance(AITemplateRegistry.TYPE_V11, 0.12),
					spawnChance(AITemplateRegistry.TYPEA21B, 0.12),
					spawnChance(AITemplateRegistry.TYPEI41, 0.12),
					spawnChance(AITemplateRegistry.BEAVER, 0.22),
					spawnChance(AITemplateRegistry.OSTRICH, 0.15),
					spawnChance(AITemplateRegistry.BADGER, 0.15),
				)
			),
			WorldSettings(worldName = "Trench",
				probability = 0.75,
				minDistanceFromPlayer = 1000.0,
				maxDistanceFromPlayer = 2500.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.WOODPECKER, 0.12),
					spawnChance(AITemplateRegistry.TYPE_V11, 0.12),
					spawnChance(AITemplateRegistry.TYPEA21B, 0.12),
					spawnChance(AITemplateRegistry.TYPEI41, 0.12),
					spawnChance(AITemplateRegistry.BEAVER, 0.22),
					spawnChance(AITemplateRegistry.OSTRICH, 0.15),
					spawnChance(AITemplateRegistry.BADGER, 0.15),
				)
			),
			WorldSettings(
				worldName = "AU-0821",
				probability = 0.05,
				minDistanceFromPlayer = 1000.0,
				maxDistanceFromPlayer = 2500.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.WOODPECKER, 0.12),
					spawnChance(AITemplateRegistry.TYPE_V11, 0.12),
					spawnChance(AITemplateRegistry.TYPEA21B, 0.12),
					spawnChance(AITemplateRegistry.TYPEI41, 0.12),
					spawnChance(AITemplateRegistry.BEAVER, 0.22),
					spawnChance(AITemplateRegistry.OSTRICH, 0.15),
					spawnChance(AITemplateRegistry.BADGER, 0.15),
				)
			)
		)
	))

	val PRIVATEER_BASIC = registerGlobalSpawner(StandardFactionSpawner(
		"PRIVATEER_BASIC",
		logger = log,
		faction = SYSTEM_DEFENSE_FORCES,
		spawnMessage = "<$PRIVATEER_LIGHT_TEAL>Privateer patrol <${HE_MEDIUM_GRAY}>operation vessel {0} spawned at {1}, {3}, in {4}".miniMessage(),
		pointChance = 0.5,
		pointThreshold = 12000,
		worlds = listOf(
			WorldSettings(
				worldName = "Asteri",
				probability = 0.15,
				minDistanceFromPlayer = 1000.0,
				maxDistanceFromPlayer = 2500.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.PROTECTOR, 0.12),
					spawnChance(AITemplateRegistry.FURIOUS, 0.12),
					spawnChance(AITemplateRegistry.INFLICT, 0.12),
					spawnChance(AITemplateRegistry.VETERAN, 0.12),
					spawnChance(AITemplateRegistry.PATROLLER, 0.12),
					spawnChance(AITemplateRegistry.TENETA, 0.12)
				)
			),
			WorldSettings(
				worldName = "Sirius",
				probability = 0.2,
				minDistanceFromPlayer = 1000.0,
				maxDistanceFromPlayer = 2500.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.PROTECTOR, 0.12),
					spawnChance(AITemplateRegistry.FURIOUS, 0.12),
					spawnChance(AITemplateRegistry.INFLICT, 0.12),
					spawnChance(AITemplateRegistry.VETERAN, 0.12),
					spawnChance(AITemplateRegistry.PATROLLER, 0.12),
					spawnChance(AITemplateRegistry.DAYBREAK, 0.12),
					spawnChance(AITemplateRegistry.TENETA, 0.12),
					spawnChance(AITemplateRegistry.CONTRACTOR, 0.05)
				)
			),
			WorldSettings(
				worldName = "Regulus",
				probability = 0.3,
				minDistanceFromPlayer = 1000.0,
				maxDistanceFromPlayer = 2500.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.INFLICT, 0.12),
					spawnChance(AITemplateRegistry.VETERAN, 0.12),
					spawnChance(AITemplateRegistry.PATROLLER, 0.12),
					spawnChance(AITemplateRegistry.DAYBREAK, 0.12),
					spawnChance(AITemplateRegistry.TENETA, 0.12),
					spawnChance(AITemplateRegistry.BULWARK, 0.12),
					spawnChance(AITemplateRegistry.CONTRACTOR, 0.12),
					spawnChance(AITemplateRegistry.DAGGER, 0.12)
				)
			),
			WorldSettings(
				worldName = "Ilios",
				probability = 0.1,
				minDistanceFromPlayer = 1000.0,
				maxDistanceFromPlayer = 2500.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.INFLICT, 0.12),
					spawnChance(AITemplateRegistry.VETERAN, 0.12),
					spawnChance(AITemplateRegistry.PATROLLER, 0.12),
					spawnChance(AITemplateRegistry.TENETA, 0.12),
					spawnChance(AITemplateRegistry.CONTRACTOR, 0.12),
					spawnChance(AITemplateRegistry.DAYBREAK, 0.12)
				)
			),
			WorldSettings(
				worldName = "Horizon",
				probability = 0.1,
				minDistanceFromPlayer = 1000.0,
				maxDistanceFromPlayer = 2500.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.VETERAN, 0.12),
					spawnChance(AITemplateRegistry.PATROLLER, 0.12),
					spawnChance(AITemplateRegistry.TENETA, 0.12),
					spawnChance(AITemplateRegistry.BULWARK, 0.12),
					spawnChance(AITemplateRegistry.CONTRACTOR, 0.12),
					spawnChance(AITemplateRegistry.DAGGER, 0.12),
					spawnChance(AITemplateRegistry.DAYBREAK, 0.12)
				)
			),
			WorldSettings(worldName = "Trench",
				probability = 0.05,
				minDistanceFromPlayer = 1000.0,
				maxDistanceFromPlayer = 2500.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.VETERAN, 0.12),
					spawnChance(AITemplateRegistry.PATROLLER, 0.12),
					spawnChance(AITemplateRegistry.TENETA, 0.12),
					spawnChance(AITemplateRegistry.BULWARK, 0.12),
					spawnChance(AITemplateRegistry.CONTRACTOR, 0.12),
					spawnChance(AITemplateRegistry.DAGGER, 0.12),
					spawnChance(AITemplateRegistry.DAYBREAK, 0.12)
				)
			),
			WorldSettings(
				worldName = "AU-0821",
				probability = 0.05,
				minDistanceFromPlayer = 1000.0,
				maxDistanceFromPlayer = 2500.0,
				templates = listOf(
					spawnChance(AITemplateRegistry.VETERAN, 0.10),
					spawnChance(AITemplateRegistry.PATROLLER, 0.10),
					spawnChance(AITemplateRegistry.TENETA, 0.10),
					spawnChance(AITemplateRegistry.BULWARK, 0.12),
					spawnChance(AITemplateRegistry.CONTRACTOR, 0.12),
					spawnChance(AITemplateRegistry.DAGGER, 0.12),
					spawnChance(AITemplateRegistry.DAYBREAK, 0.12)
				)
			)
		)
	))

	val TSAII_BASIC = registerGlobalSpawner(StandardFactionSpawner(
		"TSAII_BASIC",
		logger = log,
		faction = TSAII_RAIDERS,
		spawnMessage = "<${TSAII_DARK_ORANGE}>Dangerous Tsaii Raiders {0} has been reported in the area of {1}, {3}, in {4}. <$TSAII_MEDIUM_ORANGE>Please avoid the sector until the threat has been cleared!".miniMessage(),
		pointThreshold = 30 * 20 * 60,
		pointChance = 0.5,
		worlds = listOf(
			WorldSettings(
				worldName = "Horizon",
				minDistanceFromPlayer = 2500.0,
				maxDistanceFromPlayer = 4500.0,
				probability = 0.4,
				templates = listOf(
					spawnChance(AITemplateRegistry.RAIDER, 0.25),
					spawnChance(AITemplateRegistry.SCYTHE, 0.25),
					spawnChance(AITemplateRegistry.SWARMER, 0.25),
					spawnChance(AITemplateRegistry.REAVER, 0.25)
				)
			),
			WorldSettings(
				worldName = "Trench",
				minDistanceFromPlayer = 2500.0,
				maxDistanceFromPlayer = 4500.0,
				probability = 0.3,
				templates = listOf(
					spawnChance(AITemplateRegistry.RAIDER, 0.25),
					spawnChance(AITemplateRegistry.SCYTHE, 0.25),
					spawnChance(AITemplateRegistry.SWARMER, 0.25),
					spawnChance(AITemplateRegistry.REAVER, 0.25)
				)
			),
			WorldSettings(
				worldName = "AU-0821",
				minDistanceFromPlayer = 2500.0,
				maxDistanceFromPlayer = 4500.0,
				probability = 0.3,
				templates = listOf(
					spawnChance(AITemplateRegistry.RAIDER, 0.25),
					spawnChance(AITemplateRegistry.SCYTHE, 0.25),
					spawnChance(AITemplateRegistry.SWARMER, 0.25),
					spawnChance(AITemplateRegistry.REAVER, 0.25)
				)
			)
		)
	))
}
