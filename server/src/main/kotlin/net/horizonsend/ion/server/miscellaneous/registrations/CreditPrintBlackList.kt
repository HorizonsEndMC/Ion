package net.horizonsend.ion.server.miscellaneous.registrations

import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys
import net.horizonsend.ion.server.core.registration.registries.CustomBlockRegistry.Companion.customBlock
import net.horizonsend.ion.server.miscellaneous.utils.isBed
import net.horizonsend.ion.server.miscellaneous.utils.isButton
import net.horizonsend.ion.server.miscellaneous.utils.isCandle
import net.horizonsend.ion.server.miscellaneous.utils.isCarpet
import net.horizonsend.ion.server.miscellaneous.utils.isConcrete
import net.horizonsend.ion.server.miscellaneous.utils.isDoor
import net.horizonsend.ion.server.miscellaneous.utils.isFence
import net.horizonsend.ion.server.miscellaneous.utils.isGlass
import net.horizonsend.ion.server.miscellaneous.utils.isGlassPane
import net.horizonsend.ion.server.miscellaneous.utils.isGlazedTerracotta
import net.horizonsend.ion.server.miscellaneous.utils.isLeaves
import net.horizonsend.ion.server.miscellaneous.utils.isLog
import net.horizonsend.ion.server.miscellaneous.utils.isSign
import net.horizonsend.ion.server.miscellaneous.utils.isSlab
import net.horizonsend.ion.server.miscellaneous.utils.isStainedGlass
import net.horizonsend.ion.server.miscellaneous.utils.isStainedGlassPane
import net.horizonsend.ion.server.miscellaneous.utils.isStainedTerracotta
import net.horizonsend.ion.server.miscellaneous.utils.isStairs
import net.horizonsend.ion.server.miscellaneous.utils.isTintedGlass
import net.horizonsend.ion.server.miscellaneous.utils.isTrapdoor
import net.horizonsend.ion.server.miscellaneous.utils.isWall
import net.horizonsend.ion.server.miscellaneous.utils.isWallSign
import net.horizonsend.ion.server.miscellaneous.utils.isWood
import org.bukkit.Material
import org.bukkit.block.data.BlockData

object CreditPrintBlackList {
	var creditPrintBlackList = setOf(
		Material.IRON_BLOCK,
		Material.DIAMOND_BLOCK,
		Material.OXIDIZED_COPPER,
		Material.COPPER_BLOCK,
		Material.EXPOSED_COPPER,
		Material.WEATHERED_COPPER,
		Material.WAXED_COPPER_BLOCK,
		Material.WAXED_EXPOSED_COPPER,
		Material.WAXED_WEATHERED_COPPER,
		Material.WAXED_OXIDIZED_COPPER,
		Material.REDSTONE_BLOCK,

		Material.END_PORTAL_FRAME,
		Material.REDSTONE,
		Material.DIAMOND_ORE,
		Material.GOLD_ORE,
		Material.COPPER_ORE,
		Material.IRON_ORE,
		Material.EMERALD_ORE,
		Material.DEEPSLATE_COAL_ORE,
		Material.COAL_ORE,
		Material.DEEPSLATE_COPPER_ORE,
		Material.DEEPSLATE_DIAMOND_ORE,
		Material.DEEPSLATE_EMERALD_ORE,
		Material.DEEPSLATE_GOLD_ORE,
		Material.DEEPSLATE_REDSTONE_ORE,
		Material.REDSTONE_ORE,
		Material.DEEPSLATE_IRON_ORE,
		Material.LAPIS_ORE,
		Material.NETHER_GOLD_ORE,

		Material.RAW_GOLD_BLOCK,
		Material.RAW_IRON_BLOCK,
		Material.RAW_COPPER_BLOCK,

		Material.BEDROCK,
		Material.BARRIER,
		Material.REINFORCED_DEEPSLATE,
		Material.BEACON,
		Material.GOLD_BLOCK,
		Material.WITHER_SKELETON_SKULL,
		Material.VAULT,
		Material.SPAWNER,
		Material.DRAGON_EGG,
		Material.DRAGON_HEAD,
		Material.EMERALD_BLOCK

	)

