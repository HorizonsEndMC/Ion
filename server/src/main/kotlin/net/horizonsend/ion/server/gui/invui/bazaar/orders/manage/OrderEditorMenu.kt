package net.horizonsend.ion.server.gui.invui.bazaar.orders.manage

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.economy.BazaarOrder
import net.horizonsend.ion.common.utils.input.InputResult
import net.horizonsend.ion.common.utils.text.BAZAAR_ORDER_HEADER_ICON
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_ORANGE
import net.horizonsend.ion.common.utils.text.gui.GuiBorder
import net.horizonsend.ion.common.utils.text.gui.icons.GuiIcon
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars.cityName
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.getMenuTitleName
import net.horizonsend.ion.server.gui.invui.bazaar.stripAttributes
import net.horizonsend.ion.server.gui.invui.misc.util.input.TextInputMenu.Companion.openInputMenu
import net.horizonsend.ion.server.gui.invui.misc.util.input.validator.RangeIntegerValidator
import net.horizonsend.ion.server.gui.invui.utils.buttons.FeedbackLike
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeInformationButton
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.window.Window
import kotlin.properties.Delegates

class OrderEditorMenu(viewer: Player, private val order: Oid<BazaarOrder>) : InvUIWindowWrapper(viewer, async = true) {
	private lateinit var builtIcon: ItemStack
	private lateinit var cityTerritory: RegionTerritory
	private var fulfilledCount by Delegates.notNull<Int>()
	private var orderCount by Delegates.notNull<Int>()

	private var listingRemoved: Boolean = false

	override fun firstTimeSetup() {
		val props = BazaarOrder.findPropsById(order, BazaarOrder::itemString, BazaarOrder::cityTerritory, BazaarOrder::fulfilledQuantity, BazaarOrder::requestedQuantity)

		if (props == null) {
			listingRemoved = true
			return
		}

		builtIcon = fromItemString(props[BazaarOrder::itemString])
		cityTerritory = Regions[props[BazaarOrder::cityTerritory]]
		fulfilledCount = props[BazaarOrder::fulfilledQuantity]
		orderCount = props[BazaarOrder::requestedQuantity]
	}

	override fun buildWindow(): Window? {
		if (listingRemoved) return null

		val gui = Gui.normal()
			.setStructure(
				"b . . . . . . . i",
				"e . . . . . . . .",
				". . . . . . . . .",
				". w w w . d d d .",
				". w w w . d d d .",
				". w w w . d d d .",
			)
			.addIngredient('b', parentOrBackButton())
			.addIngredient('i', infoButton)
			.addIngredient('e', builtIcon.clone().stripAttributes())
			.addIngredient('d', deleteOrderButton)
			.addIngredient('w', withdrawStockButton)
			.build()

		return normalWindow(gui)
	}

	override fun buildTitle(): Component {
		val background = GuiText("")
			.addBackground()
			.addBorder(
				GuiBorder.regular(
				color = HE_LIGHT_ORANGE,
				headerIcon = GuiBorder.HeaderIcon(BAZAAR_ORDER_HEADER_ICON, 48, HE_LIGHT_ORANGE),
				leftText = text("Modifying"),
				rightText = text("Buy Order")
			))
			.setGuiIconOverlay(
				". . . . . . . . .",
				". . . . . . . . .",
				". . . . . . . . .",
				". . . . . . . . .",
				". . w . . . d . .",
				". . . . . . . . .",
			)
			.addIcon('w', GuiIcon.withdrawIcon(TextColor.fromHexString("#00FF7B")!!, true))
			.addIcon('d', GuiIcon.trashCanIcon(NamedTextColor.RED, true))
			.build()

		val stock = BazaarOrder.findOnePropById(order, BazaarOrder::stock) ?: 0

		val displayedText = GuiText("")
			.add(getMenuTitleName(template(text("{0} Stock Available", WHITE), stock)), alignment = GuiText.TextAlignment.CENTER, line = 0, verticalShift = 3)
			.add(getMenuTitleName(builtIcon), line = 2, horizontalShift = 18, verticalShift = -2)
			.add(template(text("@ {0}"), paramColor = null, useQuotesAroundObjects = false, cityName(cityTerritory)), line = 3, horizontalShift = 18, verticalShift = -2)
			.add(template(text("{0} have been fulfilled."), paramColor = null, fulfilledCount), line = 4)
			.add(template(text("{0} have not been fulfilled."), paramColor = null, orderCount - fulfilledCount), line = 5)

			.build()

		return ofChildren(background, displayedText)
	}

	private val withdrawStockButton = FeedbackLike.withHandler(
		GuiItem.EMPTY.makeItem(text("Withdraw Stock")),
		fallbackLoreProvider = { listOf(
			text("Click normally to select amount to withdraw."),
			text("Shift click to withdraw all."),
		) }
	) { type, _ -> withdrawStock(type) }

	private fun withdrawStock(clickType: ClickType) {
		if (clickType == ClickType.SHIFT_LEFT || clickType == ClickType.SHIFT_RIGHT) {
			val result = Bazaars.withdrawOrderStock(viewer, order, Integer.MAX_VALUE)
			withdrawStockButton.updateWith(result)

			return
		}

		Tasks.async {
			val stock = BazaarOrder.findOnePropById(order, BazaarOrder::stock) ?: 0

			if (stock <= 0) {
				withdrawStockButton.updateWith(InputResult.FailureReason(listOf(text("There is no stock to withdraw!", RED))))
				return@async
			}

			Tasks.sync {
				viewer.openInputMenu(
					prompt = text("Select amount to withdraw."),
					description = text("$stock available."),
					inputValidator = RangeIntegerValidator(0..stock),
					backButtonHandler = { this@OrderEditorMenu.openGui() },
					handler = { _, validatorResult ->
						val amount = validatorResult.result

						val result = Bazaars.withdrawOrderStock(viewer, order, amount)
						withdrawStockButton.updateWith(result)
					}
				)
			}
		}
	}

	private val deleteOrderButton  = FeedbackLike.withHandler(GuiItem.EMPTY.makeItem(text("Remove Order"))) { _, _ -> deleteOrder() }

	private fun deleteOrder() {
		val result = Bazaars.deleteOrder(viewer, order)

		if (!result.isSuccess()) {
			deleteOrderButton.updateWith(result)
		} else {
			if (parentWindow != null) parentWindow?.openGui() ?: viewer.closeInventory()
			result.sendReason(viewer)
		}
	}

	private val infoButton = makeInformationButton(text("Information")) //TODO
}
