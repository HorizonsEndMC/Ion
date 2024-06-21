package net.horizonsend.ion.server.features.custom.blocks

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.custom.items.CustomItems
import net.horizonsend.ion.server.features.custom.items.CustomItems.customItem
import net.horizonsend.ion.server.features.custom.items.mods.items.ModificationItem
import net.horizonsend.ion.server.features.custom.items.objects.ModdedCustomItem
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.custom.CustomGUI
import net.horizonsend.ion.server.features.gui.custom.slot.GUISlot
import net.horizonsend.ion.server.features.gui.custom.slot.NormalSlot
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.BlockFace.DOWN
import org.bukkit.block.BlockFace.EAST
import org.bukkit.block.BlockFace.NORTH
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.window.Window

object ModTable : InteractableCustomBlock(
	identifier = "MOD_TABLE",
	blockData = CustomBlocks.mushroomBlockData(setOf(NORTH, DOWN, EAST)),
	drops = BlockLoot(
		requiredTool = null,
		drops = CustomBlocks.customItemDrop({ CustomItems.MOD_TABLE })
	)) {

	override fun onRightClick(event: PlayerInteractEvent, block: Block) {
		openGUI(event.player, block.location.toCenterLocation(), 3)
	}

	fun openGUI(viewer: Player, location: Location, initialSlots: Int) {
		val gui = ModTableMenu(location, initialSlots)

		val subtitle = empty()
		val titleText = GuiText("Modification Table")

		titleText.add(subtitle, 0)
		titleText.setSlotOverlay(
			"# # # # # # # # #",
			"# . # . . . . . #",
			"# # # # # # # # #"
		)

		val window = Window.single()
			.setViewer(viewer)
			.setTitle(AdventureComponentWrapper(titleText.build()))
			.setGui(gui)
			.addCloseHandler(gui.closeHandler)
			.build()

		window.open()
	}

	class ModTableMenu(location: Location, var numSlots: Int) : CustomGUI(location, 9, 3) {
		private var rebuilding = false
		private val modSlots = mutableListOf<GUISlot>()

		init {
			addSlot(10, NormalSlot(
				10,
				this,
				canAdd = { it.customItem is ModdedCustomItem },
				canRemove = { true }
			).withListener { rebuildItems(this) })

			for (slot in 1..numSlots) {
				val modSlot = NormalSlot(
					11 + slot,
					this,
					canAdd = { it.customItem is ModificationItem },
					canRemove = { true }
				).withListener { rebuildItems(this) }

				modSlots.add(modSlot)

				addSlot(11 + slot, modSlot)
			}
		}

		fun clearModSlots() {
			for (slot in modSlots) {
				modSlots.remove(slot)
				removeSlot(slot.index)
			}
		}

		private fun rebuildItems(slotChanged: GUISlot) {
			if (rebuilding) return
			println("Triggered rebuild")

			println("updated slot: $slotChanged")

			rebuilding = true

			// Tool changed
			if (slotChanged.index == 10) {
				println("tool update")
			}

			// Mods changed
			if (modSlots.contains(slotChanged)) {
				println("Mod update")
			}

			runCatching {
				val toolSlot = slots[10]!!
				val toolItem = toolSlot.getGuiItem() ?: return@runCatching //TODO

				val customTool = toolItem.itemProvider.get(null)
			}

			rebuilding = false
		}

		companion object {
			fun getModItems(itemStack: ItemStack): List<ItemStack> {
				val mods = mutableListOf<ItemStack>()
				val customItem = itemStack.customItem as? ModdedCustomItem ?: return mods

				return customItem.getMods(itemStack).mapNotNull { it.modItem.get()?.constructItemStack() }
			}
		}
	}

	class ModTableGUI : InventoryHolder {
		private val inventory: Inventory = IonServer.server.createInventory(this, 36, getTitle())
		private val internalInventory = IonServer.server.createInventory(this, 6, getTitle())

		override fun getInventory(): Inventory = inventory

		private fun getTitle(): Component {
			val subtitle = empty()
			val titleText = GuiText("Modification Table")

			titleText.add(subtitle, 0)
			titleText.setSlotOverlay(
				"# # # # # # # # #",
				"# . # . . . . . #",
				"# # # # # # # # #"
			)

			return titleText.build()
		}
	}
}
