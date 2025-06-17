package net.horizonsend.ion.server.gui.invui.misc

import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.starship.dealers.DealerShip
import net.horizonsend.ion.server.features.starship.dealers.StarshipDealers
import net.horizonsend.ion.server.gui.invui.ListInvUIWindow
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window

class ShipDealerGUI(viewer: Player, private val ships: List<DealerShip>) : ListInvUIWindow<DealerShip>(viewer, async = true) {
	override val listingsPerPage: Int = 18

	override fun generateEntries(): List<DealerShip> {
		return ships
	}

	override fun createItem(entry: DealerShip): Item {
		return entry.getIcon().makeGuiButton { _, _ -> StarshipDealers.loadShip(viewer, entry) }
	}

	override fun buildWindow(): Window {
		val gui = PagedGui.items()
			.setStructure(
				"x . . . . . . . .",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"< . . . . . . . >",
			)
			.addIngredient('x', parentOrBackButton())
			.addIngredient('<', GuiItems.PageLeftItem())
			.addIngredient('>', GuiItems.PageRightItem())
			.handlePaginatedMenu('#')
			.build()

		return normalWindow(gui)
	}

	override fun buildTitle(): Component {
		val text = GuiText("Available Ships")
			.setSlotOverlay(
				"# # # # # # # # #",
				". . . . . . . . .",
				". . . . . . . . .",
				"# # # # # # # # #"
			).build()

		if (maxPageNumber > 1) return withPageNumber(text)

		return text
	}
}
