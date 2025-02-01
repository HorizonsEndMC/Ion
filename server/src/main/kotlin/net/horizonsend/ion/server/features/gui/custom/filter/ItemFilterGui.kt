package net.horizonsend.ion.server.features.gui.custom.filter

import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems.createButton
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.GuiWrapper
import net.horizonsend.ion.server.features.gui.interactable.InteractableGUI.Companion.setTitle
import net.horizonsend.ion.server.features.transport.filters.FilterData
import net.horizonsend.ion.server.features.transport.filters.FilterEntry
import net.horizonsend.ion.server.features.transport.filters.FilterMeta
import net.horizonsend.ion.server.features.transport.filters.FilterMethod
import net.horizonsend.ion.server.features.transport.filters.FilterType
import net.horizonsend.ion.server.features.transport.filters.manager.FilterManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.block.TileState
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.window.Window
import java.util.function.Supplier

class ItemFilterGui(val viewer: Player, private val data: FilterData<ItemStack, FilterMeta.ItemFilterMeta>, private val tileState: Supplier<TileState>) : GuiWrapper {
	private var currentWindow: Window? = null

	val whitelistText get() = if (data.isWhitelist) text("Whitelist") else text("Blacklist")

	val toggleWhitelistButton = createButton(GuiItem.CRUISER.makeItem(whitelistText)) { _, _, event ->
		data.isWhitelist = !data.isWhitelist
		updateSlotOverlay(event.view)
	}

	override fun open() {
		val gui = Gui.normal()
			.setStructure(
				"1 2 3 4 5 6 x z x",
				"a b c d e f x x x",
			)
			.addIngredient('z', toggleWhitelistButton)

		@Suppress("UNCHECKED_CAST")
		val filterEntries = data.entries.take(6).toMutableList() as MutableList<FilterEntry<ItemStack, FilterMeta.ItemFilterMeta>>

		// Pad list
		if (filterEntries.size < 6) repeat(6 - filterEntries.size) {
			filterEntries.add(FilterType.ItemType.buildEmptyEntry())
		}

		data.entries = filterEntries

		for (slot in data.entries.indices) {
			val entry = data.entries[slot] as FilterEntry<ItemStack, FilterMeta.ItemFilterMeta>
			gui.addIngredient('1' + slot, filterSlotProvider(entry))
			gui.addIngredient('a' + slot, strictnessSlotProvider(entry))
		}

		val window = Window
			.single()
			.setGui(gui)
			.setTitle(AdventureComponentWrapper(getSlotOverlay()))
			.build(viewer)

		currentWindow = window.apply { open() }
	}

	fun getSlotOverlay(): Component = GuiText("Item Filter")
		.setSlotOverlay(
			". . . . . . # # #",
			"# # # # # # # # #",
		)
		.add(
			component = whitelistText,
			alignment = GuiText.TextAlignment.CENTER,
			horizontalShift = 55,
			verticalShift = 20
		)
		.build()

	fun updateSlotOverlay(view: InventoryView) {
		view.setTitle(getSlotOverlay())
	}

	fun filterSlotProvider(entry: FilterEntry<ItemStack, FilterMeta.ItemFilterMeta>) = object : AbstractItem() {
		override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
			val cursor = event.cursor

			if (cursor.isEmpty) {
				entry.value = null
			} else {
				entry.value = cursor.asQuantity(1)
			}

			notifyWindows()
			updateSlotOverlay(event.view)

			FilterManager.save(tileState.get(), data)
		}

		override fun getItemProvider(viewer: Player): ItemProvider = provider
		val provider = ItemProvider {
			(data.type as FilterType.ItemType).toItem(data.type.cast(entry)) ?: ItemStack.empty()
		}
	}

	fun strictnessSlotProvider(entry: FilterEntry<ItemStack, FilterMeta.ItemFilterMeta>) = object : AbstractItem() {
		override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
			val currentIndex = entry.metaData.filterMethod.ordinal
			val size = FilterMethod.entries.size
			val nextIndex = (currentIndex + 1) % size

			entry.metaData.filterMethod = FilterMethod.entries[nextIndex]

			notifyWindows()
			updateSlotOverlay(event.view)

			FilterManager.save(tileState.get(), data)
		}

		override fun getItemProvider(viewer: Player): ItemProvider = provider
		val provider = ItemProvider { entry.metaData.filterMethod.icon }
	}
}
