package net.horizonsend.ion.server.features.transport.util

import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.transport.nodes.cache.FluidTransportCache
import net.horizonsend.ion.server.features.transport.nodes.cache.ItemTransportCache
import net.horizonsend.ion.server.features.transport.nodes.cache.NodeCacheFactory
import net.horizonsend.ion.server.features.transport.nodes.cache.PowerTransportCache
import net.horizonsend.ion.server.features.transport.nodes.cache.SolarPanelCache
import net.horizonsend.ion.server.features.transport.nodes.cache.SolarPanelCache.SolarPanelComponent
import net.horizonsend.ion.server.features.transport.nodes.cache.TransportCache
import net.horizonsend.ion.server.features.transport.nodes.types.FluidNode
import net.horizonsend.ion.server.features.transport.nodes.types.ItemNode
import net.horizonsend.ion.server.features.transport.nodes.types.ItemNode.SolidGlassNode
import net.horizonsend.ion.server.features.transport.nodes.types.PowerNode
import net.horizonsend.ion.server.features.transport.nodes.types.PowerNode.PowerFlowMeter
import net.horizonsend.ion.server.features.transport.nodes.types.PowerNode.PowerInputNode
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.STAINED_GLASS_PANE_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.STAINED_GLASS_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.UNWAXED_CHISELED_COPPER_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.axis
import org.bukkit.Material
import org.bukkit.Material.CRAFTING_TABLE
import org.bukkit.Material.END_ROD
import org.bukkit.Material.FLETCHING_TABLE
import org.bukkit.Material.GLASS
import org.bukkit.Material.GLASS_PANE
import org.bukkit.Material.GRINDSTONE
import org.bukkit.Material.IRON_BLOCK
import org.bukkit.Material.LAPIS_BLOCK
import org.bukkit.Material.NOTE_BLOCK
import org.bukkit.Material.OBSERVER
import org.bukkit.Material.REDSTONE_BLOCK
import org.bukkit.Material.SPONGE
import org.bukkit.Material.TINTED_GLASS
import org.bukkit.Material.WAXED_CHISELED_COPPER
import org.bukkit.Material.WAXED_EXPOSED_CHISELED_COPPER
import org.bukkit.Material.WAXED_OXIDIZED_COPPER
import org.bukkit.Material.WAXED_WEATHERED_CHISELED_COPPER
import org.bukkit.NamespacedKey
import org.bukkit.block.data.type.Hopper
import org.bukkit.block.data.type.Observer
import org.bukkit.block.data.type.Vault
import org.bukkit.craftbukkit.block.impl.CraftEndRod
import org.bukkit.craftbukkit.block.impl.CraftGrindstone
import org.bukkit.craftbukkit.block.impl.CraftLightningRod

