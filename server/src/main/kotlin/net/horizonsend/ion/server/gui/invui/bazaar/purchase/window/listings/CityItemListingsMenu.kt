package net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.listings

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeInformationButton
import net.kyori.adventure.text.Component.text
import org.bson.conversions.Bson
import org.bukkit.entity.Player
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.gt

class CityItemListingsMenu(viewer: Player, itemString: String, private val cityData: TradeCityData) : ItemListingMenu(viewer, itemString) {
	override val infoButton = makeInformationButton(title = text("Information"),
		text("This menu shows individual listings of $itemString at ${cityData.displayName}."),
		text("Different players have listed this item for sale, and you can view"),
		text("How much stock their listings have, and the price they have set it at."),
	)

	override val searchBson: Bson = and(BazaarItem::cityTerritory eq cityData.territoryId, BazaarItem::itemString eq itemString, BazaarItem::stock gt 0)

	override val contextName: String = cityData.displayName

	override fun openSearchResults(string: String) {
		BazaarGUIs.openCityItemListings(viewer, cityData, itemString, this)
	}
}
