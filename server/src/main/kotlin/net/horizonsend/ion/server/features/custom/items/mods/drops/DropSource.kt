package net.horizonsend.ion.server.features.custom.items.mods.drops

import net.horizonsend.ion.server.features.custom.blocks.CustomBlock
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack

/**
 * A mod that modifies drops
 **/
interface DropSource {
	val shouldDropXP: Boolean
	val usedTool: ItemStack?

	fun getDrop(block: Block): Collection<ItemStack>
	fun getDrop(block: CustomBlock): Collection<ItemStack>

	companion object {
		val PICKAXE = ItemStack(Material.DIAMOND_PICKAXE, 1)

		val DEFAULT_DROP_PROVIDER = object : DropSource {
			override val shouldDropXP: Boolean = true
			override val usedTool: ItemStack = PICKAXE

			override fun getDrop(block: Block): Collection<ItemStack> {
				return block.getDrops(PICKAXE)
			}

			override fun getDrop(block: CustomBlock): Collection<ItemStack> {
				return block.drops.getDrops(PICKAXE, false)
			}
		}
	}
}
