package net.horizonsend.ion.server.gui.invui.bazaar.purchase

import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems.closeMenuItem
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.gui.invui.InvUIGuiWrapper
import net.horizonsend.ion.server.gui.invui.utils.TabButton
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.impl.AbstractItem

class BazaarMainPurchaseMenu(
	viewer: Player,
	remote: Boolean
) : BazaarPurchaseMenuParent(viewer, remote) {
	override val tabs: List<InvUIGuiWrapper<out Gui>> = listOf(
		CitySelectionGUI(this),
		GlobalBrowseGUI(this)
	)

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
	override val citySelectionButton: AbstractItem = TabButton(
		GuiItem.CITY.makeItem()
			.updateLore(listOf(
				text("View list of cities that are selling goods."),
				text("You'll be able to view listings from this menu."),
				Component.empty(),
				text("You currently have this tab selected")
			))
			.updateDisplayName(text("View City Selection")),
		GuiItem.CITY_GRAY.makeItem()
			.updateLore(listOf(
				text("View list of cities that are selling goods."),
				text("You'll be able to view listings from this menu."),
				Component.empty(),
				text("Click to switch to this tab."),
			))
			.updateDisplayName(text("View City Selection")),
		0
	)
	override val globalBrowseButton: AbstractItem = TabButton(
		GuiItem.WORLD.makeItem()
			.updateLore(listOf(
				text("View listings from every city, combined"),
				text("into one menu."),
				Component.empty(),
				text("You currently have this tab selected")
			))
			.updateDisplayName(text("View Global Listings")),
		GuiItem.WORLD_GRAY.makeItem()
			.updateLore(listOf(
				text("View listings from every city, combined"),
				text("into one menu."),
				Component.empty(),
				text("Click to switch to this tab."),
			))
			.updateDisplayName(text("View Global Listings")),
		1
	)
}
