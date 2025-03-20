package net.horizonsend.ion.server.features.custom.blocks.filter

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.features.custom.blocks.BlockLoot
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks.customItemDrop
import net.horizonsend.ion.server.features.custom.blocks.filter.CustomFilterBlock.Companion.filterInteractCooldown
import net.horizonsend.ion.server.features.custom.blocks.misc.DirectionalCustomBlock
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.customItem
import net.horizonsend.ion.server.features.gui.GuiWrapper
import net.horizonsend.ion.server.features.gui.custom.filter.ItemFilterGui
import net.horizonsend.ion.server.features.transport.filters.FilterData
import net.horizonsend.ion.server.features.transport.filters.FilterMeta.ItemFilterMeta
import net.horizonsend.ion.server.features.transport.filters.FilterType
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.TileState
import org.bukkit.block.Vault as VaultState
import org.bukkit.block.data.type.Vault as VaultData
import org.bukkit.craftbukkit.block.CraftVault
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import java.util.function.Supplier

object ItemFilterBlock : DirectionalCustomBlock(
	identifier = "ITEM_FILTER",
	faceData = mapOf(
		BlockFace.NORTH to Material.VAULT.createBlockData { t ->
			t as VaultData
			t.facing = BlockFace.NORTH
			t.isOminous = true
			t.vaultState = VaultData.State.INACTIVE
		},
		BlockFace.SOUTH to Material.VAULT.createBlockData { t ->
			t as VaultData
			t.facing = BlockFace.SOUTH
			t.isOminous = true
			t.vaultState = VaultData.State.INACTIVE
		},
		BlockFace.EAST to Material.VAULT.createBlockData { t ->
			t as VaultData
			t.facing = BlockFace.EAST
			t.isOminous = true
			t.vaultState = VaultData.State.INACTIVE
		},
		BlockFace.WEST to Material.VAULT.createBlockData { t ->
			t as VaultData
			t.facing = BlockFace.WEST
			t.isOminous = true
			t.vaultState = VaultData.State.INACTIVE
		},
		BlockFace.UP to Material.VAULT.createBlockData { t ->
			t as VaultData
			t.facing = BlockFace.NORTH
			t.isOminous = false
			t.vaultState = VaultData.State.ACTIVE
		},
		BlockFace.DOWN to Material.VAULT.createBlockData { t ->
			t as VaultData
			t.facing = BlockFace.NORTH
			t.isOminous = true
			t.vaultState = VaultData.State.ACTIVE
		}
	),
	drops = BlockLoot(
		requiredTool = { BlockLoot.Tool.PICKAXE },
		drops = customItemDrop(CustomItemRegistry::ITEM_FILTER)
	),
	customBlockItem = CustomItemRegistry::ITEM_FILTER,
), CustomFilterBlock<ItemStack, ItemFilterMeta> {
	override fun createData(pos: BlockKey): FilterData<ItemStack, ItemFilterMeta> {
		return FilterData(pos, FilterType.ItemType)
	}

	override fun getGui(
		player: Player,
		block: Block,
		filterData: FilterData<ItemStack, ItemFilterMeta>,
		tileState: Supplier<TileState>
	): GuiWrapper {
		return ItemFilterGui(player, filterData, tileState)
	}

	override fun placeCallback(placedItem: ItemStack, block: Block) {
		val storedFilterData = placedItem.persistentDataContainer.get(NamespacedKeys.FILTER_DATA, FilterData) ?: return

		val state = block.state
		if (state !is VaultState) return
		state as CraftVault

		state.persistentDataContainer.set(NamespacedKeys.FILTER_DATA, FilterData, storedFilterData)
		state.tileEntity.sharedData

		state.update()
	}

	override fun onRightClick(event: PlayerInteractEvent, block: Block) {
		val clickedItem = event.item
		if (event.player.isSneaking && clickedItem == null) return

		event.isCancelled = true
		event.player.closeInventory()

		val chunk = IonChunk[block.world, block.x.shr(4), block.z.shr(4)] ?: return

		val key = toBlockKey(block.x, block.y, block.z)

		val filterManager = chunk.transportNetwork.filterCache
		@Suppress("UNCHECKED_CAST")
		val filterData = (filterManager.getFilter(key) ?: filterManager.registerFilter(key, this)) as FilterData<ItemStack, ItemFilterMeta>

		if (event.player.isSneaking && clickedItem != null) {
			if (clickedItem.customItem == CustomItemRegistry.WRENCH) return // Being removed

			val filtered = filterData.matchesFilter(clickedItem)
			event.player.information("Item passes filter: $filtered")
			return
		}

		filterInteractCooldown.tryExec(event.player) {
			Tasks.sync {
				val gui = getGui(event.player, block, filterData) { block.state as TileState }

				gui.open()
			}
		}
	}

	override fun removeCallback(block: Block) {
		removeFilter(block)
	}
}
