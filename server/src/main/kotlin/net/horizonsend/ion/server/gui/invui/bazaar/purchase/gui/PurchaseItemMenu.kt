package net.horizonsend.ion.server.gui.invui.bazaar.purchase.gui

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
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

		TextInputMenu(
			player = viewer,
			title = template(message = Component.text("Buying {0}'s {1}"), paramColor = null, useQuotesAroundObjects = false, listerName, itemStack.displayName()),
			description = template(Component.text("Enter a value between {0} and {1}"), paramColor = null, 1, item.stock),
			backButtonHandler = { backButtonHandler.invoke() },
			inputValidator = RangeIntegerValidator(1..item.stock),
			componentTransformer = { Component.text(it) },
			successfulInputHandler = { _, (_, result) ->
				println("click result: $result")
			}
		).open()
	}
}
