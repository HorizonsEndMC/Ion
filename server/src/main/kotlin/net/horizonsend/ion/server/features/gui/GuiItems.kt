package net.horizonsend.ion.server.features.gui

import net.horizonsend.ion.server.features.nations.gui.skullItem
import net.horizonsend.ion.server.miscellaneous.utils.text.itemName
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextDecoration.ITALIC
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.ScrollGui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.builder.ItemBuilder
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.item.impl.controlitem.ControlItem
import xyz.xenondevs.invui.item.impl.controlitem.PageItem
import xyz.xenondevs.invui.item.impl.controlitem.ScrollItem
import java.util.UUID

object GuiItems {
    class PageLeftItem : PageItem(false) {
        override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
            val builder = if (gui.hasPreviousPage()) ItemBuilder(ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta {
                it.setCustomModelData(GuiItem.LEFT.customModelData)
                it.displayName(text("Previous Page").decoration(ITALIC, false))
            }) else ItemBuilder(ItemStack(Material.AIR))
            return builder
        }
    }

    class PageRightItem : PageItem(true) {
        override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
            val builder = if (gui.hasNextPage()) ItemBuilder(ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta {
                it.setCustomModelData(GuiItem.RIGHT.customModelData)
                it.displayName(text("Next Page").decoration(ITALIC, false))
            }) else ItemBuilder(ItemStack(Material.AIR))
            return builder
        }
    }
    class ScrollLeftItem : ScrollItem(-1) {
        override fun getItemProvider(gui: ScrollGui<*>): ItemProvider {
            val builder = if (gui.canScroll(-1)) ItemBuilder(ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta {
                it.setCustomModelData(GuiItem.LEFT.customModelData)
                it.displayName(text("Previous Page").decoration(ITALIC, false))
            }) else ItemBuilder(ItemStack(Material.AIR))
            return builder
        }
    }

    class ScrollRightItem : ScrollItem(1) {
        override fun getItemProvider(gui: ScrollGui<*>): ItemProvider {
            val builder = if (gui.canScroll(1)) ItemBuilder(ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta {
                it.setCustomModelData(GuiItem.RIGHT.customModelData)
                it.displayName(text("Next Page").decoration(ITALIC, false))
            }) else ItemBuilder(ItemStack(Material.AIR))
            return builder
        }
    }

    abstract class AbstractButtonItem(val text: Component, val itemStack: ItemStack) : ControlItem<PagedGui<*>>() {
        override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
            val builder = ItemBuilder(itemStack.updateMeta {
                it.displayName(text)
            })
            return builder
        }
    }

    class BlankButton(val item: Item) : ControlItem<Gui>() {
        override fun getItemProvider(gui: Gui): ItemProvider {
            return ItemBuilder(ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta {
                it.setCustomModelData(GuiItem.EMPTY.customModelData)
                it.displayName(item.itemProvider.get().displayName().itemName)
            })
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) = item.handleClick(clickType, player, event)
    }

	val blankItem get() = ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta {
		it.displayName(empty())
		it.setCustomModelData(GuiItem.EMPTY.customModelData)
	}

	fun createButton(displayItem: ItemStack, clickHandler: (ClickType, Player, InventoryClickEvent) -> Unit) = object : AbstractItem() {
		override fun getItemProvider(): ItemProvider = ItemProvider { displayItem }

		override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
			clickHandler.invoke(clickType, player, event)
		}
	}

    open class PlayerHeadItem(val uuid: UUID, val name: String, val callback: () -> Unit = {}) : ControlItem<Gui>() {
        override fun getItemProvider(gui: Gui): ItemProvider {
            return ItemBuilder(skullItem(uuid, name))
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) { callback() }
    }
}

enum class GuiItem(val customModelData: Int) {
    EMPTY(101),
    UP(102),
    RIGHT(103),
    DOWN(104),
    LEFT(105),
    CHECKMARK(114),
    STARFIGHTER(6000),
    GUNSHIP(6001),
    CORVETTE(6002),
    FRIGATE(6003),
    DESTROYER(6004),
    CRUISER(6005),
    BATTLECRUISER(6006),
    SHUTTLE(6008),
    TRANSPORT(6009),
    LIGHT_FREIGHTER(6010),
    MEDIUM_FREIGHTER(6011),
    HEAVY_FREIGHTER(6012),
    BARGE(6013),
    PLANET(6016),
    STAR(6017),
    BEACON(6018),
    STATION(6019),
    GENERIC_STARSHIP(6026),
    ROUTE_SEGMENT(6030),
    LIST(6034),
    COMPASS_NEEDLE(6035),
    BOOKMARK(6036),
    SOUND(6058),
}
