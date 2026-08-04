package net.horizonsend.ion.server.gui.invui.bazaar.purchase.browse

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeInformationButton
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import org.bson.conversions.Bson
import org.bukkit.entity.Player
import org.litote.kmongo.gt

open class BazaarGlobalBrowseMenu(viewer: Player) : BazaarBrowseMenu(viewer) {
	override val menuTitleRight: Component = text("Global")
	override val contextName: String = "Global"
	override val isGlobalBrowse: Boolean = true

	override val bson: Bson = BazaarItem::stock gt 0

	override val infoButton = makeInformationButton(title = text("Information"),
		text("This menu shows items listed for sale from every tade city."),
		text("You may buy items from remote trade cities, but it will incur a 4x fee."),
		empty(),
		text("To view listings from individual cities, click the view city selection button"),
		text("button (top center)."),
	)

	override fun openListingsForItem(itemString: String) {
		BazaarGUIs.openGlobalItemListings(viewer, itemString, this)
	}

	override fun buildTitle(): Component {
		return withPageNumber(super.buildTitle())
	}
}
