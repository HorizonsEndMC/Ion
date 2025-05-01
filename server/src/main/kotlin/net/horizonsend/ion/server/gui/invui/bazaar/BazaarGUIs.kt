package net.horizonsend.ion.server.gui.invui.bazaar

import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.BazaarCityBrowseMenu
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.BazaarCitySelectionMenu
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.BazaarGlowbalBrowseMenu
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
}
