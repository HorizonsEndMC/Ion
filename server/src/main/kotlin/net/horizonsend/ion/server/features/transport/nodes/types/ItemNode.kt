package net.horizonsend.ion.server.features.transport.nodes.types

import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.customItem
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.transport.filters.FilterData
import net.horizonsend.ion.server.features.transport.filters.FilterMeta
import net.horizonsend.ion.server.features.transport.filters.FilterType
import net.horizonsend.ion.server.features.transport.manager.holders.CacheHolder
import net.horizonsend.ion.server.features.transport.nodes.cache.ItemTransportCache
import net.horizonsend.ion.server.features.transport.nodes.types.Node.NodePositionData
import net.horizonsend.ion.server.features.transport.old.pipe.filter.FilterItemData
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import net.horizonsend.ion.server.miscellaneous.utils.getBlockDataSafe
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextColor.fromHexString
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.lang.ref.WeakReference

interface ItemNode : Node {
	override val cacheType: CacheType get() = CacheType.ITEMS
	override fun getMaxPathfinds(): Int = 1

	sealed interface PaneAlwaysTransfersForward

	data class InventoryNode(val position: BlockKey) : ItemNode {
		override fun canTransferFrom(other: Node, offset: BlockFace): Boolean = true
		override fun canTransferTo(other: Node, offset: BlockFace): Boolean = false
		override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = setOf()

		override val pathfindingResistance: Double = 0.0

		fun getBlockData(holder: CacheHolder<*>): BlockData? {
			val (x, y, z) = holder.transportManager.getGlobalCoordinate(toVec3i(position))
			return getBlockDataSafe(holder.getWorld(), x, y, z)
		}
	}

	data object ItemExtractorNode : ItemNode {
		override val pathfindingResistance: Double = 0.5
		override fun canTransferFrom(other: Node, offset: BlockFace): Boolean = false
		override fun canTransferTo(other: Node, offset: BlockFace): Boolean = other !is InventoryNode
		override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = ADJACENT_BLOCK_FACES.minus(backwards)
	}

	sealed interface ChanneledItemNode {
		val channel: PipeChannel

		/**
		 * Checks transfer ability between nodes
		 **/
		fun channelCheck(other: Node): Boolean {
			if (other is ChanneledItemNode) return other.channel == channel
			return true
		}
	}

	data class SolidGlassNode(override val channel: PipeChannel) : ItemNode, ChanneledItemNode, PaneAlwaysTransfersForward {
		override fun canTransferFrom(other: Node, offset: BlockFace): Boolean = channelCheck(other)
		override fun canTransferTo(other: Node, offset: BlockFace): Boolean = channelCheck(other)
		override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = ADJACENT_BLOCK_FACES.minus(backwards)

		override val pathfindingResistance: Double = 1.0
	}

	data class PaneGlassNode(override val channel: PipeChannel) : ItemNode, ChanneledItemNode, PaneAlwaysTransfersForward {
		override fun canTransferFrom(other: Node, offset: BlockFace): Boolean = channelCheck(other)
		override fun canTransferTo(other: Node, offset: BlockFace): Boolean = channelCheck(other)

		override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = ADJACENT_BLOCK_FACES.minus(backwards)

		override fun filterPositionData(nextNodes: List<NodePositionData>, backwards: BlockFace): List<NodePositionData> {
			val forward = backwards.oppositeFace

			if (nextNodes.any { it.offset == forward && it.type is PaneAlwaysTransfersForward }) {
				val filtered = mutableListOf<NodePositionData>()

				for (node in nextNodes) {
					if (node.type is InventoryNode) filtered.add(node)
					if (node.type is FilterNode) filtered.add(node)

					if (node.offset == forward && node.type is PaneAlwaysTransfersForward) filtered.add(node)
				}

				return filtered
			}

			return nextNodes
		}

		override val pathfindingResistance: Double = 0.5
	}

	data object WildcardSolidGlassNode : ItemNode, PaneAlwaysTransfersForward {
		override fun canTransferFrom(other: Node, offset: BlockFace): Boolean = true
		override fun canTransferTo(other: Node, offset: BlockFace): Boolean = true
		override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = ADJACENT_BLOCK_FACES.minus(backwards)

		override val pathfindingResistance: Double = 1.0
	}

	data class ItemMergeNode(val direction: BlockFace) : ItemNode, PaneAlwaysTransfersForward {
		override fun canTransferFrom(other: Node, offset: BlockFace): Boolean = true
		override fun canTransferTo(other: Node, offset: BlockFace): Boolean = true
		override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = ADJACENT_BLOCK_FACES.minus(backwards)

		override fun filterPositionData(nextNodes: List<NodePositionData>, backwards: BlockFace): List<NodePositionData> {
			return nextNodes.filter { node ->
				node.offset == direction || node.type is InventoryNode
			}
		}

		override val pathfindingResistance: Double = 1.0
	}

	sealed interface FilterNode : ItemNode {
		fun matches(itemStack: ItemStack) : Boolean
	}

	data class AdvancedFilterNode(val position: BlockKey, val cache: ItemTransportCache, val face: BlockFace) : FilterNode {
		val filter: WeakReference<FilterData<ItemStack, FilterMeta.ItemFilterMeta>> = WeakReference(cache.holder.transportManager.filterManager.getFilter(position, FilterType.ItemType)!!)
		override val pathfindingResistance: Double = 1.0

