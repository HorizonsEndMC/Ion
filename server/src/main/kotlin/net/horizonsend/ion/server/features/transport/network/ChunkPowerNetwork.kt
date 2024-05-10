package net.horizonsend.ion.server.features.transport.network

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.util.BlockSnapshot
import net.horizonsend.ion.server.features.transport.ChunkTransportManager
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.getNeighborNodes
import net.horizonsend.ion.server.features.transport.node.power.PowerExtractorNode
import net.horizonsend.ion.server.features.transport.node.power.PowerInputNode
import net.horizonsend.ion.server.features.transport.node.power.PowerNodeFactory
import net.horizonsend.ion.server.features.transport.node.power.SolarPanelNode
import net.horizonsend.ion.server.features.transport.step.PowerOriginStep
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.filterValuesIsInstance
import org.bukkit.NamespacedKey
import org.bukkit.block.BlockFace
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.min

class ChunkPowerNetwork(manager: ChunkTransportManager) : ChunkTransportNetwork(manager) {
	val poweredMultiblockEntities = ConcurrentHashMap<Long, PoweredMultiblockEntity>()

	override val namespacedKey: NamespacedKey = NamespacedKeys.POWER_TRANSPORT
	override val nodeFactory: PowerNodeFactory = PowerNodeFactory(this)

	/** Store solar panels for ticking */
	val solarPanels: ObjectOpenHashSet<SolarPanelNode> = ObjectOpenHashSet()

	override fun setup() {
		collectPowerMultiblockEntities()
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
		for (solarPanel in nodes.filterValuesIsInstance<SolarPanelNode, BlockKey, TransportNode>().values.distinct()) {
			val power = solarPanel.tickAndGetPower()

			if (power <= 0) continue

			runCatching { solarPanel.handleStep(PowerOriginStep(AtomicInteger(), solarPanel, power)) }.onFailure {
				IonServer.slF4JLogger.error("Exception ticking solar panel! $it")
				it.printStackTrace()
			}
		}
	}

	private suspend fun tickExtractors() {
		for ((_, extractor) in extractors) {
			if (!extractor.useful) continue

			val extractablePowerPool = extractor.extractableNodes.flatMap { it.multis }
			val sum = extractablePowerPool.sumOf { it.getPower() }
			val extractablePower = min(sum, POWER_EXTRACTOR_STEP)

			runCatching { extractor.handleStep(PowerOriginStep(AtomicInteger(), extractor, extractablePower)) }.onFailure {
				it.printStackTrace()
			}
		}
	}

	override suspend fun tick() {
		tickSolars()
		tickExtractors()
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

	private fun collectPowerMultiblockEntities() {
		manager.chunk.multiblockManager.getAllMultiblockEntities().forEach { (key, entity) ->
			if (entity !is PoweredMultiblockEntity) return@forEach

			poweredMultiblockEntities[key] = entity
		}
	}


	companion object {
		const val POWER_EXTRACTOR_STEP = 1000
	}
}
