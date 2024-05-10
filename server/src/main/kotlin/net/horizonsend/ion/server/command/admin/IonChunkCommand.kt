package net.horizonsend.ion.server.command.admin

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.utils.text.formatPaginatedMenu
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlock
import net.horizonsend.ion.server.features.multiblock.util.getBlockSnapshotAsync
import net.horizonsend.ion.server.features.transport.network.ChunkPowerNetwork
import net.horizonsend.ion.server.features.world.IonChunk.Companion.ion
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player

@CommandAlias("ionchunk")
object IonChunkCommand : SLCommand() {
	@Subcommand("dumpEntities")
	fun onDumpEntities(sender: Player, @Optional visual: Boolean?, @Optional page: Int?) {
		val entities = sender.chunk.ion().multiblockManager.getAllMultiblockEntities().toList()

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
	fun dumpNodes(sender: Player, network: String) {
		val ionChunk = sender.chunk.ion()

		val grid = when (network) {
			"power" -> ionChunk.transportNetwork.powerNetwork
//			"item" -> ionChunk.transportNetwork.pipeGrid
//			"gas" -> ionChunk.transportNetwork.gasGrid
			 else -> fail { "invalid network" }
		}

		sender.information("${grid.nodes.size} covered position(s).")
		sender.information("${grid.nodes.values.distinct().size} unique node(s).")

		grid.nodes.forEach { (t, u) ->
			val vec = toVec3i(t)
			sender.highlightBlock(vec, 50L)
		}
	}

	@Subcommand("dump")
	@CommandCompletion("power") /* |item|gas") */
	fun dumpNetwork(sender: Player, network: String) {
		val ionChunk = sender.chunk.ion()

		val grid = when (network) {
			"power" -> ionChunk.transportNetwork.powerNetwork
//			"item" -> ionChunk.transportNetwork.pipeGrid
//			"gas" -> ionChunk.transportNetwork.gasGrid
			 else -> fail { "invalid network" }
		}

		sender.information("${grid.nodes.size} covered position(s).")
		sender.information("${grid.nodes.values.distinct().size} unique node(s).")
		sender.information("${grid.extractors.size} extractor node(s).")

		when (grid) {
			is ChunkPowerNetwork -> {
				sender.information("${grid.solarPanels.size} solar panels")
				sender.information("Node list: ${grid.nodes.values.groupBy { it.javaClass.simpleName }.mapValues { it.value.size }.entries.joinToString { it.toString() + "\n" }}")
			}
		}
	}

	@Subcommand("rebuild nodes")
	@CommandCompletion("power") /* |item|gas") */
	fun rebuildNodes(sender: Player, network: String) = CoroutineScope(Dispatchers.Default + Job()).launch {
		val ionChunk = sender.chunk.ion()

		val grid = when (network) {
			"power" -> ionChunk.transportNetwork.powerNetwork
//			"item" -> ionChunk.transportNetwork.pipeGrid
//			"gas" -> ionChunk.transportNetwork.gasGrid
			 else -> return@launch fail { "invalid network" }
		}

		grid.nodes.clear()
		grid.extractors.clear()

		for (x in ionChunk.originX ..ionChunk.originX + 15) {
			for (z in ionChunk.originZ..ionChunk.originZ + 15) {
				for (y in ionChunk.world.minHeight until ionChunk.world.maxHeight) {
					grid.createNodeFromBlock(getBlockSnapshotAsync(sender.world, x, y, z)!!)
				}
			}
		}
	}

	@Subcommand("get node key")
	fun getNode(sender: Player, key: Long, network: String) = CoroutineScope(Dispatchers.Default + Job()).launch {
		val ionChunk = sender.chunk.ion()

		val grid = when (network) {
			"power" -> ionChunk.transportNetwork.powerNetwork
//			"item" -> ionChunk.transportNetwork.pipeGrid
//			"gas" -> ionChunk.transportNetwork.gasGrid
			else -> return@launch fail { "invalid network" }
		}

		sender.information("Targeted node: ${grid.nodes[key]}")
	}

	@Subcommand("get node look")
	fun getNode(sender: Player, network: String) = CoroutineScope(Dispatchers.Default + Job()).launch {
		val ionChunk = sender.chunk.ion()
		val targeted = sender.getTargetBlock(null, 10)
		val key = toBlockKey(targeted.x, targeted.y, targeted.z)

		val grid = when (network) {
			"power" -> ionChunk.transportNetwork.powerNetwork
//			"item" -> ionChunk.transportNetwork.pipeGrid
//			"gas" -> ionChunk.transportNetwork.gasGrid
			else -> return@launch fail { "invalid network" }
		}

		sender.information("Targeted node: ${grid.nodes[key]}")
	}
}
