package net.horizonsend.ion.server.features.gui.custom.misc

import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.gui.invui.InvUIWrapper
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.window.Window
import kotlin.math.min

class ItemMenu(
	val title: Component,
	override val viewer: Player,
	private val guiItems: Collection<Item>,
	private val backButtonHandler: (Player) -> Unit
) : InvUIWrapper {
    private lateinit var gui: Gui

    private fun createGui(): Gui {
        val rows = ((guiItems.size + 8) / 9).coerceIn(1, 5) + 1
        // create a GUI that can store the number of elements in guiItems, plus an additional row for control items
        gui = Gui.empty(9, rows)

        // add all of guiItems or the first 45 items, whichever is smaller
        gui.addItems(*(guiItems.toList().subList(0, min(guiItems.size, 9 * 5))).toTypedArray())

        gui.setItem(0, rows - 1, backButton)

        return gui
    }

	override fun buildWindow(): Window {
		createGui()
		return Window.single()
			.setViewer(viewer)
			.setGui(gui)
			.setTitle(AdventureComponentWrapper(title))
			.build()
	}

    private val backButton = GuiItems.CustomControlItem(
        Component.text("Back"), GuiItem.DOWN, callback = { _: ClickType, player: Player, _: InventoryClickEvent ->
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
				object : AbstractItem() {
					val item = itemTransformer.invoke(entry)
					override fun getItemProvider(): ItemProvider = ItemProvider { item }

					override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
						resultConsumer.invoke(clickType, entry)
						backButtonHandler.invoke(player)
					}
				}
			}

			ItemMenu(title, player, mapped, backButtonHandler).openGui()
		}
	}
}
