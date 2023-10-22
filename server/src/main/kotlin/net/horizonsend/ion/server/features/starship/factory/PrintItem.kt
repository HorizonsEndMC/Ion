package net.horizonsend.ion.server.features.starship.factory

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import net.horizonsend.ion.server.features.customblocks.CustomBlocks
import net.horizonsend.ion.server.features.customitems.CustomItem
import net.horizonsend.ion.server.features.customitems.CustomItems
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars
import org.bukkit.Material
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.Slab
import org.bukkit.block.data.type.WallSign
import org.bukkit.inventory.ItemStack
import java.util.Optional

data class PrintItem(val itemString: String) {
	constructor(itemStack: ItemStack) : this(Bazaars.toItemString(itemStack))

	constructor(customItem: CustomItem) : this(customItem.constructItemStack())

	constructor(material: Material) : this(ItemStack(material))

	override fun toString(): String {
		return itemString
	}

	companion object {
		private val log: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger("PrintItem")

		private val printItemCache: LoadingCache<BlockData, Optional<PrintItem>> = CacheBuilder.newBuilder()
			.build(
				CacheLoader.from<BlockData, Optional<PrintItem>> {
					Optional.ofNullable(findPrintItem(checkNotNull(it)))
				}
			)

		operator fun get(blockData: BlockData): PrintItem? {
			return printItemCache.getUnchecked(blockData).orElse(null)
		}

		private fun findPrintItem(data: BlockData): PrintItem? {
			val customBlock = CustomBlocks.getByBlockData(data)
			when {
				customBlock != null -> {
					val customItem = CustomItems.getByIdentifier(customBlock.identifier) ?: return null
					return PrintItem(customItem)
				}

				data is Slab -> {
					return PrintItem(data.material)
				}

				data.material.isItem -> {
					return PrintItem(data.material)
				}

				data is WallSign -> {
					val itemMat = Material.getMaterial(data.material.name.replace("WALL_SIGN", "SIGN"))
					checkNotNull(itemMat)
					return PrintItem(itemMat)
				}

				data.material == Material.REDSTONE_WIRE -> {
					return PrintItem(Material.REDSTONE)
				}

				data.material == Material.WALL_TORCH -> {
					return PrintItem(Material.TORCH)
				}

				data.material == Material.SOUL_WALL_TORCH -> {
					return PrintItem(Material.SOUL_TORCH)
				}

				data.material == Material.REDSTONE_WALL_TORCH -> {
					return PrintItem(Material.REDSTONE_TORCH)
				}

				else -> {
					log.warn("No item material for $data")
					return null
				}
			}
		}
	}
}
