package net.horizonsend.ion.server.gui.invui.bazaar.purchase.browse

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getSetting
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.setSetting
import net.horizonsend.ion.server.features.economy.city.TradeCities
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.item.CollectionScrollButton
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.gui.CommonGuiWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarSort
import net.horizonsend.ion.server.gui.invui.bazaar.GroupedBrowseGui
import net.horizonsend.ion.server.gui.invui.bazaar.getFilterButton
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.BazaarPurchaseMenuParent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bson.conversions.Bson
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item

abstract class BazaarBrowseMenu(viewer: Player) : BazaarPurchaseMenuParent<Map.Entry<String, List<BazaarItem>>>(viewer), GroupedBrowseGui<BazaarItem> {
	override val menuTitleLeft: Component = text("Browsing")

	override val listingsPerPage: Int = 36

	companion object {
		val SORTING_METHODS = listOf(
			BazaarSort.ALPHABETICAL,
			BazaarSort.MIN_PRICE,
			BazaarSort.MAX_PRICE,
			BazaarSort.HIGHEST_STOCK,
			BazaarSort.LOWEST_STOCK,
			BazaarSort.HIGHEST_LISTINGS,
			BazaarSort.LOWEST_LISTINGS
		)
	}

	private var sortingMethod: Int = viewer.getSetting(PlayerSettings::defaultBazaarGroupedSort)

	abstract val bson: Bson

	override fun getGui(): Gui = PagedGui.items()
		.setStructure(
			"# # # # # # # # #",
			"# # # # # # # # #",
			"# # # # # # # # #",
			"# # # # # # # # #",
			"< . s . . . f S >"
		)
		.addIngredient('#', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
		.addIngredient('<', GuiItems.PageLeftItem())
		.addIngredient('>', GuiItems.PageRightItem())
		.addIngredient('s', searchButton)
		.addIngredient('f', filterButton)
		.addIngredient('S', sortButton)
		.setContent(items)
		.handlePageChange()
		.build()

	override fun generateEntries(): List<Map.Entry<String, List<BazaarItem>>> = BazaarItem
		.find(bson)
		.filter { TradeCities.isCity(Regions[it.cityTerritory]) }
		.groupBy(BazaarItem::itemString)
		.entries
		.toMutableList()
		.apply { SORTING_METHODS[sortingMethod].sortSellOrders(this) }

	override fun createItem(entry: Map.Entry<String, List<BazaarItem>>): Item = formatItem(entry)

	override fun getItemLore(entry: Map.Entry<String, List<BazaarItem>>): List<Component> {
		val (_, sellers) = entry

		val sellerCount = sellers.size
		val totalStock = sellers.sumOf { it.stock }
		val minPrice = sellers.minOfOrNull { it.price } ?: 0
		val maxPrice = sellers.maxOfOrNull { it.price } ?: 0

		return listOf(
			template(text("{0} listing${if (sellerCount != 1) "s" else ""} with a total stock of {1}", HE_MEDIUM_GRAY), sellerCount, totalStock),
			ofChildren(text("Min price of listing${if (sellerCount != 1) "s" else ""}: ", HE_MEDIUM_GRAY), minPrice.toCreditComponent()),
			ofChildren(text("Max price of listing${if (sellerCount != 1) "s" else ""}: ", HE_MEDIUM_GRAY), maxPrice.toCreditComponent()),
		)
	}

	override fun onClickDisplayedItem(entry: Map.Entry<String, List<BazaarItem>>, clickedFrom: CommonGuiWrapper) {
		val (itemString, groupedItems) = entry

		if (groupedItems.size == 1 && viewer.getSetting(PlayerSettings::skipBazaarSingleEntryMenus)) {
			val item = groupedItems.firstOrNull() ?: return

			BazaarGUIs.openBrowsePurchaseMenu(
				player = viewer,
				item = item,
				backButtonHandler = { clickedFrom.openGui() }
			)

			return
		}

		openListingsForItem(itemString)
	}

	override fun getSearchTerms(entry: Map.Entry<String, List<BazaarItem>>): List<String> {
		val terms = mutableListOf(entry.key, )
		terms.addAll(entry.value.mapNotNull { SLPlayer.getName(it.seller) })

		return terms
	}

	override fun getSearchEntries(): Collection<Map.Entry<String, List<BazaarItem>>> = entries

	abstract fun openListingsForItem(itemString: String)

	override fun buildTitle(): Component {
		return withPageNumber(super.buildTitle())
	}

	protected val sortButton = CollectionScrollButton(
		entries = SORTING_METHODS,
		providedItem = { GuiItem.SORT.makeItem(text("Change Sorting Method")) },
		value = { sortingMethod },
		nameFormatter = { it.displayName },
		valueConsumer = { index, _ ->
			sortingMethod = index
			viewer.setSetting(PlayerSettings::defaultBazaarGroupedSort, index)

			openGui()
		}
	)

	abstract val contextName: String

	@Suppress("LeakingThis") // Viewer won't be overriden, just need a reference otherwise
	private val filterInfo = getFilterButton(this, PlayerSettings::bazaarSellBrowseFilters)
	private val filterData get() = filterInfo.first
	protected val filterButton get() = filterInfo.second
}
