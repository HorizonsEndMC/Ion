package net.horizonsend.ion.server.gui.invui.bazaar.purchase.gui

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.utils.text.BACKGROUND_EXTENDER
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.custom.misc.anvilinput.TextInputMenu
import net.horizonsend.ion.server.features.gui.custom.misc.anvilinput.validator.RangeIntegerValidator
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

class PurchaseItemMenu(
	private val viewer: Player,
	private val remote: Boolean,
	private val item: BazaarItem,
	private val backButtonHandler: () -> Unit
) {
	fun openMenu() {
		val itemStack = fromItemString(item.itemString)
		val listerName = SLPlayer.getName(item.seller)

		val background = GuiText.GuiBackground(
			backgroundChar = BACKGROUND_EXTENDER,
			verticalShift = -17
		)

		val extraLine = GuiText("")
			.addBackground(background)
			.add(template(message = Component.text("Buying {0}'s"), paramColor = null, useQuotesAroundObjects = false, listerName), line = -3, verticalShift = -1)
			.add(template(message = Component.text("{0}"), paramColor = null, useQuotesAroundObjects = false, itemStack.displayName()), line = -2, verticalShift = 0)
			.add(template(Component.text("Between {0} and {1}"), paramColor = null, 1, item.stock), line = -1, verticalShift = 1)
			.build()

		TextInputMenu(
			player = viewer,
			title = extraLine,
			description = Component.empty(),
			backButtonHandler = { backButtonHandler.invoke() },
			inputValidator = RangeIntegerValidator(1..item.stock),
			componentTransformer = { Component.text(it) },
			successfulInputHandler = { _, (_, result) ->
				Bazaars.tryBuy(viewer, item, result.result, remote)
			}
		).open()
	}
}
