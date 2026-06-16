package net.horizonsend.ion.server.miscellaneous.registrations

import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys
import net.horizonsend.ion.server.core.registration.registries.CustomBlockRegistry.Companion.customBlock
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
import org.bukkit.Material.SHORT_GRASS
import org.bukkit.Material.ACACIA_PLANKS
import org.bukkit.Material.AMETHYST_BLOCK
import org.bukkit.Material.ANDESITE
import org.bukkit.Material.BARREL
import org.bukkit.Material.BASALT
import org.bukkit.Material.BELL
import org.bukkit.Material.BIRCH_PLANKS
import org.bukkit.Material.BLACKSTONE
import org.bukkit.Material.BLACK_CONCRETE_POWDER
import org.bukkit.Material.BLUE_CONCRETE_POWDER
import org.bukkit.Material.BLUE_ICE
import org.bukkit.Material.BONE_BLOCK
import org.bukkit.Material.BRICKS
import org.bukkit.Material.BROWN_CONCRETE_POWDER
import org.bukkit.Material.CALCITE
import org.bukkit.Material.CHEST
import org.bukkit.Material.CHISELED_DEEPSLATE
import org.bukkit.Material.CHISELED_NETHER_BRICKS
import org.bukkit.Material.CHISELED_POLISHED_BLACKSTONE
import org.bukkit.Material.CHISELED_QUARTZ_BLOCK
import org.bukkit.Material.CHISELED_RED_SANDSTONE
import org.bukkit.Material.CHISELED_SANDSTONE
import org.bukkit.Material.CHISELED_STONE_BRICKS
import org.bukkit.Material.CLAY
import org.bukkit.Material.COARSE_DIRT
import org.bukkit.Material.COBBLED_DEEPSLATE
import org.bukkit.Material.COBBLESTONE
import org.bukkit.Material.COPPER_BLOCK
import org.bukkit.Material.CRACKED_DEEPSLATE_BRICKS
import org.bukkit.Material.CRACKED_DEEPSLATE_TILES
import org.bukkit.Material.CRACKED_NETHER_BRICKS
import org.bukkit.Material.CRACKED_POLISHED_BLACKSTONE_BRICKS
import org.bukkit.Material.CRACKED_STONE_BRICKS
import org.bukkit.Material.CRAFTING_TABLE
import org.bukkit.Material.CRIMSON_NYLIUM
import org.bukkit.Material.CRIMSON_PLANKS
import org.bukkit.Material.CRYING_OBSIDIAN
import org.bukkit.Material.CUT_RED_SANDSTONE
import org.bukkit.Material.CUT_SANDSTONE
import org.bukkit.Material.CYAN_CONCRETE_POWDER
import org.bukkit.Material.DARK_OAK_PLANKS
import org.bukkit.Material.DARK_PRISMARINE
import org.bukkit.Material.DEEPSLATE_BRICKS
import org.bukkit.Material.DEEPSLATE_TILES
import org.bukkit.Material.DIAMOND_BLOCK
import org.bukkit.Material.DIORITE
import org.bukkit.Material.DIRT
import org.bukkit.Material.DISPENSER
import org.bukkit.Material.DRIPSTONE_BLOCK
import org.bukkit.Material.DROPPER
import org.bukkit.Material.END_PORTAL_FRAME
import org.bukkit.Material.END_ROD
import org.bukkit.Material.END_STONE
import org.bukkit.Material.END_STONE_BRICKS
import org.bukkit.Material.EXPOSED_COPPER
import org.bukkit.Material.FURNACE
import org.bukkit.Material.GOLD_BLOCK
import org.bukkit.Material.GRANITE
import org.bukkit.Material.GRASS_BLOCK
import org.bukkit.Material.GRAVEL
import org.bukkit.Material.GRAY_CONCRETE_POWDER
import org.bukkit.Material.GREEN_CONCRETE_POWDER
import org.bukkit.Material.GRINDSTONE
import org.bukkit.Material.HOPPER
import org.bukkit.Material.ICE
import org.bukkit.Material.IRON_BARS
import org.bukkit.Material.IRON_BLOCK
import org.bukkit.Material.IRON_TRAPDOOR
import org.bukkit.Material.JUKEBOX
import org.bukkit.Material.JUNGLE_PLANKS
import org.bukkit.Material.LEVER
import org.bukkit.Material.LIGHT_BLUE_CONCRETE_POWDER
import org.bukkit.Material.LIGHT_GRAY_CONCRETE_POWDER
import org.bukkit.Material.LIME_CONCRETE_POWDER
import org.bukkit.Material.LODESTONE
import org.bukkit.Material.MAGENTA_CONCRETE_POWDER
import org.bukkit.Material.MAGMA_BLOCK
import org.bukkit.Material.MANGROVE_PLANKS
import org.bukkit.Material.MOSSY_COBBLESTONE
import org.bukkit.Material.MOSSY_STONE_BRICKS
import org.bukkit.Material.MOSS_BLOCK
import org.bukkit.Material.MUD
import org.bukkit.Material.MUD_BRICKS
import org.bukkit.Material.MYCELIUM
import org.bukkit.Material.NETHER_BRICKS
import org.bukkit.Material.NOTE_BLOCK
import org.bukkit.Material.OAK_PLANKS
import org.bukkit.Material.OBSERVER
import org.bukkit.Material.OBSIDIAN
import org.bukkit.Material.OCHRE_FROGLIGHT
import org.bukkit.Material.ORANGE_CONCRETE_POWDER
import org.bukkit.Material.OXIDIZED_COPPER
import org.bukkit.Material.PACKED_ICE
import org.bukkit.Material.PEARLESCENT_FROGLIGHT
import org.bukkit.Material.PINK_CONCRETE_POWDER
import org.bukkit.Material.PISTON
import org.bukkit.Material.PODZOL
import org.bukkit.Material.POLISHED_ANDESITE
import org.bukkit.Material.POLISHED_BASALT
import org.bukkit.Material.POLISHED_BLACKSTONE
import org.bukkit.Material.POLISHED_BLACKSTONE_BRICKS
import org.bukkit.Material.POLISHED_DEEPSLATE
import org.bukkit.Material.POLISHED_DIORITE
import org.bukkit.Material.POLISHED_GRANITE
import org.bukkit.Material.PRISMARINE
import org.bukkit.Material.PRISMARINE_BRICKS
import org.bukkit.Material.PURPLE_CONCRETE_POWDER
import org.bukkit.Material.PURPUR_BLOCK
import org.bukkit.Material.PURPUR_PILLAR
import org.bukkit.Material.QUARTZ_BLOCK
import org.bukkit.Material.QUARTZ_BRICKS
import org.bukkit.Material.QUARTZ_PILLAR
import org.bukkit.Material.REDSTONE
import org.bukkit.Material.REDSTONE_BLOCK
import org.bukkit.Material.REDSTONE_LAMP
import org.bukkit.Material.RED_CONCRETE_POWDER
import org.bukkit.Material.RED_NETHER_BRICKS
import org.bukkit.Material.RED_SANDSTONE
import org.bukkit.Material.ROOTED_DIRT
import org.bukkit.Material.SAND
import org.bukkit.Material.SANDSTONE
import org.bukkit.Material.SEA_LANTERN
import org.bukkit.Material.SHROOMLIGHT
import org.bukkit.Material.SMOOTH_BASALT
import org.bukkit.Material.SMOOTH_QUARTZ
import org.bukkit.Material.SMOOTH_RED_SANDSTONE
import org.bukkit.Material.SMOOTH_SANDSTONE
import org.bukkit.Material.SMOOTH_STONE
import org.bukkit.Material.SNOW_BLOCK
import org.bukkit.Material.SOUL_SAND
import org.bukkit.Material.SOUL_SOIL
import org.bukkit.Material.SPONGE
import org.bukkit.Material.SPRUCE_PLANKS
import org.bukkit.Material.STICKY_PISTON
import org.bukkit.Material.STONE
import org.bukkit.Material.STONE_BRICKS
import org.bukkit.Material.TRAPPED_CHEST
import org.bukkit.Material.TUFF
import org.bukkit.Material.VERDANT_FROGLIGHT
import org.bukkit.Material.WARPED_NYLIUM
import org.bukkit.Material.WARPED_PLANKS
import org.bukkit.Material.WAXED_COPPER_BLOCK
import org.bukkit.Material.WAXED_EXPOSED_COPPER
import org.bukkit.Material.WAXED_OXIDIZED_COPPER
import org.bukkit.Material.WAXED_WEATHERED_COPPER
import org.bukkit.Material.WEATHERED_COPPER
import org.bukkit.Material.WHITE_CONCRETE_POWDER
import org.bukkit.Material.YELLOW_CONCRETE_POWDER
import org.bukkit.block.data.BlockData

