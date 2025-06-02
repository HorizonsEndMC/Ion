package net.horizonsend.ion.server.gui.invui.bazaar.terminal

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.utils.input.InputResult
import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_GRAY
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.gui.icons.GuiIcon
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.command.GlobalCompletions.toItemString
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.item.AsyncItem
import net.horizonsend.ion.server.features.multiblock.type.economy.BazaarTerminalMultiblock.BazaarTerminalMultiblockEntity
import net.horizonsend.ion.server.features.transport.items.util.ItemReference
import net.horizonsend.ion.server.features.transport.items.util.getRemovableItems
import net.horizonsend.ion.server.gui.invui.ListInvUIWindow
import net.horizonsend.ion.server.gui.invui.utils.buttons.FeedbackLike
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.displayNameComponent
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.horizonsend.ion.server.miscellaneous.utils.toMap
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.space
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.RED
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window

class BazaarBulkDepositMenu(viewer: Player, val multiblock: BazaarTerminalMultiblockEntity) : ListInvUIWindow<Map.Entry<ItemStack, ArrayDeque<ItemReference>>>(viewer, async = true) {
	override val listingsPerPage: Int = 27

	private val excludeItems = mutableSetOf<ItemStack>()

	override fun generateEntries(): List<Map.Entry<ItemStack, ArrayDeque<ItemReference>>> {
		val availableForDeposit = mutableMapOf<ItemStack, ArrayDeque<ItemReference>>()

		val inventoryReferences = multiblock.getInputInventories()

		for (reference in inventoryReferences) {
			for ((index, item: ItemStack) in getRemovableItems(reference.inventory)) {
				availableForDeposit.getOrPut(item.asOne()) { ArrayDeque() }.add(ItemReference(reference.inventory, index))
			}
		}

		return availableForDeposit.entries.toList()
	}

	override fun createItem(entry: Map.Entry<ItemStack, ArrayDeque<ItemReference>>): Item {
		val (asOne, references) = entry

		val referenceSum = references.sumOf { it.get()?.amount ?: 0 }

		return AsyncItem(
			resultProvider = {
				val itemString = toItemString(asOne)
				val selling = multiblock.territory?.let { BazaarItem.any(BazaarItem.matchQuery(it.id, viewer.slPlayerId, itemString)) } ?: false

				if (!selling && !excludeItems.contains(asOne)) {
					excludeItems.add(asOne)
					refreshTitle()
				}

				val updated = asOne.clone()
					.updateLore(listOf(
						template(text("{0} available for deposit.", HE_MEDIUM_GRAY), referenceSum),
						template(text("Item string: {0}.", HE_MEDIUM_GRAY), itemString),
						empty(),
						text("Right click to toggle exclusion.", HE_LIGHT_GRAY)
					))

				if (!selling) {
					updated.updateLore(listOf<Component>(text("You are not selling this item in this territory.", RED)).plus(updated.lore() ?: listOf()))
				}

				if (excludeItems.contains(asOne)) updated.updateDisplayName(ofChildren(updated.displayNameComponent, space(), bracketed(text("Excluded", RED))))
				updated
			},
			{ event -> handleItemInteract(event.click, asOne) }
		)
	}

	private fun handleItemInteract(clickType: ClickType, itemType: ItemStack) {
		if (clickType == ClickType.RIGHT) {
			excludeItem(itemType)
		}
	}

	private fun excludeItem(singleton: ItemStack) {
		if (excludeItems.contains(singleton))
			excludeItems.remove(singleton)
		else excludeItems.add(singleton)

		reGenerateItems()
		openGui()
	}

	override fun buildWindow(): Window {
		val gui = PagedGui.items()
			.setStructure(
				"x . . . . a n . c",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
				". . . . . . . . .",
				"l . . . . . . . r",
			)
			.addIngredient('x', parentOrBackButton())
			.addIngredient('c', confirmDeposit)

			.addIngredient('l', GuiItems.PageLeftItem())
			.addIngredient('r', GuiItems.PageRightItem())

			.addIngredient('a', excludeAllItem)
			.addIngredient('n', excludeNoneItem)

			.addIngredient('#', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
			.setContent(items)

			.addPageChangeHandler { _, new ->
				pageNumber = new
				refreshTitle()
			}
			.build()

		gui.setPage(pageNumber)

		return normalWindow(gui)
	}

	override fun buildTitle(): Component {
		val itemMenuTitle = GuiText("Available For Deposit")
			.setSlotOverlay(
				"# # # # # # # # #",
				". . . . . . . . .",
				". . . . . . . . .",
				". . . . . . . . .",
				"# # # # # # # # #",
				"# # # # # # # # #"
			)

		val displayed = getDisplayedEntries()

		for (rowIndex in 0..2) {
			for (columnIndex in 0..8) {
				val itemindex = (rowIndex * 9) + columnIndex
				val displayedItem = displayed.getOrNull(itemindex) ?: continue
				if (!excludeItems.contains(displayedItem.key)) continue

				itemMenuTitle.setIcon(rowIndex + 1, columnIndex, GuiIcon.coloredSlot(RED))
			}
		}

		val regularTitle = itemMenuTitle.build()

		return withPageNumber(regularTitle)
	}

	private val confirmDeposit = FeedbackLike.withHandler(GuiItem.CHECKMARK.makeItem(text("Confirm Deposit"))) { _, _ -> runDeposit() }

	private fun runDeposit() {
		val formatted = entries
			.toMap()
			.filterKeys { !excludeItems.contains(it) }
			.mapKeys { toItemString(it.key) }

		if (formatted.isEmpty()) {
			confirmDeposit.updateWith(InputResult.FailureReason(listOf(text("You don't have any items to deposit!", RED))))
			return
		}

		val result = Bazaars.bulkDepositToSellOrders(viewer, formatted)

		result.withResult {
			if (it.isSuccess()) {
				reGenerateItems()
				openGui()
			}
		}

		confirmDeposit.updateWith(result)
	}

	private val excludeAllItem = GuiItem.UP.makeItem(text("Exclude All")).makeGuiButton { _, _ -> blacklistAll() }
	private val excludeNoneItem = GuiItem.DOWN.makeItem(text("Exclude None")).makeGuiButton { _, _ -> blacklistNone() }

	private fun blacklistAll() {
		excludeItems.addAll(entries.map { it.key })
		reGenerateItems()
		openGui()
	}

	private fun blacklistNone() {
		excludeItems.clear()
		reGenerateItems()
		openGui()
	}
}
