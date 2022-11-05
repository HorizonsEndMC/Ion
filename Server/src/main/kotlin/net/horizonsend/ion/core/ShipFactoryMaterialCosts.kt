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
import org.bukkit.Material
import org.bukkit.block.data.BlockData

object ShipFactoryMaterialCosts {
	var blockprice = mapOf<Material, Double>(
		Material.IRON_BLOCK to 50.0,
		Material.DIAMOND_BLOCK to 50.0,
		Material.COPPER_BLOCK to 175.0,
		Material.GOLD_BLOCK to 50.0,
		Material.EXPOSED_COPPER to 175.0,
		Material.WEATHERED_COPPER to 175.0,
		Material.OXIDIZED_COPPER to 175.0,
		Material.WAXED_COPPER_BLOCK to 175.0,
		Material.WAXED_EXPOSED_COPPER to 175.0,
		Material.WAXED_WEATHERED_COPPER to 175.0,
		Material.WAXED_OXIDIZED_COPPER to 175.0,
		Material.REDSTONE_BLOCK to 100.0,

		Material.GRINDSTONE to 50.0,
		Material.END_ROD to 5.0,
		Material.BELL to 50.0,
		Material.SPONGE to 2.0,
		Material.END_PORTAL_FRAME to 500.0,
		Material.PISTON to 20.0,
		Material.STICKY_PISTON to 50.0,
		Material.REDSTONE to 1.0,
		Material.REDSTONE_LAMP to 25.0,
		Material.JUKEBOX to 50.0,
		Material.CRAFTING_TABLE to 30.0,
		Material.DROPPER to 15.0,
		Material.HOPPER to 50.0,
		Material.LEVER to 1.0,
		Material.CHEST to 5.0,
		Material.FURNACE to 15.0,
		Material.DISPENSER to 25.0,
		Material.MAGMA_BLOCK to 35.0,
		Material.SEA_LANTERN to 25.0,
		Material.IRON_TRAPDOOR to 50.0,
		Material.NOTE_BLOCK to 20.0,
		Material.IRON_BARS to 10.0,
		Material.OBSERVER to 5.0,
		Material.TRAPPED_CHEST to 5.0,
		Material.BARREL to 5.0
		
		Material.OAK_PLANKS to 0.1
		Material.SPRUCE_PLANKS to 0.1
		Material.BIRCH_PLANKS to 0.1
		Material.JUNGLE_PLANKS to 0.1
		Material.ACACIA_PLANKS to 0.1
		Material.DARK_OAK_PLANKS to 0.1
		Material.MANGROVE_PLANKS to 0.1
		Material.CRIMSON_PLANKS to 0.1
		Material.WARPED_PLANKS to 0.1
		Material.STONE to 0.1
		Material.SMOOTH_STONE to 0.1
		Material.SANDSTONE to 0.1
		Material.CUT_SANDSTONE to 0.1
		Material.COBBLESTONE to 0.1
		Material.BRICKS to 0.1
		Material.STONE_BRICKS to 0.1
		Material.MUD_BRICKS to 0.1
		Material.NETHER_BRICKS to 0.1
		Material.QUARTZ_BLOCK to 0.1
		Material.RED_SANDSTONE to 0.1
		Material.CUT_RED_SANDSTONE to 0.1
		Material.PURPUR_BLOCK to 0.1
		Material.PRISMARINE to 0.1
		Material.PRISMARINE_BRICKS to 0.1
		Material.DARK_PRISMARINE to 0.1
		Material.POLISHED_GRANITE to 0.1
		Material.SMOOTH_RED_SANDSTONE to 0.1
		Material.MOSSY_STONE_BRICKS to 0.1
		Material.POLISHED_DIORITE to 0.1
		Material.MOSSY_COBBLESTONE to 0.1
		Material.END_STONE_BRICKS to 0.1
		Material.SMOOTH_SANDSTONE to 0.1
		Material.SMOOTH_QUARTZ to 0.1
		Material.GRANITE to 0.1
		Material.ANDESITE to 0.1
		Material.RED_NETHER_BRICKS to 0.1
		Material.POLISHED_ANDESITE to 0.1
		Material.DIORITE to 0.1
		Material.COBBLED_DEEPSLATE to 0.1
		Material.POLISHED_DEEPSLATE to 0.1
		Material.DEEPSLATE_BRICKS to 0.1
		Material.DEEPSLATE_TILES to 0.1
		Material.BLACKSTONE to 0.1
		Material.POLISHED_BLACKSTONE to 0.1
		Material.POLISHED_BLACKSTONE_BRICKS to 0.1
	)
	fun getPrice(data: BlockData) : Double {
		when(data){
			CustomBlocks.MINERAL_TITANIUM.block.blockData -> return 200.0
			CustomBlocks.MINERAL_URANIUM.block.blockData -> return 50.0
			CustomBlocks.MINERAL_CHETHERITE.block.blockData -> return 100.0
			CustomBlocks.MINERAL_ALUMINUM.block.blockData -> return 100.0
		}
		if(data.material.isGlass) return 0.1
		else if(data.material.isConcrete) return 0.1
		else if(data.material.isGlassPane) return 0.1
		else if(data.material.isSlab) return 0.1
		else if(data.material.isStairs) return 0.1
		else if(data.material.isStainedTerracotta) return 0.1
		else if(data.material.isGlazedTerracotta) return 0.1
		else if(data.material.isConcrete) return 0.1
		else if(data.material.isSign) return 25.0
		else if(data.material.isWallSign) return 25.0
		else if(data.material.isWall) return 0.1
		else return if (blockprice.containsKey(data.material)) blockprice[data.material]!!.toDouble()
		else 3.0
	}
}