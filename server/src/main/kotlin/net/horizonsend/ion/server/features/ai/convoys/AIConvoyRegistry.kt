package net.horizonsend.ion.server.features.ai.convoys

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.colors.PRIVATEER_LIGHT_TEAL
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.server.configuration.util.VariableIntegerAmount
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.MINING_GUILD
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.PERSEUS_EXPLORERS
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.SYSTEM_DEFENSE_FORCES
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.miningGuildMini
import net.horizonsend.ion.server.features.ai.faction.AIFaction.Companion.privateerMini
import net.horizonsend.ion.server.features.ai.module.misc.AIFleetManageModule
import net.horizonsend.ion.server.features.ai.module.misc.CaravanModule
import net.horizonsend.ion.server.features.ai.module.misc.DespawnModule
import net.horizonsend.ion.server.features.ai.module.misc.DifficultyModule
import net.horizonsend.ion.server.features.ai.module.misc.NavigationModule
import net.horizonsend.ion.server.features.ai.module.misc.ReinforcementSpawnerModule
import net.horizonsend.ion.server.features.ai.module.targeting.EnmityModule
import net.horizonsend.ion.server.features.ai.spawning.formatLocationSupplier
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.BagSpawner
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.BagSpawner.Companion.asBagSpawned
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.CompositeFleetSpawner
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.RandomShipSupplier
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.SingleSpawn
import net.horizonsend.ion.server.features.ai.spawning.spawner.mechanics.SpawnerMechanic
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.AMPH
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.ANGLE
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.BULWARK
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.CONTRACTOR
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.DAGGER
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.DAYBREAK
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.DESSLE
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.DUNKLEOSTEUS
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.GROUPER
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.MINHAUL_CHETHERITE
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.MINHAUL_REDSTONE
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.MINHAUL_TITANIUM
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.NIMBLE
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.OSTRICH
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.PATROLLER
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.RESOLUTE
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.STRIKER
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.TENETA
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.VETERAN
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.WAYFINDER
import net.horizonsend.ion.server.features.ai.starship.AITemplateRegistry.WOODPECKER
import net.horizonsend.ion.server.features.ai.util.AITarget
import net.horizonsend.ion.server.features.ai.util.SpawnMessage
import net.horizonsend.ion.server.features.economy.city.TradeCities
import net.horizonsend.ion.server.features.player.NewPlayerProtection.hasProtection
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import org.bukkit.World
import java.util.function.Supplier

object AIConvoyRegistry {
	private val templates = mutableMapOf<String, AIConvoyTemplate<out ConvoyContext>>()

	val SMALL_TC_CARAVAN = caravan("SMALL_TC_CARAVAN", 2) { ctx ->
		val city = ctx.city
		val route = TraceCityCaravanRoute(
			cites = (city.allowedDestinations ?: TradeCities.getAll()).shuffled().toMutableList(),
			source = city
		)

		CompositeFleetSpawner(
			mechanics = makeSmallCaravanComponents(route = route, difficulty = fixedDifficulty(2), targetMode = fixedTargetMode(AITarget.TargetMode.MIXED)),
			locationProvider = { route.getSourceLocation() },
			groupMessage = "Small convoy fleet!".miniMessage(),
			individualSpawnMessage = SpawnMessage.WorldMessage("Ship joined the convoy!".miniMessage()),
			difficultySupplier = AIConvoyRegistry["SMALL_TC_CARAVAN"]!!.difficultySupplier,
			targetModeSupplier = fixedTargetMode(AITarget.TargetMode.MIXED),
			controllerModifier = { controller -> addCaravanModule(controller, route, "SMALL_TC_CARAVAN") }
		)
	}

