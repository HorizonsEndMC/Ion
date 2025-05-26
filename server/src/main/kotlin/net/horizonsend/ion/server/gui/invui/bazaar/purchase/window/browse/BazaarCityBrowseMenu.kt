package net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.browse

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.gt
import xyz.xenondevs.invui.item.impl.AbstractItem

class BazaarCityBrowseMenu(viewer: Player, private val cityData: TradeCityData) : BazaarBrowseMenu(viewer) {
	override val menuTitleRight: Component = text(cityData.displayName)
	override val contextName: String = "${cityData.displayName}'s"
	override val isGlobalBrowse: Boolean = false

	override val bson = and(BazaarItem::stock gt 0, BazaarItem::cityTerritory eq cityData.territoryId)

	override val infoButton: AbstractItem = GuiItem.INFO
		.makeItem(text("Information"))
		.updateLore(listOf(
			text("This menu shows items that players have listed for sale at ${cityData.displayName}"),
			text("Multiple players can be selling the same item at one city, and clicking on"),
			text("an item will show those listings."),
		))
		.makeGuiButton { _, _ -> }

	override fun buildTitle(): Component {
		return withPageNumber(super.buildTitle())
	}

	override fun openListingsForItem(itemString: String) {
		BazaarGUIs.openCityItemListings(viewer, cityData, itemString, this)
	}
}
