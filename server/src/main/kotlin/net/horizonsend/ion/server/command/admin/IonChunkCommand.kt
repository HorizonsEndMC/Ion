package net.horizonsend.ion.server.command.admin

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.utils.text.formatPaginatedMenu
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlock
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlocks
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.features.transport.nodes.types.PowerNode.PowerInputNode
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.features.world.chunk.IonChunk.Companion.ion
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
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
	fun dumpNodes(sender: Player, network: CacheType) {
		val ionChunk = sender.chunk.ion()
		val grid = network.get(ionChunk)

		sender.information("${grid.getRawCache().size} covered position(s).")
		sender.information("${grid.getRawCache().values.distinct().size} unique node(s).")

		grid.getRawCache().forEach { (t, _) ->
			val vec = toVec3i(t)
			sender.highlightBlock(vec, 50L)
		}
	}

	@Subcommand("dump extractors")
	fun dumpExtractors(sender: Player, network: CacheType) {
		val ionChunk = sender.chunk.ion()
		val extractors = network.get(ionChunk).holder.getExtractorManager()

		sender.information("${extractors.getExtractors().size} covered position(s).")

		extractors.getExtractors().forEach { extractor ->
			sender.highlightBlock(toVec3i(extractor.pos), 50L)
		}
	}

	@Subcommand("dump")
	@CommandCompletion("power")
	fun dumpNetwork(sender: Player, network: CacheType) {
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
	fun getNode(sender: Player, key: Long, network: CacheType) {
		val ionChunk = sender.chunk.ion()
		val grid = network.get(ionChunk)

		sender.information("Targeted node: ${grid.getRawCache()[key]}")
	}

	@Subcommand("get node look")
	fun getNode(sender: Player, network: CacheType) {
		val (node, location) = requireLookingAt(sender, network)
		sender.information("Targeted node: $node at ${toVec3i(location)}")
	}

	@Subcommand("test extractor")
	fun onTick(sender: Player, type: CacheType) {
		val (node, location) = requireLookingAt(sender, type)
		val chunk = IonChunk.getFromWorldCoordinates(sender.world, getX(location), getZ(location)) ?: fail { "Chunk not loaded" }
		val grid = type.get(chunk)
		if (grid.holder.getExtractorManager().isExtractor(location)) fail { "Extractor not targeted" }

		grid.tickExtractor(location, 1.0)
	}

	@Subcommand("test flood")
	fun onTestFloodFill(sender: Player, network: CacheType) {
		sender.information("Trying to find input nodes")
		val (_, location) = requireLookingAt(sender, network)
		val cache = network.get(sender.chunk.ion())
		val destinations = cache.getNetworkDestinations<PowerInputNode>(location) { true }
		sender.information("${destinations.size} destinations")
		sender.highlightBlocks(destinations.map(::toVec3i), 50L)
	}

	private fun requireLookingAt(sender: Player, network: CacheType): Pair<Node, BlockKey> {
		val targeted = sender.getTargetBlock(null, 10)
		val ionChunk = targeted.chunk.ion()
		val grid = network.get(ionChunk)
		val key = toBlockKey(targeted.x, targeted.y, targeted.z)

		val node = grid.getOrCache(key) ?: fail { "You aren't looking at a node!" }
		return node to key
	}
}
