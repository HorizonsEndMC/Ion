package net.horizonsend.ion.server.features.transport.node.power

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.multiblock.util.getBlockSnapshotAsync
import net.horizonsend.ion.server.features.transport.network.ChunkPowerNetwork
import net.horizonsend.ion.server.features.transport.node.NodeRelationship
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.type.MultiNode
import net.horizonsend.ion.server.features.transport.node.type.SourceNode
import net.horizonsend.ion.server.features.transport.step.PowerOriginStep
import net.horizonsend.ion.server.features.transport.step.Step
import net.horizonsend.ion.server.features.transport.step.TransportStep
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.NODE_COVERED_POSITIONS
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.SOLAR_CELL_EXTRACTORS
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import org.bukkit.GameRule
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType.LONG_ARRAY
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.sin

/**
 * Represents a solar panel, or multiple
 **/
class SolarPanelNode(override val network: ChunkPowerNetwork) : MultiNode<SolarPanelNode, SolarPanelNode>, SourceNode {
	override val positions: MutableSet<BlockKey> = LongOpenHashSet()
	/** The positions of extractors in this solar panel */
	val extractorPositions = LongOpenHashSet()

	/** The number of solar cells contained in this node */
	val cellNumber: Int get() = extractorPositions.size

	override val relationships: MutableSet<NodeRelationship> = ObjectOpenHashSet()

	override fun isTransferableTo(node: TransportNode): Boolean {
		// Solar panels should be able to transfer through extractors and other solar panels
		return node !is PowerExtractorNode
	}

	override fun storeData(persistentDataContainer: PersistentDataContainer) {
		persistentDataContainer.set(NODE_COVERED_POSITIONS, LONG_ARRAY, positions.toLongArray())
		persistentDataContainer.set(SOLAR_CELL_EXTRACTORS, LONG_ARRAY, extractorPositions.toLongArray())
	}

	override fun loadData(persistentDataContainer: PersistentDataContainer) {
		val coveredPositions = persistentDataContainer.get(NODE_COVERED_POSITIONS, LONG_ARRAY)
		coveredPositions?.let { positions.addAll(it.asIterable()) }

		val extractors = persistentDataContainer.get(SOLAR_CELL_EXTRACTORS, LONG_ARRAY)
		extractors?.let { extractorPositions.addAll(it.asIterable()) }
	}

	override suspend fun handleRemoval(position: BlockKey) {
		// Need to handle the extractor positions manually
		when {
			// Removed extractor, easier to find
			extractorPositions.contains(position) -> removePosition(
				position,
				listOf(getRelative(position, BlockFace.UP, 1), getRelative(position, BlockFace.UP, 2))
			)

			// Need to find extractor, search downward form position
			else -> {
				val extractorPosition: BlockKey = (0..2).firstNotNullOf { y ->
					getRelative(position, BlockFace.DOWN, y).takeIf { extractorPositions.contains(it) }
				}

				removePosition(
					extractorPosition,
					listOf(getRelative(extractorPosition, BlockFace.UP, 1), getRelative(extractorPosition, BlockFace.UP, 2))
				)
			}
		}

		rebuildNode(position)
	}

	suspend fun addPosition(extractorKey: BlockKey, others: Iterable<BlockKey>): SolarPanelNode {
		extractorPositions += extractorKey

		// Make sure there isn't still an extractor
		network.extractors.remove(extractorKey)
		addPosition(extractorKey)

		positions += others
		for (position: BlockKey in positions) {
			network.nodes[position] = this
		}

		return this
	}

	fun removePosition(extractorKey: BlockKey, others: Iterable<BlockKey>) {
		extractorPositions -= extractorKey
		network.nodes.remove(extractorKey)
		positions.remove(extractorKey)

		positions += others
		for (position: BlockKey in positions) {
			network.nodes.remove(position)
		}
	}

	override suspend fun rebuildNode(position: BlockKey) {
		network.solarPanels.remove(this)

		// Create new nodes, automatically merging together
		positions.forEach {
			network.nodeFactory.addSolarPanel(it, handleRelationships = false)
		}

		// Handle relations once fully rebuilt
		positions.forEach {
			network.nodes[it]?.buildRelations(it)
		}
	}

	override suspend fun drainTo(new: SolarPanelNode) {
		super.drainTo(new)

		new.extractorPositions.addAll(extractorPositions)
	}

	var lastTicked: Long = System.currentTimeMillis()

	/**
	 * Returns the amount of power between ticks
	 **/
	fun getPower(): Int {
		val daylightMultiplier: Double = if (
			network.world.environment == World.Environment.NORMAL &&
			network.world.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE) == true
		) {
			val daylight = sin((network.world.time / (12000.0 / PI)) - (PI / 2))
			max(0.0, daylight) * 1.5 // 1.5 to bring area under curve to around equal with night
		} else 0.5

		val time = System.currentTimeMillis()
		val diff = time - this.lastTicked

		return ((diff / 1000.0) * POWER_PER_SECOND * cellNumber * daylightMultiplier).toInt()
	}

	fun tickAndGetPower(): Int {
		val power = getPower()
		lastTicked = System.currentTimeMillis()

		return power
	}

	/**
	 * Returns whether the individual solar panel from the extractor location is intact
	 **/
	suspend fun isIntact(world: World, extractorKey: BlockKey): Boolean {
		return matchesSolarPanelStructure(world, extractorKey)
	}

	override suspend fun handleStep(step: Step) {
		when (step) {
			is TransportStep -> {
				val neighbors = getTransferableNodes()
				val next = neighbors.shuffled().firstOrNull { it !is SolarPanelNode } ?: neighbors.randomOrNull() ?: return

				// Simply move on to the next node
				TransportStep(
					step.origin,
					step.steps,
					next,
					step,
					step.traversedNodes
				).invoke()
			}

			is PowerOriginStep -> {
				val neighbors = getTransferableNodes()
				val next = neighbors.shuffled().firstOrNull { it !is SolarPanelNode } ?: neighbors.randomOrNull() ?: return

				// Simply move on to the next node
				TransportStep(step, step.steps, next, step, step.traversedNodes).invoke()
			}

			else -> throw NotImplementedError("Unrecognized step type $step")
		}
	}

	override fun loadIntoNetwork() {
		super.loadIntoNetwork()
		network.solarPanels += this
	}

	companion object {
		const val POWER_PER_SECOND = 5

		suspend fun matchesSolarPanelStructure(world: World, key: BlockKey): Boolean {
			if (getBlockSnapshotAsync(world, key)?.type != Material.CRAFTING_TABLE) return false
			val diamond = getRelative(key, BlockFace.UP)

			if (getBlockSnapshotAsync(world, diamond)?.type != Material.DIAMOND_BLOCK) return false
			val cell = getRelative(diamond, BlockFace.UP)

			return getBlockSnapshotAsync(world, cell)?.type == Material.DAYLIGHT_DETECTOR
		}
	}

	override fun toString(): String = "(SOLAR PANEL NODE: Transferable to: ${getTransferableNodes().joinToString { it.javaClass.simpleName }} nodes"

}
