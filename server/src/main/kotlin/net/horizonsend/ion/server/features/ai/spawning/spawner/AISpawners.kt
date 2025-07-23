package net.horizonsend.ion.server.features.ai.spawning.spawner

import com.google.common.collect.Multimap
import kotlinx.serialization.Serializable
import net.horizonsend.ion.common.utils.configuration.Configuration
import net.horizonsend.ion.common.utils.text.colors.EXPLORER_LIGHT_CYAN
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.colors.PIRATE_SATURATED_RED
import net.horizonsend.ion.common.utils.text.colors.PRIVATEER_LIGHT_TEAL
import net.horizonsend.ion.common.utils.text.colors.TSAII_DARK_ORANGE
import net.horizonsend.ion.common.utils.text.colors.TSAII_MEDIUM_ORANGE
import net.horizonsend.ion.common.utils.text.colors.WATCHER_ACCENT
import net.horizonsend.ion.common.utils.text.colors.WATCHER_STANDARD
import net.horizonsend.ion.common.utils.text.colors.吃饭人_STANDARD
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.configuration.util.VariableIntegerAmount
import net.horizonsend.ion.server.features.ai.configuration.WorldSettings
import net.horizonsend.ion.server.features.ai.convoys.AIConvoyRegistry.DEBUG_CONVOY_GLOBAL
import net.horizonsend.ion.server.features.ai.convoys.AIConvoyRegistry.DEBUG_CONVOY_LOCAL
import net.horizonsend.ion.server.features.ai.convoys.AIConvoyRegistry.DEEP_SPACE_MINING
import net.horizonsend.ion.server.features.ai.convoys.LocationContext
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.MINING_GUILD
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.PERSEUS_EXPLORERS
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.PIRATES
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.SYSTEM_DEFENSE_FORCES
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.TSAII_RAIDERS
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.WATCHERS
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.miningGuildMini
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.吃饭人
import net.horizonsend.ion.server.features.ai.module.misc.DifficultyModule
import net.horizonsend.ion.server.features.ai.spawning.formatLocationSupplier
import net.horizonsend.ion.server.features.ai.spawning.isSystemOccupied
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.BagSpawner
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.BagSpawner.Companion.asBagSpawned
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.RandomShipSupplier
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.SingleSpawn
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.WeightedShipSupplier
import net.horizonsend.ion.server.features.ai.spawning.spawner.scheduler.AISpawnerTicker
import net.horizonsend.ion.server.features.ai.spawning.spawner.scheduler.CaravanScheduler
import net.horizonsend.ion.server.features.ai.spawning.spawner.scheduler.LocusScheduler
import net.horizonsend.ion.server.features.ai.spawning.spawner.scheduler.SetTimeScheduler
import net.horizonsend.ion.server.features.ai.spawning.spawner.scheduler.TickedScheduler
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.ARBOREALITH
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.BULWARK
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.CONTRACTOR
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.DAGGER
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.DAYBREAK
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.MALINGSHU_REINFORCED
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.MIANBAO_REINFORCED
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.PATROLLER
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.RAIDER
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.REAVER
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.RESOLUTE
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.SCYTHE
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.SWARMER
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.TENETA
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.TERALITH
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.VERDOLITH_REINFORCED
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.VERDOLITH_REINFORCEMENT
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.VETERAN
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.spawnChance
import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.ai.util.SpawnMessage
import net.horizonsend.ion.server.features.player.NewPlayerProtection.hasProtection
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag.ALLOW_AI_SPAWNS
import net.horizonsend.ion.server.features.world.WorldFlag.SPACE_WORLD
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.getRandomDuration
import net.horizonsend.ion.server.miscellaneous.utils.multimapOf
import net.kyori.adventure.text.Component.text
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.world.WorldInitEvent
import java.time.Duration
import java.util.function.Supplier
import kotlin.random.Random

object AISpawners : IonServerComponent(true) {
	/**
	 * For variety, the spawners are defined in the code, but they get their ship configuration and spawn rates, etc. from configuration files.
	 **/
	private val spawners = mutableListOf<AISpawner>()
	val tickedAISpawners = mutableListOf<TickedScheduler>()
	private val setTimeSpawners = mutableListOf<SetTimeScheduler>()

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
		new.mapNotNullTo(tickedAISpawners) { it.scheduler as? TickedScheduler }

