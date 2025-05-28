package net.horizonsend.ion.server.gui.invui.bazaar.orders.browse

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.economy.BazaarOrder
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.common.utils.InputResult
import net.horizonsend.ion.common.utils.text.BAZAAR_ORDER_HEADER_ICON
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_ORANGE
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.gui.GuiBorder
import net.horizonsend.ion.common.utils.text.gui.icons.GuiIcon
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.item.AsyncItem
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.getMenuTitleName
import net.horizonsend.ion.server.gui.invui.input.TextInputMenu.Companion.anvilInputText
import net.horizonsend.ion.server.gui.invui.input.validator.RangeIntegerValidator
import net.horizonsend.ion.server.gui.invui.utils.asItemProvider
import net.horizonsend.ion.server.gui.invui.utils.buttons.FeedbackLike
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.displayNameComponent
import net.horizonsend.ion.server.miscellaneous.utils.text.itemName
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.window.Window
import java.text.SimpleDateFormat
import kotlin.properties.Delegates

class BuyOrderFulfillmentMenu(viewer: Player, val item: Oid<BazaarOrder>) : InvUIWindowWrapper(viewer, async = true) {
	private var fulfillmentAmount = 0
	private var remainingAmount = 0
	private var playerAmount = 0

	private lateinit var itemStack: ItemStack
	private var requestedQuantity by Delegates.notNull<Int>()

	private var itemMissing: Boolean = false

	override fun firstTimeSetup() {
		val props = BazaarOrder.findPropsById(item, BazaarOrder::itemString, BazaarOrder::requestedQuantity, BazaarOrder::fulfilledQuantity)

		if (props == null) {
			itemMissing = true
			return
		}

		requestedQuantity = props[BazaarOrder::requestedQuantity]

		remainingAmount = requestedQuantity - props[BazaarOrder::fulfilledQuantity]

		val itemString = props[BazaarOrder::itemString]
		itemStack = fromItemString(itemString)

		calculateMatchingPlayerMaterials(itemStack, remainingAmount)
	}

	override fun buildWindow(): Window? {
		if (itemMissing) return null

		val props = BazaarOrder.findPropsById(item, BazaarOrder::player, BazaarOrder::cityTerritory) ?: return null

		val owner = props[BazaarOrder::player]
		val name = SLPlayer.getName(owner) ?: return null

		val playerHeadItem = AsyncItem.getHeadItem(owner.uuid, name, { it.updateDisplayName(text(name)).updateLore(listOf(text("$name issued this order"))) }) {}

		val territory = props[BazaarOrder::cityTerritory]
		val creationDate = item.id.date
		val dateFormat = SimpleDateFormat("d MMMM, yyyy")

		val orderItem = itemStack.clone()
			.updateDisplayName(template(text("Order for {0} {1}"), requestedQuantity, itemStack.displayNameComponent))
			.updateLore(listOf(
				template(text("{0} has been fulfilled. {1} remain unfulfilled.", HE_MEDIUM_GRAY), requestedQuantity, remainingAmount),
				template(text("At {0} on {1}", HE_MEDIUM_GRAY), Bazaars.cityName(Regions[territory]), dateFormat.format(creationDate))
			))

		val gui = Gui.normal()
			.setStructure(
				"p . . . . . . . .",
				"o . . . . . . . .",
				". . . . q q q q q",
				". b b b . c c c .",
				". b b b . c c c .",
				". b b b . c c c ."
			)
			.addIngredient('p', playerHeadItem)
			.addIngredient('b', parentOrBackButton(GuiItem.EMPTY))
			.addIngredient('o', orderItem)
			.addIngredient('c', confirmButton)
			.addIngredient('q', setQuantityButton)

		return normalWindow(gui.build())
	}

