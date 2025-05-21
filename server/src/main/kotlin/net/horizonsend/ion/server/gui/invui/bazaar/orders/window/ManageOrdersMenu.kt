package net.horizonsend.ion.server.gui.invui.bazaar.orders.window

import com.mongodb.client.FindIterable
import net.horizonsend.ion.common.database.schema.economy.BazaarOrder
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.features.economy.city.TradeCities
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.stripAttributes
import net.horizonsend.ion.server.gui.invui.utils.asItemProvider
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.litote.kmongo.eq
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.window.Window

class ManageOrdersMenu(viewer: Player) : InvUIWindowWrapper(viewer, true) {
	private lateinit var orders: FindIterable<BazaarOrder>

	override fun buildWindow(): Window {
		orders = BazaarOrder.find(BazaarOrder::player eq viewer.slPlayerId)

		val items = orders.toList().mapNotNull {
			val cityName = TradeCities.getIfCity(Regions[it.cityTerritory])?.displayName ?: "Unknown"

			fromItemString(it.itemString)
				.stripAttributes()
				.updateLore(listOf(
					template(Component.text("City: {0}", HE_MEDIUM_GRAY), cityName),
					template(Component.text("Order Quantity: {0}", HE_MEDIUM_GRAY), it.requestedQuantity),
					template(Component.text("Fulfilled Quantity: {0}", HE_MEDIUM_GRAY), it.fulfilledQuantity)
				))
				.asItemProvider()
				.makeGuiButton { _, _ ->  }
		}

		val gui = PagedGui.items()
			.setStructure(
				"b . . . . . . . .",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"l . . . . . . . r",
			)
			.addIngredient('#', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
			.addIngredient('l', GuiItems.PageLeftItem())
			.addIngredient('r', GuiItems.PageRightItem())
			.addIngredient('b', parentOrBackButton())
			.setContent(items)
			.build()

		return normalWindow(gui)
	}

	override fun buildTitle(): Component {
		return GuiText("")
			.build()
	}
}