object ShipFactoryMaterialCosts {
	var blockprice = mapOf(
		IRON_BLOCK to 0.0,
		DIAMOND_BLOCK to 0.0,
		GOLD_BLOCK to 0.0,
		COPPER_BLOCK to 0.0,
		EXPOSED_COPPER to 0.0,
		WEATHERED_COPPER to 0.0,
		OXIDIZED_COPPER to 0.0,
		WAXED_COPPER_BLOCK to 0.0,
		WAXED_EXPOSED_COPPER to 0.0,
		WAXED_WEATHERED_COPPER to 0.0,
		WAXED_OXIDIZED_COPPER to 0.0,
		REDSTONE_BLOCK to 0.0,

		GRINDSTONE to 8.0,
		END_ROD to 8.0,
		BELL to 8.0,
		SPONGE to 2.0,
		END_PORTAL_FRAME to 8.0,
		PISTON to 2.0,
		STICKY_PISTON to 8.0,
		REDSTONE to 2.0,
		JUKEBOX to 8.0,
		CRAFTING_TABLE to 8.0,
		DROPPER to 8.0,
		HOPPER to 8.0,
		LEVER to 2.0,
		CHEST to 8.0,
		FURNACE to 8.0,
		DISPENSER to 8.0,
		MAGMA_BLOCK to 8.0,
		SEA_LANTERN to 28.0,
		REDSTONE_LAMP to 8.0,
		OCHRE_FROGLIGHT to 8.0,
		VERDANT_FROGLIGHT to 8.0,
		PEARLESCENT_FROGLIGHT to 8.0,
		SHROOMLIGHT to 8.0,
		IRON_TRAPDOOR to 8.0,
		NOTE_BLOCK to 10.0,
		IRON_BARS to 2.0,
		OBSERVER to 8.0,
		TRAPPED_CHEST to 8.0,
		BARREL to 8.0,
		LODESTONE to 8.0,

		OAK_PLANKS to 8.0,
		SPRUCE_PLANKS to 8.0,
		BIRCH_PLANKS to 8.0,
		JUNGLE_PLANKS to 8.0,
		ACACIA_PLANKS to 8.0,
		DARK_OAK_PLANKS to 8.0,
		MANGROVE_PLANKS to 8.0,
		CRIMSON_PLANKS to 8.0,
		WARPED_PLANKS to 8.0,
		STONE to 8.0,
		SMOOTH_STONE to 8.0,
		SANDSTONE to 8.0,
		CUT_SANDSTONE to 8.0,
		COBBLESTONE to 1.0,
		BRICKS to 1.0,
		STONE_BRICKS to 8.0,
		MUD_BRICKS to 8.0,
		NETHER_BRICKS to 8.0,
		QUARTZ_BLOCK to 8.0,
		RED_SANDSTONE to 8.0,
		CUT_RED_SANDSTONE to 8.0,
		PURPUR_BLOCK to 8.0,
		PRISMARINE to 8.0,
		PRISMARINE_BRICKS to 8.0,
		DARK_PRISMARINE to 8.0,
		POLISHED_GRANITE to 8.0,
		SMOOTH_RED_SANDSTONE to 8.0,
		MOSSY_STONE_BRICKS to 8.0,
		POLISHED_DIORITE to 8.0,
		MOSSY_COBBLESTONE to 8.0,
		END_STONE_BRICKS to 8.0,
		SMOOTH_SANDSTONE to 8.0,
		SMOOTH_QUARTZ to 8.0,
		GRANITE to 8.0,
		ANDESITE to 8.0,
		RED_NETHER_BRICKS to 8.0,
		POLISHED_ANDESITE to 8.0,
		DIORITE to 8.0,
		COBBLED_DEEPSLATE to 8.0,
		POLISHED_DEEPSLATE to 8.0,
		DEEPSLATE_BRICKS to 8.0,
		DEEPSLATE_TILES to 8.0,
		BLACKSTONE to 8.0,
		POLISHED_BLACKSTONE to 8.0,
		POLISHED_BLACKSTONE_BRICKS to 8.0,

		CHISELED_DEEPSLATE to 8.0,
		CHISELED_NETHER_BRICKS to 8.0,
		CHISELED_POLISHED_BLACKSTONE to 8.0,
		CHISELED_QUARTZ_BLOCK to 8.0,
		CHISELED_RED_SANDSTONE to 8.0,
		CHISELED_SANDSTONE to 8.0,
		CHISELED_STONE_BRICKS to 8.0,
		QUARTZ_BRICKS to 8.0,
		QUARTZ_PILLAR to 8.0,
		PURPUR_PILLAR to 8.0,
		CRACKED_STONE_BRICKS to 8.0,
		CRACKED_DEEPSLATE_BRICKS to 8.0,
		CRACKED_DEEPSLATE_TILES to 8.0,
		CRACKED_NETHER_BRICKS to 8.0,
		CRACKED_POLISHED_BLACKSTONE_BRICKS to 8.0,

		DIRT to 3.0,
		GRASS_BLOCK to 3.0,
		SHORT_GRASS to 3.0,
		CALCITE to 3.0,
		PODZOL to 3.0,
		MYCELIUM to 3.0,
		COARSE_DIRT to 3.0,
		ROOTED_DIRT to 3.0,
		MUD to 3.0,
		CLAY to 3.0,
		GRAVEL to 3.0,
		SAND to 3.0,
		ICE to 3.0,
		PACKED_ICE to 3.0,
		BLUE_ICE to 3.0,
		SNOW_BLOCK to 3.0,
		MOSS_BLOCK to 3.0,
		TUFF to 3.0,
		DRIPSTONE_BLOCK to 3.0,
		OBSIDIAN to 8.0,
		CRYING_OBSIDIAN to 3.0,
		CRIMSON_NYLIUM to 3.0,
		WARPED_NYLIUM to 3.0,
		SOUL_SAND to 3.0,
		SOUL_SOIL to 3.0,
		BONE_BLOCK to 10.0,
		BASALT to 3.0,
		SMOOTH_BASALT to 8.0,
		END_STONE to 8.1,
		POLISHED_BASALT to 8.0,
		AMETHYST_BLOCK to 8.0,


		WHITE_CONCRETE_POWDER to 3.0,
		LIGHT_GRAY_CONCRETE_POWDER to 3.0,
		GRAY_CONCRETE_POWDER to 3.0,
		BLACK_CONCRETE_POWDER to 3.0,
		RED_CONCRETE_POWDER to 3.0,
		ORANGE_CONCRETE_POWDER to 3.0,
		YELLOW_CONCRETE_POWDER to 3.0,
		GREEN_CONCRETE_POWDER to 3.0,
		LIME_CONCRETE_POWDER to 3.0,
		CYAN_CONCRETE_POWDER to 3.0,
		LIGHT_BLUE_CONCRETE_POWDER to 3.0,
		BLUE_CONCRETE_POWDER to 3.0,
		PURPLE_CONCRETE_POWDER to 3.0,
		MAGENTA_CONCRETE_POWDER to 3.0,
		PINK_CONCRETE_POWDER to 3.0,
		BROWN_CONCRETE_POWDER to 3.0

	)

