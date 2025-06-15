package net.horizonsend.ion.server.gui.invui.bazaar.purchase.browse

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeInformationButton
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.gt

open class BazaarCityBrowseMenu(viewer: Player, protected val cityData: TradeCityData) : BazaarBrowseMenu(viewer) {
	override val menuTitleRight: Component = text(cityData.displayName)
	override val contextName: String = "${cityData.displayName}'s"
	override val isGlobalBrowse: Boolean = false

	override val bson = and(BazaarItem::stock gt 0, BazaarItem::cityTerritory eq cityData.territoryId)

	override val infoButton = makeInformationButton(title = text("Information"),
		text("This menu shows items that players have listed for sale at ${cityData.displayName}"),
		text("Multiple players can be selling the same item at one city, and clicking on"),
		text("an item will show those listings."),
	)

	override fun buildTitle(): Component {
		return withPageNumber(super.buildTitle())
	}

	override fun openListingsForItem(itemString: String) {
		BazaarGUIs.openCityItemListings(viewer, cityData, itemString, this)
	}
}
