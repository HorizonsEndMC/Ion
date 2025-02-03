package net.horizonsend.ion.server.features.custom.blocks

import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack

interface WrenchRemovable {
	fun decorateItem(itemStack: ItemStack, block: Block)
}
