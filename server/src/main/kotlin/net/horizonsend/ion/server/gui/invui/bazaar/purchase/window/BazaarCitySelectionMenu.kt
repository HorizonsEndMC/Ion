package net.horizonsend.ion.server.gui.invui.bazaar.purchase.window

import net.horizonsend.ion.server.features.gui.GuiItems.closeMenuItem
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.gui.CitySelectionGUI
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.impl.AbstractItem

class BazaarCitySelectionMenu(viewer: Player, remote: Boolean) : BazaarPurchaseMenuParent(viewer, remote) {
	override val contained: Gui = CitySelectionGUI(this).getGui()

	override val citySelectionButton: AbstractItem = getCitySelectionButton(true)
	override val globalBrowseButton: AbstractItem = getGlobalBrowseButton(false)

	override val backButton: AbstractItem = closeMenuItem(viewer)
}
