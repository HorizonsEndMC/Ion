package net.horizonsend.ion.server.features.gui.custom.settings

import net.horizonsend.ion.server.features.sidebar.MainSidebar
import net.horizonsend.ion.server.features.sidebar.command.SidebarContactsCommand
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.builder.ItemBuilder
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.item.impl.controlitem.ControlItem
import xyz.xenondevs.invui.window.AnvilWindow

object SettingsSidebarContactsRangeGui {

    private fun createGui(): Gui {
        val gui = Gui.normal()

        gui.setStructure(". v x")

        gui.addIngredient('x', SetContactsDistanceButton())
            .addIngredient('.', RenameItem())
            .addIngredient('v', SettingsSidebarContactsGui.ReturnToSidebarContactsButton())

        return gui.build()
    }

    fun open(player: Player) {
        val gui = createGui()

        val window = AnvilWindow.single()
            .setViewer(player)
            .setTitle(AdventureComponentWrapper(text("Set Contacts Distance")))
            .setGui(gui)
            .build()

        window.open()
    }

    class SetContactsDistanceButton : ControlItem<Gui>() {
        override fun getItemProvider(gui: Gui?): ItemProvider {
            val builder = ItemBuilder(ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta {
                it.setCustomModelData(UI_RIGHT)
                it.displayName(text("Set Contacts Distance").decoration(ITALIC, false))
            })
            return builder
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            val currentWindow = (windows.find { it.viewer == player })
            val anvilWindow = if (currentWindow is AnvilWindow) currentWindow else return
            val currentText = anvilWindow.renameText ?: return
            val currentInt = currentText.toIntOrNull() ?: return

            SidebarContactsCommand.onSetContactsDistance(player, currentInt)
            SettingsSidebarContactsGui.open(player)
        }

        companion object {
            private const val UI_RIGHT = 103
        }
    }

    class RenameItem : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return ItemBuilder(ItemStack(Material.PAPER).updateMeta {
                it.displayName(text("Enter Range (0-${MainSidebar.CONTACTS_RANGE})").decoration(ITALIC, false))
            })
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {}
    }
}