package net.horizonsend.ion.server.features.gui.custom.misc

import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.item.AsyncItem
import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.horizonsend.ion.server.gui.invui.utils.setTitle
import net.horizonsend.ion.server.miscellaneous.utils.text.itemName
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window

class ItemMenu(
	val title: Component,
	viewer: Player,
	private val guiItems: List<Item>,
	private val backButtonHandler: (Player) -> Unit
) : InvUIWindowWrapper(viewer) {
    private fun createGui(): Gui {
		val gui = PagedGui.items()
			.setStructure(
				"x . . . . . . . .",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"l . . . . . . . r",
			)
			.addIngredient('x', backButton)
			.addIngredient('l', GuiItems.PageLeftItem())
			.addIngredient('r', GuiItems.PageRightItem())
			.addIngredient('#', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
			.setContent(guiItems)
			.build()

        return gui
    }

	override fun buildWindow(): Window {
		return Window.single()
			.setViewer(viewer)
			.setGui(createGui())
			.setTitle(title)
			.build()
	}

    private val backButton = GuiItems.CustomControlItem(
        Component.text("Back").itemName, GuiItem.CANCEL, callback = { _: ClickType, player: Player, _: InventoryClickEvent ->
            player.closeInventory()
            backButtonHandler.invoke(player)
        }
    )

	companion object {
		fun <T> selector(
			title: Component,
			player: Player,
			entries: Collection<T>,
			resultConsumer: (ClickType, T) -> Unit,
			itemTransformer: (T) -> ItemStack,
			backButtonHandler: (Player) -> Unit
		) {
			val mapped = entries.map { entry ->
				AsyncItem(
					resultProvider = { itemTransformer.invoke(entry) },
					handleClick = { event -> resultConsumer.invoke(event.click, entry) }
				)
			}

			ItemMenu(title, player, mapped, backButtonHandler).openGui()
		}
	}
}
