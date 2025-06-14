package net.horizonsend.ion.server.features.multiblock.type.processing.automason

import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table
import net.horizonsend.ion.server.miscellaneous.utils.BRICK_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.CUT_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.GRATE_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.PILLAR_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.PLANKS_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.SMOOTH_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.STRIPPED_LOG_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.STRIPPED_WOOD_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.TILES_TYPES
import net.horizonsend.ion.server.miscellaneous.utils.isButton
import net.horizonsend.ion.server.miscellaneous.utils.isChiseled
import net.horizonsend.ion.server.miscellaneous.utils.isPolished
import net.horizonsend.ion.server.miscellaneous.utils.isSlab
import net.horizonsend.ion.server.miscellaneous.utils.isStairs
import net.horizonsend.ion.server.miscellaneous.utils.isWall
import org.bukkit.Material

enum class CenterType {
	POLISHED {
		override fun matches(material: Material?): Boolean {
			return material == null || material.isPolished
		}
	},
	CHISELED {
		override fun matches(material: Material?): Boolean {
			return material == null || material.isChiseled
		}
	},
	SMOOTH {
		override fun matches(material: Material?): Boolean {
			return material == null || SMOOTH_TYPES.contains(material)
		}
	},
	STRIPPED {
		override fun matches(material: Material?): Boolean {
			return material == null || STRIPPED_LOG_TYPES.contains(material) || STRIPPED_WOOD_TYPES.contains(material)
		}
	},
	PLANKS {
		override fun matches(material: Material?): Boolean {
			return material == null || PLANKS_TYPES.contains(material)
		}
	},
	SLAB {
		override fun matches(material: Material?): Boolean {
			return material == null || material.isSlab
		}
	},
	STAIR {
		override fun matches(material: Material?): Boolean {
			return material == null || material.isStairs
		}
	},
	WALL {
		override fun matches(material: Material?): Boolean {
			return material == null || material.isWall
		}
	},
	BUTTON {
		override fun matches(material: Material?): Boolean {
			return material == null || material.isButton
		}
	},
	BRICKS {
		override fun matches(material: Material?): Boolean {
			return material == null || BRICK_TYPES.contains(material)
		}
	},
	PILLAR {
		override fun matches(material: Material?): Boolean {
			return material == null || PILLAR_TYPES.contains(material)
		}
	},
	CUT {
		override fun matches(material: Material?): Boolean {
			return material == null || CUT_TYPES.contains(material)
		}
	},
	TILES {
		override fun matches(material: Material?): Boolean {
			return material == null || TILES_TYPES.contains(material)
		}
	},
	GRATE {
		override fun matches(material: Material?): Boolean {
			return material == null || GRATE_TYPES.contains(material)
		}
	},

	;

	abstract fun matches(material: Material?): Boolean

