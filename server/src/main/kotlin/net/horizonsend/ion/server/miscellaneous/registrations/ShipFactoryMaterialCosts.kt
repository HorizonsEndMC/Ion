package net.horizonsend.ion.server.miscellaneous.registrations

import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomBlocks
import net.horizonsend.ion.server.miscellaneous.utils.isConcrete
import net.horizonsend.ion.server.miscellaneous.utils.isGlass
import net.horizonsend.ion.server.miscellaneous.utils.isGlassPane
import net.horizonsend.ion.server.miscellaneous.utils.isGlazedTerracotta
import net.horizonsend.ion.server.miscellaneous.utils.isSign
import net.horizonsend.ion.server.miscellaneous.utils.isSlab
import net.horizonsend.ion.server.miscellaneous.utils.isStainedTerracotta
import net.horizonsend.ion.server.miscellaneous.utils.isStairs
import net.horizonsend.ion.server.miscellaneous.utils.isWall
import net.horizonsend.ion.server.miscellaneous.utils.isWallSign
import org.bukkit.Material.ACACIA_PLANKS
import org.bukkit.Material.ANDESITE
import org.bukkit.Material.BARREL
import org.bukkit.Material.BELL
import org.bukkit.Material.BIRCH_PLANKS
import org.bukkit.Material.BLACKSTONE
import org.bukkit.Material.BRICKS
import org.bukkit.Material.CALCITE
import org.bukkit.Material.CHEST
import org.bukkit.Material.CHISELED_DEEPSLATE
import org.bukkit.Material.CHISELED_NETHER_BRICKS
import org.bukkit.Material.CHISELED_POLISHED_BLACKSTONE
import org.bukkit.Material.CHISELED_QUARTZ_BLOCK
import org.bukkit.Material.CHISELED_RED_SANDSTONE
import org.bukkit.Material.CHISELED_SANDSTONE
import org.bukkit.Material.CHISELED_STONE_BRICKS
import org.bukkit.Material.COBBLED_DEEPSLATE
import org.bukkit.Material.COBBLESTONE
import org.bukkit.Material.COPPER_BLOCK
import org.bukkit.Material.CRACKED_DEEPSLATE_BRICKS
import org.bukkit.Material.CRACKED_DEEPSLATE_TILES
import org.bukkit.Material.CRACKED_NETHER_BRICKS
import org.bukkit.Material.CRACKED_POLISHED_BLACKSTONE_BRICKS
import org.bukkit.Material.CRACKED_STONE_BRICKS
import org.bukkit.Material.CRAFTING_TABLE
import org.bukkit.Material.CRIMSON_PLANKS
import org.bukkit.Material.CUT_RED_SANDSTONE
import org.bukkit.Material.CUT_SANDSTONE
import org.bukkit.Material.DARK_OAK_PLANKS
import org.bukkit.Material.DARK_PRISMARINE
import org.bukkit.Material.DEEPSLATE_BRICKS
import org.bukkit.Material.DEEPSLATE_TILES
import org.bukkit.Material.DIAMOND_BLOCK
import org.bukkit.Material.DIORITE
import org.bukkit.Material.DIRT
import org.bukkit.Material.DISPENSER
import org.bukkit.Material.DROPPER
import org.bukkit.Material.END_PORTAL_FRAME
import org.bukkit.Material.END_ROD
import org.bukkit.Material.END_STONE_BRICKS
import org.bukkit.Material.EXPOSED_COPPER
import org.bukkit.Material.FURNACE
import org.bukkit.Material.GOLD_BLOCK
import org.bukkit.Material.GRANITE
import org.bukkit.Material.GRASS
import org.bukkit.Material.GRASS_BLOCK
import org.bukkit.Material.GRINDSTONE
import org.bukkit.Material.HOPPER
import org.bukkit.Material.IRON_BARS
import org.bukkit.Material.IRON_BLOCK
import org.bukkit.Material.IRON_TRAPDOOR
import org.bukkit.Material.JUKEBOX
import org.bukkit.Material.JUNGLE_PLANKS
import org.bukkit.Material.LEVER
import org.bukkit.Material.LODESTONE
import org.bukkit.Material.MAGMA_BLOCK
import org.bukkit.Material.MANGROVE_PLANKS
import org.bukkit.Material.MOSSY_COBBLESTONE
import org.bukkit.Material.MOSSY_STONE_BRICKS
import org.bukkit.Material.MUD_BRICKS
import org.bukkit.Material.NETHER_BRICKS
import org.bukkit.Material.NOTE_BLOCK
import org.bukkit.Material.OAK_PLANKS
import org.bukkit.Material.OBSERVER
import org.bukkit.Material.OXIDIZED_COPPER
import org.bukkit.Material.PISTON
import org.bukkit.Material.POLISHED_ANDESITE
import org.bukkit.Material.POLISHED_BLACKSTONE
import org.bukkit.Material.POLISHED_BLACKSTONE_BRICKS
import org.bukkit.Material.POLISHED_DEEPSLATE
import org.bukkit.Material.POLISHED_DIORITE
import org.bukkit.Material.POLISHED_GRANITE
import org.bukkit.Material.PRISMARINE
import org.bukkit.Material.PRISMARINE_BRICKS
import org.bukkit.Material.PURPUR_BLOCK
import org.bukkit.Material.PURPUR_PILLAR
import org.bukkit.Material.QUARTZ_BLOCK
import org.bukkit.Material.QUARTZ_BRICKS
import org.bukkit.Material.QUARTZ_PILLAR
import org.bukkit.Material.REDSTONE
import org.bukkit.Material.REDSTONE_BLOCK
import org.bukkit.Material.REDSTONE_LAMP
import org.bukkit.Material.RED_NETHER_BRICKS
import org.bukkit.Material.RED_SANDSTONE
import org.bukkit.Material.SANDSTONE
import org.bukkit.Material.SEA_LANTERN
import org.bukkit.Material.SMOOTH_QUARTZ
import org.bukkit.Material.SMOOTH_RED_SANDSTONE
import org.bukkit.Material.SMOOTH_SANDSTONE
import org.bukkit.Material.SMOOTH_STONE
import org.bukkit.Material.SPONGE
import org.bukkit.Material.SPRUCE_PLANKS
import org.bukkit.Material.STICKY_PISTON
import org.bukkit.Material.STONE
import org.bukkit.Material.STONE_BRICKS
import org.bukkit.Material.TRAPPED_CHEST
import org.bukkit.Material.WARPED_PLANKS
import org.bukkit.Material.WAXED_COPPER_BLOCK
import org.bukkit.Material.WAXED_EXPOSED_COPPER
import org.bukkit.Material.WAXED_OXIDIZED_COPPER
import org.bukkit.Material.WAXED_WEATHERED_COPPER
import org.bukkit.Material.WEATHERED_COPPER
import org.bukkit.Material.WHITE_CONCRETE_POWDER
import org.bukkit.Material.LIGHT_GRAY_CONCRETE_POWDER
import org.bukkit.Material.GRAY_CONCRETE_POWDER
import org.bukkit.Material.BLACK_CONCRETE_POWDER
import org.bukkit.Material.RED_CONCRETE_POWDER
import org.bukkit.Material.ORANGE_CONCRETE_POWDER
import org.bukkit.Material.YELLOW_CONCRETE_POWDER
import org.bukkit.Material.GREEN_CONCRETE_POWDER
import org.bukkit.Material.LIME_CONCRETE_POWDER
import org.bukkit.Material.CYAN_CONCRETE_POWDER
import org.bukkit.Material.LIGHT_BLUE_CONCRETE_POWDER
import org.bukkit.Material.BLUE_CONCRETE_POWDER
import org.bukkit.Material.PURPLE_CONCRETE_POWDER
import org.bukkit.Material.MAGENTA_CONCRETE_POWDER
import org.bukkit.Material.PINK_CONCRETE_POWDER
import org.bukkit.Material.BROWN_CONCRETE_POWDER
import org.bukkit.Material.PODZOL
import org.bukkit.Material.MYCELIUM
import org.bukkit.Material.COARSE_DIRT
import org.bukkit.Material.ROOTED_DIRT
import org.bukkit.Material.MUD
import org.bukkit.Material.CLAY
import org.bukkit.Material.GRAVEL
import org.bukkit.Material.SAND
import org.bukkit.Material.ICE
import org.bukkit.Material.PACKED_ICE
import org.bukkit.Material.BLUE_ICE
import org.bukkit.Material.SNOW_BLOCK
import org.bukkit.Material.MOSS_BLOCK
import org.bukkit.Material.TUFF
import org.bukkit.Material.DRIPSTONE_BLOCK
import org.bukkit.Material.OBSIDIAN
import org.bukkit.Material.CRYING_OBSIDIAN
import org.bukkit.Material.CRIMSON_NYLIUM
import org.bukkit.Material.WARPED_NYLIUM
import org.bukkit.Material.SOUL_SAND
import org.bukkit.Material.SOUL_SOIL
import org.bukkit.Material.BONE_BLOCK
import org.bukkit.Material.BASALT
import org.bukkit.Material.SMOOTH_BASALT
import org.bukkit.Material.END_STONE
import org.bukkit.Material.POLISHED_BASALT
import org.bukkit.Material.AMETHYST_BLOCK
import org.bukkit.Material.OCHRE_FROGLIGHT
import org.bukkit.Material.VERDANT_FROGLIGHT
import org.bukkit.Material.PEARLESCENT_FROGLIGHT
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
		JUKEBOX to 50.0,
		CRAFTING_TABLE to 30.0,
		DROPPER to 15.0,
		HOPPER to 50.0,
		LEVER to 1.0,
		CHEST to 5.0,
		FURNACE to 15.0,
		DISPENSER to 25.0,
		MAGMA_BLOCK to 25.0,
		SEA_LANTERN to 25.0,
		REDSTONE_LAMP to 25.0,
		OCHRE_FROGLIGHT to 25.0,
		VERDANT_FROGLIGHT to 25.0,
		PEARLESCENT_FROGLIGHT to 25.0,
		IRON_TRAPDOOR to 50.0,
		NOTE_BLOCK to 20.0,
		IRON_BARS to 10.0,
		OBSERVER to 5.0,
		TRAPPED_CHEST to 5.0,
		BARREL to 5.0,
		LODESTONE to 50.0,

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
		CRACKED_POLISHED_BLACKSTONE_BRICKS to 0.1,

		DIRT to 0.1,
		GRASS_BLOCK to 0.1,
		GRASS to 0.1,
		CALCITE to 0.1,
		PODZOL to 0.1,
		MYCELIUM to 0.1,
		COARSE_DIRT to 0.1,
		ROOTED_DIRT to 0.1,
		MUD to 0.1,
		CLAY to 0.1,
		GRAVEL to 0.1,
		SAND to 0.1,
		ICE to 0.1,
		PACKED_ICE to 0.1,
		BLUE_ICE to 0.1,
		SNOW_BLOCK to 0.1,
		MOSS_BLOCK to 0.1,
		TUFF to 0.1,
		DRIPSTONE_BLOCK to 0.1,
		OBSIDIAN to 0.1,
		CRYING_OBSIDIAN to 0.1,
		CRIMSON_NYLIUM to 0.1,
		WARPED_NYLIUM to 0.1,
		SOUL_SAND to 0.1,
		SOUL_SOIL to 0.1,
		BONE_BLOCK to 0.1,
		BASALT to 0.1,
		SMOOTH_BASALT to 0.1,
		END_STONE to 0.1,
		POLISHED_BASALT to 0.1,
		AMETHYST_BLOCK to 0.1,


		WHITE_CONCRETE_POWDER to 0.1,
		LIGHT_GRAY_CONCRETE_POWDER to 0.1,
		GRAY_CONCRETE_POWDER to 0.1,
		BLACK_CONCRETE_POWDER to 0.1,
		RED_CONCRETE_POWDER to 0.1,
		ORANGE_CONCRETE_POWDER to 0.1,
		YELLOW_CONCRETE_POWDER to 0.1,
		GREEN_CONCRETE_POWDER to 0.1,
		LIME_CONCRETE_POWDER to 0.1,
		CYAN_CONCRETE_POWDER to 0.1,
		LIGHT_BLUE_CONCRETE_POWDER to 0.1,
		BLUE_CONCRETE_POWDER to 0.1,
		PURPLE_CONCRETE_POWDER to 0.1,
		MAGENTA_CONCRETE_POWDER to 0.1,
		PINK_CONCRETE_POWDER to 0.1,
		BROWN_CONCRETE_POWDER to 0.1

	)

	fun getPrice(data: BlockData): Double {
		return when (data) {
			CustomBlocks.MINERAL_TITANIUM.block.blockData -> 200.0
			CustomBlocks.MINERAL_URANIUM.block.blockData -> 50.0
			CustomBlocks.MINERAL_CHETHERITE.block.blockData -> 100.0
			CustomBlocks.MINERAL_ALUMINUM.block.blockData -> 100.0
			else ->
				if (data.material.isGlass) {
					0.1
				} else if (data.material.isConcrete) {
					0.1
				} else if (data.material.isGlassPane) {
					0.1
				} else if (data.material.isSlab) {
					0.1
				} else if (data.material.isStairs) {
					0.1
				} else if (data.material.isStainedTerracotta) {
					0.1
				} else if (data.material.isGlazedTerracotta) {
					0.1
				} else if (data.material.isConcrete) {
					0.1
				} else if (data.material.isSign) {
					25.0
				} else if (data.material.isWallSign) {
					25.0
				} else if (data.material.isWall) {
					0.1
				} else if (blockprice.containsKey(data.material)) {
					blockprice[data.material]!!.toDouble()
				} else {
					1.0
				}
		}
	}
}
