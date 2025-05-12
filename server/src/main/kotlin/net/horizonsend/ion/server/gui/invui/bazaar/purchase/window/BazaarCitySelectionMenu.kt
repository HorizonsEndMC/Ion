package net.horizonsend.ion.server.gui.invui.bazaar.purchase.window

import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.gui.CommonGuiWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.gui.CitySelectionGUI
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player
import xyz.xenondevs.invui.item.impl.AbstractItem

class BazaarCitySelectionMenu(viewer: Player, remote: Boolean, parentWindow: CommonGuiWrapper?) : BazaarPurchaseMenuParent(viewer, remote, parentWindow) {
	override val menuTitle: Component = text("Browsing Active Trade Cities")
	override val contained = CitySelectionGUI(this)

	override val citySelectionButton: AbstractItem = getCitySelectionButton(true)
	override val globalBrowseButton: AbstractItem = getGlobalBrowseButton(false)

	override val infoButton: AbstractItem = GuiItem.INFO
		.makeItem(text("Information"))
		.updateLore(listOf(
			text("All bazaar listings are made at trade cities, both NPC and player created."),
			text("Players list items for sale at these cities, and you can browse what is being"),
			text("sold at those citites from this menu. If you are not in the territory of"),
			text("the city selling these items, there will be a 4x cost penalty for purchases."),
			empty(),
			text("To view listings from every city in one menu, click the view global listings button"),
			text("button (top center)."),
		))
		.makeGuiButton { _, _ -> }
}
