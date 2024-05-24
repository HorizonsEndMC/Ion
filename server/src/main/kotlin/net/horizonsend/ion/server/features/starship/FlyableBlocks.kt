package net.horizonsend.ion.server.features.starship

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import net.horizonsend.ion.server.miscellaneous.utils.BANNER_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.BED_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.BUTTON_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.CAKE_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.CANDLE_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.CARPET_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.CONCRETE_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.DOOR_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.FENCE_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.GLAZED_TERRACOTTA_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.PRESSURE_PLATE_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.SHULKER_BOX_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.SIGN_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.SLAB_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.STAINED_GLASS_PANE_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.STAINED_GLASS_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.STAINED_TERRACOTTA_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.STAIR_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.TRAPDOOR_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.WALL_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.WOOL_TYPES
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Material
import org.bukkit.Material.ACACIA_FENCE_GATE
import org.bukkit.Material.ANVIL
import org.bukkit.Material.BARREL
import org.bukkit.Material.BEEHIVE
import org.bukkit.Material.BEETROOTS
import org.bukkit.Material.BELL
import org.bukkit.Material.BIRCH_FENCE_GATE
import org.bukkit.Material.BLAST_FURNACE
import org.bukkit.Material.BOOKSHELF
import org.bukkit.Material.BREWING_STAND
import org.bukkit.Material.BROWN_MUSHROOM_BLOCK
import org.bukkit.Material.CAMPFIRE
import org.bukkit.Material.CARROTS
import org.bukkit.Material.CARTOGRAPHY_TABLE
import org.bukkit.Material.CAULDRON
import org.bukkit.Material.CHAIN
import org.bukkit.Material.CHEST
import org.bukkit.Material.CHIPPED_ANVIL
import org.bukkit.Material.COMPARATOR
import org.bukkit.Material.COMPOSTER
import org.bukkit.Material.COPPER_BLOCK
import org.bukkit.Material.CRAFTING_TABLE
import org.bukkit.Material.CREEPER_HEAD
import org.bukkit.Material.CREEPER_WALL_HEAD
import org.bukkit.Material.CRIMSON_FENCE_GATE
import org.bukkit.Material.DAMAGED_ANVIL
import org.bukkit.Material.DARK_OAK_FENCE_GATE
import org.bukkit.Material.DAYLIGHT_DETECTOR
import org.bukkit.Material.DECORATED_POT
import org.bukkit.Material.DIAMOND_BLOCK
import org.bukkit.Material.DISPENSER
import org.bukkit.Material.DRAGON_HEAD
import org.bukkit.Material.DRAGON_WALL_HEAD
import org.bukkit.Material.DROPPER
import org.bukkit.Material.EMERALD_BLOCK
import org.bukkit.Material.ENDER_CHEST
import org.bukkit.Material.END_PORTAL_FRAME
import org.bukkit.Material.END_ROD
import org.bukkit.Material.EXPOSED_COPPER
import org.bukkit.Material.FARMLAND
import org.bukkit.Material.FLETCHING_TABLE
import org.bukkit.Material.FLOWER_POT
import org.bukkit.Material.FURNACE
import org.bukkit.Material.GLASS
import org.bukkit.Material.GLASS_PANE
import org.bukkit.Material.GLOWSTONE
import org.bukkit.Material.GOLD_BLOCK
import org.bukkit.Material.GRINDSTONE
import org.bukkit.Material.HAY_BLOCK
import org.bukkit.Material.HOPPER
import org.bukkit.Material.IRON_BARS
import org.bukkit.Material.IRON_BLOCK
import org.bukkit.Material.JUKEBOX
import org.bukkit.Material.JUNGLE_FENCE_GATE
import org.bukkit.Material.LADDER
import org.bukkit.Material.LANTERN
import org.bukkit.Material.LAPIS_BLOCK
import org.bukkit.Material.LECTERN
import org.bukkit.Material.LEVER
import org.bukkit.Material.LIGHTNING_ROD
import org.bukkit.Material.LODESTONE
import org.bukkit.Material.LOOM
import org.bukkit.Material.MAGMA_BLOCK
import org.bukkit.Material.MANGROVE_FENCE_GATE
import org.bukkit.Material.MOVING_PISTON
import org.bukkit.Material.NETHERITE_BLOCK
import org.bukkit.Material.NETHER_PORTAL
import org.bukkit.Material.NOTE_BLOCK
import org.bukkit.Material.OAK_FENCE_GATE
import org.bukkit.Material.OBSERVER
import org.bukkit.Material.OCHRE_FROGLIGHT
import org.bukkit.Material.OXIDIZED_COPPER
import org.bukkit.Material.PEARLESCENT_FROGLIGHT
import org.bukkit.Material.PISTON
import org.bukkit.Material.PISTON_HEAD
import org.bukkit.Material.PLAYER_HEAD
import org.bukkit.Material.PLAYER_WALL_HEAD
import org.bukkit.Material.POTATOES
import org.bukkit.Material.POTTED_AZURE_BLUET
import org.bukkit.Material.POTTED_BAMBOO
import org.bukkit.Material.POTTED_BIRCH_SAPLING
import org.bukkit.Material.POTTED_BLUE_ORCHID
import org.bukkit.Material.POTTED_BROWN_MUSHROOM
import org.bukkit.Material.POTTED_CACTUS
import org.bukkit.Material.POTTED_CORNFLOWER
import org.bukkit.Material.POTTED_CRIMSON_FUNGUS
import org.bukkit.Material.POTTED_CRIMSON_ROOTS
import org.bukkit.Material.POTTED_DANDELION
import org.bukkit.Material.POTTED_DARK_OAK_SAPLING
import org.bukkit.Material.POTTED_DEAD_BUSH
import org.bukkit.Material.POTTED_FERN
import org.bukkit.Material.POTTED_FLOWERING_AZALEA_BUSH
import org.bukkit.Material.POTTED_JUNGLE_SAPLING
import org.bukkit.Material.POTTED_LILY_OF_THE_VALLEY
import org.bukkit.Material.POTTED_OAK_SAPLING
import org.bukkit.Material.POTTED_ORANGE_TULIP
import org.bukkit.Material.POTTED_OXEYE_DAISY
import org.bukkit.Material.POTTED_PINK_TULIP
import org.bukkit.Material.POTTED_POPPY
import org.bukkit.Material.POTTED_RED_MUSHROOM
import org.bukkit.Material.POTTED_RED_TULIP
import org.bukkit.Material.POTTED_SPRUCE_SAPLING
import org.bukkit.Material.POTTED_WARPED_FUNGUS
import org.bukkit.Material.POTTED_WARPED_ROOTS
import org.bukkit.Material.POTTED_WHITE_TULIP
import org.bukkit.Material.POTTED_WITHER_ROSE
import org.bukkit.Material.REDSTONE_BLOCK
import org.bukkit.Material.REDSTONE_LAMP
import org.bukkit.Material.REDSTONE_TORCH
import org.bukkit.Material.REDSTONE_WALL_TORCH
import org.bukkit.Material.REDSTONE_WIRE
import org.bukkit.Material.REPEATER
import org.bukkit.Material.SCAFFOLDING
import org.bukkit.Material.SCULK
import org.bukkit.Material.SEA_LANTERN
import org.bukkit.Material.SHROOMLIGHT
import org.bukkit.Material.SKELETON_SKULL
import org.bukkit.Material.SKELETON_WALL_SKULL
import org.bukkit.Material.SMITHING_TABLE
import org.bukkit.Material.SMOKER
import org.bukkit.Material.SOUL_CAMPFIRE
import org.bukkit.Material.SOUL_LANTERN
import org.bukkit.Material.SOUL_TORCH
import org.bukkit.Material.SPONGE
import org.bukkit.Material.SPRUCE_FENCE_GATE
import org.bukkit.Material.STICKY_PISTON
import org.bukkit.Material.STONECUTTER
import org.bukkit.Material.TARGET
import org.bukkit.Material.TERRACOTTA
import org.bukkit.Material.TORCH
import org.bukkit.Material.TRAPPED_CHEST
import org.bukkit.Material.VERDANT_FROGLIGHT
import org.bukkit.Material.WALL_TORCH
import org.bukkit.Material.WARPED_FENCE_GATE
import org.bukkit.Material.WAXED_COPPER_BLOCK
import org.bukkit.Material.WAXED_EXPOSED_COPPER
import org.bukkit.Material.WAXED_OXIDIZED_COPPER
import org.bukkit.Material.WAXED_WEATHERED_COPPER
import org.bukkit.Material.WEATHERED_COPPER
import org.bukkit.Material.WET_SPONGE
import org.bukkit.Material.WHEAT
import org.bukkit.Material.WITHER_SKELETON_SKULL
import org.bukkit.Material.WITHER_SKELETON_WALL_SKULL
import org.bukkit.Material.ZOMBIE_HEAD
import java.util.EnumSet

