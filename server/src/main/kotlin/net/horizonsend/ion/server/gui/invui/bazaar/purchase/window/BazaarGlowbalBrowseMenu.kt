package net.horizonsend.ion.server.gui.invui.bazaar.purchase.window

import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems.closeMenuItem
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.gui.GlobalBrowseGUI
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.impl.AbstractItem

class BazaarGlowbalBrowseMenu(viewer: Player, remote: Boolean, pageNumber: Int = 0) : BazaarPurchaseMenuParent(viewer, remote) {
	override val menuTitle: String = "Browsing All Items"
	override val contained: Gui = GlobalBrowseGUI(this, pageNumber).getGui()

	override val citySelectionButton: AbstractItem = getCitySelectionButton(false)
	override val globalBrowseButton: AbstractItem = getGlobalBrowseButton(true)

	override val backButton: AbstractItem = closeMenuItem(viewer)

	override val infoButton: AbstractItem = GuiItem.INFO
			.makeItem(text("Information"))
			.updateLore(listOf(
				text("Lore Line 11111"),
				text("Lore Line 2"),
				text("Lore Line 3"),
			))
			.makeGuiButton { _, _ -> }
}
