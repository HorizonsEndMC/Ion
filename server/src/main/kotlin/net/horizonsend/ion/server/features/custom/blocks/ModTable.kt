package net.horizonsend.ion.server.features.custom.blocks

import net.horizonsend.ion.server.features.custom.items.CustomItems
import net.horizonsend.ion.server.features.custom.items.CustomItems.customItem
import net.horizonsend.ion.server.features.custom.items.mods.items.ModificationItem
import net.horizonsend.ion.server.features.custom.items.objects.ModdedCustomItem
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.custom.CustomGUI
import net.horizonsend.ion.server.features.gui.custom.slot.NormalSlot
import net.kyori.adventure.text.Component.empty
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.BlockFace.DOWN
import org.bukkit.block.BlockFace.EAST
import org.bukkit.block.BlockFace.NORTH
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
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

		gui.addSlot(10, NormalSlot(
			10,
			gui,
			canAdd = { it.customItem is ModdedCustomItem },
			canRemove = { true }
		).withListener { gui.rebuildData() })

		for (slot in 1..initialSlots) {
			gui.addSlot(11 + slot, NormalSlot(
				11 + initialSlots,
				gui,
				canAdd = { it.customItem is ModificationItem },
				canRemove = { true }
			).withListener { gui.rebuildData() })
		}

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

	class ModTableMenu(location: Location, var modSlots: Int) : CustomGUI(location, 9, 3) {
		fun rebuildData() {
			println("Triggered rebuild")
			Throwable().printStackTrace()

			val toolSlot = slots[10]!!
			val item = toolSlot.getGuiItem()

			println("Slot: $toolSlot Item: $item")
		}
	}
}
