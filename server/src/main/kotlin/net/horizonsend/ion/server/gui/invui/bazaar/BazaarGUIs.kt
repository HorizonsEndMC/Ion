package net.horizonsend.ion.server.gui.invui.bazaar

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.database.schema.economy.BazaarOrder
import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.common.utils.input.InputResult
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getSetting
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars.giveOrDropItems
import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.custom.settings.SettingsPageGui.Companion.createSettingsPage
import net.horizonsend.ion.server.features.gui.custom.settings.button.DBCachedBooleanToggle
import net.horizonsend.ion.server.features.multiblock.type.economy.BazaarTerminalMultiblock
import net.horizonsend.ion.server.gui.CommonGuiWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.orders.BuyOrderMainMenu
import net.horizonsend.ion.server.gui.invui.bazaar.orders.CreateBuyOrderMenu
import net.horizonsend.ion.server.gui.invui.bazaar.orders.browse.BuyOrderFulfillmentMenu
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
import net.horizonsend.ion.server.gui.invui.utils.buttons.FeedbackLike
import net.horizonsend.ion.server.miscellaneous.utils.displayNameComponent
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import org.bukkit.entity.Player

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
		val menu = PurchaseItemMenu(player, item, terminal::intakeItems, clickedButton, backButtonHandler)
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

	fun openBuyOrderManageMenu(player: Player, previous: CommonGuiWrapper?) {
		val defaultList = player.getSetting(PlayerSettings::orderManageDefaultListView)
		if (defaultList) openBuyOrderManageListMenu(player, previous)
		else openBuyOrderManageGridMenu(player, previous)
	}

	fun openBuyOrderManageListMenu(player: Player, previous: CommonGuiWrapper?) {
		val menu = ListOrderManagementMenu(player)
		menu.openGui(previous)
	}

	fun openBuyOrderManageGridMenu(player: Player, previous: CommonGuiWrapper?) {
		val menu = GridOrderManagementWindow(player)
		menu.openGui(previous)
	}

	fun openBuyOrderFulfillmentMenu(player: Player, orderId: Oid<BazaarOrder>, previous: CommonGuiWrapper?) {
		BuyOrderFulfillmentMenu(player, orderId).openGui(previous)
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
}
