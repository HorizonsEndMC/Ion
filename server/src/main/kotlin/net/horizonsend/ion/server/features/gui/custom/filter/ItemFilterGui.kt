package net.horizonsend.ion.server.features.gui.custom.filter

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.POWER_DRILL_BASIC
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems.createButton
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.GuiWrapper
import net.horizonsend.ion.server.features.gui.interactable.InteractableGUI.Companion.setTitle
import net.horizonsend.ion.server.features.transport.filters.FilterData
import net.horizonsend.ion.server.features.transport.filters.FilterEntry
import net.horizonsend.ion.server.features.transport.filters.FilterType
import net.horizonsend.ion.server.features.transport.filters.manager.FilterManager
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.updateData
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.horizonsend.ion.server.miscellaneous.utils.updatePersistentDataContainer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.block.CommandBlock
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.window.Window
import java.util.function.Supplier

var strictess = mutableMapOf<Int, Boolean>()

class ItemFilterGui(val viewer: Player, private val data: FilterData<ItemStack>, private val commandBlock: Supplier<CommandBlock>) : GuiWrapper {
	private var currentWindow: Window? = null

	val whitelistText get() = if (data.isWhitelist) text("Whitelist") else text("Blacklist")

	val toggleWhitelistButton = createButton(GuiItem.CRUISER.makeItem(whitelistText)) { _, _, event ->
		data.isWhitelist = !data.isWhitelist
		setSlotOverlay(event.view)
	}

	override fun open() {
		val gui = Gui.normal()
			.setStructure(
				"1 2 3 4 5 6 x z x",
				"a b c d e f x x x",
			)
			.addIngredient('z', toggleWhitelistButton)

		@Suppress("UNCHECKED_CAST")
		val filterEntries = data.entries.take(6).toMutableList() as MutableList<FilterEntry<ItemStack>>

		// Pad list
		if (filterEntries.size < 6) repeat(6 - filterEntries.size) {
			filterEntries.add(FilterEntry(null, FilterType.ItemType))
		}

		data.entries = filterEntries

		for (slot in data.entries.indices) {
			val entry = data.entries[slot] as FilterEntry<ItemStack>
			gui.addIngredient('1' + slot, filterSlotProvider(entry, slot))
			gui.addIngredient('a' + slot, strictnessSlotProvider(entry, slot))
		}

		val window = Window
			.single()
			.setGui(gui)
			.setTitle(AdventureComponentWrapper(getSlotOverlay()))
			.addCloseHandler {
				FilterManager.save(commandBlock.get(), data)
			}
			.build(viewer)

		currentWindow = window.apply { open() }
	}

	fun filterSlotProvider(entry: FilterEntry<ItemStack>, slot: Int) = object : AbstractItem() {
		override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
			val cursor = event.cursor

			if (cursor.isEmpty) {
				entry.value = null
			} else {
				entry.value = cursor
			}

			notifyWindows()
			setSlotOverlay(event.view)

			@Suppress("UNCHECKED_CAST")
			(data.entries as MutableList<FilterEntry<ItemStack>>).set(slot, entry)
			FilterManager.save(commandBlock.get(), data)
		}

		override fun getItemProvider(viewer: Player?): ItemProvider = ItemProvider {
			(data.type as FilterType.ItemType).toItem(data.type.cast(entry)) ?: ItemStack.empty()
		}
	}

	fun strictnessSlotProvider(entry: FilterEntry<ItemStack>, slot: Int) = object : AbstractItem() {
		override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
			strictess[slot] = !(strictess.getOrDefault(slot, true))

			notifyWindows()
			setSlotOverlay(event.view)

			@Suppress("UNCHECKED_CAST")
			(data.entries as MutableList<FilterEntry<ItemStack>>).set(slot, entry)
			FilterManager.save(commandBlock.get(), data)
		}

		override fun getItemProvider(viewer: Player?): ItemProvider = ItemProvider {
			getStrictnessItem(strictess.getOrDefault(slot, true))
		}
	}

	fun getStrictnessItem(strict: Boolean): ItemStack {
		val base = POWER_DRILL_BASIC.constructItemStack().updatePersistentDataContainer {
			set(NamespacedKeys.CUSTOM_ITEM, PersistentDataType.STRING, "USELESS")
		}

		base.updateData(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, true)
		base.updateData(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.itemAttributes().build())

		if (strict) {
			POWER_DRILL_BASIC.getComponent(CustomComponentTypes.POWER_STORAGE).setPower(POWER_DRILL_BASIC, base, 25000)
			base.updateDisplayName(text("Strict item checks"))
			base.updateLore(mutableListOf(
				text("All item data will be matched.")
			))

			return base
		}

		base.updateDisplayName(text("Loose item checks"))
		base.updateLore(mutableListOf(
			text("Only item IDs will be matched.")
		))

		return base
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

	fun setSlotOverlay(view: InventoryView) {
		view.setTitle(getSlotOverlay())
	}
}
