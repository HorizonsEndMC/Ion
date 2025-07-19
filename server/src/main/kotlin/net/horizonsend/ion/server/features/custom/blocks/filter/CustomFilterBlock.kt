package net.horizonsend.ion.server.features.custom.blocks.filter

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.orEmpty
import net.horizonsend.ion.server.features.custom.blocks.misc.InteractableCustomBlock
import net.horizonsend.ion.server.features.custom.blocks.misc.WrenchRemovable
import net.horizonsend.ion.server.features.transport.filters.FilterData
import net.horizonsend.ion.server.features.transport.filters.FilterMeta
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.gui.CommonGuiWrapper
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.PerPlayerCooldown
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.getCustomName
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updatePersistentDataContainer
import net.kyori.adventure.text.Component
import org.bukkit.block.Block
import org.bukkit.block.TileState
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier

interface CustomFilterBlock<T: Any, M: FilterMeta> : WrenchRemovable, InteractableCustomBlock {
	companion object {
		val filterInteractCooldown = PerPlayerCooldown(5L)
	}

	override fun onRightClick(event: PlayerInteractEvent, block: Block) {
		if (event.player.isSneaking) return
		event.isCancelled = true
		event.player.closeInventory()

		val chunk = IonChunk[block.world, block.x.shr(4), block.z.shr(4)] ?: return

		val key = toBlockKey(block.x, block.y, block.z)

		val filterManager = chunk.transportNetwork.filterCache
		val filterData = filterManager.getFilter(key) ?: filterManager.registerFilter(key, this)

		filterInteractCooldown.tryExec(event.player) {
			Tasks.sync {
				@Suppress("UNCHECKED_CAST")
				val gui = getGui(event.player, block, filterData as FilterData<T, M>) { block.state as TileState }

				gui.openGui()
			}
		}
	}

	fun createData(pos: BlockKey): FilterData<T, M>

	fun getGui(player: Player, block: Block, filterData: FilterData<T, M>, tileState: Supplier<TileState>) : CommonGuiWrapper

	override fun decorateItem(itemStack: ItemStack, block: Block) {
		val state = block.state
		state as TileState

		val data = state.persistentDataContainer.get(NamespacedKeys.FILTER_DATA, FilterData) ?: return

		val customDisplayName = itemStack.getCustomName()

		itemStack
			.updateDisplayName(ofChildren(customDisplayName.orEmpty(), Component.text(" (Configured)")))
			.updatePersistentDataContainer {
				set(NamespacedKeys.FILTER_DATA, FilterData, data)
			}
	}

	fun removeFilter(block: Block) {
		val chunk = IonChunk[block.world, block.x.shr(4), block.z.shr(4)] ?: return
		val key = toBlockKey(block.x, block.y, block.z)

		val filterManager = chunk.transportNetwork.filterCache
		filterManager.removeFilter(key)
	}
}