	fun getPrice(data: BlockData): Double {

		val customBlockKey = data.customBlock?.key

		when (customBlockKey) {
			CustomBlockKeys.TITANIUM_BLOCK -> return 0.0
			CustomBlockKeys.TITANIUM_ORE -> return 0.0
			CustomBlockKeys.RAW_TITANIUM_BLOCK -> return 0.0
			CustomBlockKeys.URANIUM_BLOCK -> return 0.0
			CustomBlockKeys.URANIUM_ORE -> return 0.0
			CustomBlockKeys.RAW_URANIUM_BLOCK -> return 0.0
			CustomBlockKeys.ENRICHED_URANIUM_BLOCK -> return 0.0
			CustomBlockKeys.CHETHERITE_BLOCK -> return 0.0
			CustomBlockKeys.CHETHERITE_ORE -> return 0.0
			CustomBlockKeys.ALUMINUM_BLOCK -> return 0.0
			CustomBlockKeys.ALUMINUM_ORE -> return 0.0
			CustomBlockKeys.RAW_ALUMINUM_BLOCK -> return 0.0
			CustomBlockKeys.ATAVUM_BLOCK -> return 0.0
			CustomBlockKeys.ATAVUM_ORE -> return 0.0
			CustomBlockKeys.VANADIUM_BLOCK -> return 0.0
			CustomBlockKeys.VANADIUM_ORE -> return 0.0
			CustomBlockKeys.ZIRCON_BLOCK -> return 0.0
			CustomBlockKeys.ZIRCON_ORE -> return 0.0
			CustomBlockKeys.SCORDITE_BLOCK -> return 0.0
			CustomBlockKeys.SCORDITE_ORE -> return 0.0
			CustomBlockKeys.STEEL_BLOCK -> return 0.0
			CustomBlockKeys.SUPERCONDUCTOR_BLOCK -> return 0.0
			CustomBlockKeys.NETHERITE_CASING -> return 0.0
		}

		return when (data) {
			else ->
				if (data.material.isGlass) {
					4.0
				} else if (data.material.isConcrete) {
					4.0
				} else if (data.material.isGlassPane) {
					4.0
				} else if (data.material.isSlab) {
					10.0
				} else if (data.material.isStairs) {
					10.0
				} else if (data.material.isStainedTerracotta) {
					10.0
				} else if (data.material.isGlazedTerracotta) {
					10.0
				} else if (data.material.isConcrete) {
					8.0
				} else if (data.material.isSign) {
					18.0
				} else if (data.material.isWallSign) {
					18.0
				} else if (data.material.isWall) {
					10.0
				} else if (blockprice.containsKey(data.material)) {
					blockprice[data.material]!!.toDouble()
				} else {
					10.0
				}
		}

	}
}
