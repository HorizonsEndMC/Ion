package net.horizonsend.ion.server.features.gui

import net.horizonsend.ion.server.features.custom.items.CustomItem
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
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.builder.ItemBuilder
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.item.impl.SimpleItem
import xyz.xenondevs.invui.item.impl.controlitem.ControlItem
import xyz.xenondevs.invui.item.impl.controlitem.PageItem
import java.util.UUID

object GuiItems {

    class CustomControlItem(
        private val name: String,
        private val customGuiItem: GuiItem,
        private val lore: List<Component>? = null,
        private val callback: (ClickType, Player, InventoryClickEvent) -> Unit = { _: ClickType, _: Player, _: InventoryClickEvent -> }
    ) : ControlItem<Gui>() {
        override fun getItemProvider(gui: Gui): ItemProvider {
            return ItemBuilder(ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta {
                it.setCustomModelData(customGuiItem.customModelData)
                it.displayName(text(name).decoration(ITALIC, false))
                it.lore(lore)
            })
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            callback(clickType, player, event)
            notifyWindows()
        }
    }

    class CustomItemControlItem(
        private val customItem: CustomItem,
        private val lore: List<Component>? = null,
        private val callback: (ClickType, Player, InventoryClickEvent) -> Unit
    ) : ControlItem<Gui>() {
        override fun getItemProvider(gui: Gui): ItemProvider {
            return ItemBuilder(customItem.constructItemStack().updateMeta {
                it.lore(lore)
            })
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            callback(clickType, player, event)
            notifyWindows()
        }
    }

    class LeftPageItem : PageItem(false) {
        override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
            val builder = if (gui.hasPreviousPage()) ItemBuilder(ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta {
                it.setCustomModelData(GuiItem.LEFT.customModelData)
                it.displayName(text("Previous Page").decoration(ITALIC, false))
            }) else ItemBuilder(ItemStack(Material.AIR))
            return builder
        }
    }

    class RightPageItem : PageItem(true) {
        override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
            val builder = if (gui.hasNextPage()) ItemBuilder(ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta {
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

    class EmptyItem : SimpleItem(ItemStack(Material.AIR))

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

    fun closeMenuItem(player: Player) = CustomControlItem("Close Menu", GuiItem.CANCEL) {
            _: ClickType, _: Player, _: InventoryClickEvent -> player.closeInventory()
    }
}

enum class GuiItem(val customModelData: Int) {
    EMPTY(101),
    UP(102),
    RIGHT(103),
    DOWN(104),
    LEFT(105),
    CANCEL(106),
    ROUTE_CANCEL(107),
    ROUTE_UNDO(108),
    ROUTE_JUMP(109),
    ROUTE_CANCEL_GRAY(110),
    ROUTE_UNDO_GRAY(111),
    ROUTE_JUMP_GRAY(112),
    ROUTE_SEGMENT_2(113),
    CHECKMARK(114),
    MAGNIFYING_GLASS(115),
    MAGNIFYING_GLASS_GRAY(116),
    ASTERI_2(5105),
    HORIZON_2(5106),
    ILIOS_2(5107),
    REGULUS_2(5108),
    SIRIUS_2(5109),
    AERACH_2(5200),
    ARET_2(5201),
    CHANDRA_2(5202),
    CHIMGARA_2(5203),
    DAMKOTH_2(5204),
    DISTERRA_2(5205),
    EDEN_2(5206),
    GAHARA_2(5207),
    HERDOLI_2(5208),
    ILIUS_2(5209),
    ISIK_2(5210),
    KOVFEFE_2(5211),
    KRIO_2(5212),
    LIODA_2(5213),
    LUXITERNA_2(5214),
    QATRA_2(5215),
    RUBACIEA_2(5216),
    TURMS_2(5217),
    VASK_2(5218),
    EMPTY_STAR(5219),
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
