package net.horizonsend.ion.server.features.transport.util

import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks.ITEM_FILTER
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.transport.nodes.cache.ItemTransportCache
import net.horizonsend.ion.server.features.transport.nodes.cache.PowerTransportCache
import net.horizonsend.ion.server.features.transport.nodes.cache.SolarPanelCache
import net.horizonsend.ion.server.features.transport.nodes.cache.SolarPanelCache.SolarPanelComponent
import net.horizonsend.ion.server.features.transport.nodes.cache.TransportCache
import net.horizonsend.ion.server.features.transport.nodes.types.ItemNode
import net.horizonsend.ion.server.features.transport.nodes.types.ItemNode.SolidGlassNode
import net.horizonsend.ion.server.features.transport.nodes.types.PowerNode
import net.horizonsend.ion.server.features.transport.nodes.types.PowerNode.PowerFlowMeter
import net.horizonsend.ion.server.features.transport.nodes.types.PowerNode.PowerInputNode
import net.horizonsend.ion.server.features.transport.nodes.util.NodeCacheFactory
import net.horizonsend.ion.server.features.transport.util.CacheType.entries
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.STAINED_GLASS_PANE_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.STAINED_GLASS_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.axis
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toVec3i
import org.bukkit.Material.BARREL
import org.bukkit.Material.BLAST_FURNACE
import org.bukkit.Material.CHEST
import org.bukkit.Material.CRAFTING_TABLE
import org.bukkit.Material.DAYLIGHT_DETECTOR
import org.bukkit.Material.DECORATED_POT
import org.bukkit.Material.DIAMOND_BLOCK
import org.bukkit.Material.DISPENSER
import org.bukkit.Material.DROPPER
import org.bukkit.Material.END_ROD
import org.bukkit.Material.FURNACE
import org.bukkit.Material.GLASS
import org.bukkit.Material.GLASS_PANE
import org.bukkit.Material.GRINDSTONE
import org.bukkit.Material.HOPPER
import org.bukkit.Material.IRON_BLOCK
import org.bukkit.Material.LAPIS_BLOCK
import org.bukkit.Material.NOTE_BLOCK
import org.bukkit.Material.OBSERVER
import org.bukkit.Material.REDSTONE_BLOCK
import org.bukkit.Material.SMOKER
import org.bukkit.Material.SPONGE
import org.bukkit.Material.TINTED_GLASS
import org.bukkit.Material.TRAPPED_CHEST
import org.bukkit.NamespacedKey
import org.bukkit.block.BlockFace
import org.bukkit.block.data.FaceAttachable.AttachedFace
import org.bukkit.block.data.type.Hopper
import org.bukkit.block.data.type.Observer
import org.bukkit.block.data.type.Vault
import org.bukkit.craftbukkit.block.impl.CraftEndRod
import org.bukkit.craftbukkit.block.impl.CraftGrindstone

