package net.horizonsend.ion.server.features.custom.blocks

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks.customItemDrop
import net.horizonsend.ion.server.features.custom.items.CustomItems
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.interactable.InteractableGUI
import net.horizonsend.ion.server.features.multiblock.MultiblockRegistration
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock.Companion.getDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.PerPlayerCooldown
import net.horizonsend.ion.server.miscellaneous.utils.text.itemName
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import java.util.concurrent.TimeUnit

object MultiblockWorkbench : InteractableCustomBlock(
	identifier = "MULTIBLOCK_WORKBENCH",
	blockData = CustomBlocks.mushroomBlockData(setOf(BlockFace.NORTH, BlockFace.DOWN, BlockFace.EAST)),
	drops = BlockLoot(
		requiredTool = null,
		drops = customItemDrop(CustomItems::MULTIBLOCK_WORKBENCH, 1)
	)
) {
	private val cooldown = PerPlayerCooldown(5L, TimeUnit.MILLISECONDS)
	private var multiblockIndex = 0
	val multiblocks = MultiblockRegistration.getAllMultiblocks().toList()

	override fun onRightClick(event: PlayerInteractEvent, block: Block) {
		val player = event.player

		cooldown.tryExec(player) { openMenu(player, block.location.toCenterLocation()) }
	}

	private fun openMenu(player: Player, location: Location) {
		val inv = MultiblockWorkbenchMenu(player, location)
		InteractableGUI.setInventory(player.uniqueId, inv)
		inv.open()
	}

	class MultiblockWorkbenchMenu(viewer: Player, val location: Location): InteractableGUI(viewer) {
		override val inventorySize = 36
		override val internalInventory: Inventory = IonServer.server.createInventory(this, inventorySize)

		override fun setup(view: InventoryView) {
			addGuiButton(18, ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta {
				it.setCustomModelData(GuiItem.LEFT.customModelData)
				it.displayName(text("Previous Multiblock").itemName)
			}) {
				multiblockIndex = (multiblockIndex - 1).coerceAtLeast(0)
				updateMultiblock(it.view)
			}

			addGuiButton(20, ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta {
				it.setCustomModelData(GuiItem.RIGHT.customModelData)
				it.displayName(text("Next Multiblock").itemName)
			}) {
				multiblockIndex = (multiblockIndex + 1).coerceAtMost(multiblocks.lastIndex)
				updateMultiblock(it.view)
			}

			noDropSlots.add(19)

			view.setTitle(getGuiText(empty()))
		}

		private fun getGuiText(secondLine: Component): Component = GuiText("Multiblock Workbench")
			.setSlotOverlay(
				"# # # # # # # # #",
				"# # # . . . . . .",
				"# . # . . . . . .",
				"# # # . . . . . ."
			)
			.add(secondLine, line = 0)
			.build()

		private fun setSecondLine(view: InventoryView, secondLine: Component) {
			view.setTitle(getGuiText(secondLine))
		}

		private fun updateMultiblock(view: InventoryView) {
			val atIndex = multiblocks[multiblockIndex]
			setSecondLine(view, atIndex.getDisplayName())
		}

		private fun isLockedSlot(slot: Int): Boolean {
			if (slot in 0..11) return true
			if (slot in 18..20) return true
			return slot in 27..29
		}

		override fun getInventory(): Inventory {
			return internalInventory
		}

		override fun itemChanged(changedSlot: Int, changedItem: ItemStack) {}

		override fun canRemove(slot: Int, player: Player): Boolean {
			return !isLockedSlot(slot)
		}

		override fun canAdd(itemStack: ItemStack, slot: Int, player: Player): Boolean {
			return !isLockedSlot(slot)
		}

		override fun handleClose(event: InventoryCloseEvent) {
			for ((slot, content) in inventory.contents.withIndex()) {
				if (noDropSlots.contains(slot)) continue
				viewer.world.dropItemNaturally(location, content ?: continue)
			}
		}
	}
}
