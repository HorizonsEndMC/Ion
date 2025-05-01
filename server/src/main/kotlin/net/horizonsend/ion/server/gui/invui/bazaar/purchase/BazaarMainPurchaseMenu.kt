package net.horizonsend.ion.server.gui.invui.bazaar.purchase

import net.horizonsend.ion.server.features.gui.GuiItems.closeMenuItem
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.gui.invui.InvUIGuiWrapper
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.TabGui
import xyz.xenondevs.invui.item.impl.AbstractItem

class BazaarMainPurchaseMenu(viewer: Player, remote: Boolean) : BazaarPurchaseMenuParent(viewer, remote) {
	private val tabs = listOf(
		CitySelectionGUI(this),
		GlobalBrowseGUI(this)
	)

	private var currentTab = 0

	override fun getMenuGUI(): Gui {
		return TabGui.normal()
			.applyPurchaseMenuStructure()
			.setTabs(tabs.map(InvUIGuiWrapper<*>::getGui))
			.addTabChangeHandler { _, tab ->
				currentTab = tab
				refreshGuiText()
			}
			.build()
	}

	override fun GuiText.populateGuiText(): GuiText {
		(tabs[currentTab] as? BazaarGui)?.modifyGuiText(this)
		return this
	}

	override val backButton: AbstractItem = closeMenuItem(viewer)
}
