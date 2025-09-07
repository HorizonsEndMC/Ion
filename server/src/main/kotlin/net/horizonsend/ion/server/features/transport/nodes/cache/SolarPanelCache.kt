package net.horizonsend.ion.server.features.transport.nodes.cache

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.ConfigurationFiles.transportSettings
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlock
import net.horizonsend.ion.server.features.transport.TransportTask
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ExtractorMetaData
import net.horizonsend.ion.server.features.transport.manager.holders.CacheHolder
import net.horizonsend.ion.server.features.transport.nodes.PathfindResult
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.features.transport.nodes.types.Node.NodePositionData
import net.horizonsend.ion.server.features.transport.nodes.types.PowerNode
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.features.transport.util.CombinedSolarPanel
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import net.horizonsend.ion.server.miscellaneous.utils.getBlockDataSafe
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.DaylightDetector
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt
import kotlin.reflect.KClass

class SolarPanelCache(holder: CacheHolder<SolarPanelCache>) : TransportCache(holder) {
	val combinedSolarPanels = ConcurrentHashMap.newKeySet<CombinedSolarPanel>()
	val combinedSolarPanelPositions = ConcurrentHashMap<BlockKey, CombinedSolarPanel>()

	override val type: CacheType = CacheType.SOLAR_PANELS
    override val extractorNodeClass: KClass<out Node> = PowerNode.PowerExtractorNode::class

	val powerCache = holder.transportManager.powerNodeManager.cache

	override fun invalidate(key: BlockKey, player: UUID?) {
		combinedSolarPanelPositions[key]?.removePosition(key)

		super.invalidate(key, player)
	}

	sealed interface SolarPanelComponent: Node {
		data object CraftingTable: SolarPanelComponent
		data object DiamondBlock: SolarPanelComponent
		data object DaylightDetector: SolarPanelComponent

		// Unneeded, just piggyback off of the node cache for speed
		override val cacheType: CacheType get() = CacheType.SOLAR_PANELS
        override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = setOf()
		override fun canTransferFrom(other: Node, offset: BlockFace): Boolean = false
		override fun canTransferTo(other: Node, offset: BlockFace): Boolean = false
	}

	fun isSolarPanel(extractorKey: BlockKey): Boolean {
		if (holder.globalNodeCacher.invoke(this, holder.getWorld(), extractorKey)?.second !is SolarPanelComponent.CraftingTable) return false
		if (holder.globalNodeCacher.invoke(this, holder.getWorld(), getRelative(extractorKey, BlockFace.UP, 1))?.second !is SolarPanelComponent.DiamondBlock) return false
		if (holder.globalNodeCacher.invoke(this, holder.getWorld(), getRelative(extractorKey, BlockFace.UP, 2))?.second !is SolarPanelComponent.DaylightDetector) return false

		return true
	}

	fun getPower(extractPos: BlockKey, delta: Double): Int {
		val detectorPosition = holder.transportManager.getGlobalCoordinate(toVec3i(getRelative(extractPos, BlockFace.UP, 2)))
		val powerMultiplier = if (holder.getWorld().environment == World.Environment.NORMAL) 1.0 else 0.5
		val data = getBlockDataSafe(holder.getWorld(), detectorPosition.x, detectorPosition.y, detectorPosition.z) as? DaylightDetector ?: return 0
		val powerRatio = data.power.toDouble() / data.maximumPower.toDouble()

		val powerConfig = transportSettings().powerConfiguration
		val base = powerConfig.solarPanelTickPower * delta
		return (base * powerRatio * powerMultiplier).roundToInt()
	}

	fun tickSolarPanels() {
		val count = combinedSolarPanels.size
		val solarInterval = transportSettings().powerConfiguration.solarPanelTickInterval

		val chunkLength = count.toDouble() / solarInterval.toDouble()
		val offset = holder.transportManager.tickNumber % solarInterval

		val isLastChunk = (holder.transportManager.tickNumber + 1) % solarInterval < offset

		// Capture the remainder if it is the last chunk
		val solarTickRange = (offset * chunkLength).toInt() ..< if (isLastChunk) Int.MAX_VALUE else ((offset + 1) * chunkLength).toInt()

		for ((index, combinedPanel) in combinedSolarPanels.withIndex()) {
			if (solarTickRange.contains(index)) {
				val delta = combinedPanel.markTicked()
				combinedPanel.tick(delta)
			}
		}
	}

	override fun tickExtractor(location: BlockKey, delta: Double, metaData: ExtractorMetaData?, index: Int, count: Int) {
		if (combinedSolarPanelPositions.containsKey(location)) {
			return
		}

		val extractors = getSolarPanelExtractors(location).map { it.destinationPosition }
		extractors.forEach { debugAudience.highlightBlock(toVec3i(it), 50L) }
		val panel = CombinedSolarPanel(this, location)
		panel.addPositions(extractors)
	}

	private fun getSolarPanelExtractors(origin: BlockKey): Array<PathfindResult> {
		return powerCache.getNetworkDestinations(
			task = TransportTask(origin, holder.getWorld(), {}, 1000, IonServer.slF4JLogger),
			destinationTypeClass = PowerNode.PowerExtractorNode::class,
			originPos = origin,
			originNode = PowerNode.PowerExtractorNode,
			retainFullPath = false,
			destinationCheck = { !combinedSolarPanelPositions.containsKey(it.position) && isSolarPanel(it.position) },
			nextNodeProvider = { combinedSolarPanelProvider(this) }
		)
	}

	private val simplePowerNodes = setOf(PowerNode.SpongeNode::class, PowerNode.EndRodNode::class, PowerNode.PowerExtractorNode::class)

	private fun combinedSolarPanelProvider(node: NodePositionData): List<NodePositionData> {
		val adjacent = node.type.getTransferableDirections(node.offset.oppositeFace)
		val nodes = mutableListOf<NodePositionData>()

		for (adjacentFace in adjacent) {
			val relativePos = getRelative(node.position, adjacentFace)
			if (!holder.isLocal(relativePos)) continue

			val cached = powerCache.getOrCache(relativePos) ?: continue
			if (!simplePowerNodes.contains(cached::class)) continue
			if (node.type is PowerNode.PowerExtractorNode && cached is PowerNode.PowerExtractorNode) continue

			nodes.add(NodePositionData(
				type = cached,
				world = holder.getWorld(),
				position = relativePos,
				offset = adjacentFace,
				cache = this
			))
		}

		return nodes
	}
}
