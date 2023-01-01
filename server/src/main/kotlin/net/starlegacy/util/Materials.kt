package net.starlegacy.util

import org.bukkit.Material
import java.util.EnumSet

/**
 * This should be used instead of Material.values() to avoid encountering legacy materials
 */
val MATERIALS = Material.values().filterNot { it.isLegacy }

fun getMatchingMaterials(filter: (Material) -> Boolean): EnumSet<Material> =
	MATERIALS.filterTo(EnumSet.noneOf(Material::class.java), filter)

val STAINED_GLASS_TYPES = getMatchingMaterials { it.name.endsWith("_STAINED_GLASS") }
val Material.isGlass: Boolean get() = this == Material.GLASS || this.isStainedGlass
val Material.isStainedGlass: Boolean get() = STAINED_GLASS_TYPES.contains(this)

val STAINED_GLASS_PANE_TYPES = getMatchingMaterials { it.name.endsWith("_STAINED_GLASS_PANE") }
val Material.isGlassPane: Boolean get() = this == Material.GLASS_PANE || this.isStainedGlassPane
val Material.isStainedGlassPane: Boolean get() = STAINED_GLASS_PANE_TYPES.contains(this)

val Material.isLava: Boolean get() = this == Material.LAVA

val Material.isWater: Boolean get() = this == Material.WATER

val Material.isRedstoneLamp: Boolean get() = this == Material.REDSTONE_LAMP

val Material.isDaylightSensor: Boolean get() = this == Material.DAYLIGHT_DETECTOR

val BUTTON_TYPES = getMatchingMaterials { it.name.endsWith("_BUTTON") }
val Material.isButton: Boolean get() = BUTTON_TYPES.contains(this)

val CANDLE_TYPES = getMatchingMaterials { it.name.endsWith("CANDLE") }

val CAKE_TYPES = getMatchingMaterials { it.name.endsWith("CAKE") }

val DOOR_TYPES = getMatchingMaterials { it.name.endsWith("_DOOR") }
val Material.isDoor: Boolean get() = DOOR_TYPES.contains(this)

val TRAPDOOR_TYPES = getMatchingMaterials { it.name.endsWith("_TRAPDOOR") }

val PRESSURE_PLATE_TYPES = getMatchingMaterials { it.name.endsWith("_PRESSURE_PLATE") }

val STAIR_TYPES = getMatchingMaterials { it.name.endsWith("_STAIRS") }
val Material.isStairs: Boolean get() = STAIR_TYPES.contains(this)

val SHULKER_BOX_TYPES = getMatchingMaterials { it.name.endsWith("SHULKER_BOX") }
val Material.isShulkerBox: Boolean get() = SHULKER_BOX_TYPES.contains(this)

val LEAF_TYPES = getMatchingMaterials { it.name.endsWith("_LEAVES") }
val Material.isLeaves: Boolean get() = LEAF_TYPES.contains(this)

val LOG_TYPES = getMatchingMaterials { it.name.endsWith("_LOG") }

val WOOD_TYPES = getMatchingMaterials { it.name.endsWith("_WOOD") }

val WALL_SIGN_TYPES = getMatchingMaterials { it.name.endsWith("_WALL_SIGN") }
val Material.isWallSign: Boolean get() = WALL_SIGN_TYPES.contains(this)

val SIGN_TYPES = getMatchingMaterials { it.name.endsWith("_SIGN") }
val Material.isSign: Boolean get() = SIGN_TYPES.contains(this)

val GLAZED_TERRACOTTA_TYPES = getMatchingMaterials { it.name.endsWith("_GLAZED_TERRACOTTA") }
val Material.isGlazedTerracotta: Boolean get() = GLAZED_TERRACOTTA_TYPES.contains(this)

val STAINED_TERRACOTTA_TYPES = getMatchingMaterials { it.name.endsWith("_TERRACOTTA") && !it.isGlazedTerracotta }
val Material.isStainedTerracotta: Boolean get() = STAINED_TERRACOTTA_TYPES.contains(this)

val NETHER_WART_TYPES = getMatchingMaterials { it.name.endsWith(("_WART_BLOCK")) }
val Material.isNetherWart: Boolean get() = NETHER_WART_TYPES.contains(this)

val CONCRETE_POWDER_TYPES = getMatchingMaterials { it.name.endsWith("_CONCRETE_POWDER") }
val Material.isConcretePowder: Boolean get() = CONCRETE_POWDER_TYPES.contains(this)

val CONCRETE_TYPES = getMatchingMaterials { it.name.endsWith("_CONCRETE") }
val Material.isConcrete: Boolean get() = CONCRETE_TYPES.contains(this)

val PLANKS_TYPES = getMatchingMaterials { it.name.endsWith("_PLANKS") }

val WOOL_TYPES = getMatchingMaterials { it.name.endsWith("_WOOL") }
val Material.isWool: Boolean get() = WOOL_TYPES.contains(this)

val CARPET_TYPES = getMatchingMaterials { it.name.endsWith("_CARPET") }
val Material.isCarpet: Boolean get() = CARPET_TYPES.contains(this)

val SLAB_TYPES = getMatchingMaterials { it.name.endsWith("_SLAB") }
val Material.isSlab: Boolean get() = SLAB_TYPES.contains(this)

val BANNER_TYPES = getMatchingMaterials { it.name.endsWith("BANNER") }

val BED_TYPES = getMatchingMaterials { it.name.endsWith("_BED") }
val Material.isBed: Boolean get() = BED_TYPES.contains(this)

val FENCE_TYPES = getMatchingMaterials { it.name.endsWith("_FENCE") }

val WALL_TYPES = getMatchingMaterials { it.name.endsWith("_WALL") }
val Material.isWall: Boolean get() = WALL_TYPES.contains(this)

val CHISELED_TYPES = getMatchingMaterials { it.name.startsWith("CHISELED_") }
