package net.horizonsend.ion.server.gui.invui.bazaar.purchase

import net.horizonsend.ion.server.features.gui.GuiItems.closeMenuItem
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.gui.invui.InvUIGuiWrapper
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.TabGui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.impl.AbstractItem

class BazaarMainPurchaseMenu(viewer: Player, remote: Boolean) : BazaarPurchaseMenuParent(viewer, remote) {
	private val tabs = listOf(
		CitySelectionGUI(this),
		GlobalBrowseGUI(this)
	)

	var currentTab = 0; private set

	private lateinit var guiInstance: TabGui

	override fun getMenuGUI(): Gui {
		val new = buildGui()
		guiInstance = new
		return new
	}

	private fun buildGui(): TabGui {
		return TabGui.normal()
			.applyPurchaseMenuStructure()
			.setTabs(tabs.map(InvUIGuiWrapper<*>::getGui))
			.addTabChangeHandler { _, tab ->
				currentTab = tab
				(tabs[currentTab] as? BazaarGui)?.refresh()
				refreshGuiText()
			}
			.build()
			.apply { setTab(currentTab) }
	}

	override fun GuiText.populateGuiText(): GuiText {
		(tabs[currentTab] as? BazaarGui)?.modifyGuiText(this)
		return this
	}

	fun setTab(tabNumber: Int) {
		guiInstance.setTab(tabNumber)
	}

	fun getTabGUI(): PagedGui<Item> {
		@Suppress("UNCHECKED_CAST")
		// It will always be a paged GUI
		return guiInstance.tabs[currentTab] as PagedGui<Item>
	}

	override val backButton: AbstractItem = closeMenuItem(viewer)
}
