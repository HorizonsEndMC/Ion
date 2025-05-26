package net.horizonsend.ion.server.gui.invui.bazaar.purchase.window

import net.horizonsend.ion.common.utils.text.BAZAAR_LISTING_HEADER_ICON
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_DARK_BLUE
import net.horizonsend.ion.common.utils.text.gui.GuiBorder
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.gui.CommonGuiWrapper
import net.horizonsend.ion.server.gui.invui.InvUIGuiWrapper
import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.utils.buttons.SimpleStateButton
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.TabGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.window.Window

abstract class BazaarPurchaseMenuParent(
	viewer: Player,
	val remote: Boolean,
	final override var parentWindow: CommonGuiWrapper?
) : InvUIWindowWrapper(viewer, async = true) {
	abstract val contained: InvUIGuiWrapper<out Gui>

	private fun getMenuGUI(): Gui = TabGui.normal()
		.setStructure(
			"b m . 1 2 o . d i",
			"x x x x x x x x x",
			"x x x x x x x x x",
			"x x x x x x x x x",
			"x x x x x x x x x",
			"x x x x x x x x x"
		)
		.addIngredient('b', backButton)
		.addIngredient('m', manageListingsButton)

		.addIngredient('1', citySelectionButton)
		.addIngredient('2', globalBrowseButton)
		.addIngredient('o', buyOrdersButton)

		.addIngredient('d', settingsButton)
		.addIngredient('i', infoButton)
		.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)

		.setTabs(listOf(contained.getGui()))
		.build()

	override fun buildWindow(): Window = normalWindow(getMenuGUI())

	protected abstract val menuTitleLeft: Component
	protected abstract val menuTitleRight: Component

	override fun buildTitle(): Component = GuiText("")
		.populateGuiText()
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

	open fun GuiText.populateGuiText(): GuiText { return this }

	abstract val citySelectionButton: AbstractItem

	protected fun getCitySelectionButton(selected: Boolean) = object : SimpleStateButton(
		state = selected,
		onTrue = GuiItem.CITY.makeItem()
			.updateLore(listOf(
				text("View list of cities that are selling goods."),
				text("You'll be able to view listings from this menu."),
				Component.empty(),
				text("You currently have this tab selected")
			))
			.updateDisplayName(text("View City Selection")),
		onFalse = GuiItem.CITY_GRAY.makeItem()
			.updateLore(listOf(
				text("View list of cities that are selling goods."),
				text("You'll be able to view listings from this menu."),
				Component.empty(),
				text("Click to switch to this tab."),
			))
			.updateDisplayName(text("View City Selection"))
	) {
		override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
			BazaarGUIs.openCitySelection(player, remote, this@BazaarPurchaseMenuParent)
		}
	}

	abstract val globalBrowseButton: AbstractItem

	protected fun getGlobalBrowseButton(selected: Boolean) = object : SimpleStateButton(
		state = selected,
		onTrue = GuiItem.WORLD.makeItem()
			.updateLore(listOf(
				text("View listings from every city, combined"),
				text("into one menu."),
				Component.empty(),
				text("You currently have this tab selected")
			))
			.updateDisplayName(text("View Global Listings")),
		onFalse = GuiItem.WORLD_GRAY.makeItem()
			.updateLore(listOf(
				text("View listings from every city, combined"),
				text("into one menu."),
				Component.empty(),
				text("Click to switch to this tab."),
			))
			.updateDisplayName(text("View Global Listings"))
	) {
		override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
			BazaarGUIs.openGlobalBrowse(player, remote, this@BazaarPurchaseMenuParent)
		}
	}

	val backButton =
		if (parentWindow == null) GuiItems.closeMenuItem(viewer)
		else GuiItem.CANCEL.makeItem(text("Go Back to Previous Menu")).makeGuiButton { _, _ -> getParent()?.openGui() }

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
		.makeItem(text("Bazaar GUI Settings"))
		.makeGuiButton { _, player ->
			BazaarGUIs.openBazaarSettings(player, this)
		}
}
