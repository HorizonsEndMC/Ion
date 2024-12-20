package net.horizonsend.ion.server.features.gui

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.horizonsend.ion.common.utils.text.wrap
import net.horizonsend.ion.server.features.gui.custom.settings.SettingsPageGui
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.nations.gui.skullItem
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.text.itemLore
import net.horizonsend.ion.server.miscellaneous.utils.text.itemName
import net.horizonsend.ion.server.miscellaneous.utils.updateData
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
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
import xyz.xenondevs.invui.item.impl.SimpleItem
import xyz.xenondevs.invui.item.impl.controlitem.ControlItem
import xyz.xenondevs.invui.item.impl.controlitem.PageItem
import xyz.xenondevs.invui.item.impl.controlitem.ScrollItem
import java.util.UUID

@Suppress("UnstableApiUsage")
object GuiItems {
    class CustomControlItem(
        private val name: Component,
        private val customGuiItem: GuiItem,
        private val loreList: List<Component>? = null,
        private val callback: (ClickType, Player, InventoryClickEvent) -> Unit = { _: ClickType, _: Player, _: InventoryClickEvent -> }
    ) : ControlItem<Gui>() {
        override fun getItemProvider(gui: Gui): ItemProvider {
            return ItemBuilder(ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).apply {
                setData(DataComponentTypes.ITEM_MODEL, customGuiItem.modelKey)
                setData(DataComponentTypes.CUSTOM_NAME, name)
                if (loreList != null)
                {
                    setData(DataComponentTypes.LORE, ItemLore.lore(loreList))
                }
            })
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            callback(clickType, player, event)
            notifyWindows()
        }
    }

    class CustomItemControlItem(
        private val customItem: CustomItem,
        private val loreList: List<Component>? = null,
        private val callback: (ClickType, Player, InventoryClickEvent) -> Unit
    ) : ControlItem<Gui>() {
        override fun getItemProvider(gui: Gui): ItemProvider {
            return ItemBuilder(customItem.constructItemStack().apply {
                if (loreList != null)
                {
                    setData(DataComponentTypes.LORE, ItemLore.lore(loreList))
                }
            })
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            callback(clickType, player, event)
            notifyWindows()
        }
    }

    class PageLeftItem : PageItem(false) {
        override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
            val builder = if (gui.hasPreviousPage()) ItemBuilder(ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).apply {
				setData(DataComponentTypes.ITEM_MODEL, GuiItem.LEFT.modelKey)
				setData(DataComponentTypes.CUSTOM_NAME, text("Previous Page").itemName)
			}) else ItemBuilder(ItemStack(Material.AIR))
            return builder
        }
    }

    class PageRightItem : PageItem(true) {
        override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
            val builder = if (gui.hasNextPage()) ItemBuilder(ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).apply {
				setData(DataComponentTypes.ITEM_MODEL, GuiItem.RIGHT.modelKey)
				setData(DataComponentTypes.CUSTOM_NAME, text("Next Page").itemName)
			}) else ItemBuilder(ItemStack(Material.AIR))
            return builder
        }
    }
    class ScrollLeftItem : ScrollItem(-1) {
        override fun getItemProvider(gui: ScrollGui<*>): ItemProvider {
            val builder = if (gui.canScroll(-1)) ItemBuilder(ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).apply {
				setData(DataComponentTypes.ITEM_MODEL, GuiItem.LEFT.modelKey)
				setData(DataComponentTypes.CUSTOM_NAME, text("Scroll Left").itemName)
			}) else ItemBuilder(ItemStack(Material.AIR))
            return builder
        }
    }

    class ScrollRightItem : ScrollItem(1) {
        override fun getItemProvider(gui: ScrollGui<*>): ItemProvider {
            val builder = if (gui.canScroll(1)) ItemBuilder(ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).apply {
				setData(DataComponentTypes.ITEM_MODEL, GuiItem.RIGHT.modelKey)
				setData(DataComponentTypes.CUSTOM_NAME, text("Scroll Right").itemName)
			}) else ItemBuilder(ItemStack(Material.AIR))
            return builder
        }
    }

    abstract class AbstractButtonItem(val text: Component, val itemStack: ItemStack) : ControlItem<PagedGui<*>>() {
        override fun getItemProvider(gui: PagedGui<*>): ItemProvider = ItemBuilder(itemStack.updateDisplayName(text))
    }

    class EmptyItem : SimpleItem(ItemStack(Material.AIR))

    class BlankButton(val item: Item) : ControlItem<Gui>() {
        override fun getItemProvider(gui: Gui): ItemProvider {
            return ItemBuilder(ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).apply {
				setData(DataComponentTypes.ITEM_MODEL, GuiItem.EMPTY.modelKey)
				setData(DataComponentTypes.CUSTOM_NAME, item.itemProvider.get().displayName().itemName)
			})
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) = item.handleClick(clickType, player, event)
    }

	val blankItem get() = ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).apply {
		setData(DataComponentTypes.ITEM_MODEL, GuiItem.EMPTY.modelKey)
		setData(DataComponentTypes.CUSTOM_NAME, empty())
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

    fun closeMenuItem(player: Player) = CustomControlItem(text("Close Menu"), GuiItem.CANCEL) {
            _: ClickType, _: Player, _: InventoryClickEvent -> player.closeInventory()
    }
}