		override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = setOf(face)

		override fun canTransferTo(other: Node, offset: BlockFace): Boolean = other !is ItemExtractorNode  && other !is FilterNode
		override fun canTransferFrom(other: Node, offset: BlockFace): Boolean = other !is ItemExtractorNode  && other !is FilterNode

		override fun matches(itemStack: ItemStack): Boolean {
			return filter.get()?.matchesFilter(itemStack) == true
		}
	}

	data class HopperFilterNode(val position: BlockKey, var face: BlockFace, val cache: ItemTransportCache) : FilterNode, ComplexNode {
		private val globalPosition get() = cache.holder.transportManager.getGlobalCoordinate(toVec3i(position))
		override val pathfindingResistance: Double = 1.0

		override fun getTransferableDirections(backwards: BlockFace): Set<BlockFace> = setOf(face)

		override fun canTransferTo(other: Node, offset: BlockFace): Boolean = other !is InventoryNode && other !is FilterNode
		override fun canTransferFrom(other: Node, offset: BlockFace): Boolean = other !is ItemExtractorNode  && other !is FilterNode

		override fun matches(itemStack: ItemStack): Boolean {
			val inventory = cache.getInventory(toBlockKey(globalPosition)) ?: return false
			val filterData = getItemData(inventory)

			val itemData = createFilterItemData(itemStack)
			return filterData.any { data -> data == itemData }
		}

		override fun displace(movement: StarshipMovement) {
			face = movement.displaceFace(face)
		}

		private fun getItemData(inventory: Inventory): Set<FilterItemData> {
			val types = mutableSetOf<FilterItemData>()

			for (item: ItemStack? in inventory.contents) {
				val type = item?.type ?: continue

				if (type.isAir) {
					continue
				}

				types.add(createFilterItemData(item))
			}

			return types
		}

		fun createFilterItemData(item: ItemStack): FilterItemData {
			return FilterItemData(item.type, item.customItem?.identifier)
		}

		override fun toString(): String {
			return getItemData(cache.getInventory(toBlockKey(globalPosition))!!).toString()
		}
	}

	enum class PipeChannel(val solidMaterial: Material, val paneMaterial: Material, val textColor: TextColor) {
		WHITE(Material.WHITE_STAINED_GLASS, Material.WHITE_STAINED_GLASS_PANE, NamedTextColor.WHITE),
		LIGHT_GRAY(Material.LIGHT_GRAY_STAINED_GLASS, Material.LIGHT_GRAY_STAINED_GLASS_PANE, NamedTextColor.GRAY),
		GRAY(Material.GRAY_STAINED_GLASS, Material.GRAY_STAINED_GLASS_PANE, NamedTextColor.DARK_GRAY),
		BLACK(Material.BLACK_STAINED_GLASS, Material.BLACK_STAINED_GLASS_PANE, NamedTextColor.BLACK),
		BROWN(Material.BROWN_STAINED_GLASS, Material.BROWN_STAINED_GLASS_PANE, fromHexString("#6F4E37")!!),
		RED(Material.RED_STAINED_GLASS, Material.RED_STAINED_GLASS_PANE, NamedTextColor.RED),
		ORANGE(Material.ORANGE_STAINED_GLASS, Material.ORANGE_STAINED_GLASS_PANE, NamedTextColor.GOLD),
		YELLOW(Material.YELLOW_STAINED_GLASS, Material.YELLOW_STAINED_GLASS_PANE, NamedTextColor.YELLOW),
		LIME(Material.LIME_STAINED_GLASS, Material.LIME_STAINED_GLASS_PANE, NamedTextColor.GREEN),
		GREEN(Material.GREEN_STAINED_GLASS, Material.GREEN_STAINED_GLASS_PANE, NamedTextColor.DARK_GREEN),
		CYAN(Material.CYAN_STAINED_GLASS, Material.CYAN_STAINED_GLASS_PANE, NamedTextColor.DARK_AQUA),
		LIGHT_BLUE(Material.LIGHT_BLUE_STAINED_GLASS, Material.LIGHT_BLUE_STAINED_GLASS_PANE, NamedTextColor.AQUA),
		BLUE(Material.BLUE_STAINED_GLASS, Material.BLUE_STAINED_GLASS_PANE, NamedTextColor.BLUE),
		PURPLE(Material.PURPLE_STAINED_GLASS, Material.PURPLE_STAINED_GLASS_PANE, NamedTextColor.DARK_PURPLE),
		MAGENTA(Material.MAGENTA_STAINED_GLASS, Material.MAGENTA_STAINED_GLASS_PANE, NamedTextColor.LIGHT_PURPLE),
		PINK(Material.PINK_STAINED_GLASS, Material.PINK_STAINED_GLASS_PANE, fromHexString("#FFC0CB")!!),
		CLEAR(Material.GLASS, Material.GLASS_PANE, NamedTextColor.WHITE),

		;

		companion object {
			private val byMaterial: Map<Material, PipeChannel> = mutableMapOf(
				*entries.map { channel -> channel.solidMaterial to channel }.toTypedArray(),
				*entries.map { channel -> channel.paneMaterial to channel }.toTypedArray(),
			)

			operator fun get(material: Material) = byMaterial[material]
		}

		val displayName = Component.text(
			name.split('_').joinToString(separator = " ") { string -> string.replaceFirstChar { ch -> ch.uppercase() } },
			textColor
		)
	}
}
