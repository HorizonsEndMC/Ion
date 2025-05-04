package net.horizonsend.ion.server.gui.invui.bazaar.purchase.gui

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers
import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.utils.text.DEFAULT_GUI_WIDTH
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.economy.city.TradeCities
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.item.AsyncItem
import net.horizonsend.ion.server.features.gui.item.EnumScrollButton
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.gui.invui.InvUIGuiWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGui
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarSort
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.BazaarPurchaseMenuParent
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.horizonsend.ion.server.miscellaneous.utils.updateData
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import org.bson.conversions.Bson
import org.litote.kmongo.setValue
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.impl.AbstractItem
import kotlin.math.ceil

class IndividualListingGUI(
	private val parentWindow: BazaarPurchaseMenuParent,
	private val reOpenHandler: () -> Unit,
	private val searchBson: Bson,
	private val itemLoreProvider: (BazaarItem) -> List<Component> = { bazaarItem ->
		listOf(
			template(text("Seller: {0}", GRAY), SLPlayer.getName(bazaarItem.seller)),
			template(text("Stock: {0}", GRAY), bazaarItem.stock)
		)
	},
	private val purchaseBackButton: () -> Unit,
	private var pageNumber: Int = 0
): InvUIGuiWrapper<PagedGui<Item>>, BazaarGui {
	private var sortingMethod: BazaarSort = BazaarSort.entries[PlayerCache[parentWindow.viewer].defaultBazaarIndividualSort]

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

	private val searchButton: AbstractItem = GuiItems.closeMenuItem(parentWindow.viewer)

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
						updateData(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.itemAttributes().build())
					}
				}) { _ ->
					PurchaseItemMenu(
						viewer = parentWindow.viewer,
						remote = parentWindow.remote,
						item = bazaarItem,
						backButtonHandler = { purchaseBackButton.invoke() }
					).openMenu()
				}
			}
	}

	private val sortButton = EnumScrollButton(
		providedItem = { GuiItem.FILTER.makeItem(text("Change Sorting Method")) },
		increment = 1,
		value = {
			sortingMethod
		},
		enum = BazaarSort::class.java,
		nameFormatter = { it.displayName },
		subEntry = arrayOf(BazaarSort.MIN_PRICE, BazaarSort.MAX_PRICE, BazaarSort.HIGHEST_STOCK, BazaarSort.LOWEST_STOCK),
		valueConsumer = {
			sortingMethod = it

			PlayerCache[parentWindow.viewer].defaultBazaarIndividualSort = sortingMethod.ordinal
			Tasks.async {
				SLPlayer.updateById(parentWindow.viewer.slPlayerId, setValue(SLPlayer::defaultBazaarIndividualSort, sortingMethod.ordinal))
			}

			reOpenHandler.invoke()
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