	var creditPrintWhiteList = setOf(
		Material.GRINDSTONE,
		Material.END_ROD,
		Material.BELL,
		Material.SPONGE,
		Material.PISTON,
		Material.STICKY_PISTON,
		Material.JUKEBOX,
		Material.CRAFTING_TABLE,
		Material.DROPPER,
		Material.HOPPER,
		Material.LEVER,
		Material.CHEST,
		Material.FURNACE,
		Material.DISPENSER,
		Material.MAGMA_BLOCK,
		Material.SEA_LANTERN,
		Material.REDSTONE_LAMP,
		Material.OCHRE_FROGLIGHT,
		Material.VERDANT_FROGLIGHT,
		Material.PEARLESCENT_FROGLIGHT,
		Material.SHROOMLIGHT,
		Material.IRON_TRAPDOOR,
		Material.NOTE_BLOCK,
		Material.IRON_BARS,
		Material.OBSERVER,
		Material.TRAPPED_CHEST,
		Material.BARREL,
		Material.LODESTONE,

		Material.OAK_PLANKS,
		Material.SPRUCE_PLANKS,
		Material.BIRCH_PLANKS,
		Material.JUNGLE_PLANKS,
		Material.ACACIA_PLANKS,
		Material.DARK_OAK_PLANKS,
		Material.MANGROVE_PLANKS,
		Material.CRIMSON_PLANKS,
		Material.WARPED_PLANKS,
		Material.STONE,
		Material.SMOOTH_STONE,
		Material.SANDSTONE,
		Material.CUT_SANDSTONE,
		Material.COBBLESTONE,
		Material.BRICKS,
		Material.STONE_BRICKS,
		Material.MUD_BRICKS,
		Material.NETHER_BRICKS,
		Material.QUARTZ_BLOCK,
		Material.RED_SANDSTONE,
		Material.CUT_RED_SANDSTONE,
		Material.PURPUR_BLOCK,
		Material.PRISMARINE,
		Material.PRISMARINE_BRICKS,
		Material.DARK_PRISMARINE,
		Material.POLISHED_GRANITE,
		Material.SMOOTH_RED_SANDSTONE,
		Material.MOSSY_STONE_BRICKS,
		Material.POLISHED_DIORITE,
		Material.MOSSY_COBBLESTONE,
		Material.END_STONE_BRICKS,
		Material.SMOOTH_SANDSTONE,
		Material.SMOOTH_QUARTZ,
		Material.GRANITE,
		Material.ANDESITE,
		Material.RED_NETHER_BRICKS,
		Material.POLISHED_ANDESITE,
		Material.DIORITE,
		Material.COBBLED_DEEPSLATE,
		Material.POLISHED_DEEPSLATE,
		Material.DEEPSLATE_BRICKS,
		Material.DEEPSLATE_TILES,
		Material.BLACKSTONE,
		Material.POLISHED_BLACKSTONE,
		Material.POLISHED_BLACKSTONE_BRICKS,

		Material.CHISELED_DEEPSLATE,
		Material.CHISELED_NETHER_BRICKS,
		Material.CHISELED_POLISHED_BLACKSTONE,
		Material.CHISELED_QUARTZ_BLOCK,
		Material.CHISELED_RED_SANDSTONE,
		Material.CHISELED_SANDSTONE,
		Material.CHISELED_STONE_BRICKS,
		Material.QUARTZ_BRICKS,
		Material.QUARTZ_PILLAR,
		Material.PURPUR_PILLAR,
		Material.CRACKED_STONE_BRICKS,
		Material.CRACKED_DEEPSLATE_BRICKS,
		Material.CRACKED_DEEPSLATE_TILES,
		Material.CRACKED_NETHER_BRICKS,
		Material.CRACKED_POLISHED_BLACKSTONE_BRICKS,

		Material.DIRT,
		Material.GRASS_BLOCK,
		Material.SHORT_GRASS,
		Material.CALCITE,
		Material.PODZOL,
		Material.MYCELIUM,
		Material.COARSE_DIRT,
		Material.ROOTED_DIRT,
		Material.MUD,
		Material.CLAY,
		Material.GRAVEL,
		Material.SAND,
		Material.ICE,
		Material.PACKED_ICE,
		Material.BLUE_ICE,
		Material.SNOW_BLOCK,
		Material.MOSS_BLOCK,
		Material.TUFF,
		Material.DRIPSTONE_BLOCK,
		Material.OBSIDIAN,
		Material.CRYING_OBSIDIAN,
		Material.CRIMSON_NYLIUM,
		Material.WARPED_NYLIUM,
		Material.SOUL_SAND,
		Material.SOUL_SOIL,
		Material.BONE_BLOCK,
		Material.BASALT,
		Material.SMOOTH_BASALT,
		Material.END_STONE,
		Material.POLISHED_BASALT,
		Material.AMETHYST_BLOCK,

		Material.WHITE_CONCRETE_POWDER,
		Material.LIGHT_GRAY_CONCRETE_POWDER,
		Material.GRAY_CONCRETE_POWDER,
		Material.BLACK_CONCRETE_POWDER,
		Material.RED_CONCRETE_POWDER,
		Material.ORANGE_CONCRETE_POWDER,
		Material.YELLOW_CONCRETE_POWDER,
		Material.GREEN_CONCRETE_POWDER,
		Material.LIME_CONCRETE_POWDER,
		Material.CYAN_CONCRETE_POWDER,
		Material.LIGHT_BLUE_CONCRETE_POWDER,
		Material.BLUE_CONCRETE_POWDER,
		Material.PURPLE_CONCRETE_POWDER,
		Material.MAGENTA_CONCRETE_POWDER,
		Material.PINK_CONCRETE_POWDER,
		Material.BROWN_CONCRETE_POWDER,
	)

