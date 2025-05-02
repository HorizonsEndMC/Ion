package net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.browse

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.gui.listings.GroupedListingGUI
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.BazaarPurchaseMenuParent
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.gt
import xyz.xenondevs.invui.item.impl.AbstractItem

class BazaarCityBrowseMenu(viewer: Player, remote: Boolean, cityData: TradeCityData, pageNumber: Int = 0) : BazaarPurchaseMenuParent(viewer, remote) {
	override val menuTitle: String = "Browsing ${cityData.displayName}'s Listings"
	override val contained: GroupedListingGUI = GroupedListingGUI(
		parentWindow = this,
		searchBson = and(BazaarItem::stock gt 0, BazaarItem::cityTerritory eq cityData.territoryId),
		searchFunction = { println("search") },
		reOpenHandler = { BazaarGUIs.openCityBrowse(viewer, remote, cityData, pageNumber) },
		itemMenuHandler = { itemString -> BazaarGUIs.openCityItemListings(viewer, remote, cityData, itemString, this.pageNumber, 0) },
		pageNumber = pageNumber
	)

	override val citySelectionButton: AbstractItem = getCitySelectionButton(true)
	override val globalBrowseButton: AbstractItem = getGlobalBrowseButton(false)

	override val backButton: AbstractItem = GuiItem.LEFT.makeItem(text("Go Back to City Selection")).makeGuiButton { _, player ->
		BazaarGUIs.openCitySelection(player, true)
	}

	override val infoButton: AbstractItem = GuiItem.INFO
		.makeItem(text("Information"))
		.updateLore(listOf(
			text("This menu shows items that players have listed for sale at ${cityData.displayName}"),
			text("Multiple players can be selling the same item at one city, and clicking on"),
			text("an item will show those listings."),
		))
		.makeGuiButton { _, _ -> }

	override fun GuiText.populateGuiText(): GuiText {
		contained.modifyGuiText(this)
		return this
	}
}
