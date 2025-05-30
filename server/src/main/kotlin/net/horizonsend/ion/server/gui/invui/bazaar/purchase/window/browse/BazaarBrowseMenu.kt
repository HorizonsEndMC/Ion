package net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.browse

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getSetting
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.setSetting
import net.horizonsend.ion.server.features.economy.city.TradeCities
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.item.AsyncItem
import net.horizonsend.ion.server.features.gui.item.CollectionScrollButton
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarSort
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.SearchGui
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.BazaarPurchaseMenuParent
import net.horizonsend.ion.server.gui.invui.bazaar.stripAttributes
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bson.conversions.Bson
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item

abstract class BazaarBrowseMenu(viewer: Player) : BazaarPurchaseMenuParent<Map.Entry<String, List<BazaarItem>>>(viewer) {
	override val menuTitleLeft: Component = text("Browsing")

	override val listingsPerPage: Int = 36

	companion object {
		val SORTING_METHODS = listOf(BazaarSort.ALPHABETICAL, BazaarSort.MIN_PRICE, BazaarSort.MAX_PRICE, BazaarSort.HIGHEST_STOCK, BazaarSort.LOWEST_STOCK, BazaarSort.HIGHEST_LISTINGS, BazaarSort.LOWEST_LISTINGS)
	}

	private var sortingMethod: Int = viewer.getSetting(PlayerSettings::defaultBazaarGroupedSort)

	abstract val bson: Bson

	override fun getGui(): Gui = PagedGui.items()
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
		.setContent(items)
		.addPageChangeHandler { _, new ->
			pageNumber = new
			refreshAll()
		}
		.build()

	override fun generateEntries(): List<Map.Entry<String, List<BazaarItem>>> {
		return BazaarItem
			.find(bson)
			.filter { TradeCities.isCity(Regions[it.cityTerritory]) }
			.groupBy(BazaarItem::itemString)
			.entries
			.toMutableList()
			.apply { SORTING_METHODS[sortingMethod].sortSellOrders(this) }
	}

	override fun createItem(entry: Map.Entry<String, List<BazaarItem>>): Item = AsyncItem(
		resultProvider = { fromItemString(entry.key).stripAttributes().updateLore(getItemLore(entry)) },
		handleClick = { _ -> handleItemClick(entry) }
	)

	private fun getItemLore(entry: Map.Entry<String, List<BazaarItem>>): List<Component> {
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

	private fun handleItemClick(entry: Map.Entry<String, List<BazaarItem>>) {
		val (itemString, groupedItems) = entry

		if (groupedItems.size == 1 && viewer.getSetting(PlayerSettings::skipBazaarSingleEntryMenus)) {
			val item = groupedItems.firstOrNull() ?: return

			BazaarGUIs.openPurchaseMenu(
				player = viewer,
				item = item,
				backButtonHandler = { openGui() }
			)

			return
		}

		openListingsForItem(itemString)
	}

	abstract fun openListingsForItem(itemString: String)

	override fun buildTitle(): Component {
		return withPageNumber(super.buildTitle())
	}

	private val sortButton = CollectionScrollButton(
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

	private val searchButton = GuiItem.MAGNIFYING_GLASS
		.makeItem(text("Search for Items"))
		.makeGuiButton { _, _ -> openSearchMenu() }

	abstract val contextName: String

	private fun openSearchMenu() = SearchGui(
		player = viewer,
		contextName = contextName,
		rawItemBson = bson,
		backButtonHandler = ::openGui,
		resultStringConsumer = ::openListingsForItem
	).openGui()
}
