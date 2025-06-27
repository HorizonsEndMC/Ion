package net.horizonsend.ion.server.gui.invui.bazaar

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.database.schema.economy.BazaarOrder
import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.common.utils.input.InputResult
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getSetting
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars.giveOrDropItems
import net.horizonsend.ion.server.features.economy.bazaar.PlayerFilters
import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.custom.settings.SettingsPageGui.Companion.createSettingsPage
import net.horizonsend.ion.server.features.gui.custom.settings.button.database.DBCachedBooleanToggle
import net.horizonsend.ion.server.features.gui.custom.settings.button.general.collection.CollectionModificationButton
import net.horizonsend.ion.server.features.multiblock.type.economy.BazaarTerminalMultiblock
import net.horizonsend.ion.server.features.transport.items.util.ItemReference
import net.horizonsend.ion.server.features.transport.items.util.getRemovableItems
import net.horizonsend.ion.server.gui.CommonGuiWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.orders.BuyOrderMainMenu
import net.horizonsend.ion.server.gui.invui.bazaar.orders.browse.BuyOrderFulfillmentMenu
import net.horizonsend.ion.server.gui.invui.bazaar.orders.manage.AbstractOrderManagementMenu
import net.horizonsend.ion.server.gui.invui.bazaar.orders.manage.CreateBuyOrderMenu
import net.horizonsend.ion.server.gui.invui.bazaar.orders.manage.GridOrderManagementWindow
import net.horizonsend.ion.server.gui.invui.bazaar.orders.manage.ListOrderManagementMenu
import net.horizonsend.ion.server.gui.invui.bazaar.orders.manage.OrderEditorMenu
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.BazaarCitySelectionMenu
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.browse.BazaarCityBrowseMenu
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.browse.BazaarGlobalBrowseMenu
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.listings.CityItemListingsMenu
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.listings.GlobalItemListingsMenu
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.manage.GridListingManagementMenu
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.manage.ListListingManagementMenu
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.manage.ListingEditorMenu
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.manage.SellOrderCreationMenu
import net.horizonsend.ion.server.gui.invui.misc.util.input.TextInputMenu.Companion.openSearchMenu
import net.horizonsend.ion.server.gui.invui.utils.buttons.FeedbackLike
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.displayNameComponent
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import org.bukkit.craftbukkit.inventory.CraftInventory
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object BazaarGUIs {
	fun openBazaarListingHome(player: Player, parentWindow: CommonGuiWrapper?) {

	}

	fun openCitySelection(player: Player, parentWindow: CommonGuiWrapper?) {
		val menu = BazaarCitySelectionMenu(player)
		menu.openGui(parentWindow)
	}

	fun openCityBrowse(player: Player, city: TradeCityData, parentWindow: CommonGuiWrapper?) {
		val menu = BazaarCityBrowseMenu(player, city)
		menu.openGui(parentWindow)
	}

	fun openGlobalBrowse(player: Player, parentWindow: CommonGuiWrapper?) {
		val menu = BazaarGlobalBrowseMenu(player)
		menu.openGui(parentWindow)
	}

	fun openCityItemListings(player: Player, city: TradeCityData, itemString: String, parentWindow: CommonGuiWrapper?) {
		val menu = CityItemListingsMenu(player, itemString, city)
		menu.openGui(parentWindow)
	}

	fun openGlobalItemListings(player: Player, itemString: String, parentWindow: CommonGuiWrapper?) {
		val menu = GlobalItemListingsMenu(player, itemString)
		menu.openGui(parentWindow)
	}

	fun openBrowsePurchaseMenu(player: Player, item: BazaarItem, clickedButton: FeedbackLike? = null, backButtonHandler: () -> Unit) {
		val menu = PurchaseItemMenu(
			player,
			item,
			{ itemStack, amount, cost, priceMult -> {
				val (fullStacks, remainder) = giveOrDropItems(itemStack, amount, player)

				val quantityMessage = if (itemStack.maxStackSize == 1) "{0}" else "{0} stack${if (fullStacks == 1) "" else "s"} and {1} item${if (remainder == 1) "" else "s"}"

				val fullMessage = template(
					text("Bought $quantityMessage of {2} for {3}", GREEN),
					fullStacks,
					remainder,
					itemStack.displayNameComponent,
					cost.toCreditComponent(),
				)

				val priceMultiplicationMessage = template(text("(Price multiplied by {0} due to browsing remotely)", YELLOW), priceMult)

				val lore = mutableListOf(fullMessage)
				if (priceMult > 1) lore.add(priceMultiplicationMessage)

				InputResult.SuccessReason(lore)
			} },
			clickedButton,
			backButtonHandler
		)
		menu.openGui()
	}

	fun openTerminalPurchaseMenu(player: Player, item: BazaarItem, clickedButton: FeedbackLike? = null, terminal: BazaarTerminalMultiblock.BazaarTerminalMultiblockEntity, backButtonHandler: () -> Unit) {
		val intake: (ItemStack, Int, Double, Int) -> (() -> InputResult) = { itemStack: ItemStack, amount: Int, cost: Double, priceMult: Int ->
			terminal.intakeItems(itemStack, amount) { fullStacks, remainder, droppedStacks, droppedItems ->
				val quantityMessage = if (itemStack.maxStackSize == 1) "{0}" else "{0} stack${if (fullStacks == 1) "" else "s"} and {1} item${if (remainder == 1) "" else "s"}"

				val fullMessage = template(
					text("Bought $quantityMessage of {2} for {3}.", GREEN),
					fullStacks,
					remainder,
					itemStack.displayNameComponent,
					cost.toCreditComponent()
				)

				val lore = mutableListOf(fullMessage)

				if (droppedItems > 0 || droppedStacks > 0) {
					val droppedItemsMessage = template(
						text("${if (itemStack.maxStackSize == 1) "{0}" else "{0} stack${if (fullStacks == 1) "" else "s"} and {1} item${if (remainder == 1) "" else "s"}"} {2} was dropped due to insufficent storage space.", RED),
						droppedStacks,
						droppedItems,
						itemStack.displayNameComponent
					)
					lore.add(droppedItemsMessage)
				}

				if (priceMult > 1) {
					val priceMultiplicationMessage = template(text("(Price multiplied by {0} due to browsing remotely)", YELLOW), priceMult)
					lore.add(priceMultiplicationMessage)
				}

				InputResult.SuccessReason(lore)
			}
		}

		val menu = PurchaseItemMenu(player, item, intake, clickedButton, backButtonHandler)
		menu.openGui()
	}

	fun openListingManageMenu(player: Player, previous: CommonGuiWrapper?) {
		val defaultList = player.getSetting(PlayerSettings::listingManageDefaultListView)
		if (defaultList) openListingManageListMenu(player, previous)
		else openListingManageGridMenu(player, previous)
	}

	fun openListingManageListMenu(player: Player, previous: CommonGuiWrapper?) {
		val menu = ListListingManagementMenu(player)
		previous?.let { menu.setParent(it) }
		menu.openGui()
	}

	fun openListingManageGridMenu(player: Player, previous: CommonGuiWrapper?) {
		val menu = GridListingManagementMenu(player)
		previous?.let { menu.setParent(it) }
		menu.openGui()
	}

	fun openSellOrderEditor(player: Player, listing: BazaarItem, previous: CommonGuiWrapper?) {
		ListingEditorMenu(player, listing).openGui(previous)
	}

	fun openSellOrderCreationMenu(viewer: Player, previous: CommonGuiWrapper?) {
		SellOrderCreationMenu(viewer).openGui(previous)
	}

	fun openBuyOrderMainMenu(player: Player, previous: CommonGuiWrapper?) {
		val menu = BuyOrderMainMenu(player)
		menu.openGui(previous)
	}

	fun openBuyOrderCreationMenu(player: Player, parent: CommonGuiWrapper? = null) {
		val menu = CreateBuyOrderMenu(player)
		parent?.let { menu.setParent(it) }
		menu.openGui()
	}

	fun openBuyOrderManageMenu(player: Player, previous: CommonGuiWrapper?, handleListingClick: AbstractOrderManagementMenu.(Oid<BazaarOrder>) -> Unit = { openBuyOrderEditorMenu(viewer, it, this) }) {
		val defaultList = player.getSetting(PlayerSettings::orderManageDefaultListView)

		if (defaultList) ListOrderManagementMenu(player, handleListingClick).openGui(previous)
		else GridOrderManagementWindow(player, handleListingClick).openGui(previous)
	}

	fun openBuyOrderFulfillmentMenu(player: Player, orderId: Oid<BazaarOrder>, previous: CommonGuiWrapper?) {
		BuyOrderFulfillmentMenu(
			viewer = player,
			item = orderId,
			availableItemProvider = { player.inventory.withIndex().mapNotNull { (index, item: ItemStack?) ->
				if (item == null) return@mapNotNull null
				ItemReference(player.inventory as CraftInventory, index)
			} },
			fulfillmentFunction = { fulfillmentAmount -> Bazaars.fulfillOrder(player, player.inventory, orderId, fulfillmentAmount) }
		).openGui(previous)
	}

	fun openBulkBuyOrderFulfillmentMenu(player: Player, orderId: Oid<BazaarOrder>, previous: CommonGuiWrapper?, terminal: BazaarTerminalMultiblock.BazaarTerminalMultiblockEntity) {
		val inventoryReferences = terminal.getInputInventories()
		val references = mutableListOf<ItemReference>()

		Tasks.async {
			val itemString = BazaarOrder.findOnePropById(orderId, BazaarOrder::itemString) ?: return@async
			val exampleItem = fromItemString(itemString)

			for (reference in inventoryReferences) {
				for ((index, item: ItemStack) in getRemovableItems(reference.inventory)) {
					if (!item.isSimilar(exampleItem)) continue

					references.add(ItemReference(reference.inventory, index))
				}
			}

			BuyOrderFulfillmentMenu(
				viewer = player,
				item = orderId,
				availableItemProvider = { references },
				fulfillmentFunction = { limit -> Bazaars.bulkFulfillOrder(player, orderId, references, /* limit TODO */) }
			).openGui(previous)
		}
	}

	fun openBuyOrderEditorMenu(player: Player, orderId: Oid<BazaarOrder>, previous: CommonGuiWrapper?) {
		OrderEditorMenu(player, orderId).openGui(previous)
	}

	fun openBazaarSettings(player: Player, parent: CommonGuiWrapper?) {
		val page = createSettingsPage(
			player,
			"Placement Settings",
			DBCachedBooleanToggle(
                text("Skip Single Entry Menus"),
                butonDescription = "Skip directly to purchase menu when there is only one listing of an item.",
                icon = GuiItem.LIST,
                defaultValue = false,
                db = PlayerSettings::skipBazaarSingleEntryMenus
            )
		)

		page.openGui(parent)
	}

	fun openBazaarFilterMenu(viewer: Player, data: PlayerFilters, parent: CommonGuiWrapper?) {
		CollectionModificationButton(
			viewer = viewer,
			title = text("Filters List"),
			description = "Configure Filters",
			collectionSupplier = { data.filters },
			modifiedConsumer = { data.filters = it.toList() },
			toMutableCollection = { it.toMutableList() },
			itemTransformer = { GuiItem.LIST.makeItem(text(PlayerFilters.getKey(it))) },
			getItemLines = { text(PlayerFilters.getKey(it)) to it.description },
			playerModifier = { filter, _ -> filter.getSettingsMenu(viewer, data).openGui(this) },
			entryCreator = {
				viewer.openSearchMenu(
					PlayerFilters.getAllFilters().keys,
					{ listOf(it) },
					text("Enter filter name"),
					backButtonHandler = { this.openGui() }
				) { _, result -> it.accept(PlayerFilters.getFilter(result).generator.invoke()) }
			}
		).openGui(parent)
	}
}
