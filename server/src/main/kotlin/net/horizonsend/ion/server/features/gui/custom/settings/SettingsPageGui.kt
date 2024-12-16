package net.horizonsend.ion.server.features.gui.custom.settings

import net.horizonsend.ion.server.features.gui.AbstractBackgroundPagedGui
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window
import kotlin.math.ceil
import kotlin.math.min

abstract class SettingsPageGui(
	val player: Player,
	val title: String,
) : AbstractBackgroundPagedGui, SettingsGuiItem {
	var parent: SettingsPageGui? = null
	override var currentWindow: Window? = null
	protected abstract val buttonsList: List<SettingsGuiItem>

	private val backButton = GuiItem.DOWN.makeButton(this, "Return to Previous Menu", "") { _, _, _ -> parent?.open() }

	override fun createGui(): PagedGui<Item> {
		val gui = PagedGui.items()

		gui.setStructure(
			"x x x x x x x x x",
			"x x x x x x x x x",
			"x x x x x x x x x",
			"x x x x x x x x x",
			"x x x x x x x x x",
			"< v . . . . . . >"
		)

		gui.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
			.addIngredient('<', GuiItems.PageLeftItem())
			.addIngredient('>', GuiItems.PageRightItem())
			.addIngredient('v', backButton)

		for (button in buttonsList) {
			val button = button.makeButton(this)
			gui.addContent(button)

			// Add items to the right
			repeat(8) { gui.addContent(GuiItems.BlankButton(button)) }
		}

		return gui.build()
	}

	fun open() {
		currentWindow = buildWindow(player)
		currentWindow?.open()
	}

	override fun createText(player: Player, currentPage: Int): Component {
		// create a new GuiText builder
		val guiText = GuiText(title)
		guiText.addBackground()

		// get the index of the first setting to display for this page
		val startIndex = currentPage * SETTINGS_PER_PAGE

		for (buttonIndex in startIndex until min(startIndex + SETTINGS_PER_PAGE, buttonsList.size)) {
			val button = buttonsList[buttonIndex]
			val title = button.getFirstLine(player)
			val line = (buttonIndex - startIndex) * 2

			// setting title
			guiText.add(
				component = title,
				line = line,
				horizontalShift = 21
			)

			// setting description
			guiText.add(
				component = button.getSecondLine(player),
				line = line + 1,
				horizontalShift = 21
			)
		}

		// page number
		val pageNumberString = "${currentPage + 1} / ${ceil((buttonsList.size.toDouble() / SETTINGS_PER_PAGE)).toInt()}"
		guiText.add(
			text(pageNumberString),
			line = 10,
			GuiText.TextAlignment.CENTER,
			verticalShift = PAGE_NUMBER_VERTICAL_SHIFT
		)

		return guiText.build()
	}

	companion object {
		protected const val SETTINGS_PER_PAGE = 5
		protected const val PAGE_NUMBER_VERTICAL_SHIFT = 4

		fun createSettingsPage(player: Player, title: String, vararg buttons: SettingsGuiItem): SettingsPageGui {
			return object : SettingsPageGui(player, title) {
				// Cannot find a labeled `this` that works, oh well
				private val thisReference = this

				override val buttonsList: List<SettingsGuiItem> = buttons.toList().apply {
					filterIsInstance<SettingsPageGui>().forEach { subMenu -> subMenu.parent = thisReference }
				}
				override fun getFirstLine(player: Player): Component = text(title)
				override fun getSecondLine(player: Player): Component = Component.empty()

				override fun makeButton(pageGui: SettingsPageGui): GuiItems.AbstractButtonItem {
					return GuiItem.LIST.makeButton(this, title, "") { _, _, _ ->
						open()
					}
				}
			}
		}
	}
}
