package net.horizonsend.ion.server.gui.invui.bazaar.orders

import net.horizonsend.ion.common.utils.text.BAZAAR_BUY_ORDER_MENU_CHARACTER
import net.horizonsend.ion.common.utils.text.BAZAAR_ORDER_HEADER_ICON
import net.horizonsend.ion.common.utils.text.DEFAULT_GUI_WIDTH
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_ORANGE
import net.horizonsend.ion.common.utils.text.gui.GuiBorder
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.bazaar.getBazaarSettingsButton
import net.horizonsend.ion.server.gui.invui.bazaar.orders.browse.OrderGlobalBrowseMenu
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeInformationButton
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.window.Window

class BuyOrderMainMenu(viewer: Player) : InvUIWindowWrapper(viewer) {
	override fun buildWindow(): Window {
		val gui = Gui.normal()
			.setStructure(
				"c . . . . . . s i",
				"m m m m . b b b b",
				"m m m m . b b b b",
				"m m m m . b b b b",
				"m m m m . b b b b",
				". . . . . . . . .",
			)
			.addIngredient('c', parentOrBackButton())
			.addIngredient('s', getBazaarSettingsButton())
			.addIngredient('i', infoButton)
			.addIngredient('m', manageButton)
			.addIngredient('b', browseButton)
			.build()

		return normalWindow(gui)
	}

	override fun buildTitle(): Component = ofChildren(
		GuiText("")
			.addBackground(GuiText.GuiBackground(backgroundChar = BAZAAR_BUY_ORDER_MENU_CHARACTER))
			.addBorder(GuiBorder.regular(HE_LIGHT_ORANGE, GuiBorder.HeaderIcon(BAZAAR_ORDER_HEADER_ICON, 48, HE_LIGHT_ORANGE), leftText = text("Buy Orders"), rightText = text("Main Menu")))
			.build(),
		GuiText("", guiWidth = DEFAULT_GUI_WIDTH / 2, initialShiftDown = 85)
			.add(text("Manage"), alignment = GuiText.TextAlignment.CENTER)
			.build(),
		GuiText("", guiWidth = DEFAULT_GUI_WIDTH / 2, initialShiftDown = 85)
			.add(text("Browse"), alignment = GuiText.TextAlignment.CENTER, horizontalShift = DEFAULT_GUI_WIDTH / 2)
			.build()
	)

	private val infoButton = makeInformationButton(text("Information")) //TODO

	private val manageButton = GuiItem.EMPTY.makeItem(text("Manage Your Orders")).makeGuiButton { _, _ -> BazaarGUIs.openBuyOrderManageMenu(viewer, this) }
	private val browseButton = GuiItem.EMPTY.makeItem(text("Browse Sell Orders")).makeGuiButton { _, _ -> OrderGlobalBrowseMenu(viewer).openGui(this) }
}
