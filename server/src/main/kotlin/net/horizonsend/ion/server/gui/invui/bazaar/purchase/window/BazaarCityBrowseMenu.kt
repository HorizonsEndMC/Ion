package net.horizonsend.ion.server.gui.invui.bazaar.purchase.window

import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.gui.CityBrowseGUI
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.impl.AbstractItem

class BazaarCityBrowseMenu(viewer: Player, remote: Boolean, cityData: TradeCityData, pageNumber: Int = 0) : BazaarPurchaseMenuParent(viewer, remote) {
	override val menuTitle: String = "Browsing ${cityData.displayName}'s Listings"
	override val contained: Gui = CityBrowseGUI(this, cityData, pageNumber).getGui()

	override val citySelectionButton: AbstractItem = getCitySelectionButton(false)
	override val globalBrowseButton: AbstractItem = getGlobalBrowseButton(true)

	override val backButton: AbstractItem = GuiItem.CANCEL.makeItem(text("Go Back to City Selection")).makeGuiButton { _, player ->
		BazaarGUIs.openCitySelection(player, true)
	}

	override val infoButton: AbstractItem = GuiItem.INFO
		.makeItem(text("Information"))
		.updateLore(listOf(
			text("Lore Line 1"),
			text("Lore Line 2"),
			text("Lore Line 3"),
		))
		.makeGuiButton { _, _ -> }
}
