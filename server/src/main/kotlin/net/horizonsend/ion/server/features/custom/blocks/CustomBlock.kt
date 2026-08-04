package net.horizonsend.ion.server.features.custom.blocks

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.Keyed
import net.horizonsend.ion.server.features.custom.items.CustomItem
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.inventory.ItemStack

open class CustomBlock(
	override val key: IonRegistryKey<CustomBlock, *>,
	val blockData: BlockData,
	val drops: BlockLoot,
	private val customBlockItem: IonRegistryKey<CustomItem, *>
) : Keyed<CustomBlock> {
	val customItem get() = customBlockItem.getValue()

	open fun placeCallback(placedItem: ItemStack?, block: Block) {}
	open fun removeCallback(block: Block) {}
}
