package net.horizonsend.ion.server.features.multiblock.type.misc

import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.LegacyMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.SyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent.TickingManager
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.features.transport.nodes.inputs.InputsData
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.SPLITTER_DIRECTION
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.block.sign.Side
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType.BOOLEAN

object ItemSplitterMultiblock : Multiblock(), InteractableMultiblock, EntityMultiblock<ItemSplitterMultiblock.SplitterMultiblockEntity>, DisplayNameMultilblock {
	override val name: String = "splitter"

	private fun formatText(text: String, vararg params: String) = template(text(text, AQUA), paramColor = YELLOW, useQuotesAroundObjects = false, *params)

	private val RIGHT = formatText("[{0}]   ----->   [{1}]", "-", "+")
	private val LEFT = formatText("[{0}]   <-----   [{1}]", "+", "-")

	override val signText: Array<Component?> = arrayOf(
		text("Item Splitter", NamedTextColor.GOLD),
		null,
		text(".:[Matching items]:;", NamedTextColor.GRAY),
		RIGHT
	)

	override val displayName: Component get() = text("Item Splitter")
	override val description: Component get() = text("Sorts items based on a whitelist/blacklist system.")

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(+1) {
				x(0).anyPipedInventory()
			}
			y(+0) {
				x(-2).anyStairs()
				x(-1).aluminumBlock()
				x(0).furnace()
				x(+1).aluminumBlock()
				x(+2).anyStairs()
			}
		}
		z(+1) {
			y(+1) {
				x(-2).anyPipedInventory()
				x(-1).anyGlass()
				x(0).anyWall()
				x(+1).anyGlass()
				x(+2).anyPipedInventory()
			}
			y(+0) {
				x(-2).extractor()
				x(-1).endRod()
				x(0).redstoneBlock()
				x(+1).endRod()
				x(+2).extractor()
			}
		}
		z(+2) {
			y(+1) {
				x(0).anyPipedInventory()
			}
			y(+0) {
				x(-2).anyStairs()
				x(-1).aluminumBlock()
				x(0).aluminumBlock()
				x(+1).aluminumBlock()
				x(+2).anyStairs()
			}
		}
	}

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		val pdc = sign.persistentDataContainer

		if (isBlacklist(sign)) {
			player.success("Switched sorter to whitelist!")
			pdc.set(SPLITTER_DIRECTION, BOOLEAN, false)
			sign.getSide(Side.FRONT).line(3, LEFT)
		} else {
			player.success("Switched sorter to blacklist!")
			pdc.set(SPLITTER_DIRECTION, BOOLEAN, true)
			sign.getSide(Side.FRONT).line(3, RIGHT)
		}

		sign.update()
	}

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): SplitterMultiblockEntity {
		return SplitterMultiblockEntity(manager, x, y, z, world, structureDirection, data.getAdditionalDataOrDefault(SPLITTER_DIRECTION, BOOLEAN, true))
	}

	private fun isBlacklist(sign: Sign): Boolean {
		return sign.persistentDataContainer.getOrDefault(SPLITTER_DIRECTION, BOOLEAN, true)
	}

	private val RIGHT_OLD = text("<-----", AQUA)
	private val BLACKLIST_OLD = text("BLACKLIST", NamedTextColor.BLACK, TextDecoration.BOLD)

	private val previousBlacklistText = listOf<Component>(
		BLACKLIST_OLD,
		RIGHT_OLD
	)

	class SplitterMultiblockEntity(
		manager: MultiblockManager,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureDirection: BlockFace,
		private var isBlacklist: Boolean
	) : MultiblockEntity(manager, ItemSplitterMultiblock, world, x, y, z, structureDirection), SyncTickingMultiblockEntity, LegacyMultiblockEntity {
		override val tickingManager: TickingManager = TickingManager(interval = 20)
		override val inputsData: InputsData = none()

		override fun tick() {
			val filterItems = getBlacklist() ?: return

			val inputInventory = getInventory(0, 1, 0) ?: return
			val remainderInventory = getInventory(2, 1, 1) ?: return
			val filteredInventory = getInventory(-2, 1, 1) ?: return

			if (isBlacklist) {
				doFilter(inputInventory, filteredInventory, remainderInventory) { it?.filterContains(filterItems) == false }
			} else {
				doFilter(inputInventory, filteredInventory, remainderInventory) { it?.filterContains(filterItems) == true }
			}
		}

		override fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {
			store.addAdditionalData(SPLITTER_DIRECTION, BOOLEAN, isBlacklist)
		}

		override fun loadFromSign(sign: Sign) {
			val pdc = sign.persistentDataContainer
			if (pdc.keys.contains(SPLITTER_DIRECTION)) return

			val line3 = sign.getSide(Side.FRONT).line(3)

			if (previousBlacklistText.contains(line3)) {
				pdc.set(SPLITTER_DIRECTION, BOOLEAN, true)
				sign.getSide(Side.FRONT).line(3, RIGHT)
				return
			}

			pdc.set(SPLITTER_DIRECTION, BOOLEAN, false)
			sign.getSide(Side.FRONT).line(3, LEFT)
			sign.update()
		}

		fun getBlacklist(): List<ItemStack>? {
			val items = getInventory(0, 1, 1) ?: return null
			return items.storageContents.filterNotNull()
		}

		/** If the lambda returns true, it is put into the filtered inventory, if possible **/
		private fun doFilter(sourceInventory: Inventory, destinationInventory: Inventory, remainderInventory: Inventory, filter: (ItemStack?) -> Boolean) {
			for ((index, item: ItemStack?) in sourceInventory.withIndex()) {
				if (!filter(item)) {
					tryTransfer(index, sourceInventory, remainderInventory)
					continue
				}

				tryTransfer(index, sourceInventory, destinationInventory)
			}
		}

		private fun tryTransfer(index: Int, sourceInventory: Inventory, destination: Inventory) {
			val item = sourceInventory.contents[index] ?: return

			val result: Int = destination.addItem(item).values.firstOrNull()?.amount ?: 0

			if (result == 0) {
				// no items remaining
				sourceInventory.setItem(index, null)
			} else {
				// if the amount is different, update the item in the original slot
				if (result != item.amount) {
					sourceInventory.setItem(index, item.clone().apply { amount = result })
				}
			}
		}

		private fun ItemStack.filterContains(filter: Collection<ItemStack>): Boolean = filter.any { this.isSimilar(it) }
	}
}
