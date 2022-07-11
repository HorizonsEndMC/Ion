package net.starlegacy.feature.starship

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import java.util.EnumSet
import net.minecraft.world.level.block.state.BlockState
import net.starlegacy.util.BANNER_TYPES
import net.starlegacy.util.BED_TYPES
import net.starlegacy.util.BUTTON_TYPES
import net.starlegacy.util.CAKE_TYPES
import net.starlegacy.util.CANDLE_TYPES
import net.starlegacy.util.CARPET_TYPES
import net.starlegacy.util.CONCRETE_TYPES
import net.starlegacy.util.DOOR_TYPES
import net.starlegacy.util.FENCE_TYPES
import net.starlegacy.util.GLAZED_TERRACOTTA_TYPES
import net.starlegacy.util.PLANKS_TYPES
import net.starlegacy.util.PRESSURE_PLATE_TYPES
import net.starlegacy.util.SHULKER_BOX_TYPES
import net.starlegacy.util.SIGN_TYPES
import net.starlegacy.util.SLAB_TYPES
import net.starlegacy.util.STAINED_GLASS_PANE_TYPES
import net.starlegacy.util.STAINED_GLASS_TYPES
import net.starlegacy.util.STAINED_TERRACOTTA_TYPES
import net.starlegacy.util.STAIR_TYPES
import net.starlegacy.util.TRAPDOOR_TYPES
import net.starlegacy.util.WALL_TYPES
import net.starlegacy.util.WOOL_TYPES
import org.bukkit.Material
import org.bukkit.Material.ANVIL
import org.bukkit.Material.BARREL
import org.bukkit.Material.BELL
import org.bukkit.Material.BOOKSHELF
import org.bukkit.Material.BREWING_STAND
import org.bukkit.Material.BROWN_MUSHROOM_BLOCK
import org.bukkit.Material.CAULDRON
import org.bukkit.Material.CHAIN
import org.bukkit.Material.CHEST
import org.bukkit.Material.COMPARATOR
import org.bukkit.Material.COPPER_BLOCK
import org.bukkit.Material.CRAFTING_TABLE
import org.bukkit.Material.DAYLIGHT_DETECTOR
import org.bukkit.Material.DIAMOND_BLOCK
import org.bukkit.Material.DISPENSER
import org.bukkit.Material.DROPPER
import org.bukkit.Material.EMERALD_BLOCK
import org.bukkit.Material.ENDER_CHEST
import org.bukkit.Material.END_PORTAL_FRAME
import org.bukkit.Material.END_ROD
import org.bukkit.Material.EXPOSED_COPPER
import org.bukkit.Material.FLOWER_POT
import org.bukkit.Material.FURNACE
import org.bukkit.Material.GLASS
import org.bukkit.Material.GLASS_PANE
import org.bukkit.Material.GLOWSTONE
import org.bukkit.Material.GOLD_BLOCK
import org.bukkit.Material.GRINDSTONE
import org.bukkit.Material.HOPPER
import org.bukkit.Material.IRON_BARS
import org.bukkit.Material.IRON_BLOCK
import org.bukkit.Material.JUKEBOX
import org.bukkit.Material.LADDER
import org.bukkit.Material.LAPIS_BLOCK
import org.bukkit.Material.LECTERN
import org.bukkit.Material.LEVER
import org.bukkit.Material.LODESTONE
import org.bukkit.Material.MAGMA_BLOCK
import org.bukkit.Material.MOVING_PISTON
import org.bukkit.Material.NETHER_PORTAL
import org.bukkit.Material.NOTE_BLOCK
import org.bukkit.Material.OBSERVER
import org.bukkit.Material.OXIDIZED_COPPER
import org.bukkit.Material.PISTON
import org.bukkit.Material.PISTON_HEAD
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
import org.bukkit.Material.SEA_LANTERN
import org.bukkit.Material.SHROOMLIGHT
import org.bukkit.Material.SPONGE
import org.bukkit.Material.STICKY_PISTON
import org.bukkit.Material.TORCH
import org.bukkit.Material.TRAPPED_CHEST
import org.bukkit.Material.WALL_TORCH
import org.bukkit.Material.WAXED_COPPER_BLOCK
import org.bukkit.Material.WAXED_EXPOSED_COPPER
import org.bukkit.Material.WAXED_OXIDIZED_COPPER
import org.bukkit.Material.WAXED_WEATHERED_COPPER
import org.bukkit.Material.WEATHERED_COPPER

val FLYABLE_BLOCKS: EnumSet<Material> = mutableSetOf(
	JUKEBOX, // ship computer
	NOTE_BLOCK, // used as power input/output for machines

	SPONGE, // used for lots of ship subsystems, esp. weapons

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
	it.addAll(PLANKS_TYPES)
	it.addAll(CANDLE_TYPES)
	it.addAll(CAKE_TYPES)

}.filter { it.isBlock }.toCollection(EnumSet.noneOf(Material::class.java))

private val FLYABLE_BLOCK_DATA_CACHE = CacheBuilder.newBuilder()
	.build<BlockState, Boolean>(CacheLoader.from { blockData ->
		return@from blockData != null && FLYABLE_BLOCKS.contains(blockData.bukkitMaterial)
	})

fun isFlyable(blockData: BlockState) = FLYABLE_BLOCK_DATA_CACHE[blockData]
