package net.horizonsend.ion.server.gui.invui.bazaar.orders.browse

import net.horizonsend.ion.common.database.schema.economy.BazaarOrder
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.utils.text.BAZAAR_ORDER_HEADER_ICON
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_ORANGE
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.gui.GuiBorder
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars.cityName
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.item.AsyncItem
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.gui.invui.ListInvUIWindow
import net.horizonsend.ion.server.gui.invui.bazaar.stripAttributes
import net.horizonsend.ion.server.gui.invui.input.TextInputMenu.Companion.searchEntires
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.displayNameComponent
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bson.conversions.Bson
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window

abstract class AbstractBrowseMenu(viewer: Player) : ListInvUIWindow<BazaarOrder>(viewer, async = true), OrderWindow {
	override val listingsPerPage: Int = 36

	abstract val findBson: Bson

	override fun generateEntries(): List<BazaarOrder> {
		return BazaarOrder.find(findBson).filterNot { BazaarOrder.isFulfilled(it._id) }
	}

	override fun createItem(entry: BazaarOrder): Item = AsyncItem(
		resultProvider = { formatItem((entry)) },
		handleClick = { _ -> BuyOrderFulfillmentMenu(viewer, entry._id).openGui(this) /* TODO move to bazaarGUis */}
	)

	override fun buildWindow(): Window {
		val gui = PagedGui.items()
			.setStructure(
				"x . . c g b . o i",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"< . s . . . f S >",
			)
			.addIngredient('x', parentOrBackButton())

			.addIngredient('c', citySelectionButton)
			.addIngredient('g', globalBrowseButton)
			.addIngredient('b', listingBrowseButton)

			.addIngredient('o', settingsButton)
			.addIngredient('i', infoButton)

			.addIngredient('<', GuiItems.PageLeftItem())
			.addIngredient('>', GuiItems.PageRightItem())

			.addIngredient('s', searchButton)
			.addIngredient('S', GuiItem.SORT) //TODO
			.addIngredient('f', GuiItem.FILTER) //TODO

			.addIngredient('#', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
			.setContent(items)

		return normalWindow(gui.build())
	}

	abstract val browseName: Component

	override fun buildTitle(): Component {
		val text = GuiText("")
			.addBorder(GuiBorder.regular(
				color = HE_LIGHT_ORANGE,
				headerIcon = GuiBorder.HeaderIcon(BAZAAR_ORDER_HEADER_ICON, 48, HE_LIGHT_ORANGE),
				leftText = text("Browising"),
				rightText = browseName
			))
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

	private fun formatItem(document: BazaarOrder): ItemStack {
		return fromItemString(document.itemString)
			.stripAttributes()
			.updateLore(listOf(
				template(text("Order from {0} at {1}", HE_MEDIUM_GRAY), SLPlayer.getName(document.player), cityName(Regions[document.cityTerritory])),
				template(text("Requested {0}, {1} have been fulfilled.", HE_MEDIUM_GRAY), document.requestedQuantity, document.fulfilledQuantity),
				template(text("Priced at {0} Per item.", HE_MEDIUM_GRAY), document.pricePerItem.toCreditComponent()),
				template(text("Possible profit of {0} for fulfilling the rest of the order.", HE_MEDIUM_GRAY), document.balance.toCreditComponent()),
			))
	}

	private val searchButton = GuiItem.MAGNIFYING_GLASS.makeItem(text("Search entries")).makeGuiButton { _, _ -> Tasks.async {
		val searchItems = BazaarOrder.find(findBson).toList()

		Tasks.sync {
			viewer.searchEntires<BazaarOrder>(
				entries = searchItems,
				searchTermProvider = { order ->
					val terms = mutableListOf(cityName(Regions[order.cityTerritory]), order.itemString)
					SLPlayer.getName(order.player)?.let(terms::add)

					return@searchEntires terms
				},
				prompt = text("Search for ordersf"),
				description = Component.empty(),
				backButtonHandler = { openGui() },
				componentTransformer = { fromItemString(it.itemString).displayNameComponent },
				itemTransformer = { formatItem(it) },
			) { _, result: BazaarOrder ->
				BuyOrderFulfillmentMenu(viewer, result._id).openGui(this)
			}
		}
	} }
}
