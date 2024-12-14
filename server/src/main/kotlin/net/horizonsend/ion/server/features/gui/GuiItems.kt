package net.horizonsend.ion.server.features.gui

import io.papermc.paper.datacomponent.DataComponentTypes
import net.horizonsend.ion.server.features.nations.gui.skullItem
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.setDisplayNameAndGet
import net.horizonsend.ion.server.miscellaneous.utils.text.itemName
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
import xyz.xenondevs.invui.item.impl.controlitem.ControlItem
import xyz.xenondevs.invui.item.impl.controlitem.PageItem
import xyz.xenondevs.invui.item.impl.controlitem.ScrollItem
import java.util.UUID

object GuiItems {
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
        override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
            val builder = ItemBuilder(itemStack.apply {
				setData(DataComponentTypes.CUSTOM_NAME, text.itemName)
            })
            return builder
        }
    }

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
}

enum class GuiItem(val modelKey: Key) : ItemProvider {
    EMPTY(NamespacedKeys.packKey("ui/empty")),
    UP(NamespacedKeys.packKey("ui/up")),
    RIGHT(NamespacedKeys.packKey("ui/right")),
    DOWN(NamespacedKeys.packKey("ui/down")),
    LEFT(NamespacedKeys.packKey("ui/left")),
    CHECKMARK(NamespacedKeys.packKey("ui/checkmark")),
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

	fun makeItem(name: Component): ItemStack = ItemStack(Material.WARPED_FUNGUS_ON_A_STICK)
		.applyGuiModel(this)
		.setDisplayNameAndGet(name)

	override fun get(p0: String?): ItemStack {
		return ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).applyGuiModel(this)
	}

	companion object {
		fun ItemStack.applyGuiModel(model: GuiItem): ItemStack {
			setData(DataComponentTypes.ITEM_MODEL, model.modelKey)
			return this
		}
	}
}