// For help in searching:
// Pilotable Blocks
// Detectable Blocks
val FLYABLE_BLOCKS: EnumSet<Material> = mutableSetOf(
	JUKEBOX, // ship computer
	NOTE_BLOCK, // used as power input/output for machines

	SPONGE, // used for lots of ship subsystems, esp. weapons
	WET_SPONGE,

	GLASS,
	GLASS_PANE,
	IRON_BARS,

	// all 4used as thrusters
	SEA_LANTERN,
	GLOWSTONE,
	REDSTONE_LAMP,
	MAGMA_BLOCK,

	DIAMOND_BLOCK,
	REDSTONE_BLOCK,
	GOLD_BLOCK,
	LAPIS_BLOCK,
	IRON_BLOCK,
	EMERALD_BLOCK,
	BROWN_MUSHROOM_BLOCK, // custom ores

	// used for landing gears
	PISTON,
	PISTON_HEAD,
	MOVING_PISTON,
	STICKY_PISTON, // used for crate holders

	CHEST,
	ENDER_CHEST,
	TRAPPED_CHEST,
	FURNACE,
	DROPPER,
	HOPPER,
	DISPENSER,
	DECORATED_POT,

	// misc stuff
	TORCH,
	WALL_TORCH,
	CRAFTING_TABLE,
	END_ROD,
	LEVER,
	FLOWER_POT,
	CAULDRON,
	ANVIL,
	BOOKSHELF,
	LADDER,
	DAYLIGHT_DETECTOR,
	NETHER_PORTAL,

	OBSERVER,
	REPEATER,
	COMPARATOR,
	REDSTONE_WIRE,
	REDSTONE_TORCH,
	REDSTONE_WALL_TORCH,

	LODESTONE,
	BREWING_STAND,
	LECTERN,
	TARGET,

	END_PORTAL_FRAME,

	SHROOMLIGHT,
	BELL,
	GRINDSTONE,
	BARREL,
	SCAFFOLDING,
	CHAIN,

	COPPER_BLOCK,
	EXPOSED_COPPER,
	WEATHERED_COPPER,
	OXIDIZED_COPPER,
	WAXED_COPPER_BLOCK,
	WAXED_EXPOSED_COPPER,
	WAXED_WEATHERED_COPPER,
	WAXED_OXIDIZED_COPPER,

	POTTED_AZURE_BLUET,
	POTTED_BAMBOO,
	POTTED_BIRCH_SAPLING,
	POTTED_BLUE_ORCHID,
	POTTED_BROWN_MUSHROOM,
	POTTED_CACTUS,
	POTTED_CORNFLOWER,
	POTTED_CRIMSON_FUNGUS,
	POTTED_CRIMSON_ROOTS,
	POTTED_DANDELION,
	POTTED_DARK_OAK_SAPLING,
	POTTED_DEAD_BUSH,
	POTTED_FERN,
	POTTED_FLOWERING_AZALEA_BUSH,
	POTTED_JUNGLE_SAPLING,
	POTTED_LILY_OF_THE_VALLEY,
	POTTED_OAK_SAPLING,
	POTTED_ORANGE_TULIP,
	POTTED_OXEYE_DAISY,
	POTTED_PINK_TULIP,
	POTTED_POPPY,
	POTTED_RED_MUSHROOM,
	POTTED_RED_TULIP,
	POTTED_SPRUCE_SAPLING,
	POTTED_WARPED_FUNGUS,
	POTTED_WARPED_ROOTS,
	POTTED_WHITE_TULIP,
	POTTED_WITHER_ROSE,

	SCULK,

	PLAYER_HEAD,
	PLAYER_WALL_HEAD,
	DRAGON_HEAD,
	DRAGON_WALL_HEAD,
	CREEPER_HEAD,
	CREEPER_WALL_HEAD,
	ZOMBIE_HEAD,
	WITHER_SKELETON_SKULL,
	WITHER_SKELETON_WALL_SKULL,
	SKELETON_SKULL,
	SKELETON_WALL_SKULL,

	DAMAGED_ANVIL,
	CHIPPED_ANVIL,

	BEEHIVE,
	SMITHING_TABLE,
	STONECUTTER,
	CAMPFIRE,
	SOUL_CAMPFIRE,
	LANTERN,
	SOUL_LANTERN,
	SOUL_TORCH,
	LOOM,
	COMPOSTER,
	SMOKER,
	BLAST_FURNACE,
	CARTOGRAPHY_TABLE,
	FLETCHING_TABLE,
	OCHRE_FROGLIGHT,
	PEARLESCENT_FROGLIGHT,
	VERDANT_FROGLIGHT,

	OAK_FENCE_GATE,
	BIRCH_FENCE_GATE,
	SPRUCE_FENCE_GATE,
	JUNGLE_FENCE_GATE,
	ACACIA_FENCE_GATE,
	DARK_OAK_FENCE_GATE,
	MANGROVE_FENCE_GATE,
	CRIMSON_FENCE_GATE,
	WARPED_FENCE_GATE,

	LIGHTNING_ROD,
	NETHERITE_BLOCK,

	TERRACOTTA,

	FARMLAND,
	WHEAT,
	CARROTS,
	POTATOES,
	BEETROOTS,
	HAY_BLOCK,

).also {
	it.addAll(CONCRETE_TYPES)
	it.addAll(SLAB_TYPES)
	it.addAll(STAIR_TYPES)
	it.addAll(GLAZED_TERRACOTTA_TYPES)
	it.addAll(STAINED_TERRACOTTA_TYPES)
	it.addAll(WOOL_TYPES)
	it.addAll(CARPET_TYPES)
	it.addAll(STAINED_GLASS_TYPES)
	it.addAll(STAINED_GLASS_PANE_TYPES)
	it.addAll(SHULKER_BOX_TYPES)
	it.addAll(SIGN_TYPES)
	it.addAll(BUTTON_TYPES)
	it.addAll(BANNER_TYPES)
	it.addAll(DOOR_TYPES)
	it.addAll(TRAPDOOR_TYPES)
	it.addAll(PRESSURE_PLATE_TYPES)
	it.addAll(BED_TYPES)
	it.addAll(FENCE_TYPES)
	it.addAll(WALL_TYPES)
	it.addAll(CANDLE_TYPES)
	it.addAll(CAKE_TYPES)
}.filter { it.isBlock }.toCollection(EnumSet.noneOf(Material::class.java))

private val FLYABLE_BLOCK_DATA_CACHE = CacheBuilder.newBuilder()
	.build<BlockState, Boolean>(
		CacheLoader.from { blockData ->
			return@from blockData != null && FLYABLE_BLOCKS.contains(blockData.bukkitMaterial)
		}
	)

fun isFlyable(blockData: BlockState) = FLYABLE_BLOCK_DATA_CACHE[blockData]
