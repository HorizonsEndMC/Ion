package net.horizonsend.ion.server.gui.invui.bazaar

import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.BazaarCitySelectionMenu
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.browse.BazaarCityBrowseMenu
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.browse.BazaarGlowbalBrowseMenu
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.listings.CityItemListingsMenu
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.listings.GlobalItemListingsMenu
import org.bukkit.entity.Player

object BazaarGUIs {
	fun openCitySelection(player: Player, remote: Boolean) {
		BazaarCitySelectionMenu(player, remote).openGui()
	}

	fun openCityBrowse(player: Player, remote: Boolean, city: TradeCityData, pageNumber: Int = 0) {
		BazaarCityBrowseMenu(player, remote, city, pageNumber).openGui()
	}

	fun openGlobalBrowse(player: Player, remote: Boolean, pageNumber: Int = 0) {
		BazaarGlowbalBrowseMenu(player, remote, pageNumber).openGui()
	}

	fun openCityItemListings(player: Player, remote: Boolean, city: TradeCityData, itemString: String, pageNumber: Int = 0) {
		CityItemListingsMenu(player, remote, city, itemString, pageNumber).openGui()
	}

	fun openGlobalItemListings(player: Player, remote: Boolean, itemString: String, pageNumber: Int = 0) {
		GlobalItemListingsMenu(player, remote, itemString, pageNumber).openGui()
	}
}
