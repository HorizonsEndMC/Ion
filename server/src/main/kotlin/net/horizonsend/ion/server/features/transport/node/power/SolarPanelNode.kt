package net.horizonsend.ion.server.features.transport.node.power

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.multiblock.util.getBlockSnapshotAsync
import net.horizonsend.ion.server.features.transport.network.ChunkPowerNetwork
import net.horizonsend.ion.server.features.transport.node.NodeRelationship
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.type.MultiNode
import net.horizonsend.ion.server.features.transport.node.type.SourceNode
import net.horizonsend.ion.server.features.transport.step.PowerTransportStep
import net.horizonsend.ion.server.features.transport.step.SolarPowerOriginStep
import net.horizonsend.ion.server.features.transport.step.Step
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
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.sin

/**
 * Represents a solar panel, or multiple
 **/
class SolarPanelNode(override val network: ChunkPowerNetwork) : MultiNode<SolarPanelNode, SolarPanelNode>, SourceNode {
	override val positions: MutableSet<BlockKey> = LongOpenHashSet()
	override val relationships: MutableSet<NodeRelationship> = ObjectOpenHashSet()

	/** The positions of extractors in this solar panel */
	private val extractorPositions = LongOpenHashSet()

	/** The number of solar cells contained in this node */
	private val cellNumber: Int get() = extractorPositions.size

	/**
	 * The distance this solar node is from the nearest exit of the solar field
	 * This value will be -1 if there are no exits present
	 **/
	private var exitDistance: Int = 0

	private var lastTicked: Long = System.currentTimeMillis()

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

	/**
	 * Get the power and reset the last ticked time
	 **/
	fun tickAndGetPower(): Int {
		val power = getPower()

		lastTicked = System.currentTimeMillis()

		return power
	}

	/**
	 * Calculate the distance to an exit of a solar field
	 * This method is run upon neighbor unloads
	 **/
	private fun calculateExitDistance() {
		val neighbors = getTransferableNodes()

		// Borders an exit
		if (neighbors.any { it !is SolarPanelNode }) {
			exitDistance = 0
			return
		}

		val solars = neighbors.filterIsInstance<SolarPanelNode>()
		if (solars.isEmpty()) {
			exitDistance = -1
			return
		}

		exitDistance = solars.minOf { it.exitDistance } + 1
	}

	/**
	 * Execute the provided consumer across every interconnected solar node
	 *
	 * WARNING: use this carefully
	 **/
	private fun traverseField(visited: MutableList<SolarPanelNode> = mutableListOf(), consumer: Consumer<SolarPanelNode>) {
		if (visited.contains(this)) return

		consumer.accept(this)

		getTransferableNodes().filterIsInstance<SolarPanelNode>().filterNot { visited.contains(it) }.forEach(consumer)
	}

	/**
	 * Returns whether the individual solar panel from the extractor location is intact
	 **/
	suspend fun isIntact(world: World, extractorKey: BlockKey): Boolean {
		return matchesSolarPanelStructure(world, extractorKey)
	}

	// If relations have changed, update the exit distances of the whole field
	override suspend fun buildRelations(position: BlockKey) {
		traverseField { it.calculateExitDistance() }

		super.buildRelations(position)
	}

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

	private fun removePosition(extractorKey: BlockKey, others: Iterable<BlockKey>) {
		extractorPositions -= extractorKey
		positions.remove(extractorKey)
		network.nodes.remove(extractorKey)
		positions.removeAll(others.toSet())

		for (position: BlockKey in others) {
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

	private fun findClosestOrExit(): TransportNode? {
		val neighbors = getTransferableNodes()
		return neighbors.shuffled().firstOrNull { it !is SolarPanelNode } ?:
			neighbors
				.filterIsInstance<SolarPanelNode>()
				.shuffled() // Make sure the lowest priority, if multiple is random every time
				.minByOrNull { it.exitDistance }
	}

	// Solar panel pathfinding logic:
	// Find the closest exit from the solar fiend and transfer to it
	override suspend fun handleStep(step: Step) {
		val next = findClosestOrExit() ?: return

		when (step) {
			is PowerTransportStep -> PowerTransportStep(step.origin, step.steps, next, step, step.traversedNodes)
			is SolarPowerOriginStep -> PowerTransportStep(step, step.steps, next, step, step.traversedNodes)
			else -> throw NotImplementedError("Unrecognized step type $step")
		}.invoke()
	}

	override suspend fun startStep(): SolarPowerOriginStep? {
		val power = tickAndGetPower()
		if (power <= 0) return null

//		println("Starting solar step")

		return SolarPowerOriginStep(AtomicInteger(), this, power)
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

	override fun toString(): String = "(SOLAR PANEL NODE: Transferable to: ${getTransferableNodes().joinToString { it.javaClass.simpleName }} nodes, distance = $exitDistance"
}
