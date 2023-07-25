package net.horizonsend.ion.server.features.transport.pipe.filter

import org.bukkit.block.BlockFace

data class FilterData(val items: Set<FilterItemData>, val face: BlockFace) {
	override fun toString(): String {
		return "[$items towards $face]"
	}
}
