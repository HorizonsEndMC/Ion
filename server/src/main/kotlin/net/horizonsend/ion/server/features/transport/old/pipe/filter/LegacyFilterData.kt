package net.horizonsend.ion.server.features.transport.old.pipe.filter

import org.bukkit.block.BlockFace

data class LegacyFilterData(val items: Set<FilterItemData>, val face: BlockFace) {
	override fun toString(): String {
		return "[$items towards $face]"
	}
}
