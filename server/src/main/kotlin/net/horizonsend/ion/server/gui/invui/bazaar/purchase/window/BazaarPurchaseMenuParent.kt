package net.horizonsend.ion.server.gui.invui.bazaar.purchase.window

import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.utils.buttons.SimpleStateButton
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.gui.invui.utils.changeTitle
import net.horizonsend.ion.server.gui.invui.utils.setTitle
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
) : InvUIWindowWrapper(viewer) {
	abstract val contained: Gui

	private fun getMenuGUI(): Gui = TabGui.normal()
		.setStructure(
			"b . . 1 2 c . d i",
			"x x x x x x x x x",
			"x x x x x x x x x",
			"x x x x x x x x x",
			"x x x x x x x x x",
			"x x x x x x x x x"
		)
		.addIngredient('b', backButton)

		.addIngredient('1', citySelectionButton)
		.addIngredient('2', globalBrowseButton)
		.addIngredient('c', buyOrdersButton)

		.addIngredient('d', settingsButton)
		.addIngredient('i', infoButton)
		.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)

		.setTabs(listOf(contained))
		.build()

	override fun buildWindow(): Window = Window.single()
		.setGui(getMenuGUI())
		.setTitle(getMenuTitle())
		.setViewer(viewer)
		.build()

	fun refreshGuiText() {
		val window = currentWindow ?: return
		window.changeTitle(getMenuTitle())
	}

	private fun getMenuTitle(): Component {
		val baseText = if (remote) "Remote Bazaar" else "Bazaar"

		return GuiText(baseText)
			.setSlotOverlay(
				"# # # # # # # # #",
				". . . . . . . . .",
				". . . . . . . . .",
				". . . . . . . . .",
				". . . . . . . . .",
				"# # # # # # # # #"
			)
			.populateGuiText()
			.build()
	}

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
			BazaarGUIs.openCitySelection(player, remote)
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
			BazaarGUIs.openGlobalBrowse(player, remote)
		}
	}

	abstract val backButton: AbstractItem

	private val buyOrdersButton = GuiItem.CLOCKWISE
		.makeItem(text("Switch to the Buy Order Menu"))
		.makeGuiButton { clickType, player ->
			println("buy orders")
		}

	private val settingsButton = GuiItem.GEAR
		.makeItem(text("Bazaar GUI Settings"))
		.makeGuiButton { clickType, player ->
			println("settings")
		}

	private val infoButton = GuiItem.INFO
		.makeItem(text("Information"))
		.updateLore(listOf(
			text("Lore Line 1"),
			text("Lore Line 2"),
			text("Lore Line 3"),
		))
		.makeGuiButton { _, _ -> }
}
