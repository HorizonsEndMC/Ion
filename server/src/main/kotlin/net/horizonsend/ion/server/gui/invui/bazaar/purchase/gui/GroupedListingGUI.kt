package net.horizonsend.ion.server.gui.invui.bazaar.purchase.gui

import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.item.EnumScrollButton
import net.horizonsend.ion.server.gui.invui.InvUIGuiWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGui
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarSort
import net.horizonsend.ion.server.gui.invui.bazaar.getItemButtons
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.SearchGui
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.BazaarPurchaseMenuParent
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.text.Component.text
import org.bson.conversions.Bson
import org.litote.kmongo.setValue
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import java.util.function.Consumer
import kotlin.math.ceil

class GroupedListingGUI(
	private val parentWindow: BazaarPurchaseMenuParent,
	private val searchBson: Bson,
	private val reOpenHandler: () -> Unit,
	private val contextName: String,
	private val searchResultConsumer: Consumer<String>,
	private val itemMenuHandler: GroupedListingGUI.(String) -> Unit,
	pageNumber: Int = 0
): InvUIGuiWrapper<PagedGui<Item>>, BazaarGui {
	var pageNumber = pageNumber; private set

	private var sortingMethod: BazaarSort = BazaarSort.entries[PlayerCache[parentWindow.viewer].defaultBazaarGroupedSort]

	private var totalItems = 0

	override fun getGui(): PagedGui<Item> {
		val buttons = getButtons()
		totalItems = buttons.size

		val new = PagedGui.items()
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
			.addIngredient('s', searchButton)
			.addIngredient('S', sortButton)
			.setContent(getButtons())
			.addPageChangeHandler { _, new ->
				pageNumber = new
				parentWindow.refreshGuiText()
			}
			.build()

		new.setPage(pageNumber)

		return new
	}

	private val searchButton = GuiItem.MAGNIFYING_GLASS
		.makeItem(text("Search for Items"))
		.makeGuiButton { _, _ ->
			openSearchMenu()
		}

	fun openSearchMenu() {
		SearchGui(
			player = parentWindow.viewer,
			contextName = contextName,
			rawItemBson = searchBson,
			backButtonHandler = reOpenHandler,
			resultStringConsumer = searchResultConsumer
		).openGui()
	}

	private val sortButton = EnumScrollButton(
		providedItem = { GuiItem.FILTER.makeItem(text("Change Sorting Method")) },
		increment = 1,
		value = { sortingMethod },
		enum = BazaarSort::class.java,
		nameFormatter = { it.displayName },
		valueConsumer = {
			sortingMethod = it

			PlayerCache[parentWindow.viewer].defaultBazaarGroupedSort = sortingMethod.ordinal
			Tasks.async {
				SLPlayer.updateById(parentWindow.viewer.slPlayerId, setValue(SLPlayer::defaultBazaarGroupedSort, sortingMethod.ordinal))
			}

			reOpenHandler.invoke()
		}
	)

	private fun getButtons() = getItemButtons(
		searchBson,
		sortingMethod,
		loreBuilder = { _, sellers ->
			val sellerCount = sellers.size
			val totalStock = sellers.sumOf { it.stock }
			val minPrice = sellers.minOfOrNull { it.price } ?: 0
			val maxPrice = sellers.maxOfOrNull { it.price } ?: 0

			listOf(
				template(text("{0} listing${if (sellerCount != 1) "s" else ""} with a total stock of {1}", HE_MEDIUM_GRAY), sellerCount, totalStock),
				ofChildren(text("Min price of listing${if (sellerCount != 1) "s" else ""}: ", HE_MEDIUM_GRAY), minPrice.toCreditComponent()),
				ofChildren(text("Max price of listing${if (sellerCount != 1) "s" else ""}: ", HE_MEDIUM_GRAY), maxPrice.toCreditComponent()),
			)
		},
		clickHandler = { itemString, groupedItems, _, player ->
			if (groupedItems.size == 1 && PlayerCache[player].skipBazaarSingleEntryMenus) {
				val item = groupedItems.firstOrNull() ?: return@getItemButtons

				BazaarGUIs.openPurchaseMenu(
					player = parentWindow.viewer,
					remote = parentWindow.remote,
					item = item,
					backButtonHandler = { reOpenHandler.invoke() }
				)

				return@getItemButtons
			}

			itemMenuHandler.invoke(this, itemString)
		}
	)

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
