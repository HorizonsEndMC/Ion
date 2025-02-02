package net.horizonsend.ion.server.features.custom.blocks.filter

import net.horizonsend.ion.server.features.custom.blocks.BlockLoot
import net.horizonsend.ion.server.features.custom.blocks.InteractableCustomBlock
import net.horizonsend.ion.server.features.custom.items.type.CustomBlockItem
import net.horizonsend.ion.server.features.gui.GuiWrapper
import net.horizonsend.ion.server.features.transport.filters.FilterData
import net.horizonsend.ion.server.features.transport.filters.FilterMeta
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.utils.PerPlayerCooldown
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import org.bukkit.block.Block
import org.bukkit.block.TileState
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import java.util.function.Supplier

abstract class CustomFilterBlock<T: Any, M: FilterMeta>(
	identifier: String,
	blockData: BlockData,
	drops: BlockLoot,
	customBlockItem: Supplier<CustomBlockItem>
) : InteractableCustomBlock(identifier, blockData, drops, customBlockItem)  {
	val cooldown = PerPlayerCooldown(5L)

	override fun onRightClick(event: PlayerInteractEvent, block: Block) {
		if (event.player.isSneaking) return
		event.isCancelled = true
		event.player.closeInventory()

		val chunk = IonChunk[block.world, block.x.shr(4), block.z.shr(4)] ?: return

		val key = toBlockKey(block.x, block.y, block.z)

		val filterManager = chunk.transportNetwork.filterManager
		val filterData = filterManager.getFilter(key) ?: filterManager.registerFilter<T, M>(key, this)

		cooldown.tryExec(event.player) {
			Tasks.sync {
				@Suppress("UNCHECKED_CAST")
				val gui = getGui(event.player, block, filterData as FilterData<T, M>) { block.state as TileState }

				gui.open()
			}
		}
	}

	abstract fun createData(pos: BlockKey): FilterData<T, M>

	abstract fun getGui(player: Player, block: Block, filterData: FilterData<T, M>, tileState: Supplier<TileState>) : GuiWrapper
}