enum class GuiItem(val modelKey: Key) : ItemProvider {
    EMPTY(NamespacedKeys.packKey("ui/empty")),
    UP(NamespacedKeys.packKey("ui/up")),
    RIGHT(NamespacedKeys.packKey("ui/right")),
    DOWN(NamespacedKeys.packKey("ui/down")),
    LEFT(NamespacedKeys.packKey("ui/left")),
    CHECKMARK(NamespacedKeys.packKey("ui/checkmark")),
    CANCEL(NamespacedKeys.packKey("ui/cancel")),
    ROUTE_CANCEL(NamespacedKeys.packKey("ui/route_cancel")),
    ROUTE_UNDO(NamespacedKeys.packKey("ui/route_undo")),
    ROUTE_JUMP(NamespacedKeys.packKey("ui/route_jump")),
    ROUTE_CANCEL_GRAY(NamespacedKeys.packKey("ui/route_cancel_gray")),
    ROUTE_UNDO_GRAY(NamespacedKeys.packKey("ui/route_undo_gray")),
    ROUTE_JUMP_GRAY(NamespacedKeys.packKey("ui/route_jump_gray")),
    ROUTE_SEGMENT_2(NamespacedKeys.packKey("ui/route_segment_2")),
    MAGNIFYING_GLASS(NamespacedKeys.packKey("ui/magnifying_glass")),
    MAGNIFYING_GLASS_GRAY(NamespacedKeys.packKey("ui/magnifying_glass_gray")),
    ASTERI_2(NamespacedKeys.packKey("planet/asteri_2")),
    HORIZON_2(NamespacedKeys.packKey("planet/horizon_2")),
    ILIOS_2(NamespacedKeys.packKey("planet/ilios_2")),
    REGULUS_2(NamespacedKeys.packKey("planet/regulus_2")),
    SIRIUS_2(NamespacedKeys.packKey("planet/sirius_2")),
    AERACH_2(NamespacedKeys.packKey("planet/aerach_2")),
    ARET_2(NamespacedKeys.packKey("planet/aret_2")),
    CHANDRA_2(NamespacedKeys.packKey("planet/chandra_2")),
    CHIMGARA_2(NamespacedKeys.packKey("planet/chimgara_2")),
    DAMKOTH_2(NamespacedKeys.packKey("planet/damkoth_2")),
    DISTERRA_2(NamespacedKeys.packKey("planet/disterra_2")),
    EDEN_2(NamespacedKeys.packKey("planet/eden_2")),
    GAHARA_2(NamespacedKeys.packKey("planet/gahara_2")),
    HERDOLI_2(NamespacedKeys.packKey("planet/herdoli_2")),
    ILIUS_2(NamespacedKeys.packKey("planet/ilius_2")),
    ISIK_2(NamespacedKeys.packKey("planet/isik_2")),
    KOVFEFE_2(NamespacedKeys.packKey("planet/kovfefe_2")),
    KRIO_2(NamespacedKeys.packKey("planet/krio_2")),
    LIODA_2(NamespacedKeys.packKey("planet/lioda_2")),
    LUXITERNA_2(NamespacedKeys.packKey("planet/luxiterna_2")),
    QATRA_2(NamespacedKeys.packKey("planet/qatra_2")),
    RUBACIEA_2(NamespacedKeys.packKey("planet/rubaciea_2")),
    TURMS_2(NamespacedKeys.packKey("planet/turms_2")),
    VASK_2(NamespacedKeys.packKey("planet/vask_2")),
    EMPTY_STAR(NamespacedKeys.packKey("planet/empty_star")),
    STARFIGHTER(NamespacedKeys.packKey("ui/starfighter")),
    GUNSHIP(NamespacedKeys.packKey("ui/gunship")),
    CORVETTE(NamespacedKeys.packKey("ui/corvette")),
    FRIGATE(NamespacedKeys.packKey("ui/frigate")),
    DESTROYER(NamespacedKeys.packKey("ui/destroyer")),
    CRUISER(NamespacedKeys.packKey("ui/cruiser")),
    BATTLECRUISER(NamespacedKeys.packKey("ui/battlecruiser")),
    SHUTTLE(NamespacedKeys.packKey("ui/shuttle")),
    TRANSPORT(NamespacedKeys.packKey("ui/transport")),
    LIGHT_FREIGHTER(NamespacedKeys.packKey("ui/light_freighter")),
    MEDIUM_FREIGHTER(NamespacedKeys.packKey("ui/medium_freighter")),
    HEAVY_FREIGHTER(NamespacedKeys.packKey("ui/heavy_freighter")),
    BARGE(NamespacedKeys.packKey("ui/barge")),
    PLANET(NamespacedKeys.packKey("ui/planet")),
    STAR(NamespacedKeys.packKey("ui/star")),
    BEACON(NamespacedKeys.packKey("ui/beacon")),
    STATION(NamespacedKeys.packKey("ui/station")),
    GENERIC_STARSHIP(NamespacedKeys.packKey("ui/generic_starship")),
    ROUTE_SEGMENT(NamespacedKeys.packKey("ui/route_segment")),
    LIST(NamespacedKeys.packKey("ui/list")),
    COMPASS_NEEDLE(NamespacedKeys.packKey("ui/compass_needle")),
    BOOKMARK(NamespacedKeys.packKey("ui/bookmark")),
    SOUND(NamespacedKeys.packKey("ui/sound")),

	;

	fun makeItem(): ItemStack = ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).applyGuiModel(this)

	fun makeItem(name: Component): ItemStack = makeItem().updateDisplayName(name)

	override fun get(p0: String?): ItemStack {
		return makeItem()
	}

	fun makeButton(settingsPage: SettingsPageGui, name: String, lore: String, handleClick: (Player, PagedGui<*>, SettingsPageGui) -> Unit) : GuiItems.AbstractButtonItem =
		makeButton(settingsPage, text(name), lore, handleClick)

	fun makeButton(settingsPage: SettingsPageGui, name: Component, lore: String, handleClick: (Player, PagedGui<*>, SettingsPageGui) -> Unit) : GuiItems.AbstractButtonItem {
		val splitLore = text(lore).itemLore.wrap(150)

		return object : GuiItems.AbstractButtonItem(name, makeItem().updateLore(splitLore)) {
			override fun handleClick(type: ClickType, player: Player, event: InventoryClickEvent) {
				handleClick.invoke(player, gui, settingsPage)
			}
		}
	}

	companion object {
		fun ItemStack.applyGuiModel(model: GuiItem): ItemStack = updateData(DataComponentTypes.ITEM_MODEL, model.modelKey)
	}
}