enum class CacheType(val namespacedKey: NamespacedKey) {
	POWER(NamespacedKeys.POWER_TRANSPORT) {
		override val nodeCacheFactory: NodeCacheFactory = NodeCacheFactory.builder()
			.addSimpleNode(CRAFTING_TABLE, PowerNode.PowerExtractorNode)
			.addSimpleNode(SPONGE, PowerNode.SpongeNode)
			.addDataHandler<CraftEndRod>(END_ROD) { data, _, _ -> PowerNode.EndRodNode(data.facing.axis) }
			.addSimpleNode(REDSTONE_BLOCK, PowerNode.RedstoneMergeNode)
			.addSimpleNode(IRON_BLOCK, PowerNode.IronMergeNode)
			.addSimpleNode(LAPIS_BLOCK, PowerNode.InvertedMergeNode)
			.addDataHandler<Observer>(OBSERVER) { data, loc, holder -> PowerFlowMeter(holder.cache as PowerTransportCache, data.facing, holder.getWorld(), loc) }
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
			.addSimpleNode(Material.CRAFTING_TABLE, SolarPanelComponent.CraftingTable)
			.addSimpleNode(Material.DIAMOND_BLOCK, SolarPanelComponent.DiamondBlock)
			.addSimpleNode(Material.DAYLIGHT_DETECTOR, SolarPanelComponent.DaylightDetector)
			.build()

		override fun get(chunk: IonChunk): SolarPanelCache {
			return chunk.transportNetwork.solarPanelManager.cache
		}

		override fun get(ship: ActiveStarship): SolarPanelCache {
			return ship.transportManager.solarPanelManager.cache
		}
	},
	FLUID(NamespacedKeys.FLUID_TRANSPORT) {
		override val nodeCacheFactory: NodeCacheFactory = NodeCacheFactory.builder()
			.addDataHandler<CraftLightningRod>(Material.LIGHTNING_ROD) { data, _, _ -> FluidNode.LightningRodNode(data.facing.axis) }
			.addSimpleNode(WAXED_CHISELED_COPPER) { _, _ -> FluidNode.FluidJunctionNode(WAXED_CHISELED_COPPER) }
			.addSimpleNode(WAXED_EXPOSED_CHISELED_COPPER) { _, _ -> FluidNode.FluidJunctionNode(WAXED_EXPOSED_CHISELED_COPPER) }
			.addSimpleNode(WAXED_WEATHERED_CHISELED_COPPER) { _, _ -> FluidNode.FluidJunctionNode(WAXED_WEATHERED_CHISELED_COPPER) }
			.addSimpleNode(WAXED_OXIDIZED_COPPER) { _, _ -> FluidNode.FluidJunctionNode(WAXED_OXIDIZED_COPPER) }
			.addSimpleNode(UNWAXED_CHISELED_COPPER_TYPES) { _, _, _ -> FluidNode.FluidJunctionNode(CRAFTING_TABLE) } // All unwaxed chiseled are a single channel
			.addSimpleNode(CRAFTING_TABLE, FluidNode.FluidExtractorNode)
			.addSimpleNode(FLETCHING_TABLE, FluidNode.FluidInputNode)
			.addSimpleNode(REDSTONE_BLOCK, FluidNode.FluidMergeNode)
			.addSimpleNode(IRON_BLOCK, FluidNode.FluidMergeNode)
			.addSimpleNode(LAPIS_BLOCK, FluidNode.FluidInvertedMergeNode)
			.build()

		override fun get(chunk: IonChunk): FluidTransportCache {
			TODO("Fluid is disabled")
//			return chunk.transportNetwork.fluidNodeManager.cache
		}

		override fun get(ship: ActiveStarship): FluidTransportCache {
			TODO("Fluid is disabled")
//			return ship.transportManager.fluidNodeManager.cache
		}
	},
	ITEMS(NamespacedKeys.ITEM_TRANSPORT) {
		override val nodeCacheFactory: NodeCacheFactory = NodeCacheFactory.builder()
			.addSimpleNode(CRAFTING_TABLE, ItemNode.ItemExtractorNode)
			.addDataHandler<Vault>(CustomBlocks.ADVANCED_ITEM_EXTRACTOR) { _, _, _ -> ItemNode.ItemExtractorNode }
			.addSimpleNode(STAINED_GLASS_TYPES) { _, material, _ -> SolidGlassNode(ItemNode.PipeChannel[material]!!) }
			.addSimpleNode(STAINED_GLASS_PANE_TYPES) { _, material, _ -> ItemNode.PaneGlassNode(ItemNode.PipeChannel[material]!!) }
			.addSimpleNode(GLASS, SolidGlassNode(ItemNode.PipeChannel.CLEAR))
			.addSimpleNode(GLASS_PANE, ItemNode.PaneGlassNode(ItemNode.PipeChannel.CLEAR))
			.addSimpleNode(TINTED_GLASS, ItemNode.WildcardSolidGlassNode)
			.addDataHandler<CraftGrindstone>(GRINDSTONE) { data, key, _ -> ItemNode.ItemMergeNode }
			.addDataHandler<Vault>(CustomBlocks.ITEM_FILTER) { data, key, holder -> ItemNode.AdvancedFilterNode(key, holder.cache as ItemTransportCache) }
			.addDataHandler<Hopper>(Material.HOPPER) { data, key, holder -> ItemNode.HopperFilterNode(key, data.facing, holder.cache as ItemTransportCache) }
			.addSimpleNode(
				Material.CHEST,
				Material.TRAPPED_CHEST,
				Material.BARREL,
				Material.FURNACE,
				Material.DISPENSER,
				Material.DROPPER,
				Material.DECORATED_POT
			) { key, _, _ -> ItemNode.InventoryNode(key) }
			.build()

		override fun get(chunk: IonChunk): ItemTransportCache {
			return chunk.transportNetwork.itemPipeManager.cache
		}

		override fun get(ship: ActiveStarship): ItemTransportCache {
			return ship.transportManager.itemPipeManager.cache
		}
	},

	;

	abstract val nodeCacheFactory: NodeCacheFactory

	abstract fun get(chunk: IonChunk): TransportCache
	abstract fun get(ship: ActiveStarship): TransportCache

	companion object {
		private val byKey = entries.associateBy { it.namespacedKey }
		operator fun get(key: NamespacedKey): CacheType = byKey[key]!!
	}
}
