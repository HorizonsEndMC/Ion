package net.horizonsend.ion.server.gui.invui.bazaar.purchase.gui.browse

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.BazaarPurchaseMenuParent
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.kyori.adventure.text.Component.text
import org.bson.conversions.Bson
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.gt

class CityListingGUI(parent: BazaarPurchaseMenuParent, val city: TradeCityData, pageNumber: Int = 0) : ListingGUIParent(parent, pageNumber) {
	override val searchBson: Bson = and(BazaarItem::stock gt 0, BazaarItem::cityTerritory eq city.territoryId)

	override val searchButton = GuiItem.MAGNIFYING_GLASS
		.makeItem(text("Search for Items"))
		.makeGuiButton { _, _ ->
			println("Search")
		}

	override fun reOpen() {
		BazaarGUIs.openCityBrowse(parent.viewer, parent.remote, city, pageNumber)
	}
}