	override fun buildTitle(): Component {
		val background = GuiText("")
			.addBorder(GuiBorder.regular(
				color = HE_LIGHT_ORANGE,
				headerIcon = GuiBorder.HeaderIcon(BAZAAR_ORDER_HEADER_ICON, 48, HE_LIGHT_ORANGE),
				leftText = text("Fulfilling"),
				rightText = text("Buy Order")
			))
			.setGuiIconOverlay(
				". . . . . . . . .",
				". . . . . . . . .",
				". . . . l c c c r",
				". . . . . . . . .",
				". . x . . . s . .",
			)
			.addIcon('x', GuiIcon.crossIcon(RED, true))
			.addIcon('s', GuiIcon.checkmarkIcon(GREEN, true))
			.addIcon('l', GuiIcon.textInputBoxLeft())
			.addIcon('c', GuiIcon.textInputBoxCenter())
			.addIcon('r', GuiIcon.textInputBoxRight())
			.addBackground()
			.add(text("Fulfill Amount:"), line = 4, verticalShift = 4)
			.build()

		val props = BazaarOrder.findPropsById(item, BazaarOrder::player, BazaarOrder::itemString, BazaarOrder::pricePerItem)!!

		val owner = props[BazaarOrder::player]
		val orderProfit = props[BazaarOrder::pricePerItem]
		val orderItemString = props[BazaarOrder::itemString]
		val item = fromItemString(orderItemString)

		val potentialProfit = getMenuTitleName((orderProfit * fulfillmentAmount).toCreditComponent())

		val information = GuiText("")
			.add(template(text("Order from {0}"), paramColor = null, SLPlayer.getName(owner)), line = 0, horizontalShift = 18)
			.add(template(text("For {0} {1}"), paramColor = null, remainingAmount, getMenuTitleName(item.displayNameComponent)), line = 1, horizontalShift = 18)
			.add(template(text("Potential Profit: {0}"), potentialProfit), line = 2, verticalShift = 5, horizontalShift = 18)
			.add(getMenuTitleName(text(fulfillmentAmount).itemName), line = 4, verticalShift = 4, horizontalShift = 77)
			.build()

		return ofChildren(background, information)
	}

	private val confirmButton = FeedbackLike.withHandler(GuiItem.EMPTY.makeItem(text("Confirm"))) { _, _ -> confirmFulfillment() }

	private fun confirmFulfillment() {
		val result = Bazaars.fulfillOrder(viewer, item, fulfillmentAmount)
		confirmButton.updateWith(result)
		result.sendReason(viewer)

		if (result.isSuccess()) {
			remainingAmount -= fulfillmentAmount
			refreshTitle()
		}
	}

	private fun calculateMatchingPlayerMaterials(itemStack: ItemStack, orderQuantity: Int) = Tasks.sync {
		val playerAmount = viewer.inventory
			.filterNotNull()
			.filter { it.isSimilar(itemStack) }
			.sumOf { it.amount }

		this.playerAmount = playerAmount
		fulfillmentAmount = minOf(playerAmount, orderQuantity)

		refreshTitle()
	}

	private val setQuantityButton = FeedbackLike.withHandler(
		GuiItem.EMPTY.makeItem(text("Set Fulfillment Limit")).asItemProvider(),
		fallbackLoreProvider = { listOf(text("You may only fulfill up to the amount you hold in your inventory.")) },
		clickHandler = { _, _ -> setQuantity() }
	)

	private fun setQuantity(): Unit = viewer.anvilInputText(
		prompt = text("Select Fulfillment Limit"),
		description = text("1 through $remainingAmount"),
		backButtonHandler = { openGui() },
		inputValidator = RangeIntegerValidator(1..remainingAmount),
		handler = { _, result ->
			fulfillmentAmount = result.second.result
			setQuantityButton.updateWith(InputResult.SuccessReason(
				listOf(template(text("Set fulfillment limit to {0}", GREEN), fulfillmentAmount))
			))

			refreshAll()

			this@BuyOrderFulfillmentMenu.openGui()
		}
	)
}
