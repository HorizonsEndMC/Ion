package net.horizonsend.ion.server.gui.invui.bazaar.orders.window.manage

import net.horizonsend.ion.common.database.schema.economy.BazaarOrder
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars.cityName
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.gui.invui.ListInvUIWindow
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.bazaar.stripAttributes
import net.horizonsend.ion.server.gui.invui.utils.asItemProvider
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.litote.kmongo.eq
import xyz.xenondevs.invui.item.Item

abstract class ManageOrdersMenu(viewer: Player) : ListInvUIWindow<BazaarOrder>(viewer, async = true) {
	override fun generateEntries(): List<BazaarOrder> {
		// TODO filtering
		return BazaarOrder.find(BazaarOrder::player eq viewer.slPlayerId).toList()
	}

	override fun createItem(entry: BazaarOrder): Item {
		return fromItemString(entry.itemString)
			.stripAttributes()
			.applyItemFormatting(entry)
			.asItemProvider()
			.makeGuiButton { _, _ -> openManageOrderMenu(entry) }
	}

	protected fun ItemStack.applyItemFormatting(orderItem: BazaarOrder): ItemStack {
		return updateLore(listOf(
			template(text("City: {0}", HE_MEDIUM_GRAY), cityName(Regions[orderItem.cityTerritory])),
			template(text("Order Quantity: {0}", HE_MEDIUM_GRAY), orderItem.requestedQuantity),
			template(text("Price Per Item: {0}", HE_MEDIUM_GRAY), (orderItem.requestedQuantity * orderItem.pricePerItem).toCreditComponent()),
			template(text("Order Price: {0}", HE_MEDIUM_GRAY), orderItem.pricePerItem.toCreditComponent()),
			template(text("Fulfilled Quantity: {0}", HE_MEDIUM_GRAY), orderItem.fulfilledQuantity),
			template(text("Unfulfilled Quantity: {0}", HE_MEDIUM_GRAY), orderItem.stock),
		))
	}

	protected fun openManageOrderMenu(order: BazaarOrder) {
		println("Managing order ${order.itemString}!")
	}

	protected val infoButton = GuiItem.INFO.makeItem(text("Information")).makeGuiButton { _, _ -> println("INFO") /*TODO*/ }
	protected val searchButton = GuiItem.MAGNIFYING_GLASS.makeItem(text("Search Listings")).makeGuiButton { _, _ -> println("search") }
	protected val filterButton = GuiItem.FILTER.makeItem(text("Filter Listings")).makeGuiButton { _, _ -> println("filter") }
//	protected val sortButton = EnumScrollButton(
//		providedItem = GuiItem.SORT.makeItem(text("Change Sorting Method")).asItemProvider(),
//		value = { sortingMethod },
//		enum = BazaarSort::class.java,
//		nameFormatter = { it.displayName },
//		subEntry = arrayOf(BazaarSort.MIN_PRICE, BazaarSort.MAX_PRICE, BazaarSort.HIGHEST_STOCK, BazaarSort.LOWEST_STOCK, BazaarSort.HIGHEST_BALANCE, BazaarSort.LOWEST_BALANCE),
//		valueConsumer = {
//			sortingMethod = it
//			openGui()
//		}
//	)
	protected val createBuyOrderMenu = GuiItem.PLUS.makeItem(text("Create Bazaar Order")).makeGuiButton { _, _ ->
		BazaarGUIs.openBuyOrderCreationMenu(viewer, this)
	}
}
