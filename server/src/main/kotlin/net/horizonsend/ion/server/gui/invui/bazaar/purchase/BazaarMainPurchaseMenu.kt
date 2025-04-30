package net.horizonsend.ion.server.gui.invui.bazaar.purchase

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.gui.invui.InvUIWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarSort
import net.horizonsend.ion.server.gui.invui.bazaar.getCityButtons
import net.horizonsend.ion.server.gui.invui.bazaar.getItemButtons
import net.horizonsend.ion.server.gui.invui.utils.TabButton
import net.horizonsend.ion.server.gui.invui.utils.setTitle
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.litote.kmongo.gt
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.window.Window

class BazaarMainPurchaseMenu(override val viewer: Player, val remote: Boolean) : InvUIWrapper {
	private fun getMainMenuGui(): Gui {
		return PagedGui.guis()
			.setStructure(
				"a b 1 2 . c . d i",
				"x x x x x x x x x",
				"x x x x x x x x x",
				"x x x x x x x x x",
				"x x x x x x x x x",
				"x x x x x x x x x"
			)
			.addIngredient('1', TabButton(1))
			.addIngredient('2', ChangeViewButton())
			.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
			.setContent(listOf(getCitySelectionGui(), getGlobalBrowseGui()))
			.build()
	}

	private fun getCitySelectionGui(): Gui {
		val cityButtons = getCityButtons(
			nameBuilder = { city -> Component.text(city.displayName) },
			loreBuilder = { listOf() },
			clickHandler = { city, _, player ->
				player.information(city.displayName)
			}
		)

		return PagedGui.items()
			.setStructure(
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"< . . . . . . . >"
			)
			.addIngredient('#', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
			.setContent(cityButtons)
			.build()
	}

	private fun getGlobalBrowseGui(): Gui {
		val cityButtons = getItemButtons(
			BazaarItem::stock gt 0,
			BazaarSort.PRICE,
			true,
			clickHandler = { item, _, player ->
				player.information(item.itemString)
			}
		)

		return PagedGui.items()
			.setStructure(
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"< . . . . . . . >"
			)
			.addIngredient('#', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
			.setContent(cityButtons)
			.build()
	}

	private fun getMenuTitle(): Component {
		val baseText = if (remote) "Remote Bazaar" else "Bazaar"

		GuiText(baseText)
			.setSlotOverlay(
				"# # # # # # # # #",
				". . . . . . . . .",
				". . . . . . . . .",
				". . . . . . . . .",
				". . . . . . . . .",
				"# # # # # # # # #"
			)

		return Component.text(baseText)
	}

	override fun buildWindow(): Window = Window.single()
		.setGui(getMainMenuGui())
		.setTitle(getMenuTitle())
		.setViewer(viewer)
		.build()
}
