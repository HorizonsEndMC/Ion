package net.horizonsend.ion.server.gui.invui.bazaar.purchase.gui

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.BazaarPurchaseMenuParent
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.kyori.adventure.text.Component.text
import org.bson.conversions.Bson
import org.litote.kmongo.gt

class GlobalBrowseGUI(parent: BazaarPurchaseMenuParent, pageNumber: Int = 0) : BrowseGUIParent(parent, pageNumber) {
	override val searchBson: Bson = BazaarItem::stock gt 0

	override val searchButton = GuiItem.MAGNIFYING_GLASS
		.makeItem(text("Search for Items"))
		.makeGuiButton { _, _ ->
			println("Search")
		}

	override fun reOpen() {
		BazaarGUIs.openGlobalBrowse(parent.viewer, parent.remote, pageNumber)
	}
}
