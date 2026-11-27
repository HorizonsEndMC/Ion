package net.horizonsend.ion.server.gui.invui.bazaar.orders.browse

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.economy.BazaarOrder
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.common.utils.input.InputResult
import net.horizonsend.ion.common.utils.input.PotentiallyFutureResult
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
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.transport.items.util.ItemReference
import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.getMenuTitleName
import net.horizonsend.ion.server.gui.invui.misc.util.input.TextInputMenu.Companion.openInputMenu
import net.horizonsend.ion.server.gui.invui.misc.util.input.validator.RangeIntegerValidator
import net.horizonsend.ion.server.gui.invui.utils.asItemProvider
import net.horizonsend.ion.server.gui.invui.utils.buttons.FeedbackLike
import net.horizonsend.ion.server.gui.invui.utils.buttons.getHeadItem
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
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.window.Window
import java.text.SimpleDateFormat
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer
import kotlin.properties.Delegates

class BuyOrderFulfillmentMenu(
	viewer: Player,
	val item: Oid<BazaarOrder>,
	private val availableItemProvider: () -> Collection<ItemReference>,
	val fulfillmentFunction: (Int, Consumer<Int>) -> PotentiallyFutureResult,
) : InvUIWindowWrapper(viewer, async = true) {
	private var fulfillmentAmount = 0
	private var remainingAmount = 0
	private var availableAmount = 0
	private var orderGone = false
	private var orderGoneMessageSent = false
	private val lastConsumed = AtomicInteger()

	private lateinit var itemStack: ItemStack
	private lateinit var owner: SLPlayerId
	private lateinit var ownerName: String
	private lateinit var orderCityName: String
	private var pricePerItem by Delegates.notNull<Double>()
	private var requestedQuantity by Delegates.notNull<Int>()

	private var itemMissing: Boolean = false

	override fun firstTimeSetup() {
		val props = BazaarOrder.findPropsById(
			item,
			BazaarOrder::itemString,
			BazaarOrder::requestedQuantity,
			BazaarOrder::fulfilledQuantity,
			BazaarOrder::player,
			BazaarOrder::cityTerritory,
			BazaarOrder::pricePerItem,
		)

		if (props == null) {
			itemMissing = true
			return
		}

		requestedQuantity = props[BazaarOrder::requestedQuantity]
		owner = props[BazaarOrder::player]
		ownerName = SLPlayer.getName(owner) ?: run {
			itemMissing = true
			return
		}
		orderCityName = Bazaars.cityName(Regions[props[BazaarOrder::cityTerritory]])
		pricePerItem = props[BazaarOrder::pricePerItem]

		remainingAmount = requestedQuantity - props[BazaarOrder::fulfilledQuantity]

		val itemString = props[BazaarOrder::itemString]
		itemStack = fromItemString(itemString)

		Tasks.getSyncBlocking { calculateAvailableMaterials(itemStack) }
		fulfillmentAmount = minOf(availableAmount, remainingAmount).coerceAtLeast(0)
		if (remainingAmount <= 0) updateCompletedButtonFeedback()
	}

	override fun buildWindow(): Window? {
		if (itemMissing) return null

		val playerHeadItem = getHeadItem(owner.uuid, ownerName, { it.updateDisplayName(text(ownerName)).updateLore(listOf(text("$ownerName issued this order"))) }) {}
		val creationDate = item.id.date
		val dateFormat = SimpleDateFormat("d MMMM, yyyy")

		val orderItem = object : AbstractItem() {
			val provider = ItemProvider {
				itemStack.clone()
					.updateDisplayName(template(text("Order for {0} {1}"), requestedQuantity, itemStack.displayNameComponent))
					.updateLore(listOf(
						template(text("{0} has been fulfilled. {1} remain unfulfilled.", HE_MEDIUM_GRAY), requestedQuantity - remainingAmount, remainingAmount),
						template(text("At {0} on {1}", HE_MEDIUM_GRAY), orderCityName, dateFormat.format(creationDate))
					))
			}

			override fun getItemProvider(): ItemProvider = provider

			override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {}
		}.tracked()

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
		val completed = orderGone || remainingAmount <= 0
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

		val potentialProfit = getMenuTitleName((if (completed) 0.0 else pricePerItem * fulfillmentAmount).toCreditComponent())

		val information = GuiText("")
			.add(template(text("Order from {0}"), paramColor = null, ownerName), line = 0, horizontalShift = 18)
			.add(template(text("For {0} {1}"), paramColor = null, remainingAmount, getMenuTitleName(itemStack.displayNameComponent)), line = 1, horizontalShift = 18)
			.add(template(text("Potential Profit: {0}"), potentialProfit), line = 2, verticalShift = 5, horizontalShift = 18)

		if (completed) information.add(text("Order completed", RED), line = 3, horizontalShift = 18)

		information.add(getMenuTitleName(text(fulfillmentAmount).itemName), line = 4, verticalShift = 4, horizontalShift = 77)

		return ofChildren(background, information.build())
	}

	private val confirmButton = FeedbackLike.withHandler(GuiItem.EMPTY.makeItem(text("Confirm"))) { _, _ -> confirmFulfillment() }

	private fun confirmFulfillment() {
		if (orderGone || remainingAmount <= 0) {
			confirmButton.updateWith(completedFailure())
			return
		}

		lastConsumed.set(0)
		val asyncResult = fulfillmentFunction.invoke(fulfillmentAmount, Consumer { lastConsumed.set(it) })

		asyncResult.withResult { result ->
			confirmButton.updateWith(result)
			result.sendReason(viewer)

			val consumed = lastConsumed.getAndSet(0)
			if (consumed > 0) {
				Tasks.sync {
					remainingAmount = (remainingAmount - consumed).coerceAtLeast(0)
					refreshAll()
				}
			}

			reconcile()
		}
	}

	private fun calculateAvailableMaterials(itemStack: ItemStack) {
		val availableAmolunt = availableItemProvider.invoke()
			.mapNotNull { it.get() }
			.filter { it.isSimilar(itemStack) }
			.sumOf { it.amount }

		this.availableAmount = availableAmolunt
	}

	private fun reconcile() {
		Tasks.async {
			val props = BazaarOrder.findPropsById(item, BazaarOrder::requestedQuantity, BazaarOrder::fulfilledQuantity)
			val newRequestedQuantity = props?.get(BazaarOrder::requestedQuantity)
			val newRemainingAmount = props?.let {
				it[BazaarOrder::requestedQuantity] - it[BazaarOrder::fulfilledQuantity]
			} ?: 0

			Tasks.sync {
				if (newRequestedQuantity != null) requestedQuantity = newRequestedQuantity
				remainingAmount = newRemainingAmount
				orderGone = props == null
				calculateAvailableMaterials(itemStack)
				fulfillmentAmount = minOf(fulfillmentAmount, availableAmount, remainingAmount).coerceAtLeast(0)
				if (orderGone && !orderGoneMessageSent) {
					viewer.sendMessage(text("This buy order no longer exists; it is marked completed.", RED))
					orderGoneMessageSent = true
				}
				if (orderGone || remainingAmount <= 0) updateCompletedButtonFeedback()
				refreshAll()
			}
		}
	}

	private fun completedFailure() = InputResult.FailureReason(listOf(text("This order is completed and cannot be fulfilled.", RED)))

	private fun updateCompletedButtonFeedback() {
		val result = completedFailure()
		confirmButton.updateWith(result)
		setQuantityButton.updateWith(result)
	}

	private val setQuantityButton = FeedbackLike.withHandler(
		GuiItem.EMPTY.makeItem(text("Set Fulfillment Limit")).asItemProvider(),
		fallbackLoreProvider = { listOf(text("You may only fulfill up to the amount you hold in your inventory.")) },
		clickHandler = { _, _ -> setQuantity() }
	)

	private fun setQuantity() {
		if (orderGone || remainingAmount <= 0) {
			setQuantityButton.updateWith(completedFailure())
			return
		}

		viewer.openInputMenu(
			prompt = text("Select Fulfillment Limit"),
			description = text("1 through $remainingAmount"),
			backButtonHandler = { openGui() },
			inputValidator = RangeIntegerValidator(1..remainingAmount),
			handler = { _, result ->
				fulfillmentAmount = minOf(result.result, availableAmount, remainingAmount).coerceAtLeast(0)
				setQuantityButton.updateWith(
					InputResult.SuccessReason(
						listOf(template(text("Set fulfillment limit to {0}", GREEN), fulfillmentAmount))
					)
				)

				refreshAll()

				this@BuyOrderFulfillmentMenu.openGui()
			}
		)
	}
}
