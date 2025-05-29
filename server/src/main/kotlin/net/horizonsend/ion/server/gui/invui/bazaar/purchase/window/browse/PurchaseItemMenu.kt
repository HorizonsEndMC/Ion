package net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.browse

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.utils.text.BACKGROUND_EXTENDER
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.gui.CommonGuiWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.getMenuTitleName
import net.horizonsend.ion.server.gui.invui.input.TextInputMenu
import net.horizonsend.ion.server.gui.invui.input.validator.RangeIntegerValidator
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import java.util.function.Supplier

class PurchaseItemMenu(
	private val viewer: Player,
	private val item: BazaarItem,
	private val backButtonHandler: () -> Unit
) : CommonGuiWrapper {
	override fun openGui() {
		val itemStack = fromItemString(item.itemString)
		val listerName = SLPlayer.getName(item.seller)

		val extraLine = Supplier {
			GuiText("")
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
		}

		TextInputMenu(
			player = viewer,
			titleSupplier = extraLine,
			backButtonHandler = { backButtonHandler.invoke() },
			inputValidator = RangeIntegerValidator(1..item.stock),
			componentTransformer = { Component.text(it) },
			successfulInputHandler = menu@{ _, (_, result) ->
				val remote = Regions.get<RegionTerritory>(item.cityTerritory).contains(viewer.location)

				val futureResult = Bazaars.tryBuyFromSellOrder(viewer, item, result.result, remote)

				futureResult.withResult { buyResult ->
					if (!buyResult.isSuccess()) {
						buyResult.getReason()?.let(::updateLoreOverride)
						notifyWindows()
					} else {
						buyResult.sendReason(viewer)
						backButtonHandler.invoke()
					}
				}
			}
		).openGui()
	}
}
