package net.horizonsend.ion.server.command.admin

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.utils.text.formatPaginatedMenu
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlock
import net.horizonsend.ion.server.features.transport.nodes.cache.PowerTransportCache
import net.horizonsend.ion.server.features.transport.util.NetworkType
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.chunk.IonChunk.Companion.ion
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player

@CommandAlias("ionchunk")
object IonChunkCommand : SLCommand() {
	@Subcommand("dumpEntities")
	fun onDumpEntities(sender: Player, @Optional visual: Boolean?, @Optional page: Int?) {
		val manager = sender.chunk.ion().multiblockManager
		val entities = manager.getAllMultiblockEntities().toList()

		sender.sendMessage(formatPaginatedMenu(
			entities.size,
			"/ionchunk dumpentities ${visual ?: false}",
			page ?: 1,
		) { index ->
			val (key, entity) = entities[index]

			val vec = toVec3i(key)

			text("$vec : $entity")
		})

		if (visual == true) {
			for ((key, _) in entities) {
				val vec = toVec3i(key)

				sender.highlightBlock(vec, 30L)
			}
		}

		sender.information("Sync Ticked: ${manager.syncTickingMultiblockEntities}")
		sender.information("Async Ticked: ${manager.asyncTickingMultiblockEntities}")
	}

	@Subcommand("remove all")
	fun onRemoveAll(sender: Player) {
		val ionChunk = sender.chunk.ion()
		val entities = ionChunk.multiblockManager.getAllMultiblockEntities()

		for ((key, _) in entities) {
			val (x, y, z) = toVec3i(key)

			ionChunk.multiblockManager.removeMultiblockEntity(x, y, z)
		}
	}

	@Subcommand("dump nodes")
	@CommandCompletion("power") /* |item|gas") */
	fun dumpNodes(sender: Player, network: NetworkType) {
		val ionChunk = sender.chunk.ion()
		val grid = network.get(ionChunk)

		sender.information("${grid.getRawCache().size} covered position(s).")
		sender.information("${grid.getRawCache().values.distinct().size} unique node(s).")

		grid.getRawCache().forEach { (t, u) ->
			val vec = toVec3i(t)
			sender.highlightBlock(vec, 50L)
		}
	}

	@Subcommand("dump extractors")
	@CommandCompletion("power") /* |item|gas") */
	fun dumpExtractors(sender: Player, network: NetworkType) {
		val ionChunk = sender.chunk.ion()
		val extractors = network.get(ionChunk).holder.getExtractorManager()

		sender.information("${extractors.getExtractors().size} covered position(s).")

		extractors.getExtractors().forEach { pos ->
			sender.highlightBlock(toVec3i(pos), 50L)
		}
	}

	@Subcommand("dump")
	@CommandCompletion("power")
	fun dumpNetwork(sender: Player, network: NetworkType) {
		val ionChunk = sender.chunk.ion()
		val grid = network.get(ionChunk)

		sender.information("${grid.getRawCache().size} covered position(s).")
		sender.information("${grid.getRawCache().values.distinct().size} unique node(s).")

		when (grid) {
//			is PowerNodeManager -> {
//				sender.information("${grid.solarPanels.size} solar panels")
//				sender.information("${grid.extractors.size} extractors")
//				sender.information("Node list: ${grid.nodes.values.groupBy { it.javaClass.simpleName }.mapValues { it.value.size }.entries.joinToString { it.toString() + "\n" }}")
//			}
		}
	}

	@Subcommand("dumpchunk")
	fun dumpChunk(sender: Player) {
		val ionChunk = sender.chunk.ion()

		sender.information("Chunk: $ionChunk")
		sender.information("World has: ${ionChunk.world.ion.regionPositions.values.distinct().size} unique regions")
	}

	@Subcommand("get node key")
	fun getNode(sender: Player, key: Long, network: NetworkType) {
		val ionChunk = sender.chunk.ion()
		val grid = network.get(ionChunk)

		sender.information("Targeted node: ${grid.getRawCache()[key]}")
	}

	@Subcommand("get node look")
	fun getNode(sender: Player, network: NetworkType) {
		val targeted = sender.getTargetBlock(null, 10)
		val ionChunk = targeted.chunk.ion()
		val grid = network.get(ionChunk)
		val key = toBlockKey(targeted.x, targeted.y, targeted.z)

		sender.information("Targeted node: ${grid.getRawCache()[key]}")
	}

	@Subcommand("test extractor")
	fun onTick(sender: Player) {
		val targeted = sender.getTargetBlock(null, 10)
		val ionChunk = targeted.chunk.ion()
		val grid = NetworkType.POWER.get(ionChunk) as PowerTransportCache
		val key = toBlockKey(targeted.x, targeted.y, targeted.z)

		val node = grid.getCached(key)
		if (node !is PowerTransportCache.PowerNode.PowerExtractorNode) return
		grid.tickExtractor(node)
	}

	@Subcommand("test pathfinding")
	fun onTestPathfinding(sender: Player) {
		val targeted = sender.getTargetBlock(null, 10)
		val ionChunk = targeted.chunk.ion()
//		val grid = NetworkType.POWER.get(ionChunk) as PowerNodeManager
//		val key = toBlockKey(targeted.x, targeted.y, targeted.z)
//
//		val at = grid.nodes[key] ?: return sender.userError("No node at $targeted")
//		val transferable = at.getTransferableNodes()

//		fun getNext(node: TransportNode): Collection<TransportNode> {
//			at as PowerPathfindingNode
//			return at.getNextNodes(node, null)
//		}
//
//		sender.information("Transferable nodes from ${at.javaClass.simpleName}")
//		sender.information(transferable.joinToString(separator = "\n") { next ->
//			"${next.javaClass.simpleName} -> [${getNext(next).joinToString { it.javaClass.simpleName }}]"
//		})
	}

	@Subcommand("test flood")
	fun onTestFloodFill(sender: Player) {
//		val targeted = sender.getTargetBlock(null, 10)
//		val ionChunk = targeted.chunk.ion()
//		val grid = NetworkType.POWER.get(ionChunk) as PowerNodeManager
//		val key = toBlockKey(targeted.x, targeted.y, targeted.z)
//
//		val at = grid.nodes[key] ?: return sender.userError("No node at $targeted")
//
//			val visitQueue = ArrayDeque<TransportNode>()
//			val visitedSet = ObjectOpenHashSet<TransportNode>()
//			val destinations = ObjectOpenHashSet<PowerInputNode>()
//
//			visitQueue.addAll(at.cachedTransferable)
//			var iterations = 0L
//
//			while (visitQueue.isNotEmpty()) {
//				iterations++
//				val currentNode = visitQueue.removeFirst()
//				Tasks.syncDelay(iterations) { sender.highlightBlock(currentNode.getCenter(), 5L) }
//				visitedSet.add(currentNode)
//
//				if (currentNode is PowerInputNode) {
//					destinations.add(currentNode)
//				}
//
//				visitQueue.addAll(currentNode.cachedTransferable.filterNot { visitedSet.contains(it) })
//			}
	}
}
