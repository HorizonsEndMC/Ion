package net.horizonsend.ion.server.command.misc

import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import io.papermc.paper.util.StacktraceDeobfuscator
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.utils.text.button
import net.horizonsend.ion.common.utils.text.formatPaginatedMenu
import net.horizonsend.ion.common.utils.text.join
import net.horizonsend.ion.common.utils.text.toComponent
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlock
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlocks
import net.horizonsend.ion.server.features.multiblock.entity.linkages.MultiblockLinkage
import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.inputs.IOType
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ItemExtractorData
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNetwork
import net.horizonsend.ion.server.features.transport.nodes.cache.DestinationCacheHolder
import net.horizonsend.ion.server.features.transport.nodes.cache.ItemTransportCache
import net.horizonsend.ion.server.features.transport.nodes.cache.SolarPanelCache
import net.horizonsend.ion.server.features.transport.nodes.cache.TransportCache
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.features.transport.nodes.types.PowerNode.PowerInputNode
import net.horizonsend.ion.server.features.transport.nodes.util.CacheState
import net.horizonsend.ion.server.features.transport.nodes.util.MappedDestinationCache
import net.horizonsend.ion.server.features.transport.nodes.util.MonoDestinationCache
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.features.world.chunk.IonChunk.Companion.ion
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent.callback
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.slf4j.Logger
import java.lang.management.ManagementFactory
import java.lang.management.ThreadInfo

@CommandPermission("starlegacy.transportdebug")
@CommandAlias("transportdebug|transportbug")
object TransportDebugCommand : SLCommand() {
	override fun onEnable(manager: PaperCommandManager) {
		manager.commandCompletions.registerCompletion("inputType") { IOType.byName.keys.map(String::lowercase) }
		manager.commandContexts.registerContext(IOType::class.java) { IOType[it.popFirstArg()] }
		manager.commandCompletions.setDefaultCompletion("inputType", IOType::class.java)
	}

	@Subcommand("threaddump")
	fun forceDump(sender: Player) {
		log.error("Entire Thread Dump:")
		val threads = ManagementFactory.getThreadMXBean().dumpAllThreads(true, true)
		for (thread in threads) {
			dumpThread(thread, log)
		}
	}

	private fun dumpThread(thread: ThreadInfo, log: Logger) {
		log.error("------------------------------")
		//
		log.error("Current Thread: " + thread.threadName)
		log.error(
			("\tPID: " + thread.threadId
			+ " | Suspended: " + thread.isSuspended
			+ " | Native: " + thread.isInNative
			+ " | State: " + thread.threadState)
		)
		if (thread.lockedMonitors.size != 0) {
			log.error("\tThread is waiting on monitor(s):")
			for (monitor in thread.lockedMonitors) {
				log.error("\t\tLocked on:" + monitor.lockedStackFrame)
			}
		}
		log.error("\tStack:")
		//
		for (stack in StacktraceDeobfuscator.INSTANCE.deobfuscateStacktrace(thread.stackTrace))  // Paper
		{
			log.error("\t\t" + stack)
		}
	}

	@Subcommand("dump inputs chunk")
	fun dumpInputsChunk(sender: Player, type: IOType<*>) {
		val inputManager = sender.world.ion.inputManager
		val loc = Vec3i(sender.location)
		val inputs = inputManager.getLocations(type)
			.map { toVec3i(it) }
			.filter { it.distance(loc) < 100.0 }

		sender.highlightBlocks(inputs, 50L)
		sender.information("${inputs.size} inputs")
	}

	@Subcommand("dump inputs starship")
	fun dumpInputsShip(sender: Player, type: IOType<*>) {
		val ship = getStarshipRiding(sender)
		val inputManager = ship.transportManager.ioManager

		val inputs = inputManager
			.getLocations(type)
			.map { ship.transportManager.getGlobalCoordinate(toVec3i(it)) }

		sender.highlightBlocks(inputs, 50L)
		sender.information("${inputs.size} inputs")
	}

