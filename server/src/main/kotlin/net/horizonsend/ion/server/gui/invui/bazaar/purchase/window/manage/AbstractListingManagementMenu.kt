package net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.manage

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getEnumSetting
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.setEnumSetting
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars.cityName
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.item.AsyncItem
import net.horizonsend.ion.server.features.gui.item.EnumScrollButton
import net.horizonsend.ion.server.features.gui.item.FeedbackItem
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.gui.invui.ListInvUIWindow
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarSort
import net.horizonsend.ion.server.gui.invui.utils.asItemProvider
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player
import org.litote.kmongo.eq
import xyz.xenondevs.invui.item.Item

abstract class AbstractListingManagementMenu(viewer: Player) : ListInvUIWindow<BazaarItem>(viewer, async = true) {
    private var sortingMethod: BazaarSort = viewer.getEnumSetting(PlayerSettings::defaultBazaarListingManagementSort)

	override fun generateEntries(): List<BazaarItem> {
		val items = BazaarItem.find(BazaarItem::seller eq viewer.slPlayerId)
		sortingMethod.sort(items)
		return items.toList()
	}

	override fun createItem(entry: BazaarItem): Item {
		val city = cityName(Regions[entry.cityTerritory])
		val stock = entry.stock
		val uncollected = entry.balance.toCreditComponent()
		val price = entry.price.toCreditComponent()

		return AsyncItem(
			resultProvider = {
				val item = fromItemString(entry.itemString)

				item.updateLore(listOf(
					ofChildren(template(text("City: {0}", HE_MEDIUM_GRAY), useQuotesAroundObjects = false, city)),
					ofChildren(template(text("Stock: {0}", HE_MEDIUM_GRAY), stock)),
					ofChildren(template(text("Balance: {0}", HE_MEDIUM_GRAY), uncollected)),
					ofChildren(template(text("Price: {0}", HE_MEDIUM_GRAY), price))
				))
			},
			handleClick = { handleEntryClick(entry) }
		)
	}

	fun handleEntryClick(item: BazaarItem) {
		BazaarGUIs.openListingEditor(viewer, item, this)
	}

	protected val infoButton = GuiItem.INFO.makeItem(text("Information")).makeGuiButton { _, _ -> println("INFO") /*TODO*/ }
	protected val searchButton = GuiItem.MAGNIFYING_GLASS.makeItem(text("Search Listings")).makeGuiButton { _, _ -> println("search") }
	protected val filterButton = GuiItem.FILTER.makeItem(text("Filter Listings")).makeGuiButton { _, _ -> println("filter") }
    protected val sortButton = EnumScrollButton(
        providedItem = GuiItem.SORT.makeItem(text("Change Sorting Method")).asItemProvider(),
        value = { sortingMethod },
        enum = BazaarSort::class.java,
        nameFormatter = { it.displayName },
        subEntry = arrayOf(BazaarSort.MIN_PRICE, BazaarSort.MAX_PRICE, BazaarSort.HIGHEST_STOCK, BazaarSort.LOWEST_STOCK, BazaarSort.HIGHEST_BALANCE, BazaarSort.LOWEST_BALANCE),
        valueConsumer = {
            sortingMethod = it
			viewer.setEnumSetting(PlayerSettings::defaultBazaarListingManagementSort, it)
            openGui()
        }
    )
	protected val collectButton = FeedbackItem.builder(GuiItem.ROUTE_CANCEL.makeItem(text("Collect Listing Profits")) /*TODO*/) { _, _ -> Bazaars.collectListingProfit(viewer) }
		.withSuccessHandler { _, _ -> openGui() }
		.build()
}
