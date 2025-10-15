package net.horizonsend.ion.server.gui.invui.bazaar

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.utils.input.InputResult
import net.horizonsend.ion.common.utils.text.BACKGROUND_EXTENDER
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.gui.CommonGuiWrapper
import net.horizonsend.ion.server.gui.invui.misc.util.input.TextInputMenu
import net.horizonsend.ion.server.gui.invui.misc.util.input.validator.RangeIntegerValidator
import net.horizonsend.ion.server.gui.invui.utils.buttons.FeedbackLike
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class PurchaseItemMenu(
	private val viewer: Player,
	private val item: BazaarItem,
	private val itemConsumer: (ItemStack, Int, Double, Int) -> (() -> InputResult),
	private val parentButton: FeedbackLike? = null,
	private val backButtonHandler: () -> Unit
) : CommonGuiWrapper {
	override fun openGui() {
		val itemStack = fromItemString(item.itemString)
		val listerName = SLPlayer.getName(item.seller)

		val extraLine = GuiText("")
			.addBackground(
				GuiText.GuiBackground(
					backgroundChar = BACKGROUND_EXTENDER,
					verticalShift = -17
				)
			)
			.add(template(message = Component.text("Buying {0}'s"), paramColor = null, useQuotesAroundObjects = false, listerName), line = -3, verticalShift = -1)
			.add(template(message = Component.text("{0}"), paramColor = null, useQuotesAroundObjects = false, getMenuTitleName(itemStack)), line = -2, verticalShift = 0)
			.add(template(Component.text("Between {0} and {1}"), paramColor = null, 1, item.stock), line = -1, verticalShift = 1)
			.build()


		TextInputMenu(
			viewer = viewer,
			title = extraLine,
			backButtonHandler = { backButtonHandler.invoke() },
			inputValidator = RangeIntegerValidator(1..item.stock),
			componentTransformer = { qty -> template(message = Component.text("Buying {0} items (unit price {1}, total {2})"), paramColor = null, useQuotesAroundObjects = false, qty, String.format("%.2f", item.price), String.format("%.2f", item.price * qty)) },
			successfulInputHandler = menu@{ _, result ->
				val remote = !Regions.get<RegionTerritory>(item.cityTerritory).contains(viewer.location)
				val futureResult = Bazaars.tryBuyFromSellOrder(viewer, item, result.result, remote, itemConsumer)

				futureResult.withResult { buyResult ->
					if (!buyResult.isSuccess()) {
						buyResult.getReason()?.let(::updateLoreOverride)
						notifyWindows()
					} else {
						buyResult.sendReason(viewer)
						backButtonHandler.invoke()
						parentButton?.updateWith(buyResult)
					}
				}
			}
		).openGui()
	}
}
