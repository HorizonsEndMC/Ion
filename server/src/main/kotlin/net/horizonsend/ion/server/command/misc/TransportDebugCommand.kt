package net.horizonsend.ion.server.command.misc

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import io.papermc.paper.util.StacktraceDeobfuscator
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.utils.text.formatPaginatedMenu
import net.horizonsend.ion.common.utils.text.toComponent
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlock
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlocks
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ItemExtractorData
import net.horizonsend.ion.server.features.transport.nodes.cache.CacheState
import net.horizonsend.ion.server.features.transport.nodes.cache.ItemTransportCache
import net.horizonsend.ion.server.features.transport.nodes.cache.TransportCache
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.features.transport.nodes.types.PowerNode.PowerInputNode
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
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.slf4j.Logger
import java.lang.management.ManagementFactory
import java.lang.management.ThreadInfo

@CommandPermission("starlegacy.transportdebug")
@CommandAlias("transportdebug|transportbug")
object TransportDebugCommand : SLCommand() {
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
	fun dumpInputsChunk(sender: Player, type: CacheType) {
		val inputManager = sender.world.ion.inputManager
		val loc = Vec3i(sender.location)
		val inputs = inputManager.getLocations(type)
			.map { toVec3i(it) }
			.filter { it.distance(loc) < 100.0 }

		sender.highlightBlocks(inputs, 50L)
		sender.information("${inputs.size} inputs")
	}

	@Subcommand("dump inputs starship")
	fun dumpInputsShip(sender: Player, type: CacheType) {
		val ship = getStarshipRiding(sender)
		val inputManager = ship.transportManager.inputManager

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

	@Subcommand("get node look ship")
	fun getNodeShip(sender: Player, network: CacheType) {
		val (node, location) = requireLookingAt(sender) { network.get(getStarshipRiding(sender)) }
		sender.information("Targeted node: $node at ${toVec3i(location)}")
	}


	@Subcommand("get cached destinations chunk")
	fun getCachedDestinationsChunk(sender: Player, network: CacheType, @Optional pageNumber: Int?) {
		var cacheHolder: TransportCache? = null
		val (node, location) = requireLookingAt(sender) { network.get(it.chunk.ion()).apply { cacheHolder = this } }
		sender.information("Targeted node: $node at ${toVec3i(location)}")

		val cache = cacheHolder?.destinationCache ?: fail { "Something went wrong" }
		val destinations = cache.rawCache

		for (key in destinations.keys) {
			val paths = cache.getCache(key)[location] ?: continue
			val vectors = paths.destinations.map { toVec3i(it) }
			sender.sendMessage(formatPaginatedMenu(vectors, "/get cached destinations chunk", pageNumber ?: 1) { vec, _ -> vec.toComponent() })
		}
	}

	@Subcommand("get cached destinations ship")
	fun getCachedDestinationsShip(sender: Player, network: CacheType, @Optional pageNumber: Int?) {
		var cacheHolder: TransportCache? = null
		val (node, location) = requireLookingAt(sender) { network.get(getStarshipRiding(sender)).apply { cacheHolder = this } }
		sender.information("Targeted node: $node at ${toVec3i(location)}")

		val cache = cacheHolder?.destinationCache ?: fail { "Something went wrong" }
		val destinations = cache.rawCache

		for (key in destinations.keys) {
			val paths = cache.getCache(key)[location] ?: continue
			val vectors = paths.destinations.map { toVec3i(it) }
			sender.sendMessage(formatPaginatedMenu(vectors, "/get cached destinations ship", pageNumber ?: 1) { vec, _ -> vec.toComponent() })
		}
	}

	@Subcommand("get cached paths chunk")
	fun getCachedPathsChunk(sender: Player, network: CacheType) {
		var cacheHolder: TransportCache? = null
		val (node, location) = requireLookingAt(sender) { network.get(it.chunk.ion()).apply { cacheHolder = this } }
		sender.information("Targeted node: $node at ${toVec3i(location)}")
		if (cacheHolder == null) fail { "Something went wrong" }
		sender.information("Contains paths: ${cacheHolder?.pathCache?.containsOriginPoint(location)}")
	}

	@Subcommand("get cached paths ship")
	fun getCachedPathsShip(sender: Player, network: CacheType) {
		var cacheHolder: TransportCache? = null
		val (node, location) = requireLookingAt(sender) { network.get(getStarshipRiding(sender)).apply { cacheHolder = this } }
		sender.information("Targeted node: $node at ${toVec3i(location)}")
		if (cacheHolder == null) fail { "Something went wrong" }
		sender.information("Contains paths: ${cacheHolder?.pathCache?.containsOriginPoint(location)}")
	}

	@Subcommand("test extractor")
	fun onTick(sender: Player, type: CacheType) {
		val (_, location) = requireLookingAt(sender) { type.get(it.chunk.ion()) }
		val chunk = IonChunk.getFromWorldCoordinates(sender.world, getX(location), getZ(location)) ?: fail { "Chunk not loaded" }
		val grid = type.get(chunk)
//		if (grid.holder.getExtractorManager().isExtractorPresent(location)) fail { "Extractor not targeted" }

		grid.tickExtractor(location, 1.0, null)
	}

	@Subcommand("test item extractor")
	fun onTickItem(sender: Player) {
		val (_, location) = requireLookingAt(sender) { CacheType.ITEMS.get(it.chunk.ion()) }
		val chunk = IonChunk.getFromWorldCoordinates(sender.world, getX(location), getZ(location)) ?: fail { "Chunk not loaded" }
		val grid = CacheType.ITEMS.get(chunk) as ItemTransportCache
		if (grid.holder.getExtractorManager().isExtractorPresent(location)) fail { "Extractor not targeted" }

		grid.handleExtractorTick(location, (grid.holder.getExtractorManager().getExtractorData(location) as? ItemExtractorData)?.metaData)
	}

	@Subcommand("test flood")
	fun onTestFloodFill(sender: Player, type: CacheType) {
		sender.information("Trying to find input nodes")
		val (node, location) = requireLookingAt(sender) { type.get(it.chunk.ion()) }
		val cache = type.get(sender.chunk.ion())

		val destinations = cache.getNetworkDestinations<PowerInputNode>(location, node) { true }
		sender.information("${destinations.size} destinations")
		sender.highlightBlocks(destinations.map(::toVec3i), 50L)
	}
}
