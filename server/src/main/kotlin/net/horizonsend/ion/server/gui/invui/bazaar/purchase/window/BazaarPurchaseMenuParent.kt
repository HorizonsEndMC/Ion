package net.horizonsend.ion.server.gui.invui.bazaar.purchase.window

import net.horizonsend.ion.common.utils.text.BAZAAR_LISTING_HEADER_ICON
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_DARK_BLUE
import net.horizonsend.ion.common.utils.text.gui.GuiBorder
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.gui.invui.ListInvUIWindow
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.TabGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.window.Window

abstract class BazaarPurchaseMenuParent<T : Any>(viewer: Player) : ListInvUIWindow<T>(viewer, async = true) {
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

		.addIngredient('d', settingsButton)
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

	abstract val isGlobalBrowse: Boolean

	private val citySelectionButton get() =
		(if (!isGlobalBrowse) GuiItem.CITY.makeItem(text("Go to city selection")).updateLore(listOf(text("You already have this menu selected.")))
		else GuiItem.CITY_GRAY.makeItem(text("Go to city selection"))).makeGuiButton { _, player -> BazaarGUIs.openCitySelection(player, this) }

	private val globalBrowseButton get() =
		(if (isGlobalBrowse) GuiItem.WORLD .makeItem(text("Go to global browse")).updateLore(listOf(text("You already have this menu selected.")))
		else GuiItem.WORLD_GRAY.makeItem(text("Go to global browse"))).makeGuiButton { _, player -> BazaarGUIs.openGlobalBrowse(player, this) }

	abstract val infoButton: AbstractItem

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

	private val settingsButton = GuiItem.GEAR
		.makeItem(text("Open Bazaar Settings"))
		.makeGuiButton { _, player ->
			BazaarGUIs.openBazaarSettings(player, this)
		}
}
