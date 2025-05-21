package net.horizonsend.ion.server.gui.invui.bazaar.orders.window.manage

import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.window.Window

class ListManageMenu(viewer: Player) : ManageOrdersMenu(viewer) {
	override val listingsPerPage: Int = 4

	override fun buildWindow(): Window {
		val gui = PagedGui.items()
			.setStructure(
				"b . . . p . . . i",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"l . . . . . f s r",
			)
			.addIngredient('f', filterButton)
			.addIngredient('s', sortButton)
			.addIngredient('b', parentOrBackButton())
			.addIngredient('i', infoButton)

			.addIngredient('#', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
			.addIngredient('l', GuiItems.PageLeftItem())
			.addIngredient('r', GuiItems.PageRightItem())
			.addIngredient('p', createBuyOrderMenu)
			.setContent(items)
			.build()

		return normalWindow(gui)
	}

	override fun buildTitle(): Component {
		val text = GuiText("Managing Your Bazaar Orders")
			.setSlotOverlay(
				"# # # # # # # # #",
				". . . . . . . . .",
				". . . . . . . . .",
				". . . . . . . . .",
				". . . . . . . . .",
				"# # # # # # # # #",
			)

		return withPageNumber(text)
	}
}
