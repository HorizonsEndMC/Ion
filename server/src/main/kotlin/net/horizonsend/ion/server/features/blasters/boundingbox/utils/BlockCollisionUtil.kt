package net.horizonsend.ion.server.features.blasters.boundingbox.utils

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block

// Credits: QualityArmory
object BlockCollisionUtil {
	private val customBlockHeights: HashMap<Material, Double> = HashMap<Material, Double>().apply {
		for (m in Material.values()) {
			if (m.name.endsWith("_WALL")) this[m] = 1.5
			if (m.name.endsWith("_FENCE_GATE") || m.name.endsWith("_FENCE")) this[m] =
				1.5
			if (m.name.endsWith("_BED")) this[m] = 0.5
			if (m.name.endsWith("_SLAB") || m.name.endsWith("_FENCE")) this[m] =
				0.5
			if (m.name.endsWith("DAYLIGHT_DETECTOR")) this[m] = 0.4
			if (m.name.endsWith("CARPET")) this[m] = 0.1
			if (m.name.endsWith("TRAPDOOR")) this[m] = 0.2
		}
	}

	fun getHeight(b: Block): Double {
		val type: Material = b.type
		if (b.type.name.contains("SLAB") || b.type.name.contains("STEP")) {
			if (b.data.toInt() == 0) return 0.5
			if (b.data.toInt() == 1) return 1.0
		}

		if (customBlockHeights.containsKey(type)) return customBlockHeights[type]!!
		return if (type.isSolid) 1.0 else 0.0
	}

	fun isSolidAt(b: Block, loc: Location): Boolean {
		if (b.location.y + getHeight(b) > loc.y) return true
		val temp: Block = b.getRelative(0, -1, 0)
		return temp.location.y + getHeight(temp) > loc.y
	}

	fun isSolid(b: Block, l: Location): Boolean {
		if (b.type.name.equals("SNOW")) return false
		if (b.type.name.contains("SIGN")) return false
		if (b.type.name.endsWith("CARPET")) {
			return false
		}

		if (b.type.name.contains("SLAB") || b.type.name.contains("STEP")) {
			return (l.y - l.blockY > 0.5 && b.data.toInt() == 0) || (l.y - l.blockY <= 0.5 && b.data.toInt() == 1)
		}

		if (b.type.name.contains("BED_") || b.type.name.contains("_BED")
			|| b.type.name.contains("DAYLIGHT_DETECTOR")
		) {
			return l.y - l.blockY > 0.5
		}

		if (b.type.name.contains("STAIR")) {
			if (b.data < 4 && l.y - l.blockY < 0.5) return true
			if (b.data >= 4 && l.y - l.blockY > 0.5) return true

			when (b.data.toInt()) {
				0, 4 -> return l.x - (0.5 + l.blockX) > 0
				1, 5 -> return l.x - (0.5 + l.blockX) < 0
				2, 6 -> return l.z - (0.5 + l.blockZ) > 0
				3, 7 -> return l.z - (0.5 + l.blockZ) < 0
			}
		}

		if (b.type.name.endsWith("FERN")) {
			return false
		}

		return b.type.isOccluding
	}
}
