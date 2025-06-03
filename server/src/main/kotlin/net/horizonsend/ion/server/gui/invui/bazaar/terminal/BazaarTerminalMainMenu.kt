package net.horizonsend.ion.server.gui.invui.bazaar.terminal

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.database.schema.economy.BazaarOrder
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.common.utils.text.clip
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.gui.icons.GuiIcon
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars
import net.horizonsend.ion.server.features.economy.city.TradeCities
import net.horizonsend.ion.server.features.economy.city.TradeCityType
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock.Companion.getDisplayName
import net.horizonsend.ion.server.features.multiblock.type.economy.BazaarTerminalMultiblock
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.bazaar.getMenuTitleName
import net.horizonsend.ion.server.gui.invui.bazaar.terminal.browse.TerminalCitySelection
import net.horizonsend.ion.server.gui.invui.utils.buttons.FeedbackLike
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Player
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.gt
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.window.Window

class BazaarTerminalMainMenu(
	viewer: Player,
	private val terminalMultiblockEntity: BazaarTerminalMultiblock.BazaarTerminalMultiblockEntity
) : InvUIWindowWrapper(viewer, async = true) {
	private var saleCount: Int = 0
	private var orderCount: Int = 0
	private var ownerName: String = "Unknown"

	override fun firstTimeSetup() {
		saleCount = BazaarItem.count(and(BazaarItem::seller eq viewer.slPlayerId, BazaarItem::cityTerritory eq cityData?.territoryId)).toInt()
		orderCount = BazaarOrder.count(and(BazaarOrder::player eq viewer.slPlayerId, BazaarOrder::cityTerritory eq cityData?.territoryId)).toInt()

		val owner = terminalMultiblockEntity.owner
		ownerName = if (owner != null) SLPlayer.getName(owner.slPlayerId) ?: "Unknown" else "Unclaimed"
	}

	override fun buildWindow(): Window {
		val gui = Gui.normal()
			.setStructure(
				"c c c b b b d d d ",
				"o o o b b b d d d ",
				"l l l b b b d d d ",
				"S S S r r r f f f ",
				"O O O r r r f f f ",
				"s . . r r r f f f "
			)

			.addIngredient('b', buyButton)
			.addIngredient('d', depositButton)

			.addIngredient('r', recieveButton)
			.addIngredient('f', fulfillButton)

			.addIngredient('c', territoryButtons)
			.addIngredient('o', orderBackingItem)
			.addIngredient('l', saleBackingItem)
			.addIngredient('S', statusBackingItem)
			.addIngredient('O', ownerBackingItem)

			.addIngredient('s', settingsButton)

			.build()

		return normalWindow(gui)
	}

	override fun buildTitle(): Component {
		val text = GuiText("Bazaar Terminal Main Menu")
			.addBackground()
			.setGuiIconOverlay(
				". . . . . . . . . ",
				". . . . p . . d . ",
				". . . . . . . . . ",
				". . . . . . . . . ",
				". . . . r . . f . ",
				". . . . . . . . . "
			)

		if (terminalMultiblockEntity.isWithdrawAvailable())
			text.addIcon('p', GuiIcon.bazaarSellOrder(TextColor.fromHexString("#67C2D4")!!))
		else text.addIcon('p', GuiIcon.bazaarSellOrder(NamedTextColor.GRAY))

		if (terminalMultiblockEntity.isDepositAvailable())
			text.addIcon('d', GuiIcon.bazaarSellOrder(TextColor.fromHexString("#B4E28C")!!))
		else text.addIcon('d', GuiIcon.bazaarSellOrder(NamedTextColor.GRAY))

		if (terminalMultiblockEntity.isWithdrawAvailable())
			text.addIcon('r', GuiIcon.bazaarBuyOrder(TextColor.fromHexString("#EFC275")!!))
		else text.addIcon('r', GuiIcon.bazaarBuyOrder(NamedTextColor.GRAY))

		if (terminalMultiblockEntity.isDepositAvailable())
			text.addIcon('f', GuiIcon.bazaarBuyOrder(TextColor.fromHexString("#CB625F")!!))
		else text.addIcon('f', GuiIcon.bazaarBuyOrder(NamedTextColor.GRAY))

		val buttonLabels1 = GuiText("", guiWidth = 48)
			.add(getMenuTitleName(text("Purchase", WHITE)), alignment = GuiText.TextAlignment.CENTER, line = 0, horizontalShift = 48 + 12)
			.add(getMenuTitleName(text("Items", WHITE)), alignment = GuiText.TextAlignment.CENTER, line = 1, horizontalShift = 48 + 12)
			.add(getMenuTitleName(text("Receive", WHITE)), alignment = GuiText.TextAlignment.CENTER, line = 6, horizontalShift = 48 + 12)
			.add(getMenuTitleName(text("Orders", WHITE)), alignment = GuiText.TextAlignment.CENTER, line = 7, horizontalShift = 48 + 12)
			.build()

		val buttonLabels2 = GuiText("", guiWidth = 48)
			.add(getMenuTitleName(text("Restock", WHITE)), alignment = GuiText.TextAlignment.CENTER, line = 0, horizontalShift = 96 + 12 + 5)
			.add(getMenuTitleName(text("Listings", WHITE)), alignment = GuiText.TextAlignment.CENTER, line = 1, horizontalShift = 96 + 12 + 5)
			.add(getMenuTitleName(text("Fulfill", WHITE)), alignment = GuiText.TextAlignment.CENTER, line = 6, horizontalShift = 96 + 12 + 5)
			.add(getMenuTitleName(text("Orders", WHITE)), alignment = GuiText.TextAlignment.CENTER, line = 7, horizontalShift = 96 + 12 + 5)
			.build()

		val textBody = GuiText("", guiWidth = 48)
			.add(text("City:"), line = 0)
			.add(getMenuTitleName(text(territoryName, WHITE)).clip(48, true), line = 1)
			.add(text("Orders:"), line = 2)
			.add(getOrderText(), line = 3)
			.add(text("Sale Items:"), line = 4)
			.add(getSaleText(), line = 5)
			.add(text("Status:"), line = 6)
			.add(getStatusText(), line = 7)
			.add(text("Owner:"), line = 8)
			.add(getOwnerName(), line = 9)
			.build()

		return ofChildren(text.build(), buttonLabels1, buttonLabels2, textBody)
	}

	private fun getSaleText(): Component {
		return getMenuTitleName(text(saleCount, WHITE))
	}

	private val saleBackingItem get() = GuiItem.EMPTY.makeItem(text("Sold Items in This Territory"))
		.updateLore(listOf(
			template(text("You are selling {0} items in {1}.", HE_MEDIUM_GRAY), saleCount, cityData?.displayName)
		))

	private fun getOrderText(): Component {
		return getMenuTitleName(text(orderCount, WHITE))
	}

	private val orderBackingItem get() = GuiItem.EMPTY.makeItem(text("Item Orders in This Territory"))
		.updateLore(listOf(
			template(text("You have ordered {0} items at {1}.", HE_MEDIUM_GRAY), orderCount, cityData?.displayName)
		))

	private fun getStatusText(): Component {
		val string = if (terminalMultiblockEntity.mergeEnd?.get() != null) "Merged" else "Single"
		return getMenuTitleName(text(string, WHITE))
	}

	private val statusBackingItem get() = GuiItem.EMPTY.makeItem(text("Multiblock Merger Status"))
		.updateLore(listOf(
			template(text(if (terminalMultiblockEntity.mergeEnd?.get() != null) "This multiblock is merged with {0}." else "This multiblock is not merged with any other.", HE_MEDIUM_GRAY), terminalMultiblockEntity.mergeEnd?.get()?.multiblock?.getDisplayName())
		))

	private fun getOwnerName(): Component {
		return getMenuTitleName(text(ownerName, WHITE).clip(48, true))
	}

	private val ownerBackingItem get() = GuiItem.EMPTY.makeItem(text("Multiblock Ownership"))
		.updateLore(listOf(
			template(text("This multiblock is owned by {0}", HE_MEDIUM_GRAY), ownerName)
		))

	private val cityData = Regions.findFirstOf<RegionTerritory>(terminalMultiblockEntity.location)?.let(TradeCities::getIfCity)
	private val territoryName = cityData?.displayName ?: "Unknown"

	private val territoryButtons = GuiItem.EMPTY.makeItem(text("Current Trade City"))
		.updateLore(listOf(
			text(territoryName, HE_MEDIUM_GRAY),
			template(text("Status: {0}", HE_MEDIUM_GRAY), if (cityData?.type == TradeCityType.SETTLEMENT) Settlement.findOnePropById(cityData.settlementId, Settlement::cityState)?.name ?: "Unregistered" else if (cityData != null) "Active" else "Unregistered")
		))

	private val fulfillDescription = listOf(
		text("Orders people have placed in this territory may be fulfilled in this menu.", HE_MEDIUM_GRAY),
		text("You may make a profit by selling them items.", HE_MEDIUM_GRAY),
	)

	private val fulfillButton = FeedbackLike.withHandler({
		if (terminalMultiblockEntity.isDepositAvailable())
			GuiItem.EMPTY.makeItem(text("Fulfill Buy Orders"))
		else GuiItem.EMPTY.makeItem(text("Buy Order Fulfillment Not Available"))

	}, fallbackLoreProvider = ::fulfillDescription) { _, _ -> handleFulfill() }

	private fun handleFulfill() {
		val cityCheck = Bazaars.checkInValidCity(viewer)
		if (!cityCheck.isSuccess()) return fulfillButton.updateWith(cityCheck)

		println("Fulfill") //TODO
	}

	private val depositDescription = listOf(
		text("", HE_MEDIUM_GRAY) //TODO
	)

	private val depositButton = FeedbackLike.withHandler({
		if (terminalMultiblockEntity.isDepositAvailable()) GuiItem.EMPTY.makeItem(text("Deposit Items To Your Sell Orders"))
		else GuiItem.EMPTY.makeItem(text("Item deposit Not Available")).updateLore(listOf(text("Items may only be deposited in trade cities!", RED)))

	}, fallbackLoreProvider = ::depositDescription) { _, _ -> handleDeposit() }

	private fun handleDeposit() {
		val cityCheck = Bazaars.checkInValidCity(viewer)
		if (!cityCheck.isSuccess()) return depositButton.updateWith(cityCheck)
		BazaarBulkDepositMenu(viewer, terminalMultiblockEntity).openGui(this)
	}

	private val buyDescription = listOf(
		text("", HE_MEDIUM_GRAY) //TODO
	)

	private val buyButton = FeedbackLike.withHandler(
		providedItem = {
			if (terminalMultiblockEntity.isWithdrawAvailable()) GuiItem.EMPTY.makeItem(text("Purchase Items")).updateLore(buyDescription)
			else GuiItem.EMPTY.makeItem(text("Item Purchase not available")).updateLore(listOf(text("The left merge port is occupying the withdraw capabilities.", HE_MEDIUM_GRAY)))
		},
		clickHandler = { _, _ -> handlePurchase() }
	)

	private fun handlePurchase() {
		TerminalCitySelection(viewer, terminalMultiblockEntity).openGui(this)

		val listedItems = BazaarItem.find(and(BazaarItem::cityTerritory eq terminalMultiblockEntity.territory?.id, BazaarItem::stock gt 0))
		val item = listedItems.toList().random()
		BazaarGUIs.openTerminalPurchaseMenu(viewer, item, buyButton, terminalMultiblockEntity, { openGui() }) //TODO
	}

	private val recieveOrdersDescription = listOf(
		text("", HE_MEDIUM_GRAY) //TODO
	)

	private val recieveButton = ItemProvider {
		if (terminalMultiblockEntity.isWithdrawAvailable()) GuiItem.EMPTY.makeItem(text("Receive Items From Fulfilled Orders")).updateLore(recieveOrdersDescription)
		else GuiItem.EMPTY.makeItem(text("Item withdraw not available")).updateLore(listOf(text("The left merge port is occupying the withdraw capabilities.", HE_MEDIUM_GRAY)))
	}.makeGuiButton { _, _ -> handleWithdrawOrder() }

	private fun handleWithdrawOrder() {
		println("Withdraw") //TODO
	}

	private val settingsButton = GuiItem.GEAR //TODO
}
