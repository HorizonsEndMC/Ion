package net.horizonsend.ion.server.gui.invui.bazaar.purchase.window

import net.horizonsend.ion.server.features.gui.GuiItems.closeMenuItem
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.gui.GlobalBrowseGUI
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.impl.AbstractItem

class BazaarGlowbalBrowseMenu(viewer: Player, remote: Boolean, pageNumber: Int = 0) : BazaarPurchaseMenuParent(viewer, remote) {
	override val contained: Gui = GlobalBrowseGUI(this, pageNumber).getGui()

	override val citySelectionButton: AbstractItem = getCitySelectionButton(false)
	override val globalBrowseButton: AbstractItem = getGlobalBrowseButton(true)

	override val backButton: AbstractItem = closeMenuItem(viewer)
}
