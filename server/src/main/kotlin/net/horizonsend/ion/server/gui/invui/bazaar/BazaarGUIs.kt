package net.horizonsend.ion.server.gui.invui.bazaar

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.database.schema.economy.BazaarOrder
import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getSetting
import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.custom.settings.SettingsPageGui.Companion.createSettingsPage
import net.horizonsend.ion.server.features.gui.custom.settings.button.DBCachedBooleanToggle
import net.horizonsend.ion.server.gui.CommonGuiWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.orders.BuyOrderMainMenu
import net.horizonsend.ion.server.gui.invui.bazaar.orders.CreateBuyOrderMenu
import net.horizonsend.ion.server.gui.invui.bazaar.orders.browse.BuyOrderFulfillmentMenu
import net.horizonsend.ion.server.gui.invui.bazaar.orders.manage.GridOrderManagementWindow
import net.horizonsend.ion.server.gui.invui.bazaar.orders.manage.ListOrderManagementMenu
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.BazaarCitySelectionMenu
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.browse.BazaarCityBrowseMenu
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.browse.BazaarGlowbalBrowseMenu
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.browse.PurchaseItemMenu
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.listings.CityItemListingsMenu
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.listings.GlobalItemListingsMenu
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.manage.GridListingManagementMenu
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.manage.ListListingManagementMenu
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.manage.ListingEditorMenu
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

object BazaarGUIs {
	fun openBazaarListingHome(player: Player, parentWindow: CommonGuiWrapper?) {

	}

	fun openCitySelection(player: Player, parentWindow: CommonGuiWrapper?) {
		val menu = BazaarCitySelectionMenu(player)
		menu.openGui(parentWindow)
	}

	fun openCityBrowse(player: Player, city: TradeCityData, parentWindow: CommonGuiWrapper?) {
		val menu = BazaarCityBrowseMenu(player, city)
		menu.openGui(parentWindow)
	}

	fun openGlobalBrowse(player: Player, parentWindow: CommonGuiWrapper?) {
		val menu = BazaarGlowbalBrowseMenu(player)
		menu.openGui(parentWindow)
	}

	fun openCityItemListings(player: Player, city: TradeCityData, itemString: String, parentWindow: CommonGuiWrapper?) {
		val menu = CityItemListingsMenu(player, itemString, city)
		menu.openGui(parentWindow)
	}

	fun openGlobalItemListings(player: Player, itemString: String, parentWindow: CommonGuiWrapper?) {
		val menu = GlobalItemListingsMenu(player, itemString)
		menu.openGui(parentWindow)
	}

	fun openPurchaseMenu(player: Player, item: BazaarItem, backButtonHandler: () -> Unit) {
		val menu = PurchaseItemMenu(player, item, backButtonHandler)
		menu.openGui()
	}

	fun openListingManageMenu(player: Player, previous: CommonGuiWrapper?) {
		val defaultList = player.getSetting(PlayerSettings::listingManageDefaultListView)
		if (defaultList) openListingManageListMenu(player, previous)
		else openListingManageGridMenu(player, previous)
	}

	fun openListingManageListMenu(player: Player, previous: CommonGuiWrapper?) {
		val menu = ListListingManagementMenu(player)
		previous?.let { menu.setParent(it) }
		menu.openGui()
	}

	fun openListingManageGridMenu(player: Player, previous: CommonGuiWrapper?) {
		val menu = GridListingManagementMenu(player)
		previous?.let { menu.setParent(it) }
		menu.openGui()
	}

	fun openListingEditor(player: Player, listing: BazaarItem, previous: CommonGuiWrapper?) {
		val menu = ListingEditorMenu(player, listing)
		previous?.let { menu.setParent(it) }
		menu.openGui()
	}

	fun openBuyOrderMainMenu(player: Player, previous: CommonGuiWrapper?) {
		val menu = BuyOrderMainMenu(player)
		menu.openGui(previous)
	}

	fun openBuyOrderCreationMenu(player: Player, parent: CommonGuiWrapper? = null) {
		val menu = CreateBuyOrderMenu(player)
		parent?.let { menu.setParent(it) }
		menu.openGui()
	}

	fun openBuyOrderManageMenu(player: Player, previous: CommonGuiWrapper?) {
		val defaultList = player.getSetting(PlayerSettings::orderManageDefaultListView)
		if (defaultList) openBuyOrderManageListMenu(player, previous)
		else openBuyOrderManageGridMenu(player, previous)
	}

	fun openBuyOrderManageListMenu(player: Player, previous: CommonGuiWrapper?) {
		val menu = ListOrderManagementMenu(player)
		menu.openGui(previous)
	}

	fun openBuyOrderManageGridMenu(player: Player, previous: CommonGuiWrapper?) {
		val menu = GridOrderManagementWindow(player)
		menu.openGui(previous)
	}

	fun openBuyOrderFulfillmentMenu(player: Player, orderId: Oid<BazaarOrder>, previous: CommonGuiWrapper?) {
		BuyOrderFulfillmentMenu(player, orderId).openGui(previous)
	}

	fun openBazaarSettings(player: Player, parent: CommonGuiWrapper?) {
		val page = createSettingsPage(
			player,
			"Placement Settings",
			DBCachedBooleanToggle(
                Component.text("Skip Single Entry Menus"),
                butonDescription = "Skip directly to purchase menu when there is only one listing of an item.",
                icon = GuiItem.LIST,
                defaultValue = false,
                db = PlayerSettings::skipBazaarSingleEntryMenus
            )
		)

		page.openGui(parent)
	}
}