	fun isInBlacklist(data: BlockData): Boolean {
		val customBlockKey = data.customBlock?.key
		val material = data.material
		if (creditPrintBlackList.contains(material)) return true

		when (customBlockKey) {
			CustomBlockKeys.TITANIUM_BLOCK -> return true
			CustomBlockKeys.URANIUM_BLOCK -> return true
			CustomBlockKeys.CHETHERITE_BLOCK -> return true
			CustomBlockKeys.ALUMINUM_BLOCK -> return true
			CustomBlockKeys.RAW_ALUMINUM_BLOCK -> return true
			CustomBlockKeys.RAW_TITANIUM_BLOCK -> return true
			CustomBlockKeys.RAW_URANIUM_BLOCK -> return true
			CustomBlockKeys.TITANIUM_ORE -> return true
			CustomBlockKeys.URANIUM_ORE -> return true
			CustomBlockKeys.ALUMINUM_ORE -> return true
			CustomBlockKeys.CHETHERITE_ORE -> return true
			CustomBlockKeys.ATAVUM_BLOCK -> return true
			CustomBlockKeys.ATAVUM_ORE -> return true
			CustomBlockKeys.ZIRCON_BLOCK -> return true
			CustomBlockKeys.ZIRCON_ORE -> return true
			CustomBlockKeys.SCORDITE_BLOCK -> return true
			CustomBlockKeys.SCORDITE_ORE -> return true
			CustomBlockKeys.VANADIUM_BLOCK -> return true
			CustomBlockKeys.VANADIUM_ORE -> return true
			CustomBlockKeys.ASSEMBLY_CORE -> return true
			CustomBlockKeys.BATTLECRUISER_REACTOR_CORE -> return true
			CustomBlockKeys.BARGE_REACTOR_CORE -> return true
			CustomBlockKeys.CRUISER_REACTOR_CORE -> return true
			CustomBlockKeys.MINI_REACTOR_CORE -> return true
			CustomBlockKeys.SMALL_REACTOR_CORE -> return true
			CustomBlockKeys.MEDIUM_REACTOR_CORE -> return true
			CustomBlockKeys.LARGE_REACTOR_CORE -> return true
			CustomBlockKeys.STEEL_BLOCK -> return true
			CustomBlockKeys.NETHERITE_CASING -> return true
			CustomBlockKeys.ENRICHED_URANIUM_BLOCK -> return true
		}
		return false
	}

	fun isInWhitelist(data: BlockData): Boolean {
		val customBlockKey = data.customBlock?.key
		val material = data.material
		if (creditPrintWhiteList.contains(material)) return true

		if (data.material.isGlass
			|| data.material.isGlassPane
			|| data.material.isStainedGlass
			|| data.material.isStainedGlassPane
			|| data.material.isConcrete
			|| data.material.isSlab
			|| data.material.isStairs
			|| data.material.isStainedTerracotta
			|| data.material.isGlazedTerracotta
			|| data.material.isConcrete
			|| data.material.isSign
			|| data.material.isWallSign
			|| data.material.isWall
			|| data.material.isTintedGlass
			|| data.material.isButton
			|| data.material.isCandle
			|| data.material.isDoor
			|| data.material.isTrapdoor
			|| data.material.isLeaves
			|| data.material.isLog
			|| data.material.isWood
			|| data.material.isCarpet
			|| data.material.isBed
			|| data.material.isFence) return true

		return false
	}
}
