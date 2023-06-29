package net.starlegacy

import co.aikar.commands.BukkitCommandCompletionContext
import co.aikar.commands.BukkitCommandExecutionContext
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.PaperCommandManager
import java.io.File
import java.util.Locale
import net.horizonsend.ion.server.database.schema.nations.Nation
import net.horizonsend.ion.server.IonServer
import net.starlegacy.cache.nations.NationCache
import net.starlegacy.cache.nations.PlayerCache
import net.starlegacy.cache.nations.SettlementCache
import net.starlegacy.cache.trade.CargoCrates
import net.starlegacy.cache.trade.EcoStations
import net.starlegacy.command.SLCommand
import net.horizonsend.ion.server.database.MongoManager
import net.horizonsend.ion.server.database.schema.economy.BazaarItem
import net.horizonsend.ion.server.database.schema.economy.CargoCrate
import net.horizonsend.ion.server.database.schema.economy.CityNPC
import net.horizonsend.ion.server.database.schema.economy.CollectedItem
import net.horizonsend.ion.server.database.schema.economy.EcoStation
import net.horizonsend.ion.server.database.schema.misc.Shuttle
import net.horizonsend.ion.server.database.schema.starships.Blueprint
import net.horizonsend.ion.server.database.slPlayerId
import net.horizonsend.ion.server.features.spacestations.CachedSpaceStation
import net.horizonsend.ion.server.features.spacestations.SpaceStations
import net.starlegacy.feature.misc.CustomItem
import net.starlegacy.feature.misc.CustomItems
import net.starlegacy.feature.misc.Shuttles
import net.starlegacy.feature.nations.NationsMasterTasks
import net.starlegacy.feature.nations.region.Regions
import net.starlegacy.feature.nations.region.types.RegionSettlementZone
import net.starlegacy.feature.nations.region.types.RegionSpaceStation
import net.starlegacy.feature.nations.region.types.RegionTerritory
import net.starlegacy.feature.progression.MAX_LEVEL
import net.starlegacy.feature.space.CachedPlanet
import net.starlegacy.feature.space.CachedStar
import net.starlegacy.feature.space.Space
import net.starlegacy.util.Tasks
import net.starlegacy.util.loadConfig
import net.starlegacy.util.orNull
import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.litote.kmongo.and
import org.litote.kmongo.eq
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import redis.clients.jedis.Protocol
import kotlin.jvm.optionals.getOrNull

lateinit var SETTINGS: Config
lateinit var redisPool: JedisPool
var INITIALIZATION_COMPLETE: Boolean = false

val sharedDataFolder by lazy { File(SETTINGS.sharedFolder).apply { mkdirs() } }

fun legacyEnable(commandManager: PaperCommandManager) {
	SETTINGS = loadConfig(IonServer.dataFolder, "config") // Settings
	MongoManager.onEnable() // Database
	redisPool = JedisPool(SETTINGS.redis.host, Protocol.DEFAULT_PORT) // Redis
	for (component in components) { // Components
		component.onEnable()
		IonServer.server.pluginManager.registerEvents(component, IonServer)
	}
	for (listeners in listeners) IonServer.server.pluginManager.registerEvents(listeners, IonServer) // Listeners
	registerCommands(commandManager)
	scheduleNationTasks()
	INITIALIZATION_COMPLETE = true
}

fun legacyDisable() {
	SLCommand.ASYNC_COMMAND_THREAD.shutdown()
	for (component in components.asReversed()) try {
		component.onDisable()
	} catch (e: Exception) {
		e.printStackTrace()
	}
	redisPool.close()
	MongoManager.onDisable()
}

fun scheduleNationTasks() {
	if (SETTINGS.master) {
		// 20 ticks * 60 = 1 minute, 20 ticks * 60 * 60 = 1 hour
		Tasks.asyncRepeat(20 * 60, 20 * 60 * 60) {
			NationsMasterTasks.executeAll()
		}
	}
}