	@Subcommand("dump nodes chunk")
	fun dumpNodesChunk(sender: Player, network: CacheType) {
		val ionChunk = sender.chunk.ion()
		val grid = network.get(ionChunk)
			.getRawCache()
			.filter { entry -> entry.value !is CacheState.Empty }

		sender.information("Is ready: ${network.get(ionChunk).ready}")
		sender.information("${grid.size} covered position(s).")
		sender.information("${grid.values.distinct().size} unique node(s).")

		grid.forEach { (t, _) ->
			val vec = toVec3i(t)
			sender.highlightBlock(vec, 50L)
		}
	}

	@Subcommand("dump nodes ship")
	fun dumpNodesShip(sender: Player, network: CacheType) {
		val ship = getStarshipRiding(sender)
		val grid = network.get(ship)
			.getRawCache()
			.filter { entry -> entry.value !is CacheState.Empty }

		sender.information("${grid.size} covered position(s).")
		sender.information("${grid.values.distinct().size} unique node(s).")

		grid.forEach { (localKey, _) ->
			val vec = ship.transportManager.getGlobalCoordinate(toVec3i(localKey))
			sender.highlightBlock(vec, 50L)
		}
	}

	@Subcommand("dump extractors chunk")
	fun dumpExtractorsChunk(sender: Player, network: CacheType) {
		val ionChunk = sender.chunk.ion()
		val extractors = network.get(ionChunk).holder.getExtractorManager()

		sender.information("${extractors.getExtractors().size} covered position(s).")

		extractors.getExtractors().forEach { extractor ->
			sender.highlightBlock(toVec3i(extractor.pos), 50L)
		}
	}

	@Subcommand("dump extractors ship")
	fun dumpExtractorsShip(sender: Player, network: CacheType) {
		val ship = getStarshipRiding(sender)
		val extractors = network.get(ship).holder.getExtractorManager()

		sender.information("${extractors.getExtractors().size} covered position(s).")

		extractors.getExtractors().forEach { extractor ->
			sender.highlightBlock(ship.transportManager.getGlobalCoordinate(toVec3i(extractor.pos)), 50L)
		}
	}

	@Subcommand("dump filters chunk")
	fun dumpFiltersChunk(sender: Player, network: CacheType) {
		val ionChunk = sender.chunk.ion()
		val filters = network.get(ionChunk).holder.getFilterManager()

		sender.information("${filters.getFilters().size} covered position(s).")

		filters.getFilters().forEach { filterData ->
			sender.highlightBlock(toVec3i(filterData.position), 50L)
		}
	}

	@Subcommand("dump filters ship")
	fun dumpFiltersShip(sender: Player, network: CacheType) {
		val ship = getStarshipRiding(sender)
		val filters = network.get(ship).holder.getFilterManager()

		sender.information("${filters.getFilters().size} covered position(s).")

		filters.getFilters().forEach { filterData ->
			sender.highlightBlock(toVec3i(filterData.position), 50L)
		}
	}

	@Subcommand("dump merges chunk")
	fun dumpMergePointsChunk(sender: Player, network: CacheType) {
		val ionChunk = sender.chunk.ion()
		val mergePoints = network.get(ionChunk).holder.getMultiblockManager().getLinkageManager().getAll()

		sender.information("${mergePoints.size} linkages(s).")

		mergePoints.forEach {
			sender.highlightBlock(toVec3i(it.key), 50L)
		}

		mergePoints.forEach { entry ->
			sender.information("holder {0}", entry.value.getOwners())
		}
	}

