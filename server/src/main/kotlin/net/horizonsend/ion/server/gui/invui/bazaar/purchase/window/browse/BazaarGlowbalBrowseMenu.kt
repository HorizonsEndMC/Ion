package net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.browse

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems.closeMenuItem
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.gui.GroupedListingGUI
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.BazaarPurchaseMenuParent
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player
import org.litote.kmongo.gt
import xyz.xenondevs.invui.item.impl.AbstractItem

class BazaarGlowbalBrowseMenu(viewer: Player, remote: Boolean, pageNumber: Int = 0) : BazaarPurchaseMenuParent(viewer, remote) {
	override val menuTitle: Component = text("Browsing All Items")
	override val contained: GroupedListingGUI = GroupedListingGUI(
		parentWindow = this,
		searchBson = BazaarItem::stock gt 0,
		contextName = "Global",
		reOpenHandler = { BazaarGUIs.openGlobalBrowse(viewer, remote, pageNumber) },
		itemMenuHandler = { itemString -> BazaarGUIs.openGlobalItemListings(viewer, remote, itemString, this.pageNumber, 0) },
		searchResultConsumer = { itemString -> BazaarGUIs.openGlobalItemListings(viewer, remote, itemString, previousPageNumber = -1) },
		pageNumber = pageNumber
	)

	override val citySelectionButton: AbstractItem = getCitySelectionButton(false)
	override val globalBrowseButton: AbstractItem = getGlobalBrowseButton(true)

	override val backButton: AbstractItem = closeMenuItem(viewer)

	override val infoButton: AbstractItem = GuiItem.INFO
			.makeItem(text("Information"))
			.updateLore(listOf(
				text("This menu shows items listed for sale from every tade city."),
				text("Lore Line 2"),
				text("Lore Line 3"),
				empty(),
				text("To view listings from individual cities, click the view city selection button"),
				text("button (top center)."),
			))
			.makeGuiButton { _, _ -> }

	override fun GuiText.populateGuiText(): GuiText {
		contained.modifyGuiText(this)
		return this
	}
}
