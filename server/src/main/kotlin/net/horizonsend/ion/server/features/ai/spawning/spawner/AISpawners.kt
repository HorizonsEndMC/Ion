package net.horizonsend.ion.server.features.ai.spawning.spawner

import com.google.common.collect.Multimap
import net.horizonsend.ion.common.utils.text.colors.EXPLORER_LIGHT_CYAN
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.colors.PIRATE_SATURATED_RED
import net.horizonsend.ion.common.utils.text.colors.PRIVATEER_LIGHT_TEAL
import net.horizonsend.ion.common.utils.text.colors.TSAII_DARK_ORANGE
import net.horizonsend.ion.common.utils.text.colors.TSAII_MEDIUM_ORANGE
import net.horizonsend.ion.common.utils.text.colors.吃饭人_STANDARD
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.configuration.StaticIntegerAmount
import net.horizonsend.ion.server.configuration.VariableIntegerAmount
import net.horizonsend.ion.server.features.ai.configuration.WorldSettings
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.MINING_GUILD
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.PERSEUS_EXPLORERS
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.PIRATES
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.SYSTEM_DEFENSE_FORCES
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.TSAII_RAIDERS
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.WATCHERS
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.miningGuildMini
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.吃饭人
import net.horizonsend.ion.server.features.ai.spawning.formatLocationSupplier
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.BagSpawner
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.BagSpawner.Companion.asBagSpawned
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.GroupSpawner
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.SingleSpawn
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.WeightedShipSupplier
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.BULWARK
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.CONTRACTOR
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.DAGGER
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.MALINGSHU_REINFORCED
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.MIANBAO_REINFORCED
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.PATROLLER
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.TENETA
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.TERALITH
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.TEST_BATTLECRUISER
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.TEST_CYCLE
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.TEST_DISINTEGRATOR
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.TEST_JAMMER
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.TEST_LOGISTIC
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.VERDOLITH_REINFORCED
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.VERDOLITH_REINFORCEMENT
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.VETERAN
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.spawnChance
import net.horizonsend.ion.server.miscellaneous.utils.multimapOf
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.world.WorldInitEvent

object AISpawners : IonServerComponent(true) {
	/**
	 * For variety, the spawners are defined in the code, but they get their ship configuration and spawn rates, etc. from configuration files.
	 **/
	private val spawners = mutableListOf<AISpawner>()
	val tickedAISpawners = mutableListOf<AISpawnerTicker>()

	fun getAllSpawners(): List<AISpawner> = spawners

	operator fun get(identifier: String): AISpawner? = spawners.firstOrNull { it.identifier == identifier }

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

		val new = singleWorldSpawners[name].map { it.invoke(event.world) }
		new.mapNotNullTo(tickedAISpawners) { it.scheduler as? AISpawnerTicker }

