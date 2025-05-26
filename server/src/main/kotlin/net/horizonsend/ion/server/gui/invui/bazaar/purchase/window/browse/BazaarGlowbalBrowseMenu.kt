package net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.browse

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import org.bson.conversions.Bson
import org.bukkit.entity.Player
import org.litote.kmongo.gt
import xyz.xenondevs.invui.item.impl.AbstractItem

class BazaarGlowbalBrowseMenu(viewer: Player) : BazaarBrowseMenu(viewer) {
	override val menuTitleRight: Component = text("Global")
	override val contextName: String = "Global"
	override val isGlobalBrowse: Boolean = true

	override val bson: Bson = BazaarItem::stock gt 0

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

	override fun openListingsForItem(itemString: String) {
		BazaarGUIs.openGlobalItemListings(viewer, itemString, this)
	}

	override fun buildTitle(): Component {
		return withPageNumber(super.buildTitle())
	}
}
