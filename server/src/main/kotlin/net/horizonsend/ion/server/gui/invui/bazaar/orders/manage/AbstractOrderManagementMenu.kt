package net.horizonsend.ion.server.gui.invui.bazaar.orders.manage

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.economy.BazaarOrder
import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.common.database.schema.nations.Territory
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getSettingOrThrow
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.setSetting
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars.cityName
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.item.CollectionScrollButton
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.gui.invui.ListInvUIWindow
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarSort
import net.horizonsend.ion.server.gui.invui.bazaar.getFilterButton
import net.horizonsend.ion.server.gui.invui.bazaar.stripAttributes
import net.horizonsend.ion.server.gui.invui.misc.util.input.TextInputMenu.Companion.openSearchMenu
import net.horizonsend.ion.server.gui.invui.utils.asItemProvider
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeInformationButton
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.litote.kmongo.eq
import xyz.xenondevs.invui.item.Item

abstract class AbstractOrderManagementMenu(
	viewer: Player,
	protected val handleListingClick: AbstractOrderManagementMenu.(Oid<BazaarOrder>) -> Unit
) : ListInvUIWindow<BazaarOrder>(viewer, async = true) {
	override fun generateEntries(): List<BazaarOrder> {
		return BazaarOrder
			.find(BazaarOrder::player eq viewer.slPlayerId)
			.apply { SORTING_METHODS[sortingMethod].sortBuyOrders(this) }
			.filter { filterData.matches(it) }
			.toList()
	}

	override fun createItem(entry: BazaarOrder): Item {
		return fromItemString(entry.itemString)
			.stripAttributes()
			.applyItemFormatting(entry)
			.asItemProvider()
			.makeGuiButton { _, _ -> handleListingClick.invoke(this, entry._id) }
	}

	protected fun ItemStack.applyItemFormatting(orderItem: BazaarOrder): ItemStack {
		return updateLore(listOf(
			template(text("City: {0}", HE_MEDIUM_GRAY), cityName(Regions[orderItem.cityTerritory])),
			template(text("Order Quantity: {0}", HE_MEDIUM_GRAY), orderItem.requestedQuantity),
			template(text("Price Per Item: {0}", HE_MEDIUM_GRAY), (orderItem.requestedQuantity * orderItem.pricePerItem).toCreditComponent()),
			template(text("Order Price: {0}", HE_MEDIUM_GRAY), orderItem.pricePerItem.toCreditComponent()),
			template(text("Fulfilled Quantity: {0}", HE_MEDIUM_GRAY), orderItem.fulfilledQuantity),
			template(text("Unfulfilled Quantity: {0}", HE_MEDIUM_GRAY), orderItem.requestedQuantity - orderItem.fulfilledQuantity),
		))
	}

	protected val infoButton = makeInformationButton(
		title = text("Information"),
		text("From this menu, you can see all the orders you have placed at trade cities."),
		text("Clicking on one of these items will bring you to a menu where you can update them."),
		text(""),
		text("From the top bar, you may create a new order, or switch to the alternate layout for this menu.")
	)

	protected val searchButton = GuiItem.MAGNIFYING_GLASS.makeItem(text("Search Listings")).makeGuiButton { _, _ -> doSearch() }

	private fun doSearch() {
		val cityNames = mutableMapOf<Oid<Territory>, String>()

		viewer.openSearchMenu(
			entries = entries,
			prompt = text("Search your sell orders."),
			description = empty(),
			searchTermProvider = { item: BazaarOrder ->
				listOf(item.itemString, cityNames.getOrPut(item.cityTerritory) { cityName(Regions[item.cityTerritory]) })
			},
			componentTransformer = { item: BazaarOrder ->
				template(text("{0} at {1}"), item.itemString, cityNames.getOrPut(item.cityTerritory) { cityName(Regions[item.cityTerritory]) })
			},
			itemTransformer = { item: BazaarOrder ->
				fromItemString(item.itemString)
					.stripAttributes()
					.applyItemFormatting(item)
			},
			backButtonHandler = { this.openGui() },
			handler = { _, item: BazaarOrder -> handleListingClick.invoke(this@AbstractOrderManagementMenu, item._id) }
		)
	}

	@Suppress("LeakingThis") // Viewer won't be overriden, just need a reference otherwise
	private val filterInfo = getFilterButton(this, PlayerSettings::bazaarOrderManageFilters)
	private val filterData get() = filterInfo.first
	protected val filterButton get() = filterInfo.second

	protected val createBuyOrderMenu = GuiItem.PLUS.makeItem(text("Create Bazaar Order")).makeGuiButton { _, _ ->
		BazaarGUIs.openBuyOrderCreationMenu(viewer, this)
	}

	abstract val switchLayoutButton: Item

	companion object {
		private val SORTING_METHODS = listOf(
			BazaarSort.ALPHABETICAL,
			BazaarSort.MIN_PRICE,
			BazaarSort.MAX_PRICE,
			BazaarSort.HIGHEST_STOCK,
			BazaarSort.LOWEST_STOCK,
			BazaarSort.HIGHEST_ORDER_SIZE,
			BazaarSort.LOWEST_ORDER_SIZE
		)
	}

	private var sortingMethod: Int = viewer.getSettingOrThrow(PlayerSettings::orderManageSort)

	protected val sortButton = CollectionScrollButton(
		entries = SORTING_METHODS,
		providedItem = GuiItem.SORT.makeItem(text("Change Sorting Method")).asItemProvider(),
		value = ::sortingMethod,
		nameFormatter = { it.displayName },
		valueConsumer = { index, _ ->
			sortingMethod = index
			viewer.setSetting(PlayerSettings::orderManageSort, index)
			openGui()
		}
	)
}
