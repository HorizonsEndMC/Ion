package net.horizonsend.ion.server.gui.invui.bazaar.purchase

import net.horizonsend.ion.common.utils.text.BAZAAR_LISTING_HEADER_ICON
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_DARK_BLUE
import net.horizonsend.ion.common.utils.text.gui.GuiBorder
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.gui.invui.ListInvUIWindow
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.bazaar.BrowseGui
import net.horizonsend.ion.server.gui.invui.bazaar.getBazaarSettingsButton
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.TabGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.window.Window

abstract class BazaarPurchaseMenuParent<T : Any>(viewer: Player) : ListInvUIWindow<T>(viewer, async = true), BrowseGui {
	abstract fun getGui(): Gui

	private fun getMenuGUI(): Gui = TabGui.normal()
		.setStructure(
			"b m . 1 2 o . d i",
			"x x x x x x x x x",
			"x x x x x x x x x",
			"x x x x x x x x x",
			"x x x x x x x x x",
			"x x x x x x x x x"
		)
		.addIngredient('b', parentOrBackButton())
		.addIngredient('m', manageListingsButton)

		.addIngredient('1', citySelectionButton)
		.addIngredient('2', globalBrowseButton)
		.addIngredient('o', buyOrdersButton)

		.addIngredient('d', getBazaarSettingsButton())
		.addIngredient('i', infoButton)
		.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)

		.setTabs(listOf(getGui()))
		.build()

	override fun buildWindow(): Window = normalWindow(getMenuGUI())

	protected abstract val menuTitleLeft: Component
	protected abstract val menuTitleRight: Component

	override fun buildTitle(): Component = GuiText("")
		.addBorder(GuiBorder.regular(
			color = HE_DARK_BLUE,
			headerIcon = GuiBorder.HeaderIcon(BAZAAR_LISTING_HEADER_ICON, 48, HE_DARK_BLUE),
			leftText = menuTitleLeft,
			rightText = menuTitleRight
		))
		.setSlotOverlay(
			"# # # # # # # # #",
			". . . . . . . . .",
			". . . . . . . . .",
			". . . . . . . . .",
			". . . . . . . . .",
			"# # # # # # # # #"
		)
		.build()

	abstract val infoButton: ItemProvider

	private val manageListingsButton = GuiItem.MATERIALS
		.makeItem(text("Manage Your Listings"))
		.makeGuiButton { _, _ ->
			BazaarGUIs.openListingManageMenu(viewer, this)
		}

	private val buyOrdersButton = GuiItem.UP
		.makeItem(text("Switch to the Buy Order Menu"))
		.makeGuiButton { _, _ ->
			BazaarGUIs.openBuyOrderMainMenu(viewer, this)
		}

	override fun goToCitySelection(viewer: Player) {
		BazaarGUIs.openCitySelection(viewer, this)
	}

	override fun goToGlobalBrowse(viewer: Player) {
		BazaarGUIs.openGlobalBrowse(viewer, this)
	}
}
