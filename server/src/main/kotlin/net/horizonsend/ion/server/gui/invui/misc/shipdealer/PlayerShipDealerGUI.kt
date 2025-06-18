package net.horizonsend.ion.server.gui.invui.misc.shipdealer

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.dealers.PlayerCreatedDealerShip
import net.horizonsend.ion.server.gui.invui.ListInvUIWindow
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window

class PlayerShipDealerGUI(viewer: Player, val ships: List<PlayerCreatedDealerShip>) : ListInvUIWindow<Map.Entry<PlayerShipDealerGUI.ShipClass, List<PlayerCreatedDealerShip>>>(viewer) {
	override val listingsPerPage: Int = 18

	override fun generateEntries(): List<Map.Entry<ShipClass, List<PlayerCreatedDealerShip>>> {
		return ships.groupBy { ShipClass(it.className, it.starshipType) }.entries.toList()
	}

	override fun createItem(entry: Map.Entry<ShipClass, List<PlayerCreatedDealerShip>>): Item {
		return entry.key.type.menuItemRaw.get()
			.updateDisplayName(template(Component.text("Click to View {0} Class Vessels"), Component.text(entry.key.name)))
			.updateLore(listOf(
				template(Component.text("Type: {0}", HE_MEDIUM_GRAY), entry.key.type.displayNameComponent)
			))
			.makeGuiButton { _, _ -> ShipDealerGUI(viewer, entry.value).openGui(this) }
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

	override val pageNumberLine: Int = 6

	override fun buildTitle(): Component {
		val text = GuiText("Available Ship Classes")
			.setSlotOverlay(
				"# # # # # # # # #",
				". . . . . . . . .",
				". . . . . . . . .",
				"# # # # # # # # #"
			).build()

		if (maxPageNumber > 1) return withPageNumber(text)

		return withPageNumber(text)
	}

	data class ShipClass(val name: String, val type: StarshipType)
}