	@Subcommand("dump merges ship")
	fun dumpMergePointsShip(sender: Player, network: CacheType) {
		val ship = getStarshipRiding(sender)
		val manager = network.get(ship).holder.getMultiblockManager()
		val mergePoints = manager.getLinkageManager().getAll()

		sender.information("${mergePoints.size} linkages(s).")

		mergePoints.forEach {
			val global = manager.getGlobalCoordinate(toVec3i(it.key))

			val holder = it.value

			 holder.getLinkages().forEach { multiblockLinkage: MultiblockLinkage ->
				sender.information("Linkage facing ${multiblockLinkage.linkDirection} (${multiblockLinkage.linkDirection[multiblockLinkage.owner.structureDirection]} at ${toVec3i(multiblockLinkage.location)} (${multiblockLinkage.owner.manager.getGlobalCoordinate(toVec3i(multiblockLinkage.location))}) to ${toVec3i(multiblockLinkage.getLinkLocation())}")
				sender.highlightBlock(global, 50L)
			}
		}
	}

	private fun requireLookingAt(sender: Player, network: (Block) -> TransportCache): Pair<Node, BlockKey> {
		val targeted = sender.getTargetBlockExact(10) ?: fail { "No block in range" }
		val grid = network(targeted)
		val key = toBlockKey(targeted.x, targeted.y, targeted.z)

		val node = grid.getOrCache(key) ?: fail { "You aren't looking at a node!" }
		return node to key
	}

	@Subcommand("get node look chunk")
	fun getNodeChunk(sender: Player, network: CacheType) {
		val (node, location) = requireLookingAt(sender) { network.get(it.chunk.ion()) }
		sender.information("Targeted node: $node at ${toVec3i(location)}")
	}

	@Subcommand("check node look chunk")
	fun checkNodeChunk(sender: Player, network: CacheType) {
		val targeted = sender.getTargetBlockExact(10) ?: fail { "No block in range" }
		val grid = network.get(targeted.chunk.ion())
		val key = toBlockKey(targeted.x, targeted.y, targeted.z)

		sender.information("Present: ${grid.isCached(key)}")
		val node = grid.getOrCache(key) ?: fail { "You aren't looking at a node!" }
		sender.information("Targeted node: $node at ${toVec3i(key)}")
	}

	@Subcommand("get node look ship")
	fun getNodeShip(sender: Player, network: CacheType) {
		val (node, location) = requireLookingAt(sender) { network.get(getStarshipRiding(sender)) }
		sender.information("Targeted node: $node at ${toVec3i(location)}")
	}

	@Subcommand("get cached destinations chunk")
	fun getCachedDestinationsChunk(sender: Player, network: CacheType, @Optional pageNumber: Int?) {
		var cacheHolder: DestinationCacheHolder? = null
		val (node, location) = requireLookingAt(sender) { network.get(sender.chunk.ion()).apply { cacheHolder = this as? DestinationCacheHolder } }
		sender.information("Targeted node: $node at ${toVec3i(location)}")

		val cache = cacheHolder?.destinationCache ?: fail { "Something went wrong" }

		if (cache is MonoDestinationCache) {
			val paths = cache.get(node::class, location) ?: fail { "Expired Cache" }
			val vectors = paths.map { toVec3i(it.destinationPosition) }
			sender.sendMessage(formatPaginatedMenu(vectors, "/get cached destinations chunk", pageNumber ?: 1) { vec, _ -> vec.toComponent() })
		} else if (cache is MappedDestinationCache<*>) {
			cache as MappedDestinationCache<ItemStack>

			val paths = cache.get(node::class, sender.inventory.itemInMainHand, location) ?: fail { "Expired Cache" }
			val vectors = paths.map { toVec3i(it.destinationPosition) }
			sender.sendMessage(formatPaginatedMenu(vectors, "/get cached destinations chunk", pageNumber ?: 1) { vec, _ -> vec.toComponent() })
		}
	}

