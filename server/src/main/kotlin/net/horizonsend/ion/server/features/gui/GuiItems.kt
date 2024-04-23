package net.horizonsend.ion.server.features.gui

import net.horizonsend.ion.server.features.achievements.Achievements
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.builder.ItemBuilder
import xyz.xenondevs.invui.item.impl.controlitem.PageItem

object GuiItems {

    private const val UI_LEFT = 105
    private const val UI_RIGHT = 103

    class LeftItem : PageItem(false) {

        override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
            val builder = ItemBuilder(ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta {
                it.setCustomModelData(UI_LEFT)
                it.displayName(Component.empty())
            })
            return builder
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            super.handleClick(clickType, player, event)
            // Player may be viewing two GUIs
            val windows = this.gui.findAllWindows().filter { it.viewer == player }
            for (window in windows)
                window.changeTitle(AdventureComponentWrapper(Achievements.createAchievementText(player, this.gui)))
        }
    }

    class RightItem : PageItem(true) {

        override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
            val builder = ItemBuilder(ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta {
                it.setCustomModelData(UI_RIGHT)
                it.displayName(Component.empty())
            })
            return builder
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            super.handleClick(clickType, player, event)
            // Player may be viewing two GUIs
            val windows = this.gui.findAllWindows().filter { it.viewer == player }
            for (window in windows)
                window.changeTitle(AdventureComponentWrapper(Achievements.createAchievementText(player, this.gui)))
        }
    }
}