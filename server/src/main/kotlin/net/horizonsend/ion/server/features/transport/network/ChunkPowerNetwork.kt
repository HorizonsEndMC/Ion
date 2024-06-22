package net.horizonsend.ion.server.features.transport.network

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.util.BlockSnapshot
import net.horizonsend.ion.server.features.transport.ChunkTransportManager
import net.horizonsend.ion.server.features.transport.node.NetworkType
import net.horizonsend.ion.server.features.transport.node.getNeighborNodes
import net.horizonsend.ion.server.features.transport.node.power.PowerExtractorNode
import net.horizonsend.ion.server.features.transport.node.power.PowerInputNode
import net.horizonsend.ion.server.features.transport.node.power.PowerNodeFactory
import net.horizonsend.ion.server.features.transport.node.power.SolarPanelNode
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import org.bukkit.NamespacedKey
import org.bukkit.block.BlockFace

class ChunkPowerNetwork(manager: ChunkTransportManager) : ChunkTransportNetwork(manager) {
	override val type: NetworkType = NetworkType.POWER
	override val namespacedKey: NamespacedKey = NamespacedKeys.POWER_TRANSPORT
	override val nodeFactory: PowerNodeFactory = PowerNodeFactory(this)

	/** Store solar panels for ticking */
	val solarPanels: ObjectOpenHashSet<SolarPanelNode> = ObjectOpenHashSet()

	override val dataVersion: Int = 0 //TODO 1

	override fun setup() {

	}

	override fun processBlockRemoval(key: Long) { manager.scope.launch {
		val previousNode = nodes[key] ?: return@launch

		extractors.remove(key)

		previousNode.handleRemoval(key)
	}}

	override fun processBlockAddition(key: Long, new: BlockSnapshot) { manager.scope.launch {
		createNodeFromBlock(new)
	}}

	private suspend fun tickSolars() {
		for (solarPanel in solarPanels) {
			runCatching { solarPanel.startStep()?.invoke() }.onFailure {
				IonServer.slF4JLogger.error("Exception ticking solar panel! $it")
				it.printStackTrace()
			}
		}
	}

	private suspend fun tickExtractors() = extractors.forEach { (key, extractor) ->
		runCatching { extractor.startStep()?.invoke() }.onFailure {
			IonServer.slF4JLogger.error("Exception ticking extractor at ${toVec3i(key)}! $it")
			it.printStackTrace()
		}
	}

	override suspend fun tick() {
		tickSolars()
		tickExtractors()
	}

	override suspend fun clearData() {
		nodes.clear()
		solarPanels.clear()
		extractors.clear()
	}

	/**
	 * Handle the addition of a new powered multiblock entity
	 **/
	suspend fun handleNewPoweredMultiblock(new: MultiblockEntity) {
		// All directions
		val neighboring = getNeighborNodes(new.position, nodes, BlockFace.entries)
			.filterValues { it is PowerInputNode || it is PowerExtractorNode }

		neighboring.forEach {
			it.value.buildRelations(getRelative(new.locationKey, it.key))
		}
	}

	companion object {
		const val POWER_EXTRACTOR_STEP = 1000
	}
}