enum class CacheType(val namespacedKey: NamespacedKey) {
	POWER(NamespacedKeys.POWER_TRANSPORT) {
		override val nodeCacheFactory: NodeCacheFactory = NodeCacheFactory.builder()
			.addSimpleNode(CRAFTING_TABLE, PowerNode.PowerExtractorNode)
			.addSimpleNode(SPONGE, PowerNode.SpongeNode)
			.addDataHandler<CraftEndRod>(END_ROD) { data, _, _ -> PowerNode.EndRodNode(data.facing.axis) }
			.addSimpleNode(REDSTONE_BLOCK, PowerNode.RedstoneMergeNode)
			.addSimpleNode(IRON_BLOCK, PowerNode.IronMergeNode)
			.addSimpleNode(LAPIS_BLOCK, PowerNode.InvertedMergeNode)
			.addDataHandler<Observer>(OBSERVER) { data, loc, holder -> PowerFlowMeter(data.facing, holder.getWorld(), holder.transportManager, loc) }
			.addSimpleNode(NOTE_BLOCK, PowerInputNode)
			.build()

		override fun get(chunk: IonChunk): PowerTransportCache {
			return chunk.transportNetwork.powerNodeManager.cache
		}

		override fun get(ship: ActiveStarship): PowerTransportCache {
			return ship.transportManager.powerNodeManager.cache
		}
	},
	SOLAR_PANELS(NamespacedKeys.POWER_TRANSPORT) {
		override val nodeCacheFactory: NodeCacheFactory = NodeCacheFactory.builder()
			.addSimpleNode(CRAFTING_TABLE, SolarPanelComponent.CraftingTable)
			.addSimpleNode(DIAMOND_BLOCK, SolarPanelComponent.DiamondBlock)
			.addSimpleNode(DAYLIGHT_DETECTOR, SolarPanelComponent.DaylightDetector)
			.build()

		override fun get(chunk: IonChunk): SolarPanelCache {
			return chunk.transportNetwork.solarPanelManager.cache
		}

		override fun get(ship: ActiveStarship): SolarPanelCache {
			return ship.transportManager.solarPanelManager.cache
		}
	},
//	FLUID(NamespacedKeys.FLUID_TRANSPORT, FluidNode.FluidInputNode::class) {
//		override val nodeCacheFactory: NodeCacheFactory = NodeCacheFactory.builder()
//			.addDataHandler<CraftLightningRod>(Material.LIGHTNING_ROD) { data, _, _ -> FluidNode.LightningRodNode(data.facing.axis) }
//			.addSimpleNode(WAXED_CHISELED_COPPER) { _, _ -> FluidNode.FluidJunctionNode(WAXED_CHISELED_COPPER) }
//			.addSimpleNode(WAXED_EXPOSED_CHISELED_COPPER) { _, _ -> FluidNode.FluidJunctionNode(WAXED_EXPOSED_CHISELED_COPPER) }
//			.addSimpleNode(WAXED_WEATHERED_CHISELED_COPPER) { _, _ -> FluidNode.FluidJunctionNode(WAXED_WEATHERED_CHISELED_COPPER) }
//			.addSimpleNode(WAXED_OXIDIZED_COPPER) { _, _ -> FluidNode.FluidJunctionNode(WAXED_OXIDIZED_COPPER) }
//			.addSimpleNode(UNWAXED_CHISELED_COPPER_TYPES) { _, _, _ -> FluidNode.FluidJunctionNode(CRAFTING_TABLE) } // All unwaxed chiseled are a single channel
//			.addSimpleNode(CRAFTING_TABLE, FluidNode.FluidExtractorNode)
//			.addSimpleNode(FLETCHING_TABLE, FluidNode.FluidInputNode)
//			.addSimpleNode(REDSTONE_BLOCK, FluidNode.FluidMergeNode)
//			.addSimpleNode(IRON_BLOCK, FluidNode.FluidMergeNode)
//			.addSimpleNode(LAPIS_BLOCK, FluidNode.FluidInvertedMergeNode)
//			.build()
//
//		override fun get(chunk: IonChunk): FluidTransportCache {
//			TODO("Fluid is disabled")
////			return chunk.transportNetwork.fluidNodeManager.cache
//		}
//
//		override fun get(ship: ActiveStarship): FluidTransportCache {
//			TODO("Fluid is disabled")
////			return ship.transportManager.fluidNodeManager.cache
//		}
//	},
	ITEMS(NamespacedKeys.ITEM_TRANSPORT) {
		override val nodeCacheFactory: NodeCacheFactory = NodeCacheFactory.builder()
			.addSimpleNode(CRAFTING_TABLE, ItemNode.ItemExtractorNode)
			.addDataHandler<Vault>(CustomBlocks.ADVANCED_ITEM_EXTRACTOR) { _, _, _ -> ItemNode.ItemExtractorNode }
			.addSimpleNode(STAINED_GLASS_TYPES) { _, material, _ -> SolidGlassNode(ItemNode.PipeChannel[material]!!) }
			.addSimpleNode(STAINED_GLASS_PANE_TYPES) { _, material, _ -> ItemNode.PaneGlassNode(ItemNode.PipeChannel[material]!!) }
			.addSimpleNode(GLASS, SolidGlassNode(ItemNode.PipeChannel.CLEAR))
			.addSimpleNode(GLASS_PANE, ItemNode.PaneGlassNode(ItemNode.PipeChannel.CLEAR))
			.addSimpleNode(TINTED_GLASS, ItemNode.WildcardSolidGlassNode)
			.addDataHandler<CraftGrindstone>(GRINDSTONE) { data, _, _ ->
				val outFace: BlockFace = when (data.attachedFace) {
					AttachedFace.CEILING -> BlockFace.DOWN
					AttachedFace.FLOOR -> BlockFace.UP
					else -> data.facing
				}

				ItemNode.ItemMergeNode(outFace)
			}
			.addDataHandler<Vault>(ITEM_FILTER) { data, key, holder -> ItemNode.AdvancedFilterNode(
				toBlockKey(holder.transportManager.getLocalCoordinate(toVec3i(key))),
				holder.cache as ItemTransportCache,
				ITEM_FILTER.getFace(data)
			) }
			.addDataHandler<Hopper>(HOPPER) { data, key, holder -> ItemNode.HopperFilterNode(
				toBlockKey(holder.transportManager.getLocalCoordinate(toVec3i(key))),
				data.facing,
				holder.cache as ItemTransportCache
			) }
			.addSimpleNode(
				CHEST,
				TRAPPED_CHEST,
				BARREL,
				FURNACE,
				SMOKER,
				BLAST_FURNACE,
				DISPENSER,
				DROPPER,
				DECORATED_POT
			) { key, _, _ -> ItemNode.InventoryNode(key) }
			.build()

		override fun get(chunk: IonChunk): ItemTransportCache {
			return chunk.transportNetwork.itemPipeManager.cache
		}

		override fun get(ship: ActiveStarship): ItemTransportCache {
			return ship.transportManager.itemPipeManager.cache
		}
	};

	abstract val nodeCacheFactory: NodeCacheFactory

	abstract fun get(chunk: IonChunk): TransportCache
	abstract fun get(ship: ActiveStarship): TransportCache

	companion object {
		private val byKey = entries.associateBy { it.namespacedKey }
		operator fun get(key: NamespacedKey): CacheType = byKey[key]!!
	}
}