	@Subcommand("get cached destinations ship")
	fun getCachedDestinationsShip(sender: Player, network: CacheType, @Optional pageNumber: Int?) {
		var cacheHolder: DestinationCacheHolder? = null
		val (node, location) = requireLookingAt(sender) { network.get(getStarshipRiding(sender)).apply { cacheHolder = this as? DestinationCacheHolder } }
		sender.information("Targeted node: $node at ${toVec3i(location)}")

		val cache = cacheHolder?.destinationCache ?: fail { "Something went wrong" }

		if (cache is MonoDestinationCache) {
			val paths = cache.get(node::class, location) ?: fail { "Expired Cache" }
			val vectors = paths.map { toVec3i(it.destinationPosition) }
			sender.sendMessage(formatPaginatedMenu(vectors, "/get cached destinations ship", pageNumber ?: 1) { vec, _ -> vec.toComponent().clickEvent(callback { audience -> audience.highlightBlock(vec, 10L) }) })
		} else if (cache is MappedDestinationCache<*>) {
			cache as MappedDestinationCache<ItemStack>

			val paths = cache.get(node::class, sender.inventory.itemInMainHand, location) ?: fail { "Expired Cache" }
			val vectors = paths.map { toVec3i(it.destinationPosition) }
			sender.sendMessage(formatPaginatedMenu(vectors, "/get cached destinations ship", pageNumber ?: 1) { vec, _ -> vec.toComponent().clickEvent(callback { audience -> audience.highlightBlock(vec, 10L) }) })
		}
	}

	@Subcommand("test extractor")
	fun onTick(sender: Player, type: CacheType) {
		val (_, location) = requireLookingAt(sender) { type.get(it.chunk.ion()) }
		val chunk = IonChunk.getFromWorldCoordinates(sender.world, getX(location), getZ(location)) ?: fail { "Chunk not loaded" }
		val grid = type.get(chunk)
//		if (grid.holder.getExtractorManager().isExtractorPresent(location)) fail { "Extractor not targeted" }

		grid.tickExtractor(location, 1.0, null, 0, 1)
	}

	@Subcommand("test item extractor")
	fun onTickItem(sender: Player) {
		val (_, location) = requireLookingAt(sender) { CacheType.ITEMS.get(it.chunk.ion()) }
		val chunk = IonChunk.getFromWorldCoordinates(sender.world, getX(location), getZ(location)) ?: fail { "Chunk not loaded" }
		val grid = CacheType.ITEMS.get(chunk) as ItemTransportCache
		if (grid.holder.getExtractorManager().isExtractorPresent(location)) fail { "Extractor not targeted" }

		NewTransport.runTask(location, sender.world) {
			grid.handleExtractorTick(this, location, (grid.holder.getExtractorManager().getExtractorData(location) as? ItemExtractorData)?.metaData)
		}
	}

	@Subcommand("test flood")
	fun onTestFloodFill(sender: Player, type: CacheType) {
		sender.information("Trying to find input nodes")
		val (node, location) = requireLookingAt(sender) { type.get(it.chunk.ion()) }
		val cache = type.get(sender.chunk.ion())

		NewTransport.runTask(location, sender.world) {
			val destinations = cache.getNetworkDestinations<PowerInputNode>(this, location, node, retainFullPath = true)
			sender.information("${destinations.size} destinations")
			sender.highlightBlocks(destinations.map { toVec3i(it.destinationPosition) }, 50L)
		}
	}

	@Subcommand("dump solars chunk")
	fun dumpSolarsChunk(sender: Player) {
		val ionChunk = sender.chunk.ion()
		val grid = CacheType.SOLAR_PANELS.get(ionChunk) as SolarPanelCache

		sender.information("Is ready: ${CacheType.SOLAR_PANELS.get(ionChunk).ready}")
		sender.information("${grid.combinedSolarPanelPositions.keys.size} covered position(s).")
		sender.information("${grid.combinedSolarPanels.size} unique panel(s).")

		grid.combinedSolarPanelPositions.forEach { (t, _) ->
			val vec = toVec3i(t)
			sender.highlightBlock(vec, 50L)
		}
	}

