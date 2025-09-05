package net.horizonsend.ion.server.features.multiblock.type.processing.automason

import net.horizonsend.ion.server.features.multiblock.type.processing.automason.CenterType.entries
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
		operator fun get(material: Material): CenterType? {
			return entries.firstOrNull { it.matches(material) }
		}
	}
}
