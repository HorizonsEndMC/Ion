package net.horizonsend.ion.server.gui.invui.bazaar.purchase.gui

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.utils.text.DEFAULT_GUI_WIDTH
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache
import net.horizonsend.ion.server.features.economy.city.TradeCities
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.item.AsyncItem
import net.horizonsend.ion.server.features.gui.item.EnumScrollButton
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.gui.invui.InvUIGuiWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGui
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarSort
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.SearchGui
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.BazaarPurchaseMenuParent
import net.horizonsend.ion.server.gui.invui.bazaar.stripAttributes
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bson.conversions.Bson
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.impl.AbstractItem
import java.util.function.Consumer
import kotlin.math.ceil

class IndividualListingGUI(
	private val parentWindow: BazaarPurchaseMenuParent,
	private val searchBson: Bson,
	private val itemLoreProvider: (BazaarItem) -> List<Component> = { bazaarItem ->
		listOf(
			template(text("Seller: {0}", HE_MEDIUM_GRAY), SLPlayer.getName(bazaarItem.seller)),
			template(text("Stock: {0}", HE_MEDIUM_GRAY), bazaarItem.stock)
		)
	},
	private val contextName: String,
	private val searchResultConsumer: Consumer<String>,
	private val purchaseBackButton: () -> Unit,
	private var pageNumber: Int = 0
): InvUIGuiWrapper<PagedGui<Item>>, BazaarGui {
	private var sortingMethod: BazaarSort = PlayerSettingsCache[parentWindow.viewer, PlayerSettings::defaultBazaarIndividualSort]

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
				parentWindow.refreshTitle()
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
			backButtonHandler = { parentWindow.openGui() },
			resultStringConsumer = searchResultConsumer
		).openGui()
	}

	private fun getButtons(): List<AbstractItem> {
		val items = BazaarItem.find(searchBson)

		return items
			.apply { sortingMethod.sort(this) }
			.filter { TradeCities.isCity(Regions[it.cityTerritory]) }
			.mapTo(mutableListOf()) { bazaarItem ->
				AsyncItem({
					with(fromItemString(bazaarItem.itemString)) {
						updateDisplayName(bazaarItem.price.toCreditComponent())
						updateLore(itemLoreProvider.invoke(bazaarItem))
						// Clear attributes
						stripAttributes()
					}
				}) { _ ->
					BazaarGUIs.openPurchaseMenu(
						player = parentWindow.viewer,
						remote = parentWindow.remote,
						item = bazaarItem,
						backButtonHandler = { purchaseBackButton.invoke() }
					)
				}
			}
	}

	private val sortButton = EnumScrollButton(
		providedItem = { GuiItem.SORT.makeItem(text("Change Sorting Method")) },
		increment = 1,
		value = {
			sortingMethod
		},
		enum = BazaarSort::class.java,
		nameFormatter = { it.displayName },
		subEntry = arrayOf(BazaarSort.MIN_PRICE, BazaarSort.MAX_PRICE, BazaarSort.HIGHEST_STOCK, BazaarSort.LOWEST_STOCK),
		valueConsumer = {
			sortingMethod = it

			PlayerSettingsCache[parentWindow.viewer, PlayerSettings::defaultBazaarIndividualSort] = it

			parentWindow.openGui()
		}
	)

	override fun modifyGuiText(guiText: GuiText) {
		val maxPageNumber = ceil(totalItems.toDouble() / (4.0 * 9.0)).toInt()

		val pageNumberString = "${pageNumber + 1} / $maxPageNumber"
		guiText.add(
			text(pageNumberString),
			line = 10,
			GuiText.TextAlignment.CENTER,
			verticalShift = 4,
			horizontalShift = -DEFAULT_GUI_WIDTH
		)
	}
}
