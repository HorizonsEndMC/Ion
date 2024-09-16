package net.horizonsend.ion.server.features.transport.node.power

import net.horizonsend.ion.server.features.transport.node.NodeType
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.manager.node.PowerNodeManager
import net.horizonsend.ion.server.features.transport.node.separateNode
import net.horizonsend.ion.server.features.transport.node.type.MultiNode
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.NODE_COVERED_POSITIONS
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.SOLAR_CELL_DETECTORS
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.SOLAR_CELL_EXTRACTORS
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.getBlockDataSafe
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.getRelativeIfLoaded
import org.bukkit.GameRule
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace.DOWN
import org.bukkit.block.BlockFace.UP
import org.bukkit.block.data.AnaloguePowerable
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType.LONG_ARRAY
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

/**
 * Represents a solar panel, or multiple
 **/
class SolarPanelNode(
	override val manager: PowerNodeManager
) : MultiNode<SolarPanelNode, SolarPanelNode>() {
	override val type: NodeType = NodeType.SOLAR_PANEL_NODE
	/** The positions of extractors in this solar panel */
	private val extractorPositions = ConcurrentHashMap.newKeySet<BlockKey>()

	/** The positions of daylight detectors in this solar panel */
	private val detectorPositions = ConcurrentHashMap.newKeySet<BlockKey>()

	/** The number of solar cells contained in this node */
	private val cellNumber: Int get() = extractorPositions.size

	override fun isTransferableTo(node: TransportNode): Boolean {
		// Solar panels should be able to transfer through extractors and other solar panels
		return node !is PowerExtractorNode
	}

	/**
	 * The distance this solar node is from the nearest exit of the solar field
	 * This value will be -1 if there are no exits present
	 **/
	private var exitDistance: Int = 0

	/**
	 * Calculate the distance to an exit of a solar field
	 * This method is run upon neighbor unloads
	 **/
	private fun calculateExitDistance() {
		val neighbors = getTransferableNodes()

		// Transferable node provides an exit
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
	 * Store the last time this node successfully transferred power, so that a difference can be gained.
	 *
	 * This will be used to calculate the amount of power generated, so it remains consistent, even in laggy conditions.
	 **/
	private var lastTicked: Long = System.currentTimeMillis()

	/**
	 * Get the power and reset the last ticked time
	 **/
	fun tickAndGetPower(): Int {
		val power = getPower()

		lastTicked = System.currentTimeMillis()

		return power
	}

	/**
	 * Returns the amount of power between ticks
	 **/
	fun getPower(): Int {
		val daylightMultiplier: Double = if (
			manager.world.environment == World.Environment.NORMAL &&
			manager.world.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE) == true
		) {
			getPowerRatio()
		} else 0.5

		val time = System.currentTimeMillis()
		val diff = time - this.lastTicked

		return ((diff / 1000.0) * POWER_PER_SECOND * cellNumber * daylightMultiplier).toInt()
	}

	/**
	 * Calculates the light level at the detectors
	 **/
	private fun getPowerRatio(): Double {
		val total = detectorPositions.size * 15
		val sum = detectorPositions.sumOf {
			val data = getBlockDataSafe(manager.world, getX(it), getY(it), getZ(it)) ?: return@sumOf 0
			if (data !is AnaloguePowerable) return@sumOf 0

			data.power
		}

		return sum.toDouble() / total.toDouble()
	}

	/**
	 * Execute the provided consumer across every interconnected solar node
	 *
	 * WARNING: use this carefully
	 **/
	private fun traverseField(visited: MutableList<SolarPanelNode> = mutableListOf(), consumer: Consumer<SolarPanelNode>) {
		if (visited.contains(this)) return

		consumer.accept(this)

		visited.add(this)

		getTransferableNodes().filterIsInstance<SolarPanelNode>().filterNot { visited.contains(it) }.forEach(consumer)
	}

	/**
	 * Returns whether the individual solar panel from the extractor location is intact
	 **/
	fun isIntact(world: World, extractorKey: BlockKey): Boolean {
		return matchesSolarPanelStructure(world, extractorKey)
	}

	// If relations have changed, update the exit distances of the whole field
	override fun buildRelations(position: BlockKey) {
		super.buildRelations(position)

		// Calculate exit distance after relations have been built
		traverseField { it.calculateExitDistance() }
	}

	/*
	 * When the neighbor changes, re-calculate the exit distances
	 */
	override fun neighborChanged(neighbor: TransportNode) {
		traverseField { it.calculateExitDistance() }
	}

	override fun handlePositionRemoval(position: BlockKey) {
		isDead = true

		removePosition(position)

		if (separateNode(this)) {
			positions.clear()
			clearRelations()
		}
	}

	fun addPosition(extractorKey: BlockKey, diamondKey: BlockKey, detectorKey: BlockKey): SolarPanelNode {
		extractorPositions += extractorKey

		// Make sure there isn't still an extractor
		manager.extractors.remove(extractorKey)
		addPosition(extractorKey)
		buildRelations(extractorKey)

		positions += diamondKey
		positions += detectorKey

		detectorPositions += detectorKey

		for (position: BlockKey in positions) {
			manager.nodes[position] = this
			buildRelations(position)
		}

		return this
	}

	private fun removePosition(axisPosition: BlockKey) {
		val extractorPos = if (extractorPositions.contains(axisPosition)) axisPosition else (1..2).firstNotNullOf { offset ->
			getRelative(axisPosition, DOWN, offset).takeIf { extractorPositions.contains(it) }
		}

		val otherPositions = (1..2).map { getRelative(extractorPos, UP, it) }

		positions.remove(extractorPos)
		extractorPositions.remove(extractorPos)
		manager.nodes.remove(extractorPos)

		for (otherPos: BlockKey in otherPositions) {
			positions.remove(otherPos)
			manager.nodes.remove(otherPos)
		}
	}

	override fun addBack(position: BlockKey) {
		manager.nodeFactory.addSolarPanel(position, handleRelationships = false)
	}

	override fun rebuildNode(position: BlockKey) {
		manager.solarPanels.remove(this)
		detectorPositions.clear()

		super.rebuildNode(position)
	}

	override fun loadIntoNetwork() {
		super.loadIntoNetwork()
		manager.solarPanels += this
	}

	override fun storeData(persistentDataContainer: PersistentDataContainer) {
		persistentDataContainer.set(NODE_COVERED_POSITIONS, LONG_ARRAY, positions.toLongArray())
		persistentDataContainer.set(SOLAR_CELL_EXTRACTORS, LONG_ARRAY, extractorPositions.toLongArray())
		persistentDataContainer.set(SOLAR_CELL_DETECTORS, LONG_ARRAY, detectorPositions.toLongArray())
	}

	override fun loadData(persistentDataContainer: PersistentDataContainer) {
		val coveredPositions = persistentDataContainer.get(NODE_COVERED_POSITIONS, LONG_ARRAY)
		coveredPositions?.let { positions.addAll(it.asIterable()) }

		val extractors = persistentDataContainer.get(SOLAR_CELL_EXTRACTORS, LONG_ARRAY)
		extractors?.let { extractorPositions.addAll(it.asIterable()) }

		val detectors = persistentDataContainer.get(SOLAR_CELL_DETECTORS, LONG_ARRAY)
		detectors?.let { detectorPositions.addAll(it.asIterable()) }
	}

	companion object {
		const val POWER_PER_SECOND = 5

		fun matchesSolarPanelStructure(world: World, extractorPosition: BlockKey): Boolean {
			val extractorBlock = getBlockIfLoaded(world, getX(extractorPosition), getY(extractorPosition), getZ(extractorPosition))
			if (extractorBlock?.type != Material.CRAFTING_TABLE) return false

			val diamond = extractorBlock.getRelativeIfLoaded(UP)
			if (diamond?.type != Material.DIAMOND_BLOCK) return false

			val cell = diamond.getRelativeIfLoaded(UP)
			return cell?.type == Material.DAYLIGHT_DETECTOR
		}
	}

	override fun toString(): String = "(SOLAR PANEL NODE: Transferable to: ${getTransferableNodes().joinToString { it.javaClass.simpleName }} nodes, distance = $exitDistance, powerRatio = ${getPowerRatio()}, location = ${toVec3i(positions.random())}"
}