		spawners.addAll(new)
	}

	init {
		registerSpawners()

		spawners.mapNotNullTo(tickedAISpawners) { it.scheduler as? AISpawnerTicker }
	}

	// Run after tick is true
	override fun onEnable() {
		// Initialize all the per world spawners, after the worlds have all initialized
		for (world in IonServer.server.worlds) {
			spawners.addAll(perWorldSpawners.map { it.invoke(world) })
		}
	}

	private fun registerSpawners() {
		registerSingleWorldSpawner("Trench", "AU-0821") {
			SingleWorldSpawner(
				"WATCHER_SPAWNER",
				it,
				AISpawnerTicker(
					0.5,
					pointThreshold = 20 * 60 * 7
				),
				SingleSpawn(
					WeightedShipSupplier(
						spawnChance(WATCHERS.asSpawnedShip(VERDOLITH_REINFORCED), 0.75),
						spawnChance(WATCHERS.asSpawnedShip(TERALITH), 0.25)
					),
					formatLocationSupplier(it, 2500.0, 4500.0),
				)
			)
		}

		registerSingleWorldSpawner("Trench", "AU-0821") {
			SingleWorldSpawner(
				"WATCHER_BAG_SPAWNER",
				it,
				AISpawnerTicker(
					pointChance = 0.5,
					pointThreshold = 20 * 60 * 7
				),
				BagSpawner(
					formatLocationSupplier(it, 2500.0, 4500.0),
					StaticIntegerAmount(100),
					asBagSpawned(WATCHERS.asSpawnedShip(VERDOLITH_REINFORCEMENT), 5),
					asBagSpawned(WATCHERS.asSpawnedShip(TERALITH), 10)
				)
			)
		}

		registerSingleWorldSpawner("Trench", "AU-0821") {
			SingleWorldSpawner(
				"DAGGER_SWARM",
				it,
				AISpawnerTicker(
					pointChance = 0.5,
					pointThreshold = 20 * 60 * 7
				),
				BagSpawner(
					formatLocationSupplier(it, 1500.0, 2500.0),
					VariableIntegerAmount(3, 5),
					asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(DAGGER).withRandomRadialOffset(0.0, 250.0, 0.0, 250.0), 1)
				)
			)
		}

		registerSingleWorldSpawner("Trench", "AU-0821") {
			SingleWorldSpawner(
				"PRIVATEER_ASSAULT_FORCE",
				it,
				AISpawnerTicker(
					pointChance = 0.5,
					pointThreshold = 20 * 60 * 7 * 10
				),
				BagSpawner(
					formatLocationSupplier(it, 1500.0, 2500.0),
					VariableIntegerAmount(30, 50),
					asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(DAGGER).withRandomRadialOffset(200.0, 225.0, 0.0, 250.0), 1),
					asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(VETERAN).withRandomRadialOffset(175.0, 200.0, 0.0, 250.0), 3),
					asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(PATROLLER).withRandomRadialOffset(150.0, 175.0, 0.0, 250.0), 3),
					asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(TENETA).withRandomRadialOffset(100.0, 125.0, 0.0, 250.0), 5),
					asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(CONTRACTOR).withRandomRadialOffset(50.0, 75.0, 0.0, 250.0), 5),
					asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(BULWARK).withRandomRadialOffset(0.0, 50.0, 0.0, 250.0), 10),
				)
			)
		}

		registerSingleWorldSpawner("Space") {
			SingleWorldSpawner(
				"AI_2_BC_JAM_TEST_SPAWNER",
				it,
				AISpawnerTicker(
					pointChance = 0.5,
					pointThreshold = 20 * 60 * 7
				),
				GroupSpawner(
					formatLocationSupplier(it, 1500.0, 2500.0),
					mutableListOf(
						吃饭人.asSpawnedShip(TEST_BATTLECRUISER).withDirectionalOffset(1.0, BlockFace.NORTH.direction, 38.0),
						吃饭人.asSpawnedShip(TEST_LOGISTIC).withDirectionalOffset(120.0, BlockFace.SOUTH.direction, 60.0),
						吃饭人.asSpawnedShip(TEST_LOGISTIC).withDirectionalOffset(120.0, BlockFace.EAST.direction, 60.0),
						吃饭人.asSpawnedShip(TEST_LOGISTIC).withDirectionalOffset(120.0, BlockFace.WEST.direction, 60.0),
						吃饭人.asSpawnedShip(TEST_JAMMER).withRandomRadialOffset(180.0, 181.0, 0.0, 192.0),
					)
				)
			)
		}

		registerSingleWorldSpawner("Space") {
			SingleWorldSpawner(
				"AI_2_BC_CYCLE_TEST_SPAWNER",
				it,
				AISpawnerTicker(
					pointChance = 0.5,
					pointThreshold = 20 * 60 * 7
				),
				GroupSpawner(
					formatLocationSupplier(it, 1500.0, 2500.0),
					mutableListOf(
						吃饭人.asSpawnedShip(TEST_BATTLECRUISER).withDirectionalOffset(1.0, BlockFace.NORTH.direction, 38.0),
						吃饭人.asSpawnedShip(TEST_CYCLE).withDirectionalOffset(1.0, BlockFace.NORTH.direction, 100.0),
						吃饭人.asSpawnedShip(TEST_LOGISTIC).withDirectionalOffset(120.0, BlockFace.SOUTH.direction, 60.0),
						吃饭人.asSpawnedShip(TEST_LOGISTIC).withDirectionalOffset(120.0, BlockFace.EAST.direction, 60.0),
					)
				)
			)
		}

		registerSingleWorldSpawner("Space") {
			SingleWorldSpawner(
				"AI_2_DESTROYER_JAM_TEST_SPAWNER",
				it,
				AISpawnerTicker(
					pointChance = 0.5,
					pointThreshold = 20 * 60 * 7
				),
				GroupSpawner(
					formatLocationSupplier(it, 1500.0, 2500.0),
					mutableListOf(
						吃饭人.asSpawnedShip(TEST_DISINTEGRATOR).withDirectionalOffset(1.0, BlockFace.NORTH.direction, 38.0),
						吃饭人.asSpawnedShip(TEST_DISINTEGRATOR).withDirectionalOffset(150.0, BlockFace.NORTH.direction, 38.0),
						吃饭人.asSpawnedShip(TEST_LOGISTIC).withDirectionalOffset(120.0, BlockFace.SOUTH.direction, 60.0),
						吃饭人.asSpawnedShip(TEST_LOGISTIC).withDirectionalOffset(120.0, BlockFace.EAST.direction, 60.0),
						吃饭人.asSpawnedShip(TEST_LOGISTIC).withDirectionalOffset(120.0, BlockFace.WEST.direction, 60.0),
						吃饭人.asSpawnedShip(TEST_JAMMER).withRandomRadialOffset(180.0, 181.0, 0.0, 192.0),
					)
				)
			)
		}

		registerSingleWorldSpawner("Space") {
			SingleWorldSpawner(
				"AI_2_DESTROYER_CYCLE_TEST_SPAWNER",
				it,
				AISpawnerTicker(
					pointChance = 0.5,
					pointThreshold = 20 * 60 * 7,
				),
				GroupSpawner(
					formatLocationSupplier(it, 1500.0, 2500.0),
					mutableListOf(
						吃饭人.asSpawnedShip(TEST_DISINTEGRATOR).withDirectionalOffset(1.0, BlockFace.NORTH.direction, 38.0),
						吃饭人.asSpawnedShip(TEST_DISINTEGRATOR).withDirectionalOffset(150.0, BlockFace.NORTH.direction, 38.0),
						吃饭人.asSpawnedShip(TEST_CYCLE).withDirectionalOffset(1.0, BlockFace.NORTH.direction, 100.0),
						吃饭人.asSpawnedShip(TEST_LOGISTIC).withDirectionalOffset(120.0, BlockFace.SOUTH.direction, 60.0),
						吃饭人.asSpawnedShip(TEST_LOGISTIC).withDirectionalOffset(120.0, BlockFace.EAST.direction, 60.0),
						吃饭人.asSpawnedShip(TEST_LOGISTIC).withDirectionalOffset(120.0, BlockFace.WEST.direction, 60.0),
					)
				)
			)
		}

		registerSingleWorldSpawner("Space") {
			SingleWorldSpawner(
				"AI_2_CYCLE_DESTROYER_JAM_TEST_SPAWNER",
				it,
				AISpawnerTicker( pointChance = 0.5,
				pointThreshold = 20 * 60 * 7),
				GroupSpawner(
					formatLocationSupplier(it, 1500.0, 2500.0),
					mutableListOf(
						吃饭人.asSpawnedShip(TEST_DISINTEGRATOR).withDirectionalOffset(1.0, BlockFace.NORTH.direction, 38.0),
						吃饭人.asSpawnedShip(TEST_CYCLE).withDirectionalOffset(1.0, BlockFace.NORTH.direction, 100.0),
						吃饭人.asSpawnedShip(TEST_CYCLE).withDirectionalOffset(100.0, BlockFace.NORTH.direction, 75.0),
						吃饭人.asSpawnedShip(TEST_LOGISTIC).withDirectionalOffset(120.0, BlockFace.SOUTH.direction, 60.0),
						吃饭人.asSpawnedShip(TEST_LOGISTIC).withDirectionalOffset(120.0, BlockFace.EAST.direction, 60.0),
						吃饭人.asSpawnedShip(TEST_JAMMER).withRandomRadialOffset(180.0, 181.0, 0.0, 192.0),
					)
				)
			)
		}

		registerSingleWorldSpawner("Space") {
			SingleWorldSpawner(
				"AI_2_GIGA_FLEET_TEST_SPAWNER",
				it,
				AISpawnerTicker( pointChance = 0.5,
				pointThreshold = 20 * 60 * 7),
				GroupSpawner(
					formatLocationSupplier(it, 1500.0, 2500.0),
					mutableListOf(
						吃饭人.asSpawnedShip(TEST_BATTLECRUISER).withDirectionalOffset(1.0, BlockFace.NORTH.direction, 38.0),
						吃饭人.asSpawnedShip(TEST_BATTLECRUISER).withDirectionalOffset(500.0, BlockFace.NORTH.direction, 75.0),
						吃饭人.asSpawnedShip(TEST_DISINTEGRATOR).withDirectionalOffset(250.0, BlockFace.NORTH.direction, 60.0),
						吃饭人.asSpawnedShip(TEST_DISINTEGRATOR).withDirectionalOffset(250.0, BlockFace.SOUTH.direction, 60.0),
						吃饭人.asSpawnedShip(TEST_CYCLE).withDirectionalOffset(300.0, BlockFace.SOUTH_EAST.direction, 100.0),
						吃饭人.asSpawnedShip(TEST_CYCLE).withDirectionalOffset(300.0, BlockFace.SOUTH_WEST.direction, 100.0),
						吃饭人.asSpawnedShip(TEST_CYCLE).withDirectionalOffset(750.0, BlockFace.NORTH.direction, 100.0),
						吃饭人.asSpawnedShip(TEST_LOGISTIC).withDirectionalOffset(500.0, BlockFace.EAST.direction, 192.0),
						吃饭人.asSpawnedShip(TEST_LOGISTIC).withDirectionalOffset(500.0, BlockFace.WEST.direction, 192.0),
						吃饭人.asSpawnedShip(TEST_LOGISTIC).withDirectionalOffset(500.0, BlockFace.SOUTH.direction, 192.0),
						吃饭人.asSpawnedShip(TEST_LOGISTIC).withDirectionalOffset(500.0, BlockFace.NORTH.direction, 192.0),
						吃饭人.asSpawnedShip(TEST_JAMMER).withRandomRadialOffset(400.0, 401.0, 0.0, 250.0),
						吃饭人.asSpawnedShip(TEST_JAMMER).withRandomRadialOffset(400.0, 401.0, 0.0, 250.0),
					)
				)
			)
		}

		registerGlobalSpawner(StandardFactionSpawner(
			"吃饭人_BASIC",
			faction = 吃饭人,
			AISpawnerTicker(
				pointChance = 0.5,
				pointThreshold = 20 * 60 * 7,
			),
			spawnMessage = "<${吃饭人_STANDARD}>An unknown starship signature is being broadcast in {4} at {1}, {3}".miniMessage(),
			worlds = listOf(
				WorldSettings(
					worldName = "Trench",
					probability = 0.5,
					minDistanceFromPlayer = 2500.0,
					maxDistanceFromPlayer = 4500.0,
					templates = listOf(
						spawnChance(吃饭人.asSpawnedShip(MIANBAO_REINFORCED), 0.5),
						spawnChance(吃饭人.asSpawnedShip(MALINGSHU_REINFORCED), 0.5)
					)
				),
				WorldSettings(
					worldName = "AU-0821",
					probability = 0.5,
					minDistanceFromPlayer = 2500.0,
					maxDistanceFromPlayer = 4500.0,
					templates = listOf(
						spawnChance(吃饭人.asSpawnedShip(MIANBAO_REINFORCED), 0.5),
						spawnChance(吃饭人.asSpawnedShip(MALINGSHU_REINFORCED), 0.5)
					)
				)
			)
		))

		registerGlobalSpawner(StandardFactionSpawner(
			"PIRATE_BASIC",
			faction = PIRATES,
			AISpawnerTicker(
				pointChance = 0.5,
				pointThreshold = 10000
			),
			spawnMessage = "<${HE_MEDIUM_GRAY}>A pirate {0} has been identified in the area of {1}, {3}, in {4}. <$PIRATE_SATURATED_RED>Please avoid the sector until the threat has been cleared.".miniMessage(),
			worlds = listOf(
				WorldSettings(
					worldName = "Asteri",
					probability = 0.15,
					minDistanceFromPlayer = 2000.0,
					maxDistanceFromPlayer = 4000.0,
					templates = listOf(
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.ISKAT), 0.2),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.VOSS), 0.2),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.HECTOR), 0.4),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.HIRO), 0.4),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.WASP), 0.4)
					)
				),
				WorldSettings(
					worldName = "Regulus",
					probability = 0.25,
					minDistanceFromPlayer = 2000.0,
					maxDistanceFromPlayer = 4000.0,
					templates = listOf(
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.ISKAT), 0.2),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.VOSS), 0.2),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.HECTOR), 0.4),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.HIRO), 0.4),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.WASP), 0.4),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.FRENZ), 0.4),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.TEMPEST), 0.2),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.VELASCO), 0.2),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.VENDETTA), 0.2),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.ANAAN), 0.2),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.CORMORANT), 0.2),
					)
				),
				WorldSettings(
					worldName = "Sirius",
					probability = 0.15,
					minDistanceFromPlayer = 2000.0,
					maxDistanceFromPlayer = 4000.0,
					templates = listOf(
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.ISKAT), 0.2),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.VOSS), 0.2),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.HECTOR), 0.4),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.HIRO), 0.4),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.WASP), 0.4),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.FRENZ), 0.4),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.TEMPEST), 0.2),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.VELASCO), 0.2)
					)
				),
				WorldSettings(
					worldName = "Ilios",
					probability = 0.15,
					minDistanceFromPlayer = 2000.0,
					maxDistanceFromPlayer = 4000.0,
					templates = listOf(
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.ISKAT), 0.2),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.VOSS), 0.2),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.HECTOR), 0.4),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.HIRO), 0.4),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.WASP), 0.4),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.FRENZ), 0.4),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.TEMPEST), 0.2),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.VELASCO), 0.2),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.VENDETTA), 0.2),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.ANAAN), 0.2),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.CORMORANT), 0.2)
					)
				),
				WorldSettings(
					worldName = "Horizon",
					probability = 0.15,
					minDistanceFromPlayer = 2000.0,
					maxDistanceFromPlayer = 4000.0,
					templates = listOf(
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.VENDETTA), 0.2),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.ANAAN), 0.2),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.CORMORANT), 0.2),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.MANTIS), 0.2),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.HERNSTEIN), 0.2),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.FYR), 0.2),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.BLOODSTAR), 0.2)
					)
				),
				WorldSettings(
					worldName = "Trench",
					probability = 0.15,
					minDistanceFromPlayer = 2000.0,
					maxDistanceFromPlayer = 4000.0,
					templates = listOf(
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.VENDETTA), 0.2),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.ANAAN), 0.2),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.CORMORANT), 0.2),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.MANTIS), 0.2),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.HERNSTEIN), 0.2),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.FYR), 0.2),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.BLOODSTAR), 0.2)
					)
				),
				WorldSettings(
					worldName = "AU-0821",
					probability = 0.15,
					minDistanceFromPlayer = 2000.0,
					maxDistanceFromPlayer = 4000.0,
					templates = listOf(
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.VENDETTA), 0.2),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.ANAAN), 0.2),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.CORMORANT), 0.2),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.MANTIS), 0.2),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.HERNSTEIN), 0.2),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.FYR), 0.2),
						spawnChance(PIRATES.asSpawnedShip(AITemplateRegistry.BLOODSTAR), 0.2)
					)
				)
			)
		))

		fun explorerWorld(worldName: String, probability: Double): WorldSettings = WorldSettings(
			worldName = worldName,
			probability = probability,
			minDistanceFromPlayer = 1500.0,
			maxDistanceFromPlayer = 3500.0,
			templates = listOf(
				spawnChance(PERSEUS_EXPLORERS.asSpawnedShip(AITemplateRegistry.WAYFINDER), 0.35),
				spawnChance(PERSEUS_EXPLORERS.asSpawnedShip(AITemplateRegistry.STRIKER), 0.3),
				spawnChance(PERSEUS_EXPLORERS.asSpawnedShip(AITemplateRegistry.NIMBLE), 0.2),
				spawnChance(PERSEUS_EXPLORERS.asSpawnedShip(AITemplateRegistry.DESSLE), 0.2),
				spawnChance(PERSEUS_EXPLORERS.asSpawnedShip(AITemplateRegistry.MINHAUL_CHETHERITE), 0.15),
				spawnChance(PERSEUS_EXPLORERS.asSpawnedShip(AITemplateRegistry.MINHAUL_REDSTONE), 0.1),
				spawnChance(PERSEUS_EXPLORERS.asSpawnedShip(AITemplateRegistry.MINHAUL_TITANIUM), 0.1),
				spawnChance(PERSEUS_EXPLORERS.asSpawnedShip(AITemplateRegistry.EXOTRAN_CHETHERITE), 0.15),
				spawnChance(PERSEUS_EXPLORERS.asSpawnedShip(AITemplateRegistry.EXOTRAN_REDSTONE), 0.1),
				spawnChance(PERSEUS_EXPLORERS.asSpawnedShip(AITemplateRegistry.EXOTRAN_TITANIUM), 0.1),
				spawnChance(PERSEUS_EXPLORERS.asSpawnedShip(AITemplateRegistry.AMPH), 0.35)
			)
		)

		registerGlobalSpawner(StandardFactionSpawner(
			"EXPLORER_BASIC",
			PERSEUS_EXPLORERS,
			AISpawnerTicker(
				pointChance = 0.75,
				pointThreshold = 20 * 60 * 10
			),
			spawnMessage = "<$EXPLORER_LIGHT_CYAN>Horizon Transit Lines<${HE_MEDIUM_GRAY}> {0} spawned at {1}, {3}, in {4}".miniMessage(),
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

		registerGlobalSpawner(StandardFactionSpawner(
			"MINING_GUILD_BASIC",
			MINING_GUILD,
			AISpawnerTicker(
				pointChance = 0.8,
				pointThreshold = 8400
			),
			spawnMessage = "$miningGuildMini <${HE_MEDIUM_GRAY}>extraction vessel {0} spawned at {1}, {3}, in {4}".miniMessage(),
			worlds = listOf(
				WorldSettings(
					worldName = "Asteri",
					probability = 0.2,
					minDistanceFromPlayer = 1000.0,
					maxDistanceFromPlayer = 2500.0,
					templates = listOf(
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.WOODPECKER), 0.22),
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.TYPE_V11), 0.22),
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.TYPEA21B), 0.22),
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.TYPEI41), 0.22),
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.BEAVER), 0.12)
					)
				),
				WorldSettings(
					worldName = "Sirius",
					probability = 0.11,
					minDistanceFromPlayer = 1000.0,
					maxDistanceFromPlayer = 2500.0,
					templates = listOf(
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.WOODPECKER), 0.22),
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.TYPE_V11), 0.22),
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.TYPEA21B), 0.22),
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.TYPEI41), 0.22),
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.BEAVER), 0.12)
					)
				),
				WorldSettings(
					worldName = "Regulus",
					probability = 0.2,
					minDistanceFromPlayer = 1000.0,
					maxDistanceFromPlayer = 2500.0,
					templates = listOf(
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.WOODPECKER), 0.22),
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.TYPE_V11), 0.22),
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.TYPEA21B), 0.22),
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.TYPEI41), 0.22),
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.BEAVER), 0.12),
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.OSTRICH), 0.05),
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.BADGER), 0.05),
					)
				),
				WorldSettings(
					worldName = "Ilios",
					probability = 0.13,
					minDistanceFromPlayer = 1000.0,
					maxDistanceFromPlayer = 2500.0,
					templates = listOf(
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.WOODPECKER), 0.22),
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.TYPE_V11), 0.22),
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.TYPEA21B), 0.22),
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.TYPEI41), 0.22),
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.BEAVER), 0.12)
					)
				),
				WorldSettings(
					worldName = "Horizon",
					probability = 0.27,
					minDistanceFromPlayer = 1000.0,
					maxDistanceFromPlayer = 2500.0,
					templates = listOf(
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.WOODPECKER), 0.12),
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.TYPE_V11), 0.12),
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.TYPEA21B), 0.12),
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.TYPEI41), 0.12),
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.BEAVER), 0.22),
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.OSTRICH), 0.15),
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.BADGER), 0.15),
					)
				),
				WorldSettings(worldName = "Trench",
					probability = 0.75,
					minDistanceFromPlayer = 1000.0,
					maxDistanceFromPlayer = 2500.0,
					templates = listOf(
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.WOODPECKER), 0.12),
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.TYPE_V11), 0.12),
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.TYPEA21B), 0.12),
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.TYPEI41), 0.12),
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.BEAVER), 0.22),
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.OSTRICH), 0.15),
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.BADGER), 0.15),
					)
				),
				WorldSettings(
					worldName = "AU-0821",
					probability = 0.05,
					minDistanceFromPlayer = 1000.0,
					maxDistanceFromPlayer = 2500.0,
					templates = listOf(
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.WOODPECKER), 0.12),
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.TYPE_V11), 0.12),
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.TYPEA21B), 0.12),
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.TYPEI41), 0.12),
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.BEAVER), 0.22),
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.OSTRICH), 0.15),
						spawnChance(MINING_GUILD.asSpawnedShip(AITemplateRegistry.BADGER), 0.15),
					)
				)
			)
		))

		registerGlobalSpawner(StandardFactionSpawner(
			"PRIVATEER_BASIC",
			faction = SYSTEM_DEFENSE_FORCES,
			AISpawnerTicker(
				pointChance = 0.5,
				pointThreshold = 12000
			),
			spawnMessage = "<$PRIVATEER_LIGHT_TEAL>Privateer patrol <${HE_MEDIUM_GRAY}>operation vessel {0} spawned at {1}, {3}, in {4}".miniMessage(),
			worlds = listOf(
				WorldSettings(
					worldName = "Asteri",
					probability = 0.15,
					minDistanceFromPlayer = 1000.0,
					maxDistanceFromPlayer = 2500.0,
					templates = listOf(
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(AITemplateRegistry.PROTECTOR), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(AITemplateRegistry.FURIOUS), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(AITemplateRegistry.INFLICT), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(VETERAN), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(PATROLLER), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(TENETA), 0.12)
					)
				),
				WorldSettings(
					worldName = "Sirius",
					probability = 0.2,
					minDistanceFromPlayer = 1000.0,
					maxDistanceFromPlayer = 2500.0,
					templates = listOf(
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(AITemplateRegistry.PROTECTOR), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(AITemplateRegistry.FURIOUS), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(AITemplateRegistry.INFLICT), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(VETERAN), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(PATROLLER), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(AITemplateRegistry.DAYBREAK), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(TENETA), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(CONTRACTOR), 0.05)
					)
				),
				WorldSettings(
					worldName = "Regulus",
					probability = 0.3,
					minDistanceFromPlayer = 1000.0,
					maxDistanceFromPlayer = 2500.0,
					templates = listOf(
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(AITemplateRegistry.INFLICT), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(VETERAN), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(PATROLLER), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(AITemplateRegistry.DAYBREAK), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(TENETA), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(BULWARK), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(CONTRACTOR), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(DAGGER), 0.12)
					)
				),
				WorldSettings(
					worldName = "Ilios",
					probability = 0.1,
					minDistanceFromPlayer = 1000.0,
					maxDistanceFromPlayer = 2500.0,
					templates = listOf(
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(AITemplateRegistry.INFLICT), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(VETERAN), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(PATROLLER), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(TENETA), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(CONTRACTOR), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(AITemplateRegistry.DAYBREAK), 0.12)
					)
				),
				WorldSettings(
					worldName = "Horizon",
					probability = 0.1,
					minDistanceFromPlayer = 1000.0,
					maxDistanceFromPlayer = 2500.0,
					templates = listOf(
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(VETERAN), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(PATROLLER), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(TENETA), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(BULWARK), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(CONTRACTOR), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(DAGGER), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(AITemplateRegistry.DAYBREAK), 0.12)
					)
				),
				WorldSettings(worldName = "Trench",
					probability = 0.05,
					minDistanceFromPlayer = 1000.0,
					maxDistanceFromPlayer = 2500.0,
					templates = listOf(
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(VETERAN), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(PATROLLER), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(TENETA), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(BULWARK), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(CONTRACTOR), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(DAGGER), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(AITemplateRegistry.DAYBREAK), 0.12)
					)
				),
				WorldSettings(
					worldName = "AU-0821",
					probability = 0.05,
					minDistanceFromPlayer = 1000.0,
					maxDistanceFromPlayer = 2500.0,
					templates = listOf(
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(VETERAN), 0.10),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(PATROLLER), 0.10),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(TENETA), 0.10),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(BULWARK), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(CONTRACTOR), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(DAGGER), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(AITemplateRegistry.DAYBREAK), 0.12)
					)
				)
			)
		))

		registerGlobalSpawner(StandardFactionSpawner(
			"TSAII_BASIC",
			faction = TSAII_RAIDERS,
			AISpawnerTicker(
				pointThreshold = 30 * 20 * 60,
				pointChance = 0.5
			),
			spawnMessage = "<${TSAII_DARK_ORANGE}>Dangerous Tsaii Raiders {0} has been reported in the area of {1}, {3}, in {4}. <$TSAII_MEDIUM_ORANGE>Please avoid the sector until the threat has been cleared!".miniMessage(),
			worlds = listOf(
				WorldSettings(
					worldName = "Horizon",
					minDistanceFromPlayer = 2500.0,
					maxDistanceFromPlayer = 4500.0,
					probability = 0.4,
					templates = listOf(
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(AITemplateRegistry.RAIDER), 0.25),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(AITemplateRegistry.SCYTHE), 0.25),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(AITemplateRegistry.SWARMER), 0.25),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(AITemplateRegistry.REAVER), 0.25)
					)
				),
				WorldSettings(
					worldName = "Trench",
					minDistanceFromPlayer = 2500.0,
					maxDistanceFromPlayer = 4500.0,
					probability = 0.3,
					templates = listOf(
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(AITemplateRegistry.RAIDER), 0.25),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(AITemplateRegistry.SCYTHE), 0.25),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(AITemplateRegistry.SWARMER), 0.25),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(AITemplateRegistry.REAVER), 0.25)
					)
				),
				WorldSettings(
					worldName = "AU-0821",
					minDistanceFromPlayer = 2500.0,
					maxDistanceFromPlayer = 4500.0,
					probability = 0.3,
					templates = listOf(
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(AITemplateRegistry.RAIDER), 0.25),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(AITemplateRegistry.SCYTHE), 0.25),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(AITemplateRegistry.SWARMER), 0.25),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(AITemplateRegistry.REAVER), 0.25)
					)
				)
			)
		))
	}
}
