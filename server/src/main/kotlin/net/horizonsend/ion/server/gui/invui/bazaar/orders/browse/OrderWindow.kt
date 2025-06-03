package net.horizonsend.ion.server.gui.invui.bazaar.orders.browse

import net.horizonsend.ion.common.database.schema.economy.BazaarOrder
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.bazaar.IndividualBrowseGui
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player
import xyz.xenondevs.invui.item.ItemProvider

interface OrderWindow : IndividualBrowseGui<BazaarOrder> {
	override fun goToCitySelection(viewer: Player) {
		OrderCitySelection(viewer).openGui(this)
	}

	override fun goToGlobalBrowse(viewer: Player) {
		OrderGlobalBrowseMenu(viewer).openGui(this)
	}

	val listingBrowseButton get() = GuiItem.DOWN.makeItem(text("View Bazaar Sale Listings")).makeGuiButton { _, player -> BazaarGUIs.openCitySelection(player, this) }

	val infoButton: ItemProvider
}