	@Subcommand("dump solars ship")
	fun dumpSolarsShip(sender: Player) {
		val ship = getStarshipRiding(sender)
		val grid = CacheType.SOLAR_PANELS.get(ship) as SolarPanelCache

		sender.information("Is ready: ${CacheType.SOLAR_PANELS.get(ship).ready}")
		sender.information("${grid.combinedSolarPanelPositions.keys.size} covered position(s).")
		sender.information("${grid.combinedSolarPanels.size} unique panel(s).")

		grid.combinedSolarPanelPositions.forEach { (localKey, _) ->
			val vec = ship.transportManager.getGlobalCoordinate(toVec3i(localKey))
			sender.highlightBlock(vec, 50L)
		}
	}

	@Subcommand("get solar look chunk")
	fun getSolarChunk(sender: Player) {
		val (node, location) = requireLookingAt(sender) { CacheType.SOLAR_PANELS.get(it.chunk.ion()) }
		sender.information("Targeted node: $node at ${toVec3i(location)}")
		val cache = CacheType.SOLAR_PANELS.get(sender.chunk.ion()) as SolarPanelCache
		sender.information("${cache.combinedSolarPanelPositions[location]}")
		sender.information("${cache.combinedSolarPanelPositions[location]?.getPositions()?.size} covered position(s).")
		cache.combinedSolarPanelPositions[location]?.getPositions()?.forEach {
			sender.highlightBlock(toVec3i(it), 50L)
		}
	}

	@Subcommand("get solar look ship")
	fun getSolarShip(sender: Player) {
		val ship = getStarshipRiding(sender)
		val (node, location) = requireLookingAt(sender) { CacheType.SOLAR_PANELS.get(ship) }
		sender.information("Targeted node: $node at ${toVec3i(location)}")
		val cache = CacheType.SOLAR_PANELS.get(ship) as SolarPanelCache
		sender.information("${cache.combinedSolarPanelPositions[location]}")
		sender.information("${cache.combinedSolarPanelPositions[location]?.getPositions()?.size} covered position(s).")
		cache.combinedSolarPanelPositions[location]?.getPositions()?.forEach {
			sender.highlightBlock(toVec3i(it), 50L)
		}
	}

	@Subcommand("get chunk grids")
	@CommandCompletion("FLUID|GRID_ENERGY")
	fun getChunkGrids(sender: Player, type: String, @Optional pageNumber: Int?) {
		val transportManager = sender.world.ion.transportManager
		val fluidManager = when (type.uppercase()) {
			"FLUID" -> transportManager.fluidGraphManager
			"GRID_ENERGY" -> transportManager.gridEnergyGraphManager
			else -> fail { "Unknown network type $type" }
		}

//		sender.information("Grid ID at ${toVec3i(key)}: ${fluidManager.getGraphAtLocation(key)?.uuid}")

		val grids = fluidManager.getByChunkKey(sender.chunk.chunkKey).toList()
		sender.information("Grids: ${grids.size}")

		fluidManager.allLocations().forEach { t -> sender.highlightBlock(toVec3i(t), 50L) }

		val menu = formatPaginatedMenu(
			grids,
			"/transportdebug get chunk grids",
			pageNumber ?: 1,
			10
		) { grid, _ ->

			val uuid = button(Component.text("show uuid")) { it.sendMessage(grid.uuid.toComponent()) }
			val highlight = button(Component.text("highlight")) { grid.getGraphNodes().forEach { t -> it.highlightBlock(toVec3i(t.location), 30L) } }

			listOf<Component>(
				Component.text("Grid $grid "),
				uuid,
				highlight,
				if (grid is FluidNetwork) grid.getVolume().toComponent() else Component.empty()
			).join(separator = Component.space())
		}

		sender.sendMessage(menu)
	}

	@Subcommand("network reset fluid")
	fun networkReset(sender: Player) {
		val transportManager = sender.world.ion.transportManager
		val fluidManager = transportManager.fluidGraphManager
		fluidManager.clear()
		sender.information("Reset world's fluid grids")
	}
}
