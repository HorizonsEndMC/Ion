package net.horizonsend.ion.server.gui.invui.bazaar

import net.horizonsend.ion.common.database.cache.nations.AbstractPlayerCache
import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.custom.settings.SettingsPageGui.Companion.createSettingsPage
import net.horizonsend.ion.server.features.gui.custom.settings.button.DBCachedBooleanToggle
import net.horizonsend.ion.server.gui.CommonGuiWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.orders.BuyOrderMainMenu
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.PurchaseItemMenu
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.BazaarCitySelectionMenu
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.browse.BazaarCityBrowseMenu
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.browse.BazaarGlowbalBrowseMenu
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.listings.CityItemListingsMenu
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.listings.GlobalItemListingsMenu
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

object BazaarGUIs {
	fun openCitySelection(player: Player, remote: Boolean): BazaarCitySelectionMenu {
		val menu = BazaarCitySelectionMenu(player, remote)
		menu.openGui()
		return menu
	}

	fun openCityBrowse(player: Player, remote: Boolean, city: TradeCityData, pageNumber: Int = 0): BazaarCityBrowseMenu {
		val menu = BazaarCityBrowseMenu(player, remote, city, pageNumber)
		menu.openGui()
		return menu
	}

	fun openGlobalBrowse(player: Player, remote: Boolean, pageNumber: Int = 0): BazaarGlowbalBrowseMenu {
		val menu = BazaarGlowbalBrowseMenu(player, remote, pageNumber)
		menu.openGui()
		return menu
	}

	fun openCityItemListings(player: Player, remote: Boolean, city: TradeCityData, itemString: String, previousPageNumber: Int? = null, pageNumber: Int = 0): CityItemListingsMenu {
		val menu = CityItemListingsMenu(player, remote, city, itemString, previousPageNumber, pageNumber)
		menu.openGui()
		return menu
	}

	fun openGlobalItemListings(player: Player, remote: Boolean, itemString: String, previousPageNumber: Int? = null, pageNumber: Int = 0): GlobalItemListingsMenu {
		val menu = GlobalItemListingsMenu(player, remote, itemString, previousPageNumber, pageNumber)
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

	fun openBazaarSettings(player: Player, parent: CommonGuiWrapper?) {
		val page = createSettingsPage(
			player,
			"Placement Settings",
			DBCachedBooleanToggle(
				Component.text("Skip Single Entry Menus"),
				butonDescription = "Skip directly to purchase menu when there is only one listing of an item.",
				icon = GuiItem.LIST,
				defaultValue = false,
				db = SLPlayer::skipBazaarSingleEntryMenus,
				cache = AbstractPlayerCache.PlayerData::skipBazaarSingleEntryMenus
			)
		)

		if (parent != null) page.parent = parent

		page.openGui()
	}
}
