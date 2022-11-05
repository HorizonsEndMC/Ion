package net.horizonsend.ion.core

import net.starlegacy.feature.misc.CustomBlocks
import net.starlegacy.util.isConcrete
import net.starlegacy.util.isGlass
import net.starlegacy.util.isGlassPane
import net.starlegacy.util.isGlazedTerracotta
import net.starlegacy.util.isSign
import net.starlegacy.util.isSlab
import net.starlegacy.util.isStainedTerracotta
import net.starlegacy.util.isStairs
import net.starlegacy.util.isWall
import net.starlegacy.util.isWallSign
import org.bukkit.Material.*
import org.bukkit.block.data.BlockData

object ShipFactoryMaterialCosts {
	var blockprice = mapOf(
		IRON_BLOCK to 50.0,
		DIAMOND_BLOCK to 50.0,
		COPPER_BLOCK to 175.0,
		GOLD_BLOCK to 50.0,
		EXPOSED_COPPER to 175.0,
		WEATHERED_COPPER to 175.0,
		OXIDIZED_COPPER to 175.0,
		WAXED_COPPER_BLOCK to 175.0,
		WAXED_EXPOSED_COPPER to 175.0,
		WAXED_WEATHERED_COPPER to 175.0,
		WAXED_OXIDIZED_COPPER to 175.0,
		REDSTONE_BLOCK to 100.0,

		GRINDSTONE to 50.0,
		END_ROD to 5.0,
		BELL to 50.0,
		SPONGE to 2.0,
		END_PORTAL_FRAME to 500.0,
		PISTON to 20.0,
		STICKY_PISTON to 50.0,
		REDSTONE to 1.0,
		REDSTONE_LAMP to 25.0,
		JUKEBOX to 50.0,
		CRAFTING_TABLE to 30.0,
		DROPPER to 15.0,
		HOPPER to 50.0,
		LEVER to 1.0,
		CHEST to 5.0,
		FURNACE to 15.0,
		DISPENSER to 25.0,
		MAGMA_BLOCK to 35.0,
		SEA_LANTERN to 25.0,
		IRON_TRAPDOOR to 50.0,
		NOTE_BLOCK to 20.0,
		IRON_BARS to 10.0,
		OBSERVER to 5.0,
		TRAPPED_CHEST to 5.0,
		BARREL to 5.0,
		
		OAK_PLANKS to 0.1,
		SPRUCE_PLANKS to 0.1,
		BIRCH_PLANKS to 0.1,
		JUNGLE_PLANKS to 0.1,
		ACACIA_PLANKS to 0.1,
		DARK_OAK_PLANKS to 0.1,
		MANGROVE_PLANKS to 0.1,
		CRIMSON_PLANKS to 0.1,
		WARPED_PLANKS to 0.1,
		STONE to 0.1,
		SMOOTH_STONE to 0.1,
		SANDSTONE to 0.1,
		CUT_SANDSTONE to 0.1,
		COBBLESTONE to 0.1,
		BRICKS to 0.1,
		STONE_BRICKS to 0.1,
		MUD_BRICKS to 0.1,
		NETHER_BRICKS to 0.1,
		QUARTZ_BLOCK to 0.1,
		RED_SANDSTONE to 0.1,
		CUT_RED_SANDSTONE to 0.1,
		PURPUR_BLOCK to 0.1,
		PRISMARINE to 0.1,
		PRISMARINE_BRICKS to 0.1,
		DARK_PRISMARINE to 0.1,
		POLISHED_GRANITE to 0.1,
		SMOOTH_RED_SANDSTONE to 0.1,
		MOSSY_STONE_BRICKS to 0.1,
		POLISHED_DIORITE to 0.1,
		MOSSY_COBBLESTONE to 0.1,
		END_STONE_BRICKS to 0.1,
		SMOOTH_SANDSTONE to 0.1,
		SMOOTH_QUARTZ to 0.1,
		GRANITE to 0.1,
		ANDESITE to 0.1,
		RED_NETHER_BRICKS to 0.1,
		POLISHED_ANDESITE to 0.1,
		DIORITE to 0.1,
		COBBLED_DEEPSLATE to 0.1,
		POLISHED_DEEPSLATE to 0.1,
		DEEPSLATE_BRICKS to 0.1,
		DEEPSLATE_TILES to 0.1,
		BLACKSTONE to 0.1,
		POLISHED_BLACKSTONE to 0.1,
		POLISHED_BLACKSTONE_BRICKS to 0.1,
		
		CHISELED_DEEPSLATE to 0.1,
		CHISELED_NETHER_BRICKS to 0.1,
		CHISELED_POLISHED_BLACKSTONE to 0.1,
		CHISELED_QUARTZ_BLOCK to 0.1,
		CHISELED_RED_SANDSTONE to 0.1,
		CHISELED_SANDSTONE to 0.1,
		CHISELED_STONE_BRICKS to 0.1,
		QUARTZ_BRICKS to 0.1,
		QUARTZ_PILLAR to 0.1,
		PURPUR_PILLAR to 0.1,
		CRACKED_STONE_BRICKS to 0.1,
		CRACKED_DEEPSLATE_BRICKS to 0.1,
		CRACKED_DEEPSLATE_TILES to 0.1,
		CRACKED_NETHER_BRICKS to 0.1,
		CRACKED_POLISHED_BLACKSTONE_BRICKS to 0.1
	)

	fun getPrice(data: BlockData) : Double {
		return when(data) {
			CustomBlocks.MINERAL_TITANIUM.block.blockData -> 200.0
			CustomBlocks.MINERAL_URANIUM.block.blockData -> 50.0
			CustomBlocks.MINERAL_CHETHERITE.block.blockData -> 100.0
			CustomBlocks.MINERAL_ALUMINUM.block.blockData -> 100.0
			else ->
				     if (data.material.isGlass) 0.1
			  else if (data.material.isConcrete) 0.1
			  else if (data.material.isGlassPane) 0.1
			  else if (data.material.isSlab) 0.1
			  else if (data.material.isStairs) 0.1
			  else if (data.material.isStainedTerracotta) 0.1
			  else if (data.material.isGlazedTerracotta) 0.1
			  else if (data.material.isConcrete) 0.1
			  else if (data.material.isSign) 25.0
			  else if (data.material.isWallSign) 25.0
			  else if (data.material.isWall) 0.1
			  else if (blockprice.containsKey(data.material)) blockprice[data.material]!!.toDouble()
			  else 3.0
		}
	}
}