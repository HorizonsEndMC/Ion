package net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.listings

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.gui.listings.IndividualListingGUI
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.BazaarPurchaseMenuParent
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.displayNameString
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.gt
import xyz.xenondevs.invui.item.impl.AbstractItem

class CityItemListingsMenu(
	viewer: Player,
	remote: Boolean,
	cityData: TradeCityData,
	itemString: String,
	private val previousPageNumber: Int? = null,
	pageNumber: Int = 0
) : BazaarPurchaseMenuParent(viewer, remote) {
	override val menuTitle: String = "${fromItemString(itemString).displayNameString} @ ${cityData.displayName}"
	override val contained: IndividualListingGUI = IndividualListingGUI(
		parentWindow = this,
		reOpenHandler = {
			BazaarGUIs.openCityItemListings(viewer, remote, cityData, itemString, pageNumber)
		},
		searchBson = and(BazaarItem::cityTerritory eq cityData.territoryId, BazaarItem::itemString eq itemString, BazaarItem::stock gt 0),
		pageNumber = pageNumber
	)

	override val citySelectionButton: AbstractItem = getCitySelectionButton(true)
	override val globalBrowseButton: AbstractItem = getGlobalBrowseButton(false)

	override val backButton: AbstractItem = GuiItem.LEFT.makeItem(text("Go Back to Viewing City Listings")).makeGuiButton { _, player ->
		BazaarGUIs.openCityBrowse(player, remote, cityData, previousPageNumber ?: 0)
	}
	override val infoButton: AbstractItem = GuiItem.INFO
		.makeItem(text("Information"))
		.updateLore(listOf(
			text("This menu shows individual listings of $itemString at ${cityData.displayName}."),
			text("Different players have listed this item for sale, and you can view"),
			text("How much stock their listings have, and the price they have set it at."),
		))
		.makeGuiButton { _, _ -> }

	override fun GuiText.populateGuiText(): GuiText {
		contained.modifyGuiText(this)
		return this
	}
}
