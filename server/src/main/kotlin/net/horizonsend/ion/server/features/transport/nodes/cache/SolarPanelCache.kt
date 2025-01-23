package net.horizonsend.ion.server.features.transport.nodes.cache

import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.transport.manager.extractors.data.ExtractorMetaData
import net.horizonsend.ion.server.features.transport.manager.holders.CacheHolder
import net.horizonsend.ion.server.features.transport.nodes.types.Node
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.getBlockDataSafe
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.DaylightDetector
import kotlin.math.roundToInt

class SolarPanelCache(holder: CacheHolder<SolarPanelCache>) : TransportCache(holder) {
	override val nodeFactory: NodeCacheFactory = NodeCacheFactory.builder()
		.addSimpleNode(Material.CRAFTING_TABLE, SolarPanelComponent.CraftingTable)
		.addSimpleNode(Material.DIAMOND_BLOCK, SolarPanelComponent.DiamondBlock)
		.addSimpleNode(Material.DAYLIGHT_DETECTOR, SolarPanelComponent.DaylightDetector)
		.build()

	override val type: CacheType = CacheType.SOLAR_PANELS

	sealed interface SolarPanelComponent: Node {
		data object CraftingTable: SolarPanelComponent
		data object DiamondBlock: SolarPanelComponent
		data object DaylightDetector: SolarPanelComponent

		// Unneeded, just piggyback off of the node cache for speed
		override val cacheType: CacheType get() = CacheType.SOLAR_PANELS
		override val pathfindingResistance: Double get() = 0.0
		override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = setOf()
		override fun canTransferFrom(other: Node, offset: BlockFace): Boolean = false
		override fun canTransferTo(other: Node, offset: BlockFace): Boolean = false
	}

	fun isSolarPanel(extractorKey: BlockKey): Boolean {
		if (holder.nodeProvider.invoke(type, holder.getWorld(), extractorKey) !is SolarPanelComponent.CraftingTable) return false
		if (holder.nodeProvider.invoke(type, holder.getWorld(), getRelative(extractorKey, BlockFace.UP, 1)) !is SolarPanelComponent.DiamondBlock) return false
		if (holder.nodeProvider.invoke(type, holder.getWorld(), getRelative(extractorKey, BlockFace.UP, 2)) !is SolarPanelComponent.DaylightDetector) return false

		return true
	}

	fun getPower(world: World, extractPos: BlockKey, delta: Double): Int {
		val detectorPosition = holder.transportManager.getGlobalCoordinate(toVec3i(getRelative(extractPos, BlockFace.UP, 2)))
		val powerMultiplier = if (world.environment == World.Environment.NORMAL) 1.0 else 0.5
		val data = getBlockDataSafe(world, detectorPosition.x, detectorPosition.y, detectorPosition.z) as? DaylightDetector ?: return 0
		val powerRatio = data.power.toDouble() / data.maximumPower.toDouble()

		val base = ConfigurationFiles.transportSettings().powerConfiguration.solarPanelTickPower * delta
		return (base * powerRatio * powerMultiplier).roundToInt()
	}

	override fun tickExtractor(location: BlockKey, delta: Double, metaData: ExtractorMetaData?) {  }
}