		spawners.addAll(new)
	}

	init {
		registerSpawners()

		spawners.filterIsInstanceTo(tickedAISpawners)
		spawners.filterIsInstanceTo(setTimeSpawners)

		setTimeSpawners.forEach { t -> t.schedule() }

		tickedAISpawners.add(CaravanScheduler)
	}


	// Run after tick is true
	override fun onEnable() {
		// Initialize all the per world spawners, after the worlds have all initialized
		for (world in IonServer.server.worlds) {
			if (!world.ion.hasFlag(ALLOW_AI_SPAWNS)) continue
			val new = perWorldSpawners.map { it.invoke(world) }
			spawners.addAll(new)
			new.mapNotNullTo(tickedAISpawners) { it.scheduler as? TickedScheduler }
		}

		loadPersistentData()

		Tasks.asyncRepeat(120, 120, ::savePersistentData)
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
                        // for testing purposes
						spawnChance(WATCHERS.asSpawnedShip(VERDOLITH_REINFORCED), 0.70),
                        spawnChance(WATCHERS.asSpawnedShip(TERALITH), 0.20),
						spawnChance(WATCHERS.asSpawnedShip(ARBOREALITH), 0.1)
                    ),
                    formatLocationSupplier(it, 2500.0, 4500.0) { player -> !player.hasProtection() },
                    SpawnMessage.WorldMessage("<$WATCHER_ACCENT>An unknown starship signature is being broadcast in {4} spawned at {1}, {3}".miniMessage()),
                    DifficultyModule::regularSpawnDifficultySupplier,
					targetModeSupplier = { AITarget.TargetMode.PLAYER_ONLY }
                )
			)
		}

		val watcherLocusScheduler = LocusScheduler(
			"<$WATCHER_STANDARD>Unknown Signal Locus".miniMessage(),
			WATCHER_STANDARD,
			duration = { Duration.ofMinutes(20) },
			separation = { getRandomDuration(Duration.ofHours(6), Duration.ofHours(9)) },
			difficultySupplier = DifficultyModule::regularSpawnDifficultySupplier,
			"<${HE_MEDIUM_GRAY}>An <$WATCHER_STANDARD>Unknown Signal<${HE_MEDIUM_GRAY}> has been detected in {0} at {1} {3}. <$WATCHER_ACCENT>Alien starships patrol the area.".miniMessage(),
			"<${HE_MEDIUM_GRAY}>The <$WATCHER_STANDARD>Unknown Signal<${HE_MEDIUM_GRAY}> has disappeared".miniMessage(),
			radius = 1500.0,
			spawnSeparation = { getRandomDuration(Duration.ofSeconds(180), Duration.ofSeconds(260)) },
			listOf("Trench", "AU-0821", "Horizon")
		)

		registerGlobalSpawner(GlobalWorldSpawner(
			"WATCHER_LOCUS",
			watcherLocusScheduler,
			BagSpawner(
				watcherLocusScheduler.spawnLocationProvider,
				VariableIntegerAmount(3, 5),
				groupMessage = null,
				individualSpawnMessage = null,
				asBagSpawned(WATCHERS.asSpawnedShip(VERDOLITH_REINFORCED), 1),
				asBagSpawned(WATCHERS.asSpawnedShip(TERALITH), 2),
				difficultySupplier = {_ -> Supplier { watcherLocusScheduler.difficulty }},
				targetModeSupplier = { AITarget.TargetMode.PLAYER_ONLY },
			)
		))

		registerSingleWorldSpawner("Trench", "AU-0821") {
			SingleWorldSpawner(
				"WATCHER_BAG_SPAWNER",
				it,
				AISpawnerTicker(
					pointChance = 0.5,
					pointThreshold = 20 * 60 * 7 * 5
				),
				BagSpawner(
					formatLocationSupplier(it, 2500.0, 4500.0) { player -> !player.hasProtection() },
					VariableIntegerAmount(10, 20),
					text("An unusually strong alien signature has been detected in {3} at {0}, {2}", WATCHER_ACCENT),
					null,
					asBagSpawned(WATCHERS.asSpawnedShip(VERDOLITH_REINFORCEMENT), 10),
					asBagSpawned(WATCHERS.asSpawnedShip(TERALITH), 10),
					difficultySupplier = DifficultyModule::regularSpawnDifficultySupplier,
					targetModeSupplier = { AITarget.TargetMode.PLAYER_ONLY },
				)
			)
		}
		/*
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
					),
					null,
					null
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
					),
					null,
					null
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
					),
					null,
					null
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
					),
					null,
					null
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
					),
					null,
					null
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
					),
					null,
					null
				)
			)
		}
		*/
		registerGlobalSpawner(LegacyFactionSpawner(
			"吃饭人_BASIC",
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

		registerGlobalSpawner(LegacyFactionSpawner(
			"PIRATE_BASIC",
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
					probability = 0.25,
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

		val pirateLocusScheduler = LocusScheduler(
			"<${HE_MEDIUM_GRAY}>Increased <$PIRATE_SATURATED_RED>Pirate<${HE_MEDIUM_GRAY}> Activity".miniMessage(),
			PIRATE_SATURATED_RED,
			duration = { Duration.ofMinutes(30) },
			separation = { getRandomDuration(Duration.ofHours(2), Duration.ofHours(4)) },
			difficultySupplier = DifficultyModule::regularSpawnDifficultySupplier,
			"<${HE_MEDIUM_GRAY}>Increased <$PIRATE_SATURATED_RED>Pirate<${HE_MEDIUM_GRAY}> activity has been noted in {0} at {1} {3}. <$PIRATE_SATURATED_RED>Please avoid the area.".miniMessage(),
			"<$PIRATE_SATURATED_RED>Pirate<${HE_MEDIUM_GRAY}> activity has waned".miniMessage(),
			radius = 1500.0,
			spawnSeparation = { getRandomDuration(Duration.ofSeconds(30), Duration.ofSeconds(90)) },
			listOf("Trench", "AU-0821", "Horizon")
		)

		registerGlobalSpawner(GlobalWorldSpawner(
			"PIRATE_LOCUS",
			pirateLocusScheduler,
			SingleSpawn(
                RandomShipSupplier(
                    PIRATES.asSpawnedShip(AITemplateRegistry.VENDETTA),
                    PIRATES.asSpawnedShip(AITemplateRegistry.ANAAN),
                    PIRATES.asSpawnedShip(AITemplateRegistry.CORMORANT),
                    PIRATES.asSpawnedShip(AITemplateRegistry.MANTIS),
                    PIRATES.asSpawnedShip(AITemplateRegistry.HERNSTEIN),
                    PIRATES.asSpawnedShip(AITemplateRegistry.FYR),
                    PIRATES.asSpawnedShip(AITemplateRegistry.BLOODSTAR)
                ),
                pirateLocusScheduler.spawnLocationProvider,
                SpawnMessage.WorldMessage("<$PIRATE_SATURATED_RED>More pirates spotted!".miniMessage()),
                { _ -> Supplier {pirateLocusScheduler.difficulty}},
				{ AITarget.TargetMode.PLAYER_ONLY }
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

		registerGlobalSpawner(LegacyFactionSpawner(
			"EXPLORER_BASIC",
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

		registerGlobalSpawner(LegacyFactionSpawner(
			"MINING_GUILD_BASIC",
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

		registerGlobalSpawner(LegacyFactionSpawner(
			"PRIVATEER_BASIC",
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
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(DAYBREAK), 0.12),
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
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(DAYBREAK), 0.12),
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
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(DAYBREAK), 0.12)
					)
				),
				WorldSettings(
					worldName = "Horizon",
					probability = 0.2,
					minDistanceFromPlayer = 1000.0,
					maxDistanceFromPlayer = 2500.0,
					templates = listOf(
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(VETERAN), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(PATROLLER), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(TENETA), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(BULWARK), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(CONTRACTOR), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(DAGGER), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(DAYBREAK), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(RESOLUTE), 0.20)
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
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(DAYBREAK), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(RESOLUTE), 0.30),

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
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(DAYBREAK), 0.12),
						spawnChance(SYSTEM_DEFENSE_FORCES.asSpawnedShip(RESOLUTE), 0.10)
					)
				)
			)
		))

		registerSingleWorldSpawner("Trench", "AU-0821","Horizon") {
			SingleWorldSpawner(
				"DAGGER_SWARM",
				it,
				AISpawnerTicker(
					pointChance = 0.5,
					pointThreshold = 20 * 60 * 7 * 5
				),
				BagSpawner(
					formatLocationSupplier(it, 1500.0, 2500.0) { player -> !player.hasProtection() },
					VariableIntegerAmount(3, 5),
					"<$PRIVATEER_LIGHT_TEAL>Privateer Dagger <${HE_MEDIUM_GRAY}>Flight Squadron has spawned at {0}, {2}, in {3}".miniMessage(),
					null,
					asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(DAGGER).withRandomRadialOffset(0.0, 250.0, 0.0, 250.0), 1),
					difficultySupplier = DifficultyModule::regularSpawnDifficultySupplier,
					targetModeSupplier = { AITarget.TargetMode.PLAYER_ONLY },
				)
			)
		}

		val daggerLocusScheduler = LocusScheduler(
			"<$PRIVATEER_LIGHT_TEAL>Privateer<${HE_MEDIUM_GRAY}> Naval Drills".miniMessage(),
			PRIVATEER_LIGHT_TEAL,
			duration = { Duration.ofMinutes(30) },
			separation = { getRandomDuration(Duration.ofHours(1), Duration.ofHours(5)) },
			difficultySupplier = DifficultyModule::regularSpawnDifficultySupplier,
			"<$PRIVATEER_LIGHT_TEAL>Privateer Naval Drills<${HE_MEDIUM_GRAY}> will be conducted in {0} at {1} {3}. Please avoid the area.".miniMessage(),
			"<$PRIVATEER_LIGHT_TEAL>Privateer Naval Drills<${HE_MEDIUM_GRAY}> have ended".miniMessage(),
			radius = 1500.0,
			spawnSeparation = { getRandomDuration(Duration.ofSeconds(30), Duration.ofSeconds(90)) },
			listOf("Trench", "AU-0821", "Horizon")
		)

		registerGlobalSpawner(GlobalWorldSpawner(
			"PRIVATEER_LOCUS",
			daggerLocusScheduler,
			SingleSpawn(
                RandomShipSupplier(
                    SYSTEM_DEFENSE_FORCES.asSpawnedShip(VETERAN),
                    SYSTEM_DEFENSE_FORCES.asSpawnedShip(PATROLLER),
                    SYSTEM_DEFENSE_FORCES.asSpawnedShip(TENETA),
                    SYSTEM_DEFENSE_FORCES.asSpawnedShip(BULWARK),
                    SYSTEM_DEFENSE_FORCES.asSpawnedShip(CONTRACTOR),
                    SYSTEM_DEFENSE_FORCES.asSpawnedShip(DAGGER),
                    SYSTEM_DEFENSE_FORCES.asSpawnedShip(DAYBREAK),
					SYSTEM_DEFENSE_FORCES.asSpawnedShip(RESOLUTE)
                ),
                daggerLocusScheduler.spawnLocationProvider,
                SpawnMessage.WorldMessage("<$PRIVATEER_LIGHT_TEAL>Privateer patrol <${HE_MEDIUM_GRAY}>operation vessel {0} spawned at {1}, {3}, in {4}".miniMessage()),
                {_ -> Supplier { daggerLocusScheduler.difficulty }},
				targetModeSupplier = { AITarget.TargetMode.PLAYER_ONLY }
            )
		))

		registerSingleWorldSpawner("Trench", "AU-0821", "Horizon") {
			SingleWorldSpawner(
				"PRIVATEER_ASSAULT_FORCE",
				it,
				AISpawnerTicker(
					pointChance = 0.5,
					pointThreshold = 20 * 60 * 7 * 10
				),
				BagSpawner(
					formatLocationSupplier(it, 1500.0, 2500.0) { player -> !player.hasProtection() },
					VariableIntegerAmount(30, 50),
					"<$PRIVATEER_LIGHT_TEAL>Privateer <${HE_MEDIUM_GRAY}>Assault Force has been spotted engaging a target in {3}, at {0} {2}".miniMessage(),
					null,
					asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(DAGGER).withRandomRadialOffset(200.0, 225.0, 0.0, 250.0), 2),
					asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(VETERAN).withRandomRadialOffset(175.0, 200.0, 0.0, 250.0), 4),
					asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(PATROLLER).withRandomRadialOffset(150.0, 175.0, 0.0, 250.0), 4),
					asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(TENETA).withRandomRadialOffset(100.0, 125.0, 0.0, 250.0), 4),
					asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(CONTRACTOR).withRandomRadialOffset(50.0, 75.0, 0.0, 250.0), 7),
					asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(BULWARK).withRandomRadialOffset(0.0, 50.0, 0.0, 250.0), 15),
					asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(RESOLUTE).withRandomRadialOffset(0.0, 50.0, 0.0, 250.0), 25),
					difficultySupplier = DifficultyModule::regularSpawnDifficultySupplier,
					targetModeSupplier = { AITarget.TargetMode.PLAYER_ONLY },
				)
			)
		}

		registerSingleWorldSpawner("AU-0821", "Horizon", "Trench") { SingleWorldSpawner(
			"TSAII_BASIC",
			it,
			AISpawnerTicker(
				pointThreshold = 30 * 20 * 60,
				pointChance = 0.5
			),
			BagSpawner(
				formatLocationSupplier(it, 1000.0, 2000.0) { player -> !player.hasProtection() },
				VariableIntegerAmount(10, 15),
				"<${TSAII_DARK_ORANGE}>Dangerous Tsaii Raiders have been reported in the area of {0}, {2}, in {3}. <$TSAII_MEDIUM_ORANGE>Please avoid the sector until the threat has been cleared!".miniMessage(),
				null,
				asBagSpawned(TSAII_RAIDERS.asSpawnedShip(SWARMER).withRandomRadialOffset(150.0, 200.0, 0.0), 1),
				asBagSpawned(TSAII_RAIDERS.asSpawnedShip(SCYTHE).withRandomRadialOffset(75.0, 150.0, 0.0), 3),
				asBagSpawned(TSAII_RAIDERS.asSpawnedShip(RAIDER).withRandomRadialOffset(50.0, 75.0, 0.0), 5),
				asBagSpawned(TSAII_RAIDERS.asSpawnedShip(REAVER).withRandomRadialOffset(0.0, 0.0, 0.0), 10),
				difficultySupplier = DifficultyModule::regularSpawnDifficultySupplier,
				targetModeSupplier = { AITarget.TargetMode.PLAYER_ONLY },
			)
		) }

		val tsaiiLocusScheduler = LocusScheduler(
			"<$TSAII_DARK_ORANGE>Tsaii Warband".miniMessage(),
			PIRATE_SATURATED_RED,
			duration = { Duration.ofMinutes(30) },
			separation = { getRandomDuration(Duration.ofHours(2), Duration.ofHours(5)) },
			difficultySupplier = DifficultyModule::regularSpawnDifficultySupplier,
			"<${HE_MEDIUM_GRAY}>A <$TSAII_DARK_ORANGE>Tsaii Warband<${HE_MEDIUM_GRAY}> has been spotted in {0} at {1} {3}. <$TSAII_MEDIUM_ORANGE>Please avoid the area.".miniMessage(),
			"<${HE_MEDIUM_GRAY}>The <$TSAII_DARK_ORANGE>Tsaii Warband<${HE_MEDIUM_GRAY}> has departed".miniMessage(),
			radius = 1500.0,
			spawnSeparation = { getRandomDuration(Duration.ofSeconds(30), Duration.ofSeconds(90)) },
			listOf("Trench", "AU-0821", "Horizon")
		)

		registerGlobalSpawner(GlobalWorldSpawner(
			"TSAII_LOCUS",
			tsaiiLocusScheduler,
			SingleSpawn(
                RandomShipSupplier(
                    TSAII_RAIDERS.asSpawnedShip(RAIDER),
                    TSAII_RAIDERS.asSpawnedShip(SCYTHE),
                    TSAII_RAIDERS.asSpawnedShip(SWARMER),
                    TSAII_RAIDERS.asSpawnedShip(REAVER)
                ),
                tsaiiLocusScheduler.spawnLocationProvider,
                SpawnMessage.WorldMessage("<${TSAII_DARK_ORANGE}>{0} has joined the raid {1}, {3}, in {4}.".miniMessage()),
                {_ -> Supplier { tsaiiLocusScheduler.difficulty }},
				targetModeSupplier = { AITarget.TargetMode.PLAYER_ONLY }
            )
		))

		registerGlobalSpawner(GlobalWorldSpawner(
			"BAIT_SHIP",
			AISpawnerTicker(
				pointChance = 0.5,
				pointThreshold = 20 * 60 * 7 * 5
			),
			SingleSpawn(
                RandomShipSupplier(
                    TSAII_RAIDERS.asSpawnedShip(AITemplateRegistry.BAIT_NIMBLE),
                    TSAII_RAIDERS.asSpawnedShip(AITemplateRegistry.BAIT_STRIKER),
                    TSAII_RAIDERS.asSpawnedShip(AITemplateRegistry.BAIT_WAYFINDER)
                ),
                Supplier {
                    val occupiedWorld = IonServer.server.worlds.filter { isSystemOccupied(it) && it.ion.hasFlag(ALLOW_AI_SPAWNS) }.randomOrNull() ?: return@Supplier null
                    return@Supplier formatLocationSupplier(occupiedWorld, 1000.0, 3000.0) { player -> !player.hasProtection() }.get()
                },
                spawnMessage = SpawnMessage.WorldMessage("<$EXPLORER_LIGHT_CYAN>Horizon Transit Lines<${HE_MEDIUM_GRAY}> {0} spawned at {1}, {3}, in {4}".miniMessage()),
                {_ -> Supplier { 0 }},
				targetModeSupplier = { AITarget.TargetMode.PLAYER_ONLY }
            )
		))