	fun makeSmallCaravanComponents(route: ConvoyRoute, difficulty: (World) -> Supplier<Int>, targetMode: Supplier<AITarget.TargetMode>): List<SpawnerMechanic> {
		return listOf(
			SingleSpawn(
				RandomShipSupplier(
					PERSEUS_EXPLORERS.asSpawnedShip(AITemplateRegistry.EXOTRAN_CHETHERITE),
					PERSEUS_EXPLORERS.asSpawnedShip(AITemplateRegistry.EXOTRAN_REDSTONE),
					PERSEUS_EXPLORERS.asSpawnedShip(AITemplateRegistry.EXOTRAN_TITANIUM),
					PERSEUS_EXPLORERS.asSpawnedShip(AMPH)
				),
				{ route.getSourceLocation() },
				SpawnMessage.WorldMessage("Flag trade ship joined the convoy!".miniMessage()),
				difficulty, targetModeSupplier = targetMode
			),
			BagSpawner(
				locationProvider = formatLocationSupplier(route.getSourceLocation().world, 1500.0, 2500.0) { player -> !player.hasProtection() },
				budget = VariableIntegerAmount(5, 15),
				groupMessage = "<$PRIVATEER_LIGHT_TEAL>Privateer <$HE_MEDIUM_GRAY>Auxiliary caravan ships in {3}, at {0} {2}".miniMessage(),
				individualSpawnMessage = null,
				difficultySupplier = difficulty,
				targetModeSupplier = targetMode,
				fleetSupplier = { null },
				asBagSpawned(PERSEUS_EXPLORERS.asSpawnedShip(WAYFINDER).withRandomRadialOffset(200.0, 225.0, 0.0, 250.0), 1),
				asBagSpawned(PERSEUS_EXPLORERS.asSpawnedShip(STRIKER).withRandomRadialOffset(200.0, 225.0, 0.0, 250.0), 1),
				asBagSpawned(PERSEUS_EXPLORERS.asSpawnedShip(NIMBLE).withRandomRadialOffset(200.0, 225.0, 0.0, 250.0), 1),
				asBagSpawned(PERSEUS_EXPLORERS.asSpawnedShip(DESSLE).withRandomRadialOffset(200.0, 225.0, 0.0, 250.0), 1),
				asBagSpawned(PERSEUS_EXPLORERS.asSpawnedShip(MINHAUL_CHETHERITE).withRandomRadialOffset(175.0, 200.0, 0.0, 250.0), 3),
				asBagSpawned(PERSEUS_EXPLORERS.asSpawnedShip(MINHAUL_REDSTONE).withRandomRadialOffset(175.0, 200.0, 0.0, 250.0), 3),
				asBagSpawned(PERSEUS_EXPLORERS.asSpawnedShip(MINHAUL_TITANIUM).withRandomRadialOffset(150.0, 175.0, 0.0, 250.0), 3),
				asBagSpawned(PERSEUS_EXPLORERS.asSpawnedShip(AMPH).withRandomRadialOffset(100.0, 125.0, 0.0, 250.0), 5)
			),
			BagSpawner(
				locationProvider = formatLocationSupplier(route.getSourceLocation().world, 1500.0, 2500.0) { player -> !player.hasProtection() },
				budget = VariableIntegerAmount(5, 15),
				groupMessage = "<$PRIVATEER_LIGHT_TEAL>Privateer <$HE_MEDIUM_GRAY>Escorting defense force spotted in {3}, at {0} {2}".miniMessage(),
				individualSpawnMessage = null,
				difficultySupplier = difficulty, targetModeSupplier = targetMode, fleetSupplier = { null },
				asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(DAGGER).withRandomRadialOffset(200.0, 225.0, 0.0, 250.0), 1),
				asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(VETERAN).withRandomRadialOffset(175.0, 200.0, 0.0, 250.0), 3),
				asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(PATROLLER).withRandomRadialOffset(150.0, 175.0, 0.0, 250.0), 3),
				asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(TENETA).withRandomRadialOffset(100.0, 125.0, 0.0, 250.0), 5)
			),
		)
	}

	val DEEP_SPACE_MINING = freeRoute("DEEP_SPACE_MINING", 2) { ctx ->
		val route = RandomConvoyRoute.fromList(worldList = listOf("Trench", "AU-0821", "Horizon"), numDestinations = 10)

		CompositeFleetSpawner(
			mechanics = makeMiningComponents(route = route, difficulty = fixedDifficulty(2), targetMode = fixedTargetMode(AITarget.TargetMode.MIXED)),
			locationProvider = { route.getSourceLocation() },
			groupMessage = "$miningGuildMini<GOLD><bold> Deep Space Mining Convoy</bold> <${HE_MEDIUM_GRAY}>has arrived in {3}, at {0} {2}".miniMessage(),
			individualSpawnMessage = SpawnMessage.WorldMessage("$miningGuildMini <${HE_MEDIUM_GRAY}> {0} joined the convoy".miniMessage()),
			difficultySupplier = AIConvoyRegistry["DEEP_SPACE_MINING"]!!.difficultySupplier,
			targetModeSupplier = fixedTargetMode(AITarget.TargetMode.PLAYER_ONLY),
			controllerModifier = { controller -> addCaravanModule(controller, route, "DEEP_SPACE_MINING") }
		)
	}

	fun makeMiningComponents(route: ConvoyRoute, difficulty: (World) -> Supplier<Int>, targetMode: Supplier<AITarget.TargetMode>): List<SpawnerMechanic> {
		return listOf(
			SingleSpawn(
				shipPool = RandomShipSupplier(
					MINING_GUILD.asSpawnedShip(ANGLE),
				),
				locationProvider = { route.getSourceLocation() },
				spawnMessage = SpawnMessage.WorldMessage("Flag trade ship joined the convoy!".miniMessage()),
				difficultySupplier = difficulty, targetModeSupplier = targetMode
			),
			BagSpawner(
				locationProvider = formatLocationSupplier(route.getSourceLocation().world, 1500.0, 2500.0) { player -> !player.hasProtection() },
				budget = VariableIntegerAmount(5, 15),
				groupMessage = "Additional mining ships".miniMessage(),
				individualSpawnMessage = null,
				difficultySupplier = difficulty,
				targetModeSupplier = targetMode,
				fleetSupplier = { null },
				asBagSpawned(MINING_GUILD.asSpawnedShip(DUNKLEOSTEUS).withRandomRadialOffset(100.0, 200.0, 0.0, 250.0), 7),
				asBagSpawned(MINING_GUILD.asSpawnedShip(GROUPER).withRandomRadialOffset(100.0, 200.0, 0.0, 250.0), 5),
				asBagSpawned(MINING_GUILD.asSpawnedShip(OSTRICH).withRandomRadialOffset(100.0, 200.0, 0.0, 250.0), 3),
				asBagSpawned(MINING_GUILD.asSpawnedShip(WOODPECKER).withRandomRadialOffset(100.0, 200.0, 0.0, 250.0), 2),
			),
			BagSpawner(
				locationProvider = formatLocationSupplier(route.getSourceLocation().world, 1500.0, 2500.0) { player -> !player.hasProtection() },
				budget = VariableIntegerAmount(5, 10),
				groupMessage = "<$PRIVATEER_LIGHT_TEAL>Privateer <$HE_MEDIUM_GRAY>Escorting defense force spotted in {3}, at {0} {2}".miniMessage(),
				individualSpawnMessage = null,
				difficultySupplier = difficulty,
				targetModeSupplier = targetMode,
				fleetSupplier = { null },
				asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(DAGGER).withRandomRadialOffset(200.0, 225.0, 0.0, 250.0), 1),
				asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(VETERAN).withRandomRadialOffset(175.0, 200.0, 0.0, 250.0), 3),
				asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(PATROLLER).withRandomRadialOffset(150.0, 175.0, 0.0, 250.0), 3),
				asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(TENETA).withRandomRadialOffset(100.0, 125.0, 0.0, 250.0), 5),
			)
		)
	}

	val PRIVATEER_PATROL_SMALL = freeRoute("PRIVATEER_PATROL_SMALL", 1) { ctx ->
		val route = RandomConvoyRoute.fromAnyStation(numDestinations = 8)

		CompositeFleetSpawner(
			mechanics = makePatrolSmallComponents(route = route, difficulty = fixedDifficulty(1), targetMode = fixedTargetMode(AITarget.TargetMode.MIXED)),
			locationProvider = { route.getSourceLocation() },
			groupMessage = "$privateerMini<${HE_MEDIUM_GRAY}> Patrol has arrived in {3}, at {0} {2}".miniMessage(),
			individualSpawnMessage = SpawnMessage.WorldMessage("Ship <${HE_MEDIUM_GRAY}> {0} joined the patrol".miniMessage()),
			difficultySupplier = AIConvoyRegistry["PRIVATEER_PATROL_SMALL"]!!.difficultySupplier,
			targetModeSupplier = fixedTargetMode(AITarget.TargetMode.MIXED),
			controllerModifier = { controller -> modifyPatrol(controller = controller, route = route, templateId = "PRIVATEER_PATROL_SMALL") }
		)
	}

	fun makePatrolSmallComponents(route: ConvoyRoute, difficulty: (World) -> Supplier<Int>, targetMode: Supplier<AITarget.TargetMode>): List<SpawnerMechanic> {
		return listOf(
			SingleSpawn(
				RandomShipSupplier(
					SYSTEM_DEFENSE_FORCES.asSpawnedShip(BULWARK),
				),
				{ route.getSourceLocation() },
				SpawnMessage.WorldMessage("Flag trade ship joined the patrol!".miniMessage()),
				difficulty, targetMode
			),
			BagSpawner(
				locationProvider = formatLocationSupplier(route.getSourceLocation().world, 1500.0, 2500.0) { player -> !player.hasProtection() },
				budget = VariableIntegerAmount(5, 15),
				groupMessage = "Additional patrol ships".miniMessage(),
				individualSpawnMessage = null,
				difficultySupplier = difficulty,
				targetModeSupplier = targetMode,
				fleetSupplier = { null },
				asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(DAGGER).withRandomRadialOffset(200.0, 225.0, 0.0, 250.0), 2),
				asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(VETERAN).withRandomRadialOffset(175.0, 200.0, 0.0, 250.0), 4),
				asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(PATROLLER).withRandomRadialOffset(150.0, 175.0, 0.0, 250.0), 4),
				asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(TENETA).withRandomRadialOffset(100.0, 125.0, 0.0, 250.0), 4),
				asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(CONTRACTOR).withRandomRadialOffset(50.0, 75.0, 0.0, 250.0), 7)
			)
		)
	}

	val PRIVATEER_PATROL_MEDIUM = freeRoute("PRIVATEER_PATROL_SMALL", 1) { ctx ->
		val route = RandomConvoyRoute.fromAnyStation(numDestinations = 8)

		CompositeFleetSpawner(
			mechanics = makePatrolMediumSpawners(route = route, difficulty = fixedDifficulty(1), targetMode = fixedTargetMode(AITarget.TargetMode.MIXED)),
			locationProvider = { route.getSourceLocation() },
			groupMessage = "$privateerMini<${HE_MEDIUM_GRAY}> Patrol has arrived in {3}, at {0} {2}".miniMessage(),
			individualSpawnMessage = SpawnMessage.WorldMessage("Ship <${HE_MEDIUM_GRAY}> {0} joined the patrol".miniMessage()),
			difficultySupplier = AIConvoyRegistry["PRIVATEER_PATROL_SMALL"]!!.difficultySupplier,
			targetModeSupplier = fixedTargetMode(AITarget.TargetMode.MIXED),
			controllerModifier = { controller -> modifyPatrol(controller = controller, route = route, templateId = "PRIVATEER_PATROL_SMALL") }
		)
	}

	fun makePatrolMediumSpawners(route: ConvoyRoute, difficulty: (World) -> Supplier<Int>, targetMode: Supplier<AITarget.TargetMode>): List<SpawnerMechanic> {
		return listOf(
			SingleSpawn(
				RandomShipSupplier(
					SYSTEM_DEFENSE_FORCES.asSpawnedShip(RESOLUTE),
				),
				{ route.getSourceLocation() },
				SpawnMessage.WorldMessage("Flag trade ship joined the patrol!".miniMessage()),
				difficulty,
				targetMode
			),
			BagSpawner(
				locationProvider = formatLocationSupplier(route.getSourceLocation().world, 1500.0, 2500.0) { player -> !player.hasProtection() },
				budget = VariableIntegerAmount(10, 20),
				groupMessage = "Additional patrol ships".miniMessage(),
				individualSpawnMessage = null,
				difficultySupplier = difficulty,
				targetModeSupplier = targetMode,
				fleetSupplier = { null },
				asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(DAGGER).withRandomRadialOffset(200.0, 225.0, 0.0, 250.0), 3),
				asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(VETERAN).withRandomRadialOffset(175.0, 200.0, 0.0, 250.0), 5),
				asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(PATROLLER).withRandomRadialOffset(150.0, 175.0, 0.0, 250.0), 5),
				asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(TENETA).withRandomRadialOffset(100.0, 125.0, 0.0, 250.0), 3),
				asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(CONTRACTOR).withRandomRadialOffset(50.0, 75.0, 0.0, 250.0), 8),
				asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(BULWARK).withRandomRadialOffset(50.0, 75.0, 0.0, 250.0), 10),
				asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(BULWARK).withRandomRadialOffset(50.0, 75.0, 0.0, 250.0), 10),
				asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(DAYBREAK).withRandomRadialOffset(50.0, 75.0, 0.0, 250.0), 10),
			)
		)
	}

	fun modifyPatrol(
		controller: AIController,
		route: ConvoyRoute,
		templateId: String
	) {
		val targeting = controller.getCoreModuleByType<EnmityModule>()!!
		targeting.enmityFilter = EnmityModule.naughtyFilter(controller)

		val difficulty = controller.getCoreModuleByType<DifficultyModule>()!!
		if (controller.getCoreModuleByType<NavigationModule>() == null) {
			controller.addCoreModule(NavigationModule(controller,targeting, difficulty))
		}
		addCaravanModule(controller,route,templateId)
	}

	val DEBUG_CONVOY_LOCAL: AIConvoyTemplate<LocationContext> = freeRoute("DEBUG_CONVOY_LOCAL", 2) { ctx ->
		val route = RandomConvoyRoute.sameWorld(worldName = ctx.source.world.name, numDestinations = 5)

		CompositeFleetSpawner(
			mechanics = makedebugComponents(route, fixedDifficulty(2), fixedTargetMode(AITarget.TargetMode.MIXED)),
			locationProvider = { route.getSourceLocation() },
			groupMessage = "Debug convoy (local)".miniMessage(),
			individualSpawnMessage = null,
			difficultySupplier = AIConvoyRegistry["DEBUG_CONVOY_LOCAL"]!!.difficultySupplier,
			targetModeSupplier = fixedTargetMode(AITarget.TargetMode.MIXED),
			controllerModifier = { controller -> addCaravanModule(controller, route, "DEBUG_CONVOY_LOCAL") }
		)
	}

	val DEBUG_CONVOY_GLOBAL = freeRoute("DEBUG_CONVOY_GLOBAL", 2) { _ ->
		val route = RandomConvoyRoute.anyWorld(numDestinations = 5)

		CompositeFleetSpawner(
			mechanics = makedebugComponents(route, fixedDifficulty(2), fixedTargetMode(AITarget.TargetMode.MIXED)),
			locationProvider = { route.getSourceLocation() },
			groupMessage = "Debug convoy (global)".miniMessage(),
			individualSpawnMessage = null,
			difficultySupplier = AIConvoyRegistry["DEBUG_CONVOY_GLOBAL"]!!.difficultySupplier,
			targetModeSupplier = fixedTargetMode(AITarget.TargetMode.MIXED),
			controllerModifier = { controller -> addCaravanModule(controller, route, "DEBUG_CONVOY_GLOBAL") }
		)
	}

	fun makedebugComponents(route: ConvoyRoute, difficulty: (World) -> Supplier<Int>, targetMode: Supplier<AITarget.TargetMode>): List<SpawnerMechanic> {
		return listOf(
			SingleSpawn(
				RandomShipSupplier(
					SYSTEM_DEFENSE_FORCES.asSpawnedShip(BULWARK),
				),
				{ route.getSourceLocation() },
				SpawnMessage.WorldMessage("Flag trade ship joined the convoy!".miniMessage()),
				difficulty, targetMode
			),
			BagSpawner(
				locationProvider = formatLocationSupplier(route.getSourceLocation().world, 1500.0, 2500.0) { player -> !player.hasProtection() },
				budget = VariableIntegerAmount(5, 15),
				groupMessage = "<$PRIVATEER_LIGHT_TEAL>Privateer <$HE_MEDIUM_GRAY>Escorting defense force spotted in {3}, at {0} {2}".miniMessage(),
				individualSpawnMessage = null,
				difficultySupplier = difficulty,
				targetModeSupplier = targetMode,
				fleetSupplier = { null },
				asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(DAGGER).withRandomRadialOffset(200.0, 225.0, 0.0, 250.0), 1),
				asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(VETERAN).withRandomRadialOffset(175.0, 200.0, 0.0, 250.0), 3),
				asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(PATROLLER).withRandomRadialOffset(150.0, 175.0, 0.0, 250.0), 3),
				asBagSpawned(SYSTEM_DEFENSE_FORCES.asSpawnedShip(TENETA).withRandomRadialOffset(100.0, 125.0, 0.0, 250.0), 5)
			),
		)
	}

	/* ---------- caravan (city‑to‑city) ----------------------------------- */
	fun caravan(
		id: String,
		difficulty: Int,
		spawner: (CityContext) -> SpawnerMechanic
	) = register(
		AIConvoyTemplate(
			id,
			spawner,
			fixedDifficulty(difficulty),
		)
	)

	/* ---------- free‑route convoys -------------------------------------- */
	fun freeRoute(
		id: String,
		difficulty: Int,
		spawner: (LocationContext) -> SpawnerMechanic
	) = register(
		AIConvoyTemplate(
			id,
			spawner,
			fixedDifficulty(difficulty),
		)
	)

	/**
	 * Attaches a CaravanModule that drives the convoy along [route].
	 *
	 * @param controller the freshly‑spawned AIController of the flagship
	 * @param route      the ConvoyRoute shared by the whole convoy
	 * @param templateId identifier of the convoy template (for logging / UI)
	 */
	fun addCaravanModule(
		controller: AIController,
		route: ConvoyRoute,
		templateId: String
	) {
		controller.getCoreModuleByType<ReinforcementSpawnerModule>()?.controllerModifiers?.add { controller -> addCaravanModule(controller, route, templateId) }

		controller.addUtilModule(
			CaravanModule(
				controller,
				controller.getUtilModule(AIFleetManageModule::class.java)!!.fleet,
				/* template = */ templates[templateId] as AIConvoyTemplate<out ConvoyContext>,
				route.getSourceLocation(),
				route
			)
		)
		controller.getCoreModuleByType<EnmityModule>()?.removeAnchor()
		controller.addUtilModule(DespawnModule(controller, DespawnModule.neverDespawn))
	}

	private fun fixedDifficulty(v: Int): (World) -> Supplier<Int> = { _: World -> Supplier { v } }
	private fun fixedTargetMode(mode: AITarget.TargetMode): Supplier<AITarget.TargetMode> = Supplier { mode }

	private fun <C : ConvoyContext> register(template: AIConvoyTemplate<C>): AIConvoyTemplate<C> {
		templates[template.identifier] = template   // <- just store it
		return template                              // <- and return it unchanged
	}

	operator fun get(id: String) = templates[id]
}