fun registerCommands(manager: PaperCommandManager) {
	// Add contexts
	manager.commandContexts.run {
		registerContext(CustomItem::class.java) { c: BukkitCommandExecutionContext ->
			val arg = c.popFirstArg()
			return@registerContext CustomItems[arg]
				?: throw InvalidCommandArgument("No custom item $arg found!")
		}

		registerContext(RegionSettlementZone::class.java) { c: BukkitCommandExecutionContext ->
			val arg = c.popFirstArg() ?: throw InvalidCommandArgument("Zone is required")
			return@registerContext Regions.getAllOf<RegionSettlementZone>().firstOrNull { it.name == arg }
				?: throw InvalidCommandArgument("Zone $arg not found")
		}

		registerContext(CachedStar::class.java) { c: BukkitCommandExecutionContext ->
			Space.starNameCache[c.popFirstArg().uppercase(Locale.getDefault())].orNull()
				?: throw InvalidCommandArgument("No such star")
		}

		registerContext(CachedPlanet::class.java) { c: BukkitCommandExecutionContext ->
			Space.planetNameCache[c.popFirstArg().uppercase(Locale.getDefault())].orNull()
				?: throw InvalidCommandArgument("No such planet")
		}

		registerContext(CargoCrate::class.java) { c: BukkitCommandExecutionContext ->
			CargoCrates[c.popFirstArg().uppercase(Locale.getDefault())]
				?: throw InvalidCommandArgument("No such crate")
		}

		registerContext(EcoStation::class.java) { c: BukkitCommandExecutionContext ->
			val name: String = c.popFirstArg()

			return@registerContext EcoStations.getByName(name)
				?: throw InvalidCommandArgument("Eco station $name not found")
		}

		registerContext(CachedSpaceStation::class.java) { c: BukkitCommandExecutionContext ->
			SpaceStations.spaceStationCache[c.popFirstArg().uppercase(Locale.getDefault())].getOrNull()
				?: throw InvalidCommandArgument("No such space station")
		}
	}

	// Add static tab completions
	mapOf(
		"levels" to (0..MAX_LEVEL).joinToString("|"),
		"customitems" to CustomItems.all().joinToString("|") { it.id },
		"npctypes" to CityNPC.Type.values().joinToString("|") { it.name }
	).forEach { manager.commandCompletions.registerStaticCompletion(it.key, it.value) }

	// Add async tab completions
	mapOf<String, (BukkitCommandCompletionContext) -> List<String>>(
		"gamerules" to { _ -> Bukkit.getWorlds().first().gameRules.toList() },
		"settlements" to { _ -> SettlementCache.all().map { it.name } },
		"member_settlements" to { c ->
			val player = c.player ?: throw InvalidCommandArgument("Players only")
			val nation = PlayerCache[player].nationOid

			SettlementCache.all().filter { nation != null && it.nation == nation }.map { it.name }
		},
		"nations" to { _ -> NationCache.all().map { it.name } },
		"zones" to { c ->
			val player = c.player ?: throw InvalidCommandArgument("Players only")
			val settlement = PlayerCache[player].settlementOid

			Regions.getAllOf<RegionSettlementZone>()
				.filter { settlement != null && it.settlement == settlement }
				.map { it.name }
		},
		"plots" to { c ->
			val player = c.player ?: throw InvalidCommandArgument("Players only")
			val slPlayerId = player.slPlayerId

			Regions.getAllOf<RegionSettlementZone>()
				.filter { it.owner == slPlayerId }
				.map { it.name }
		},
		"outposts" to { c ->
			val player = c.player ?: throw InvalidCommandArgument("Players only")
			val nation = PlayerCache[player].nationOid
			Regions.getAllOf<RegionTerritory>().filter { it.nation == nation }.map { it.name }
		},
		"stars" to { _ -> Space.getStars().map(CachedStar::name) },
		"planets" to { _ -> Space.getPlanets().map(CachedPlanet::name) },
		"crates" to { _ -> CargoCrates.crates.map { it.name } },
		"collecteditems" to { _ -> CollectedItem.all().map { "${EcoStations[it.station].name}.${it.itemString}" } },
		"ecostations" to { _ -> EcoStations.getAll().map { it.name } },
		"shuttles" to { _ -> Shuttle.all().map { it.name } },
		"shuttleSchematics" to { _ -> Shuttles.getAllSchematics() },
		"bazaarItemStrings" to { c ->
			val player = c.player ?: throw InvalidCommandArgument("Players only")
			val slPlayerId = player.slPlayerId
			val territory = Regions.findFirstOf<RegionTerritory>(player.location)
				?: throw InvalidCommandArgument("You're not in a territory!")
			BazaarItem.findProp(
				and(BazaarItem::seller eq slPlayerId, BazaarItem::cityTerritory eq territory.id),
				BazaarItem::itemString
			).toList()
		},
		"blueprints" to { c ->
			val player = c.player ?: throw InvalidCommandArgument("Players only")
			val slPlayerId = player.slPlayerId
			Blueprint.col.find(Blueprint::owner eq slPlayerId).map { it.name }.toList()
		}
	).forEach { manager.commandCompletions.registerAsyncCompletion(it.key, it.value) }

	// Register commands
	for (command in commands) manager.registerCommand(command)
}

inline fun <reified T : Event> listen(
	priority: EventPriority = EventPriority.NORMAL,
	ignoreCancelled: Boolean = false,
	noinline block: (T) -> Unit
): Unit = listen<T>(priority, ignoreCancelled) { _, event -> block(event) }

inline fun <reified T : Event> listen(
	priority: EventPriority = EventPriority.NORMAL,
	ignoreCancelled: Boolean = false,
	noinline block: (Listener, T) -> Unit
) {
	IonServer.server.pluginManager.registerEvent(
		T::class.java,
		object : Listener {},
		priority,
		{ listener, event -> block(listener, event as? T ?: return@registerEvent) },
		IonServer,
		ignoreCancelled
	)
}

fun <T> redis(block: Jedis.() -> T): T = redisPool.resource.use(block)
