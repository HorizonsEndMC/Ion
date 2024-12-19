package net.horizonsend.ion.server.features.gui.custom.navigation

import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.builder.ItemBuilder
import xyz.xenondevs.invui.item.impl.SimpleItem
import xyz.xenondevs.invui.item.impl.controlitem.ControlItem
import xyz.xenondevs.invui.window.AnvilWindow
import xyz.xenondevs.invui.window.Window

class NavigationSearchGui(val player: Player) {

    private var currentWindow: Window? = null
    private var currentQuery: String = ""

    private val searchButton = SearchButton()

    private fun createGui(): Gui {
        val gui = Gui.normal()

        gui.setStructure(". v x")

        gui.addIngredient('.', renameButton)
            .addIngredient('v', returnButton)
            .addIngredient('x', searchButton)

        return gui.build()
    }

    /**
     * Opens the Navigation Search GUI
     */
    fun open(player: Player): Window {
        val gui = createGui()

        val window = AnvilWindow.single()
            .setViewer(player)
            .setGui(gui)
            .setTitle(AdventureComponentWrapper(Component.text("Search Destination")))
            .addRenameHandler { string ->
                currentQuery = string
                searchButton.notifyWindows()
            }
            .build()

        return window
    }

    fun openMainWindow() {
        currentWindow = open(player).apply { open() }
    }

    private val renameButton = SimpleItem(ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta {
        it.displayName(Component.text("").decoration(TextDecoration.ITALIC, false))
        it.setCustomModelData(GuiItem.EMPTY.customModelData)
    })

    private val returnButton = GuiItems.CustomControlItem("Return To System Map", GuiItem.DOWN) {
        _: ClickType, player: Player, _: InventoryClickEvent -> NavigationSystemMapGui(player, player.world).openMainWindow()
    }

    private inner class SearchButton : ControlItem<Gui>() {
        override fun getItemProvider(gui: Gui?): ItemProvider {
            if (currentQuery.isNotBlank()) {
                return ItemBuilder(ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta {
                    it.setCustomModelData(GuiItem.MAGNIFYING_GLASS.customModelData)
                    it.displayName(Component.text("Search").decoration(TextDecoration.ITALIC, false))
                })
            } else {
                return ItemBuilder(ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta {
                    it.setCustomModelData(GuiItem.MAGNIFYING_GLASS_GRAY.customModelData)
                    it.displayName(Component.text("Enter a search query").decoration(TextDecoration.ITALIC, false))
                })
            }
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
        }
    }
}