package net.horizonsend.ion.server.gui.invui.bazaar.purchase

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.item.EnumScrollButton
import net.horizonsend.ion.server.gui.invui.InvUIGuiWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarSort
import net.horizonsend.ion.server.gui.invui.bazaar.getItemButtons
import net.horizonsend.ion.server.gui.invui.utils.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import org.litote.kmongo.gt
import org.litote.kmongo.setValue
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import kotlin.math.ceil

class GlobalBrowseGUI(override val parent: BazaarMainPurchaseMenu) : InvUIGuiWrapper<Gui>, BazaarGui {
	private var sortingMethod: BazaarSort = BazaarSort.entries[PlayerCache[parent.viewer].defaultBazaarSort]
	private var ascendingSort: Boolean = true

	private var pageNumber = 0
	private var totalItems = 0

	override fun getGui(): Gui {
		val buttons = getButtons()
		totalItems = buttons.size

		val gui = PagedGui.items()
			.setStructure(
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"< . s . . . S . >"
			)
			.addIngredient('#', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
			.addIngredient('<', GuiItems.PageLeftItem())
			.addIngredient('>', GuiItems.PageRightItem())
			.addIngredient('s', globalSearchButton)
			.addIngredient('S', sortButton)
			.setContent(getButtons())
			.addPageChangeHandler { _, new ->
				pageNumber = new
				parent.refreshGuiText()
			}
			.build()

		return gui
	}

	fun getButtons() = getItemButtons(
		BazaarItem::stock gt 0,
		sortingMethod,
		ascendingSort,
		loreBuilder = { item, filteredItems ->
			val sellers = filteredItems.filter { it.itemString == item }
			val sellerCount = sellers.size
			val totalStock = sellers.sumOf { it.stock }
			val minPrice = sellers.minOfOrNull { it.price } ?: 0
			val maxPrice = sellers.maxOfOrNull { it.price } ?: 0

			listOf(
				template(text("{0} listing${if (sellerCount != 1) "s" else ""} with a total stock of {1}", GRAY), sellerCount, totalStock),
				ofChildren(text("Min price of listing${if (sellerCount != 1) "s" else ""}: ", GRAY), minPrice.toCreditComponent()),
				ofChildren(text("Max price of listing${if (sellerCount != 1) "s" else ""}: ", GRAY), maxPrice.toCreditComponent()),
			)
		},
		clickHandler = { itemString, _, player ->
			player.information(itemString)
		}
	)

	private val globalSearchButton = GuiItem.MAGNIFYING_GLASS
		.makeItem(text("Search for Items"))
		.makeGuiButton { clickType, player ->
			println("Search")
		}

	private val sortButton = EnumScrollButton(
		{ GuiItem.GEAR.makeItem(text("Change Sorting Method")) },
		increment = 1,
		value = {
			sortingMethod
		},
		BazaarSort::class.java,
		nameFormatter = { text(it.name) },
		valueConsumer = {
			sortingMethod = it

			PlayerCache[parent.viewer].defaultBazaarSort = sortingMethod.ordinal
			Tasks.async {
				SLPlayer.updateById(parent.viewer.slPlayerId, setValue(SLPlayer::defaultBazaarSort, sortingMethod.ordinal))
			}

			reOpen()
		}
	)

	private fun reOpen() {
		val new = BazaarMainPurchaseMenu(parent.viewer, parent.remote)
		new.openGui()

		new.setTab(parent.currentTab)
		new.getTabGUI().setPage(pageNumber)
	}

	override fun modifyGuiText(guiText: GuiText) {
		val maxPageNumber = ceil(totalItems.toDouble() / (4.0 * 9.0)).toInt()

		val pageNumberString = "${pageNumber + 1} / $maxPageNumber"
		guiText.add(
			text(pageNumberString),
			line = 10,
			GuiText.TextAlignment.CENTER,
			verticalShift = 4
		)
	}
}
