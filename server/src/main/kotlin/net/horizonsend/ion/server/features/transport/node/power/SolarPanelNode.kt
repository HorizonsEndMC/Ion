package net.horizonsend.ion.server.features.transport.node.power

import net.horizonsend.ion.server.features.multiblock.util.getBlockSnapshotAsync
import net.horizonsend.ion.server.features.transport.network.PowerNetwork
import net.horizonsend.ion.server.features.transport.node.NodeRelationship
import net.horizonsend.ion.server.features.transport.node.TransportNode
import net.horizonsend.ion.server.features.transport.node.type.MultiNode
import net.horizonsend.ion.server.features.transport.node.type.SourceNode
import net.horizonsend.ion.server.features.transport.node.type.StepHandler
import net.horizonsend.ion.server.features.transport.step.Step
import net.horizonsend.ion.server.features.transport.step.head.SingleBranchHead
import net.horizonsend.ion.server.features.transport.step.head.power.SinglePowerBranchHead
import net.horizonsend.ion.server.features.transport.step.origin.power.SolarPowerOrigin
import net.horizonsend.ion.server.features.transport.step.result.MoveForward
import net.horizonsend.ion.server.features.transport.step.result.StepResult
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.NODE_COVERED_POSITIONS
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.SOLAR_CELL_EXTRACTORS
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.firsts
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.level.LightLayer
import org.bukkit.GameRule
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockFace.DOWN
import org.bukkit.block.BlockFace.SELF
import org.bukkit.block.BlockFace.UP
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType.LONG_ARRAY
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.sin

/**
 * Represents a solar panel, or multiple
 **/
class SolarPanelNode(
	override val network: PowerNetwork
) : MultiNode<SolarPanelNode, SolarPanelNode>,
	SourceNode<PowerNetwork>,
	StepHandler<PowerNetwork> {
	override var isDead: Boolean = false
	override val positions: MutableSet<BlockKey> = ConcurrentHashMap.newKeySet()
	override val relationships: MutableSet<NodeRelationship> = ConcurrentHashMap.newKeySet()

	/** The positions of extractors in this solar panel */
	private val extractorPositions = ConcurrentHashMap.newKeySet<BlockKey>()

	/** The number of solar cells contained in this node */
	private val cellNumber: Int get() = extractorPositions.size

	override fun isTransferableTo(node: TransportNode): Boolean {
		// Solar panels should be able to transfer through extractors and other solar panels
		return node !is PowerExtractorNode
	}

	override suspend fun startStep(): Step<PowerNetwork>? {
		val power = getPower()
		if (power <= 0) return null

		return Step(
			network = this.network,
			origin = getOriginData()
		) {
			SinglePowerBranchHead(
				holder = this,
				lastDirection = SELF,
				currentNode = this@SolarPanelNode,
				share = 1.0,
			)
		}
	}

	override suspend fun handleHeadStep(head: SingleBranchHead<PowerNetwork>): StepResult<PowerNetwork> {
		// Simply move on to the next node
		return MoveForward()
	}

	override suspend fun getOriginData(): SolarPowerOrigin = SolarPowerOrigin(this)

	/**
	 * The distance this solar node is from the nearest exit of the solar field
	 * This value will be -1 if there are no exits present
	 **/
	private var exitDistance: Int = 0

	override suspend fun getNextNode(head: SingleBranchHead<PowerNetwork>, entranceDirection: BlockFace): Pair<TransportNode, BlockFace>? {
		val neighbors = getTransferableNodes()
		return neighbors.shuffled().firstOrNull { it.first !is SolarPanelNode } ?:
		neighbors
			.filter {
				val node = it.first
				node is SolarPanelNode && node.exitDistance < this.exitDistance
			} // Make sure it can't move further from an exit
			.shuffled() // Make sure the lowest priority, if multiple is random every time
			.minByOrNull { (it.first as SolarPanelNode).exitDistance }
	}

	/**
	 * Calculate the distance to an exit of a solar field
	 * This method is run upon neighbor unloads
	 **/
	private fun calculateExitDistance() {
		val neighbors = getTransferableNodes()

		// Transferable node provides an exit
		if (neighbors.any { it.first !is SolarPanelNode }) {
			exitDistance = 0
			return
		}

		val solars = neighbors.firsts().filterIsInstance<SolarPanelNode>()
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
			network.world.environment == World.Environment.NORMAL &&
			network.world.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE) == true
		) {
			// Sample position
			val sample = positions.first()
			val pos = BlockPos(getX(sample), getY(sample), getZ(sample))
			val lightLevel = network.world.minecraft.getBrightness(LightLayer.SKY, pos)

			if (lightLevel == 0) return 0

			// Calculate via sine curve otherwise
			val daylight = sin((network.world.time / (12000.0 / PI)) - (PI / 2))
			max(0.0, daylight) * 1.5 // 1.5 to bring area under curve to around equal with night
		} else 0.5

		val time = System.currentTimeMillis()
		val diff = time - this.lastTicked

		return ((diff / 1000.0) * POWER_PER_SECOND * cellNumber * daylightMultiplier).toInt()
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
	suspend fun isIntact(world: World, extractorKey: BlockKey): Boolean {
		return matchesSolarPanelStructure(world, extractorKey)
	}

	// If relations have changed, update the exit distances of the whole field
	override suspend fun buildRelations(position: BlockKey) {
		super.buildRelations(position)

		// Calculate exit distance after relations have been built
		traverseField { it.calculateExitDistance() }
	}

	/*
	 * When the neighbor changes, re-calculate the exit distances
	 */
	override suspend fun neighborChanged(neighbor: TransportNode) {
		traverseField { it.calculateExitDistance() }
	}

	override suspend fun handleRemoval(position: BlockKey) {
		isDead = true

		removePosition(position)

		// Remove all positions
		positions.forEach {
			network.nodes.remove(it)
		}

		// Rebuild relations after cleared
		clearRelations()

		// Rebuild the node without the lost position
		rebuildNode(position)
	}

	suspend fun addPosition(extractorKey: BlockKey, others: Iterable<BlockKey>): SolarPanelNode {
		extractorPositions += extractorKey

		// Make sure there isn't still an extractor
		network.extractors.remove(extractorKey)
		addPosition(extractorKey)
		buildRelations(extractorKey)

		positions += others

		for (position: BlockKey in positions) {
			network.nodes[position] = this
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
		network.nodes.remove(extractorPos)

		for (otherPos: BlockKey in otherPositions) {
			positions.remove(otherPos)
			network.nodes.remove(otherPos)
		}
	}

	override suspend fun rebuildNode(position: BlockKey) {
		network.solarPanels.remove(this)

		// Create new nodes, automatically merging together
		extractorPositions.forEach {
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

	override fun loadIntoNetwork() {
		super.loadIntoNetwork()
		network.solarPanels += this
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

	companion object {
		const val POWER_PER_SECOND = 5

		suspend fun matchesSolarPanelStructure(world: World, extractorPosition: BlockKey): Boolean {
			if (getBlockSnapshotAsync(world, extractorPosition)?.type != Material.CRAFTING_TABLE) return false
			val diamond = getRelative(extractorPosition, UP)

			if (getBlockSnapshotAsync(world, diamond)?.type != Material.DIAMOND_BLOCK) return false
			val cell = getRelative(diamond, UP)

			return getBlockSnapshotAsync(world, cell)?.type == Material.DAYLIGHT_DETECTOR
		}
	}

	override fun toString(): String = "(SOLAR PANEL NODE: Transferable to: ${getTransferableNodes().firsts().joinToString { it.javaClass.simpleName }} nodes, distance = $exitDistance"
}