	companion object {
		fun getRecipeTable(): Table<Material, CenterType, Material> {
			val table = HashBasedTable.create<Material, CenterType, Material>()

			fun add(startMaterial: Material, category: CenterType, result: Material) {
				if (table.get(startMaterial, category) != null) error("$startMaterial already has a result for $category!")

				table.put(startMaterial, category, result)
			}

			add(Material.STONE, CHISELED, Material.CHISELED_STONE_BRICKS)
			add(Material.STONE, SLAB, Material.STONE_SLAB)
			add(Material.STONE, STAIR, Material.STONE_STAIRS)
			add(Material.STONE, SMOOTH, Material.SMOOTH_STONE)
			add(Material.STONE, BUTTON, Material.STONE_BUTTON)
			add(Material.STONE, BRICKS, Material.STONE_BRICKS)

			add(Material.STONE_BRICKS, STAIR, Material.STONE_BRICK_STAIRS)
			add(Material.STONE_BRICKS, SLAB, Material.STONE_BRICK_SLAB)
			add(Material.STONE_BRICKS, WALL, Material.STONE_BRICK_WALL)

			add(Material.ANDESITE, SLAB, Material.ANDESITE_SLAB)
			add(Material.ANDESITE, STAIR, Material.ANDESITE_STAIRS)
			add(Material.ANDESITE, WALL, Material.ANDESITE_WALL)
			add(Material.ANDESITE, POLISHED, Material.POLISHED_ANDESITE)
			add(Material.POLISHED_ANDESITE, SLAB, Material.POLISHED_ANDESITE_SLAB)
			add(Material.POLISHED_ANDESITE, STAIR, Material.POLISHED_ANDESITE_STAIRS)

			add(Material.DIORITE, SLAB, Material.DIORITE_SLAB)
			add(Material.DIORITE, STAIR, Material.DIORITE_STAIRS)
			add(Material.DIORITE, WALL, Material.DIORITE_WALL)
			add(Material.DIORITE, POLISHED, Material.POLISHED_DIORITE)
			add(Material.POLISHED_DIORITE, SLAB, Material.POLISHED_DIORITE_SLAB)
			add(Material.POLISHED_DIORITE, STAIR, Material.POLISHED_DIORITE_STAIRS)

			add(Material.GRANITE, SLAB, Material.GRANITE_SLAB)
			add(Material.GRANITE, STAIR, Material.GRANITE_STAIRS)
			add(Material.GRANITE, WALL, Material.GRANITE_WALL)
			add(Material.GRANITE, POLISHED, Material.POLISHED_GRANITE)
			add(Material.POLISHED_GRANITE, SLAB, Material.POLISHED_GRANITE_SLAB)
			add(Material.POLISHED_GRANITE, STAIR, Material.POLISHED_GRANITE_STAIRS)


			add(Material.BRICKS, SLAB, Material.BRICK_SLAB)
			add(Material.BRICKS, STAIR, Material.BRICK_STAIRS)
			add(Material.BRICKS, WALL, Material.BRICK_WALL)

			add(Material.NETHER_BRICKS, CHISELED, Material.CHISELED_NETHER_BRICKS)
			add(Material.RESIN_BRICKS, CHISELED, Material.CHISELED_RESIN_BRICKS)
			add(Material.SANDSTONE, CHISELED, Material.CHISELED_SANDSTONE)
			add(Material.STONE_BRICKS, CHISELED, Material.CHISELED_STONE_BRICKS)

			add(Material.BLACKSTONE, SLAB, Material.BLACKSTONE_SLAB)
			add(Material.BLACKSTONE, STAIR, Material.BLACKSTONE_STAIRS)
			add(Material.BLACKSTONE, WALL, Material.BLACKSTONE_WALL)
			add(Material.BLACKSTONE, POLISHED, Material.POLISHED_BLACKSTONE)
			add(Material.BLACKSTONE, CHISELED, Material.CHISELED_POLISHED_BLACKSTONE)
			add(Material.BLACKSTONE, BRICKS, Material.POLISHED_BLACKSTONE_BRICKS)

			add(Material.POLISHED_BLACKSTONE, CHISELED, Material.CHISELED_POLISHED_BLACKSTONE)
			add(Material.POLISHED_BLACKSTONE, SLAB, Material.POLISHED_BLACKSTONE_SLAB)
			add(Material.POLISHED_BLACKSTONE, STAIR, Material.POLISHED_BLACKSTONE_STAIRS)
			add(Material.POLISHED_BLACKSTONE, WALL, Material.POLISHED_BLACKSTONE_WALL)
			add(Material.POLISHED_BLACKSTONE, BRICKS, Material.POLISHED_BLACKSTONE_BRICKS)
			add(Material.POLISHED_BLACKSTONE_BRICKS, WALL, Material.POLISHED_BLACKSTONE_BRICK_WALL)
			add(Material.POLISHED_BLACKSTONE_BRICKS, STAIR, Material.POLISHED_BLACKSTONE_BRICK_STAIRS)
			add(Material.POLISHED_BLACKSTONE_BRICKS, SLAB, Material.POLISHED_BLACKSTONE_BRICK_SLAB)

			add(Material.COBBLED_DEEPSLATE, CHISELED, Material.CHISELED_DEEPSLATE)
			add(Material.COBBLED_DEEPSLATE, SLAB, Material.COBBLED_DEEPSLATE_SLAB)
			add(Material.COBBLED_DEEPSLATE, STAIR, Material.COBBLED_DEEPSLATE_STAIRS)
			add(Material.COBBLED_DEEPSLATE, WALL, Material.COBBLED_DEEPSLATE_WALL)
			add(Material.COBBLED_DEEPSLATE, BRICKS, Material.DEEPSLATE_BRICKS)
			add(Material.COBBLED_DEEPSLATE, TILES, Material.DEEPSLATE_TILES)
			add(Material.COBBLED_DEEPSLATE, POLISHED, Material.POLISHED_DEEPSLATE)

			add(Material.COBBLESTONE, SLAB, Material.COBBLESTONE_SLAB)
			add(Material.COBBLESTONE, STAIR, Material.COBBLESTONE_STAIRS)
			add(Material.COBBLESTONE, WALL, Material.COBBLESTONE_WALL)
			add(Material.MOSSY_COBBLESTONE, SLAB, Material.MOSSY_COBBLESTONE_SLAB)
			add(Material.MOSSY_COBBLESTONE, STAIR, Material.MOSSY_COBBLESTONE_STAIRS)
			add(Material.MOSSY_COBBLESTONE, WALL, Material.MOSSY_COBBLESTONE_WALL)

			add(Material.COPPER_BLOCK, CHISELED, Material.CHISELED_COPPER)
			add(Material.COPPER_BLOCK, GRATE, Material.COPPER_GRATE)
			add(Material.COPPER_BLOCK, CUT, Material.CUT_COPPER)
			add(Material.COPPER_BLOCK, SLAB, Material.CUT_COPPER_SLAB)
			add(Material.COPPER_BLOCK, STAIR, Material.CUT_COPPER_STAIRS)

			add(Material.CUT_COPPER, CHISELED, Material.CHISELED_COPPER)
			add(Material.CUT_COPPER, SLAB, Material.CUT_COPPER_SLAB)
			add(Material.CUT_COPPER, STAIR, Material.CUT_COPPER_STAIRS)

			add(Material.DARK_PRISMARINE, SLAB, Material.DARK_PRISMARINE_SLAB)
			add(Material.DARK_PRISMARINE, STAIR, Material.DARK_PRISMARINE_STAIRS)
			add(Material.PRISMARINE_BRICKS, SLAB, Material.PRISMARINE_BRICK_SLAB)
			add(Material.PRISMARINE_BRICKS, STAIR, Material.PRISMARINE_BRICK_STAIRS)
			add(Material.PRISMARINE, SLAB, Material.PRISMARINE_SLAB)
			add(Material.PRISMARINE, STAIR, Material.PRISMARINE_STAIRS)
			add(Material.PRISMARINE, WALL, Material.PRISMARINE_WALL)

			add(Material.PURPUR_BLOCK, PILLAR, Material.PURPUR_PILLAR)
			add(Material.PURPUR_BLOCK, SLAB, Material.PURPUR_SLAB)
			add(Material.PURPUR_BLOCK, STAIR, Material.PURPUR_STAIRS)

			add(Material.QUARTZ_BLOCK, CHISELED, Material.CHISELED_QUARTZ_BLOCK)
			add(Material.QUARTZ_BLOCK, BRICKS, Material.QUARTZ_BRICKS)
			add(Material.QUARTZ_BLOCK, PILLAR, Material.QUARTZ_PILLAR)
			add(Material.QUARTZ_BLOCK, SLAB, Material.QUARTZ_SLAB)
			add(Material.QUARTZ_BLOCK, STAIR, Material.QUARTZ_STAIRS)
			add(Material.SMOOTH_QUARTZ, SLAB, Material.SMOOTH_QUARTZ_SLAB)
			add(Material.SMOOTH_QUARTZ, STAIR, Material.SMOOTH_QUARTZ_STAIRS)

			add(Material.RED_SANDSTONE, CHISELED, Material.CHISELED_RED_SANDSTONE)
			add(Material.RED_SANDSTONE, CUT, Material.CUT_RED_SANDSTONE)
			add(Material.CUT_RED_SANDSTONE, SLAB, Material.CUT_RED_SANDSTONE_SLAB)
			add(Material.SANDSTONE, CUT, Material.CUT_SANDSTONE)
			add(Material.CUT_SANDSTONE, SLAB, Material.CUT_SANDSTONE_SLAB)

			add(Material.POLISHED_DEEPSLATE, SLAB, Material.POLISHED_DEEPSLATE_SLAB)
			add(Material.POLISHED_DEEPSLATE, STAIR, Material.POLISHED_DEEPSLATE_STAIRS)
			add(Material.POLISHED_DEEPSLATE, WALL, Material.POLISHED_DEEPSLATE_WALL)
			add(Material.POLISHED_DEEPSLATE, BRICKS, Material.DEEPSLATE_BRICKS)
			add(Material.POLISHED_DEEPSLATE, TILES, Material.DEEPSLATE_TILES)

			add(Material.DEEPSLATE_BRICKS, SLAB, Material.DEEPSLATE_BRICK_SLAB)
			add(Material.DEEPSLATE_BRICKS, STAIR, Material.DEEPSLATE_BRICK_STAIRS)
			add(Material.DEEPSLATE_BRICKS, WALL, Material.DEEPSLATE_BRICK_WALL)
			add(Material.DEEPSLATE_BRICKS, TILES, Material.DEEPSLATE_TILES)

			add(Material.DEEPSLATE_TILES, SLAB, Material.DEEPSLATE_TILE_SLAB)
			add(Material.DEEPSLATE_TILES, STAIR, Material.DEEPSLATE_TILE_STAIRS)
			add(Material.DEEPSLATE_TILES, WALL, Material.DEEPSLATE_TILE_WALL)

			add(Material.END_STONE_BRICKS, SLAB, Material.END_STONE_BRICK_SLAB)
			add(Material.END_STONE, SLAB, Material.END_STONE_BRICK_SLAB)
			add(Material.END_STONE_BRICKS, STAIR, Material.END_STONE_BRICK_STAIRS)
			add(Material.END_STONE, STAIR, Material.END_STONE_BRICK_STAIRS)
			add(Material.END_STONE_BRICKS, WALL, Material.END_STONE_BRICK_WALL)
			add(Material.END_STONE, WALL, Material.END_STONE_BRICK_WALL)
			add(Material.END_STONE, BRICKS, Material.END_STONE_BRICKS)
			add(Material.EXPOSED_COPPER, CHISELED, Material.EXPOSED_CHISELED_COPPER)
			add(Material.EXPOSED_CUT_COPPER, CHISELED, Material.EXPOSED_CHISELED_COPPER)
			add(Material.EXPOSED_COPPER, GRATE, Material.EXPOSED_COPPER_GRATE)
			add(Material.EXPOSED_COPPER, CUT, Material.EXPOSED_CUT_COPPER)
			add(Material.EXPOSED_COPPER, SLAB, Material.EXPOSED_CUT_COPPER_SLAB)
			add(Material.EXPOSED_CUT_COPPER, SLAB, Material.EXPOSED_CUT_COPPER_SLAB)
			add(Material.EXPOSED_COPPER, STAIR, Material.EXPOSED_CUT_COPPER_STAIRS)
			add(Material.EXPOSED_CUT_COPPER, STAIR, Material.EXPOSED_CUT_COPPER_STAIRS)
			add(Material.MOSSY_STONE_BRICKS, SLAB, Material.MOSSY_STONE_BRICK_SLAB)
			add(Material.MOSSY_STONE_BRICKS, STAIR, Material.MOSSY_STONE_BRICK_STAIRS)
			add(Material.MOSSY_STONE_BRICKS, WALL, Material.MOSSY_STONE_BRICK_WALL)
			add(Material.MUD_BRICKS, SLAB, Material.MUD_BRICK_SLAB)
			add(Material.MUD_BRICKS, STAIR, Material.MUD_BRICK_STAIRS)
			add(Material.MUD_BRICKS, WALL, Material.MUD_BRICK_WALL)
			add(Material.NETHER_BRICKS, SLAB, Material.NETHER_BRICK_SLAB)
			add(Material.NETHER_BRICKS, STAIR, Material.NETHER_BRICK_STAIRS)
			add(Material.NETHER_BRICKS, WALL, Material.NETHER_BRICK_WALL)
			add(Material.OXIDIZED_COPPER, CHISELED, Material.OXIDIZED_CHISELED_COPPER)
			add(Material.OXIDIZED_CUT_COPPER, CHISELED, Material.OXIDIZED_CHISELED_COPPER)
			add(Material.OXIDIZED_COPPER, GRATE, Material.OXIDIZED_COPPER_GRATE)
			add(Material.OXIDIZED_COPPER, CUT, Material.OXIDIZED_CUT_COPPER)
			add(Material.OXIDIZED_COPPER, SLAB, Material.OXIDIZED_CUT_COPPER_SLAB)
			add(Material.OXIDIZED_CUT_COPPER, SLAB, Material.OXIDIZED_CUT_COPPER_SLAB)
			add(Material.OXIDIZED_COPPER, STAIR, Material.OXIDIZED_CUT_COPPER_STAIRS)
			add(Material.OXIDIZED_CUT_COPPER, STAIR, Material.OXIDIZED_CUT_COPPER_STAIRS)
			add(Material.BASALT, POLISHED, Material.POLISHED_BASALT)

			add(Material.RED_NETHER_BRICKS, SLAB, Material.RED_NETHER_BRICK_SLAB)
			add(Material.RED_NETHER_BRICKS, STAIR, Material.RED_NETHER_BRICK_STAIRS)
			add(Material.RED_NETHER_BRICKS, WALL, Material.RED_NETHER_BRICK_WALL)

			add(Material.RED_SANDSTONE, SLAB, Material.RED_SANDSTONE_SLAB)
			add(Material.RED_SANDSTONE, STAIR, Material.RED_SANDSTONE_STAIRS)
			add(Material.RED_SANDSTONE, WALL, Material.RED_SANDSTONE_WALL)

			add(Material.RESIN_BRICKS, SLAB, Material.RESIN_BRICK_SLAB)
			add(Material.RESIN_BRICKS, STAIR, Material.RESIN_BRICK_STAIRS)
			add(Material.RESIN_BRICKS, WALL, Material.RESIN_BRICK_WALL)

			add(Material.SANDSTONE, SLAB, Material.SANDSTONE_SLAB)
			add(Material.SANDSTONE, STAIR, Material.SANDSTONE_STAIRS)
			add(Material.SANDSTONE, WALL, Material.SANDSTONE_WALL)

			add(Material.SMOOTH_RED_SANDSTONE, SLAB, Material.SMOOTH_RED_SANDSTONE_SLAB)
			add(Material.SMOOTH_RED_SANDSTONE, STAIR, Material.SMOOTH_RED_SANDSTONE_STAIRS)
			add(Material.SMOOTH_SANDSTONE, SLAB, Material.SMOOTH_SANDSTONE_SLAB)
			add(Material.SMOOTH_SANDSTONE, STAIR, Material.SMOOTH_SANDSTONE_STAIRS)
			add(Material.SMOOTH_STONE, SLAB, Material.SMOOTH_STONE_SLAB)
			add(Material.STONE, WALL, Material.STONE_BRICK_WALL)

			add(Material.TUFF, SLAB, Material.TUFF_SLAB)
			add(Material.TUFF, STAIR, Material.TUFF_STAIRS)
			add(Material.TUFF, WALL, Material.TUFF_WALL)
			add(Material.TUFF, CHISELED, Material.CHISELED_TUFF)
			add(Material.TUFF, POLISHED, Material.POLISHED_TUFF)
			add(Material.TUFF, BRICKS, Material.TUFF_BRICKS)
			add(Material.POLISHED_TUFF, CHISELED, Material.CHISELED_TUFF_BRICKS)
			add(Material.POLISHED_TUFF, SLAB, Material.POLISHED_TUFF_SLAB)
			add(Material.POLISHED_TUFF, STAIR, Material.POLISHED_TUFF_STAIRS)
			add(Material.POLISHED_TUFF, WALL, Material.POLISHED_TUFF_WALL)
			add(Material.POLISHED_TUFF, BRICKS, Material.TUFF_BRICKS)
			add(Material.TUFF_BRICKS, CHISELED, Material.CHISELED_TUFF_BRICKS)
			add(Material.TUFF_BRICKS, SLAB, Material.TUFF_BRICK_SLAB)
			add(Material.TUFF_BRICKS, STAIR, Material.TUFF_BRICK_STAIRS)
			add(Material.TUFF_BRICKS, WALL, Material.TUFF_BRICK_WALL)

			add(Material.WAXED_COPPER_BLOCK, CHISELED, Material.WAXED_CHISELED_COPPER)
			add(Material.WAXED_CUT_COPPER, CHISELED, Material.WAXED_CHISELED_COPPER)
			add(Material.WAXED_COPPER_BLOCK, GRATE, Material.WAXED_COPPER_GRATE)
			add(Material.WAXED_COPPER_BLOCK, CUT, Material.WAXED_CUT_COPPER)
			add(Material.WAXED_COPPER_BLOCK, SLAB, Material.WAXED_CUT_COPPER_SLAB)
			add(Material.WAXED_CUT_COPPER, SLAB, Material.WAXED_CUT_COPPER_SLAB)
			add(Material.WAXED_COPPER_BLOCK, STAIR, Material.WAXED_CUT_COPPER_STAIRS)
			add(Material.WAXED_CUT_COPPER, STAIR, Material.WAXED_CUT_COPPER_STAIRS)
			add(Material.WAXED_EXPOSED_COPPER, CHISELED, Material.WAXED_EXPOSED_CHISELED_COPPER)
			add(Material.WAXED_EXPOSED_CUT_COPPER, CHISELED, Material.WAXED_EXPOSED_CHISELED_COPPER)
			add(Material.WAXED_EXPOSED_COPPER, GRATE, Material.WAXED_EXPOSED_COPPER_GRATE)
			add(Material.WAXED_EXPOSED_COPPER, CUT, Material.WAXED_EXPOSED_CUT_COPPER)
			add(Material.WAXED_EXPOSED_COPPER, SLAB, Material.WAXED_EXPOSED_CUT_COPPER_SLAB)
			add(Material.WAXED_EXPOSED_CUT_COPPER, SLAB, Material.WAXED_EXPOSED_CUT_COPPER_SLAB)
			add(Material.WAXED_EXPOSED_COPPER, STAIR, Material.WAXED_EXPOSED_CUT_COPPER_STAIRS)
			add(Material.WAXED_EXPOSED_CUT_COPPER, STAIR, Material.WAXED_EXPOSED_CUT_COPPER_STAIRS)
			add(Material.WAXED_OXIDIZED_COPPER, CHISELED, Material.WAXED_OXIDIZED_CHISELED_COPPER)
			add(Material.WAXED_OXIDIZED_CUT_COPPER, CHISELED, Material.WAXED_OXIDIZED_CHISELED_COPPER)
			add(Material.WAXED_OXIDIZED_COPPER, GRATE, Material.WAXED_OXIDIZED_COPPER_GRATE)
			add(Material.WAXED_OXIDIZED_COPPER, CUT, Material.WAXED_OXIDIZED_CUT_COPPER)
			add(Material.WAXED_OXIDIZED_COPPER, SLAB, Material.WAXED_OXIDIZED_CUT_COPPER_SLAB)
			add(Material.WAXED_OXIDIZED_CUT_COPPER, SLAB, Material.WAXED_OXIDIZED_CUT_COPPER_SLAB)
			add(Material.WAXED_OXIDIZED_COPPER, STAIR, Material.WAXED_OXIDIZED_CUT_COPPER_STAIRS)
			add(Material.WAXED_OXIDIZED_CUT_COPPER, STAIR, Material.WAXED_OXIDIZED_CUT_COPPER_STAIRS)
			add(Material.WAXED_WEATHERED_COPPER, CHISELED, Material.WAXED_WEATHERED_CHISELED_COPPER)
			add(Material.WAXED_WEATHERED_CUT_COPPER, CHISELED, Material.WAXED_WEATHERED_CHISELED_COPPER)
			add(Material.WAXED_WEATHERED_COPPER, GRATE, Material.WAXED_WEATHERED_COPPER_GRATE)
			add(Material.WAXED_WEATHERED_COPPER, CUT, Material.WAXED_WEATHERED_CUT_COPPER)
			add(Material.WAXED_WEATHERED_COPPER, SLAB, Material.WAXED_WEATHERED_CUT_COPPER_SLAB)
			add(Material.WAXED_WEATHERED_CUT_COPPER, SLAB, Material.WAXED_WEATHERED_CUT_COPPER_SLAB)
			add(Material.WAXED_WEATHERED_COPPER, STAIR, Material.WAXED_WEATHERED_CUT_COPPER_STAIRS)
			add(Material.WAXED_WEATHERED_CUT_COPPER, STAIR, Material.WAXED_WEATHERED_CUT_COPPER_STAIRS)
			add(Material.WEATHERED_COPPER, CHISELED, Material.WEATHERED_CHISELED_COPPER)
			add(Material.WEATHERED_CUT_COPPER, CHISELED, Material.WEATHERED_CHISELED_COPPER)
			add(Material.WEATHERED_COPPER, GRATE, Material.WEATHERED_COPPER_GRATE)
			add(Material.WEATHERED_COPPER, CUT, Material.WEATHERED_CUT_COPPER)
			add(Material.WEATHERED_COPPER, SLAB, Material.WEATHERED_CUT_COPPER_SLAB)
			add(Material.WEATHERED_CUT_COPPER, SLAB, Material.WEATHERED_CUT_COPPER_SLAB)
			add(Material.WEATHERED_COPPER, STAIR, Material.WEATHERED_CUT_COPPER_STAIRS)
			add(Material.WEATHERED_CUT_COPPER, STAIR, Material.WEATHERED_CUT_COPPER_STAIRS)

			return table
		}

		operator fun get(material: Material): CenterType? {
			return entries.firstOrNull { it.matches(material) }
		}
	}
}
