package net.horizonsend.ion.server.gui.invui.misc.util.input

import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.item.AsyncItem
import net.horizonsend.ion.server.gui.CommonGuiWrapper
import net.horizonsend.ion.server.gui.invui.ListInvUIWindow
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
) : ListInvUIWindow<Item>(viewer) {
	override val listingsPerPage: Int = 36
	override fun generateEntries(): List<Item> = guiItems
	override fun createItem(entry: Item): Item = entry

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
			.setContent(items)
			.handlePageChange()
			.build()

        return gui
    }

	override fun buildTitle(): Component {
		return withPageNumber(title)
	}

	override fun buildWindow(): Window = normalWindow(createGui())

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
			resultConsumer: CommonGuiWrapper.(ClickType, T) -> Unit,
			itemTransformer: (T) -> ItemStack,
			backButtonHandler: (Player) -> Unit
		) {
			lateinit var menu: ItemMenu

			val mapped = entries.map { entry ->
				AsyncItem(
					resultProvider = { itemTransformer.invoke(entry) },
					handleClick = { event -> resultConsumer.invoke(menu, event.click, entry) }
				)
			}

			menu = ItemMenu(title, player, mapped, backButtonHandler)
			menu.openGui()
		}
	}
}
