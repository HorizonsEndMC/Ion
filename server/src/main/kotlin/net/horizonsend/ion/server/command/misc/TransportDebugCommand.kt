package net.horizonsend.ion.server.command.misc

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import io.papermc.paper.util.StacktraceDeobfuscator
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.command.admin.IonChunkCommand
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlock
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlocks
import net.horizonsend.ion.server.features.transport.nodes.cache.TransportCache
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.features.transport.nodes.types.PowerNode.PowerInputNode
import net.horizonsend.ion.server.features.transport.old.TransportConfig
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
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.slf4j.Logger
import java.lang.management.ManagementFactory
import java.lang.management.ThreadInfo
import java.util.concurrent.LinkedBlockingDeque
import kotlin.math.roundToInt
import kotlin.system.measureNanoTime

@CommandPermission("starlegacy.transportdebug")
@CommandAlias("transportdebug|transportbug")
object TransportDebugCommand : SLCommand() {
	@Suppress("Unused")
	@Subcommand("reload")
	fun reload(sender: CommandSender) {
		TransportConfig.reload()
		sender.success("Reloaded config")
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
		val loc = Vec3i(sender.location)
		val inputs = inputManager.getLocations(type)
			.map { toVec3i(it) }
			.filter { it.distance(loc) < 100.0 }

		sender.highlightBlocks(inputs, 50L)
		sender.information("${inputs.size} inputs")
	}

	@Subcommand("dump nodes chunk")
	@CommandCompletion("power") /* |item|gas") */
	fun dumpNodesChunk(sender: Player, network: CacheType) {
		val ionChunk = sender.chunk.ion()
		val grid = network.get(ionChunk)

		sender.information("${grid.getRawCache().size} covered position(s).")
		sender.information("${grid.getRawCache().values.distinct().size} unique node(s).")

		grid.getRawCache().forEach { (t, _) ->
			val vec = toVec3i(t)
			sender.highlightBlock(vec, 50L)
		}
	}

	@Subcommand("dump nodes ship")
	@CommandCompletion("power") /* |item|gas") */
	fun dumpNodesShip(sender: Player, network: CacheType) {
		val ship = getStarshipRiding(sender)
		val grid = network.get(ship)

		sender.information("${grid.getRawCache().size} covered position(s).")
		sender.information("${grid.getRawCache().values.distinct().size} unique node(s).")

		grid.getRawCache().forEach { (t, _) ->
			val vec = toVec3i(t)
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
			sender.highlightBlock(toVec3i(extractor.pos), 50L)
		}
	}

	private fun requireLookingAt(sender: Player, network: (Block) -> TransportCache): Pair<Node, BlockKey> {
		val targeted = sender.getTargetBlock(null, 10)
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

	@Subcommand("test extractor test")
	fun onTick(sender: Player, type: CacheType) {
		val (node, location) = requireLookingAt(sender) { type.get(it.chunk.ion()) }
		val chunk = IonChunk.getFromWorldCoordinates(sender.world, getX(location), getZ(location)) ?: fail { "Chunk not loaded" }
		val grid = type.get(chunk)
		if (grid.holder.getExtractorManager().isExtractor(location)) IonChunkCommand.fail { "Extractor not targeted" }

		grid.tickExtractor(location, 1.0)
	}

	@Subcommand("test flood test")
	fun onTestFloodFill(sender: Player, type: CacheType) {
		sender.information("Trying to find input nodes")
		val (_, location) = requireLookingAt(sender) { type.get(it.chunk.ion()) }
		val cache = type.get(sender.chunk.ion())
		val destinations = cache.getNetworkDestinations<PowerInputNode>(location) { true }
		sender.information("${destinations.size} destinations")
		sender.highlightBlocks(destinations.map(::toVec3i), 50L)
	}

	const val COLLECT_TRANSPORT_METRICS = true

	val floodFillTimes = LinkedBlockingDeque<Long>(10_000)
	val solarFloodFillTimes = LinkedBlockingDeque<Long>(10_000)
	val pathfindTimes = LinkedBlockingDeque<Long>(10_000)
	val runTransferTimes = LinkedBlockingDeque<Long>(10_000)
	val extractorTickTimes = LinkedBlockingDeque<Long>(10_000)
	val solarTickTimes = LinkedBlockingDeque<Long>(10_000)

	fun <T> measureOrFallback(metric: LinkedBlockingDeque<Long>, block: () -> T): T {
		if (!COLLECT_TRANSPORT_METRICS) return block()

		if (metric.remainingCapacity() == 0) metric.removeFirst()

		var result: T

		metric.addLast(measureNanoTime {
			result = block()
		})

		return result
	}

	@Subcommand("metrics")
	fun getMetrics(sender: Player) {
		if (!COLLECT_TRANSPORT_METRICS) fail { "Transport metrics are not enabled" }

		sender.sendMessage("Flood fill average: ${floodFillTimes.average().takeIf { d -> d.isFinite() }?.roundToInt()} ns")
		sender.sendMessage("Pathfind average: ${pathfindTimes.average().takeIf { d -> d.isFinite() }?.roundToInt()} ns")
		sender.sendMessage("Transfer average: ${runTransferTimes.average().takeIf { d -> d.isFinite() }?.roundToInt()} ns")
		sender.sendMessage("Extractor average: ${extractorTickTimes.average().takeIf { d -> d.isFinite() }?.roundToInt()} ns")
		sender.sendMessage("Solar panel average: ${solarTickTimes.average().takeIf { d -> d.isFinite() }?.roundToInt()} ns")
		sender.sendMessage("Solar flood average: ${solarFloodFillTimes.average().takeIf { d -> d.isFinite() }?.roundToInt()} ns")
	}
}
