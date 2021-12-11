package net.starlegacy.feature.transport.pipe.filter

import org.bukkit.block.BlockFace

data class FilterData(val items: Set<FilterItemData>, val face: BlockFace) {
    override fun toString(): String {
        return "[$items towards $face]"
    }
}
