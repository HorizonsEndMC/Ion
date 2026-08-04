package net.horizonsend.ion.server.gui.invui.bazaar.orders.browse

import net.horizonsend.ion.common.database.schema.economy.BazaarOrder
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeInformationButton
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import org.bson.conversions.Bson
import org.bukkit.entity.Player
import org.litote.kmongo.EMPTY_BSON

class OrderGlobalBrowseMenu(
	viewer: Player,
	fulfillmentHandler: AbstractBrowseMenu.(BazaarOrder) -> Unit = { BazaarGUIs.openBuyOrderFulfillmentMenu(viewer, it._id, this) }
) : AbstractBrowseMenu(viewer, fulfillmentHandler) {
	override val findBson: Bson = EMPTY_BSON
	override val isGlobalBrowse: Boolean = true

	override val browseName: Component = text("Global")

	override val infoButton = makeInformationButton(
		title = text("Information"),
		text("This menu displays orders that players have created at every trade city."),
		empty(),
		text("You may only fulfill orders while you are in the territory that the item"),
		text("is listed in.")
	)
}
