package net.horizonsend.ion.server.gui.invui.misc

import net.horizonsend.ion.common.utils.text.gui.icons.GuiIcon
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.gui.CommonGuiWrapper
import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.window.Window

class ConfirmationMenu(parentWindow: CommonGuiWrapper?, viewer: Player, val title: GuiText, private val onConfirm: () -> Unit) : InvUIWindowWrapper(viewer, async = true) {
	override fun buildWindow(): Window {
		val gui = Gui.normal()
			.setStructure(
				". b b b . c c c .",
				". b b b . c c c .",
				". b b b . c c c ."
			)
			.addIngredient('b', backButton)
			.addIngredient('c', confirmButton)
			.build()

		return normalWindow(gui)
	}

	override fun buildTitle(): Component {
		val base =  GuiText("")
			.setGuiIconOverlay(
				". . . . . . . . .",
				". . b . . . c . ."
			)
			.setSlotOverlay(
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
			)
			.addIcon('b', GuiIcon.crossIcon(color = RED, bordered = true))
			.addIcon('c', GuiIcon.checkmarkIcon(color = GREEN, bordered = true))
			.build()

		return ofChildren(base, title.build())
	}

	private val backButton =
		if (parentWindow == null) GuiItem.EMPTY.makeItem(Component.text("Go back")).makeGuiButton { _, _ -> viewer.closeInventory() }
		else GuiItem.EMPTY.makeItem(Component.text("Go back to previous menu")).makeGuiButton { _, _ -> parentWindow.openGui() }

	private val confirmButton = GuiItem.EMPTY.makeItem(Component.text("Confirm")).makeGuiButton { _, _ -> onConfirm.invoke() }

	companion object {
		fun promptConfirmation(viewer: Player, prompt: GuiText, onConfirm: () -> Unit) {
			ConfirmationMenu(null, viewer, prompt, onConfirm).openGui()
		}

		fun promptConfirmation(viewer: Player, window: CommonGuiWrapper, prompt: GuiText, onConfirm: () -> Unit) {
			ConfirmationMenu(window, viewer, prompt, onConfirm).openGui()
		}

		fun promptConfirmation(window: InvUIWindowWrapper, prompt: GuiText, onConfirm: () -> Unit) {
			ConfirmationMenu(window, window.viewer, prompt, onConfirm).openGui()
		}
	}
}
