package net.horizonsend.ion.server.gui.invui.bazaar.orders.browse

import net.horizonsend.ion.common.database.schema.economy.BazaarOrder
import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeInformationButton
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import org.bson.conversions.Bson
import org.bukkit.entity.Player
import org.litote.kmongo.eq

class OrderCityBrowseMenu(viewer: Player, city: TradeCityData) : AbstractBrowseMenu(viewer) {
	override val findBson: Bson = BazaarOrder::cityTerritory eq city.territoryId
	override val isGlobalBrowse: Boolean = false

	override val browseName: Component = text(city.displayName)

	override val infoButton = makeInformationButton(
		title = text("Information"),
		text("This menu displays orders that players have created at ${city.displayName}."),
		empty(),
		text("You may only fulfill orders while you are in the territory that the item"),
		text("is listed in.")
	)
}
