package net.horizonsend.ion.server.gui.invui.bazaar.orders.browse

import net.horizonsend.ion.common.database.schema.economy.BazaarOrder
import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.utils.text.BAZAAR_ORDER_HEADER_ICON
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_ORANGE
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.gui.GuiBorder
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getSettingOrThrow
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.setSetting
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars.cityName
import net.horizonsend.ion.server.features.economy.city.TradeCities
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.item.CollectionScrollButton
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.gui.CommonGuiWrapper
import net.horizonsend.ion.server.gui.invui.ListInvUIWindow
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarSort
import net.horizonsend.ion.server.gui.invui.bazaar.getBazaarSettingsButton
import net.horizonsend.ion.server.gui.invui.bazaar.getFilterButton
import net.horizonsend.ion.server.gui.invui.utils.asItemProvider
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bson.conversions.Bson
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window

abstract class AbstractBrowseMenu(viewer: Player, private val fullfillmentHandler: AbstractBrowseMenu.(BazaarOrder) -> Unit) : ListInvUIWindow<BazaarOrder>(viewer, async = true), OrderWindow {
	override val listingsPerPage: Int = 36

	abstract val findBson: Bson

	override fun generateEntries(): List<BazaarOrder> {
		return BazaarOrder.find(findBson)
			.apply { SORTING_METHODS[sortingMethod].sortBuyOrders(this) }
			.filter { !BazaarOrder.isFulfilled(it._id) && TradeCities.isCity(Regions[it.cityTerritory]) && filterData.matches(it) }
	}

	override fun createItem(entry: BazaarOrder): Item = formatItem(entry)

	override fun buildWindow(): Window {
		val gui = PagedGui.items()
			.setStructure(
				"x . . c g b . o i",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"< . s . . . f S >",
			)
			.addIngredient('x', parentOrBackButton())

			.addIngredient('c', citySelectionButton)
			.addIngredient('g', globalBrowseButton)
			.addIngredient('b', listingBrowseButton)

			.addIngredient('o', getBazaarSettingsButton())
			.addIngredient('i', infoButton)

			.addIngredient('<', GuiItems.PageLeftItem())
			.addIngredient('>', GuiItems.PageRightItem())

			.addIngredient('s', searchButton)
			.addIngredient('S', sortButton)
			.addIngredient('f', filterButton)

			.addIngredient('#', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
			.setContent(items)

		return normalWindow(gui.build())
	}

	abstract val browseName: Component

	override fun buildTitle(): Component {
		val text = GuiText("")
			.addBorder(GuiBorder.regular(
				color = HE_LIGHT_ORANGE,
				headerIcon = GuiBorder.HeaderIcon(BAZAAR_ORDER_HEADER_ICON, 48, HE_LIGHT_ORANGE),
				leftText = text("Browsing"),
				rightText = browseName
			))
			.setSlotOverlay(
				"# # # # # # # # #",
				". . . . . . . . .",
				". . . . . . . . .",
				". . . . . . . . .",
				". . . . . . . . .",
				"# # # # # # # # #",
			)

		return withPageNumber(text)
	}

	override fun getItemLore(entry: BazaarOrder): List<Component> {
		return listOf(
			template(text("Order from {0} at {1}", HE_MEDIUM_GRAY), SLPlayer.getName(entry.player), cityName(Regions[entry.cityTerritory])),
			template(text("Requested {0}, {1} have been fulfilled.", HE_MEDIUM_GRAY), entry.requestedQuantity, entry.fulfilledQuantity),
			template(text("Priced at {0} Per item.", HE_MEDIUM_GRAY), entry.pricePerItem.toCreditComponent()),
			template(text("Possible profit of {0} for fulfilling the rest of the order.", HE_MEDIUM_GRAY), entry.balance.toCreditComponent()),
		)
	}

	override fun onClickDisplayedItem(entry: BazaarOrder, clickedFrom: CommonGuiWrapper) {
		fullfillmentHandler.invoke(this, entry)
	}

	override fun getSearchEntries(): Collection<BazaarOrder> {
		return BazaarOrder.find(findBson).toList()
	}

	override fun getSearchTerms(entry: BazaarOrder): List<String> {
		val terms = mutableListOf(cityName(Regions[entry.cityTerritory]), entry.itemString)
		SLPlayer.getName(entry.player)?.let(terms::add)

		return terms
	}

	companion object {
		private val SORTING_METHODS = listOf(
			BazaarSort.ALPHABETICAL,
			BazaarSort.MIN_PRICE,
			BazaarSort.MAX_PRICE,
			BazaarSort.HIGHEST_ORDER_SIZE,
			BazaarSort.LOWEST_ORDER_SIZE
		)
	}

	private var sortingMethod: Int = viewer.getSettingOrThrow(PlayerSettings::orderBrowseSort)

	private val sortButton = CollectionScrollButton(
		entries = SORTING_METHODS,
		providedItem = GuiItem.SORT.makeItem(text("Change Sorting Method")).asItemProvider(),
		value = ::sortingMethod,
		nameFormatter = { it.displayName },
		valueConsumer = { index, _ ->
			sortingMethod = index
			viewer.setSetting(PlayerSettings::orderBrowseSort, index)
			openGui()
		}
	)

	@Suppress("LeakingThis") // Viewer won't be overriden, just need a reference otherwise
	private val filterInfo = getFilterButton(this, PlayerSettings::bazaarOrderBrowseFilters)
	private val filterData get() = filterInfo.first
	private val filterButton get() = filterInfo.second
}
