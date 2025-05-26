package net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.listings

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component.text
import org.bson.conversions.Bson
import org.bukkit.entity.Player
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.gt
import xyz.xenondevs.invui.item.impl.AbstractItem

class CityItemListingsMenu(viewer: Player, itemString: String, private val cityData: TradeCityData) : ItemListingMenu(viewer, itemString) {
	override val infoButton: AbstractItem = GuiItem.INFO
		.makeItem(text("Information"))
		.updateLore(listOf(
			text("This menu shows individual listings of $itemString at ${cityData.displayName}."),
			text("Different players have listed this item for sale, and you can view"),
			text("How much stock their listings have, and the price they have set it at."),
		))
		.makeGuiButton { _, _ -> }

	override val searchBson: Bson = and(BazaarItem::cityTerritory eq cityData.territoryId, BazaarItem::itemString eq itemString, BazaarItem::stock gt 0)

	override val contextName: String = cityData.displayName

	override fun openSearchResults(string: String) {
		BazaarGUIs.openCityItemListings(viewer, cityData, itemString, this)
	}
}
