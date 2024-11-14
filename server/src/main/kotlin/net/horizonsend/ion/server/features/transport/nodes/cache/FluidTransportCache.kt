package net.horizonsend.ion.server.features.transport.nodes.cache

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.FluidStoringEntity
import net.horizonsend.ion.server.features.transport.NewTransport
import net.horizonsend.ion.server.features.transport.manager.holders.CacheHolder
import net.horizonsend.ion.server.features.transport.nodes.types.FluidNode
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.features.transport.util.getNetworkDestinations
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.UNWAXED_CHISELED_COPPER_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.axis
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import org.bukkit.Material
import org.bukkit.Material.CRAFTING_TABLE
import org.bukkit.Material.WAXED_CHISELED_COPPER
import org.bukkit.Material.WAXED_EXPOSED_CHISELED_COPPER
import org.bukkit.Material.WAXED_OXIDIZED_COPPER
import org.bukkit.Material.WAXED_WEATHERED_CHISELED_COPPER
import org.bukkit.World
import org.bukkit.craftbukkit.v1_20_R3.block.impl.CraftLightningRod
import kotlin.math.roundToInt

class FluidTransportCache(holder: CacheHolder<FluidTransportCache>): TransportCache(holder) {
	override val type: CacheType = CacheType.FLUID
	override val nodeFactory: NodeCacheFactory = NodeCacheFactory.builder()
		.addDataHandler<CraftLightningRod>(Material.LIGHTNING_ROD) { data, _ -> FluidNode.LightningRodNode(data.facing.axis) }

		.addSimpleNode(WAXED_CHISELED_COPPER) { FluidNode.FluidJunctionNode(WAXED_CHISELED_COPPER) }
		.addSimpleNode(WAXED_EXPOSED_CHISELED_COPPER) { FluidNode.FluidJunctionNode(WAXED_EXPOSED_CHISELED_COPPER) }
		.addSimpleNode(WAXED_WEATHERED_CHISELED_COPPER) { FluidNode.FluidJunctionNode(WAXED_WEATHERED_CHISELED_COPPER) }
		.addSimpleNode(WAXED_OXIDIZED_COPPER) { FluidNode.FluidJunctionNode(WAXED_OXIDIZED_COPPER) }

		.addSimpleNode(UNWAXED_CHISELED_COPPER_TYPES) { FluidNode.FluidJunctionNode(CRAFTING_TABLE) }

		.addSimpleNode(CRAFTING_TABLE, FluidNode.FluidExtractorNode)
		.addSimpleNode(Material.FLETCHING_TABLE, FluidNode.FluidInputNode)
		.addSimpleNode(Material.REDSTONE_BLOCK, FluidNode.FluidMergeNode)
		.addSimpleNode(Material.IRON_BLOCK, FluidNode.FluidMergeNode)
		.addSimpleNode(Material.LAPIS_BLOCK, FluidNode.FluidInvertedMergeNode)
		.build()

	override fun tickExtractor(location: BlockKey, delta: Double) { NewTransport.executor.submit {
		val world = holder.getWorld()
		val sources = getFluidExtractorSourcePool(location, world)
		val source = sources.randomOrNull() ?: return@submit //TODO take from all

		if (source.getStoredResources().isEmpty()) return@submit

		// Flood fill on the network to find power inputs, and check input data for multiblocks using that input that can store any power
		val destinations: List<BlockKey> = getNetworkDestinations<FluidNode.FluidInputNode>(CacheType.POWER, world, location) { node ->
			world.ion.inputManager.getHolders(type, node.position).any { entity -> entity is FluidStoringEntity && !entity.isFull() }
		}

		if (destinations.isEmpty()) return@submit

		val transferLimit = (IonServer.transportSettings.extractorConfiguration.maxFluidRemovedPerExtractorTick * delta).roundToInt()
	} }

	private fun getFluidExtractorSourcePool(extractorLocation: BlockKey, world: World): List<FluidStoringEntity> {
		val sources = mutableListOf<FluidStoringEntity>()

		for (face in ADJACENT_BLOCK_FACES) {
			val inputLocation = getRelative(extractorLocation, face)
			if (holder.getOrCacheGlobalNode(inputLocation) !is FluidNode.FluidInputNode) continue
			val entities = getInputEntities(world, inputLocation)

			for (entity in entities) {
				if (entity !is FluidStoringEntity) continue
				sources.add(entity)
			}
		}

		return sources
	}
}