//		registerGlobalSpawner(GlobalWorldSpawner(
//			"SKUTTLE_SWARM",
//			AISpawnerTicker(
//				pointChance = 0.5,
//				pointThreshold = 20 * 60 * 7
//			),
//			BagSpawner(
//				Supplier {
//					val occupiedWorld = IonServer.server.worlds.filter { isSystemOccupied(it) && it.ion.hasFlag(ALLOW_AI_SPAWNS) }.randomOrNull() ?: return@Supplier null
//					return@Supplier formatLocationSupplier(occupiedWorld, 1000.0, 3000.0).get()
//				},
//				VariableIntegerAmount(3, 5),
//				groupMessage = "<$EXPLORER_LIGHT_CYAN>Horizon Transit Lines<${HE_MEDIUM_GRAY}> {0} spawned at {1}, {3}, in {4}".miniMessage(),
//				individualSpawnMessage = null,
//				asBagSpawned(SKELETONS.asSpawnedShip(AITemplateRegistry.SKUTTLE), 1)
//			)
//		))
//
//		registerSingleWorldSpawner("AU-0821") { SingleWorldSpawner(
//			"PUMPKIN_SPAWNER",
//			it,
//			AISpawnerTicker(
//				pointChance = 0.5,
//				pointThreshold = 20 * 60 * 7
//			),
//			SingleSpawn(
//				RandomShipSupplier(PUMPKINS.asSpawnedShip(PUMPKIN_DEVOURER), PUMPKINS.asSpawnedShip(PUMPKIN_KIN)),
//				formatLocationSupplier(it, 1000.0, 3000.0),
//				spawnMessage = SpawnMessage.ChatMessage("<#FFA500>A... {0}? has been spotted at {1}, {3}, in {4}".miniMessage())
//			)
//		)}
//
//		registerSingleWorldSpawner("AU-0821") { SingleWorldSpawner(
//			"ABYSSAL_SPAWNER",
//			it,
//			AISpawnerTicker(
//				pointChance = 0.5,
//				pointThreshold = 20 * 60 * 7
//			),
//			SingleSpawn(
//				RandomShipSupplier(
//					ABYSSAL.asSpawnedShip(HIGH_PRIESTESS),
//					ABYSSAL.asSpawnedShip(DREDGE),
//					ABYSSAL.asSpawnedShip(CHARM),
//					ABYSSAL.asSpawnedShip(EMPEROR),
//					ABYSSAL.asSpawnedShip(GRAFT)
//				),
//				formatLocationSupplier(it, 1000.0, 3000.0),
//				spawnMessage = SpawnMessage.ChatMessage("<$ABYSSAL_DESATURATED_RED>We arrive in your \"{4}\".".miniMessage())
//			)
//		)}

		/* helper suppliers --------------------------------------------------- */
		val localCtx : (World) -> LocationContext = { w -> LocationContext(randomLocationIn(w)) }
		val anyCtx   : () -> LocationContext      = { LocationContext(randomLocationAnywhere()) }

		/* GLOBAL (any world) ------------------------------------------------- */
		registerGlobalSpawner(
			LazyWorldSpawner(
				id = "DEEP_SPACE_MINING",
				worldFilter      = { it.hasFlag(SPACE_WORLD) }, //TODO: do something about this unused param
				mechanicSupplier = {
					DEEP_SPACE_MINING.spawnMechanicBuilder(anyCtx())
				}
			)
		)


		/* LOCAL (same world) ------------------------------------------------- */
		registerPerWorldSpawner { world ->
			LazyWorldSpawner(
				id = "DEBUG_CONVOY_LOCAL_${world.name}",
				worldFilter      = { it.uid == world.uid },
				mechanicSupplier = {
					DEBUG_CONVOY_LOCAL.spawnMechanicBuilder(localCtx(world))
				}
			)
		}

		/* GLOBAL (any world) ------------------------------------------------- */
		registerGlobalSpawner(
			LazyWorldSpawner(
				id = "DEBUG_CONVOY_GLOBAL",
				worldFilter      = { it.hasFlag(SPACE_WORLD) },
				mechanicSupplier = {
					DEBUG_CONVOY_GLOBAL.spawnMechanicBuilder(anyCtx())
				}
			)
		)


	}

	/** Returns a uniformly random location inside this world's current WorldBorder. */
	fun randomLocationIn(world: World): Location {
		val border = world.worldBorder
		val half   = border.size / 2.0
		val cx     = border.center.x
		val cz     = border.center.z

		val x = Random.nextDouble(cx - half, cx + half)
		val z = Random.nextDouble(cz - half, cz + half)

		// Pick a safe Y: 192 is above almost every structure but inside the height cap.
		// Replace with world.getHighestBlockYAt(x.toInt(), z.toInt()).plus(2) if you
		// want surface height instead.
		return Location(world, x, 192.0, z)
	}

	/** Same logic, but picks a random *loaded* world first. */
	fun randomLocationAnywhere(): Location =
		randomLocationIn(Bukkit.getWorlds().filter { it.hasFlag(SPACE_WORLD) }.random())

	fun loadPersistentData() {
		val stored = Configuration.load<PersistentSpawnerData>(IonServer.dataFolder, "persistentSpawnerData.json")

		for (spawner in getAllSpawners().filterIsInstance<PersistentDataSpawner<*>>()) {
			val data = stored.keyed[spawner.storageKey] ?: continue
			spawner.load(data)
		}
	}

	fun savePersistentData() = Tasks.async {
		val data = mutableMapOf<String, String>()

		for (spawner in getAllSpawners().filterIsInstance<PersistentDataSpawner<*>>()) {
			val stored = spawner.write() ?: continue
			data[spawner.storageKey] = stored
		}

		Configuration.save(PersistentSpawnerData(data), IonServer.dataFolder, "persistentSpawnerData.json")
	}

	@Serializable
	class PersistentSpawnerData(
		val keyed: MutableMap<String, String> = mutableMapOf<String, String>()
	)
}
