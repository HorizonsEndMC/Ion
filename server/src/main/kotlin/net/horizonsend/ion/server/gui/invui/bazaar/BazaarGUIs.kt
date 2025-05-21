package net.horizonsend.ion.server.gui.invui.bazaar

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.custom.settings.SettingsPageGui.Companion.createSettingsPage
import net.horizonsend.ion.server.features.gui.custom.settings.button.DBCachedBooleanToggle
import net.horizonsend.ion.server.gui.CommonGuiWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.orders.window.BuyOrderMainMenu
import net.horizonsend.ion.server.gui.invui.bazaar.orders.window.CreateBuyOrderMenu
import net.horizonsend.ion.server.gui.invui.bazaar.orders.window.manage.ManageOrdersMenu
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.BazaarCitySelectionMenu
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.browse.BazaarCityBrowseMenu
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.browse.BazaarGlowbalBrowseMenu
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.browse.PurchaseItemMenu
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.listings.CityItemListingsMenu
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.listings.GlobalItemListingsMenu
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

object BazaarGUIs {
	fun openCitySelection(player: Player, remote: Boolean, parentWindow: CommonGuiWrapper?): BazaarCitySelectionMenu {
		val menu = BazaarCitySelectionMenu(player, remote, parentWindow)
		menu.openGui()
		return menu
	}

	fun openCityBrowse(player: Player, remote: Boolean, city: TradeCityData, parentWindow: CommonGuiWrapper?, pageNumber: Int = 0): BazaarCityBrowseMenu {
		val menu = BazaarCityBrowseMenu(player, remote, city, parentWindow, pageNumber)
		menu.openGui()
		return menu
	}

	fun openGlobalBrowse(player: Player, remote: Boolean, parentWindow: CommonGuiWrapper?, pageNumber: Int = 0): BazaarGlowbalBrowseMenu {
		val menu = BazaarGlowbalBrowseMenu(player, remote, parentWindow, pageNumber)
		menu.openGui()
		return menu
	}

	fun openCityItemListings(player: Player, remote: Boolean, city: TradeCityData, itemString: String, parentWindow: CommonGuiWrapper?, previousPageNumber: Int? = null, pageNumber: Int = 0): CityItemListingsMenu {
		val menu = CityItemListingsMenu(player, remote, city, itemString, parentWindow, previousPageNumber, pageNumber)
		menu.openGui()
		return menu
	}

	fun openGlobalItemListings(player: Player, remote: Boolean, itemString: String, parentWindow: CommonGuiWrapper?, previousPageNumber: Int? = null, pageNumber: Int = 0): GlobalItemListingsMenu {
		val menu = GlobalItemListingsMenu(player, remote, itemString, parentWindow, previousPageNumber, pageNumber)
		menu.openGui()
		return menu
	}

	fun openPurchaseMenu(player: Player, remote: Boolean, item: BazaarItem, backButtonHandler: () -> Unit): PurchaseItemMenu {
		val menu = PurchaseItemMenu(player, remote, item, backButtonHandler)
		menu.openGui()
		return menu
	}

	fun openBuyOrderMainMenu(player: Player) {
		BuyOrderMainMenu(player).openGui()
	}

	fun openBuyOrderMainMenu(player: Player, previous: CommonGuiWrapper) {
		val menu = BuyOrderMainMenu(player)
		menu.setParent(previous)
		menu.openGui()
	}

	fun openBuyOrderCreationMenu(player: Player) {
		CreateBuyOrderMenu(player).openGui()
	}

	fun openBuyOrderManageMenu(player: Player) {
		ManageOrdersMenu(player).openGui()
	}

	fun openBuyOrderManageMenu(player: Player, previous: CommonGuiWrapper) {
		val menu = ManageOrdersMenu(player)
		menu.setParent(previous)
		menu.openGui()
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

		if (parent != null) page.setParent(parent)

		page.openGui()
	}
}
