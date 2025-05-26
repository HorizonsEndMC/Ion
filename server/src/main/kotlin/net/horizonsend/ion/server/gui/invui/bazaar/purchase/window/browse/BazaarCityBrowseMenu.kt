package net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.browse

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.gui.CommonGuiWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.gui.GroupedListingGUI
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.BazaarPurchaseMenuParent
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.gt
import xyz.xenondevs.invui.item.impl.AbstractItem

class BazaarCityBrowseMenu(viewer: Player, remote: Boolean, cityData: TradeCityData, parentWindow: CommonGuiWrapper?, pageNumber: Int = 0) : BazaarPurchaseMenuParent(viewer, remote, parentWindow) {
	override val menuTitleLeft: Component = text("Browsing")
	override val menuTitleRight: Component = text(cityData.displayName)

	override val contained: GroupedListingGUI = GroupedListingGUI(
		parentWindow = this,
		searchBson = and(BazaarItem::stock gt 0, BazaarItem::cityTerritory eq cityData.territoryId),
		reOpenHandler = { BazaarGUIs.openCityBrowse(viewer, remote, cityData, this@BazaarCityBrowseMenu, pageNumber) },
		itemMenuHandler = { itemString -> BazaarGUIs.openCityItemListings(viewer, remote, cityData, itemString, this@BazaarCityBrowseMenu, this.pageNumber, 0) },
		contextName = "${cityData.displayName}'s",
		searchResultConsumer = { itemString -> BazaarGUIs.openCityItemListings(viewer, remote, cityData, itemString, this@BazaarCityBrowseMenu, previousPageNumber = -1) },
		pageNumber = pageNumber
	)

	override val citySelectionButton: AbstractItem = getCitySelectionButton(true)
	override val globalBrowseButton: AbstractItem = getGlobalBrowseButton(false)

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
