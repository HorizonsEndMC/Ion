package net.horizonsend.ion.server.gui.invui.bazaar.purchase.listings

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.utils.text.BACKGROUND_EXTENDER
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.setEnumSetting
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars.cityName
import net.horizonsend.ion.server.features.economy.city.TradeCities
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.item.EnumScrollButton
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarSort
import net.horizonsend.ion.server.gui.invui.bazaar.IndividualBrowseGui
import net.horizonsend.ion.server.gui.invui.bazaar.getFilterButton
import net.horizonsend.ion.server.gui.invui.bazaar.getMenuTitleName
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.BazaarPurchaseMenuParent
import net.horizonsend.ion.server.miscellaneous.utils.displayNameString
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import org.bson.conversions.Bson
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item

abstract class ItemListingMenu(viewer: Player, protected val itemString: String) : BazaarPurchaseMenuParent<BazaarItem>(viewer), IndividualBrowseGui<BazaarItem> {
	override val menuTitleLeft: Component = empty()
	override val menuTitleRight: Component = empty()

	override val isGlobalBrowse: Boolean = false

	override val listingsPerPage: Int = 36

	abstract val searchBson: Bson
	private var sortingMethod: BazaarSort = PlayerSettingsCache.getEnumSettingOrThrow(viewer.slPlayerId, PlayerSettings::defaultBazaarIndividualSort)

	override fun generateEntries(): List<BazaarItem> = BazaarItem.find(searchBson)
		.apply { sortingMethod.sortSellOrders(this) }
		.filter { TradeCities.isCity(Regions[it.cityTerritory]) }
		.filter { filterData.matches(it) }

	override fun createItem(entry: BazaarItem): Item = formatItem(entry)

	override fun onClickDisplayedItem(entry: BazaarItem) {
		BazaarGUIs.openBrowsePurchaseMenu(player = viewer, item = entry, backButtonHandler = { openGui() })
	}

	override fun formatItemStack(entry: BazaarItem): ItemStack {
		return super.formatItemStack(entry).updateDisplayName(entry.price.toCreditComponent())
	}

	override fun getItemLore(entry: BazaarItem): List<Component> = listOf(
		template(text("Seller: {0}", HE_MEDIUM_GRAY), SLPlayer.getName(entry.seller)),
		template(text("Stock: {0}", HE_MEDIUM_GRAY), entry.stock)
	)

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
		.addPageChangeHandler { _, new ->
			pageNumber = new
			refreshAll()
		}
		.build()

	override fun buildTitle(): Component = GuiText("")
		.setSlotOverlay(
			"# # # # # # # # #",
			". . . . . . . . .",
			". . . . . . . . .",
			". . . . . . . . .",
			". . . . . . . . .",
			"# # # # # # # # #"
		)
		.addBackground(GuiText.GuiBackground(
			backgroundChar = BACKGROUND_EXTENDER,
			verticalShift = -11
		))
		.add(getMenuTitleName(fromItemString(itemString)), line = -2, verticalShift = -4)
		.add(ofChildren(text(contextName)), line = -1, verticalShift = -2)
		.build()

	private val sortButton = EnumScrollButton(
		providedItem = { GuiItem.SORT.makeItem(text("Change Sorting Method")) },
		increment = 1,
		value = { sortingMethod },
		enum = BazaarSort::class.java,
		nameFormatter = { it.displayName },
		valueConsumer = {
			sortingMethod = it
			viewer.setEnumSetting(PlayerSettings::defaultBazaarIndividualSort, sortingMethod)

			openGui()
		}
	)

	abstract val contextName: String

	override fun getSearchEntries(): Collection<BazaarItem> {
		return BazaarItem.find(searchBson).toList()
	}

	override fun getSearchTerms(entry: BazaarItem): List<String> {
		val terms = mutableListOf(cityName(Regions[entry.cityTerritory]), entry.itemString, fromItemString(itemString).displayNameString)
		SLPlayer.getName(entry.seller)?.let(terms::add)
		return terms
	}

	abstract fun openSearchResults(string: String)

	override fun goToCitySelection(viewer: Player) {
		BazaarGUIs.openCitySelection(viewer, this)
	}

	override fun goToGlobalBrowse(viewer: Player) {
		BazaarGUIs.openGlobalBrowse(viewer, this)
	}

	@Suppress("LeakingThis") // Viewer won't be overriden, just need a reference otherwise
	private val filterInfo = getFilterButton(this, PlayerSettings::bazaarSellBrowseFilters)
	private val filterData get() = filterInfo.first
	protected val filterButton get() = filterInfo.second
}
