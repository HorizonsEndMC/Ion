package net.horizonsend.ion.server.gui.invui.bazaar.purchase.gui

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.features.gui.custom.misc.anvilinput.TextInputMenu
import net.horizonsend.ion.server.features.gui.custom.misc.anvilinput.validator.IntegerValidator
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
			title = template(Component.text("Buying {0}'s {1}", HE_MEDIUM_GRAY), useQuotesAroundObjects = false, listerName,itemStack.displayName()),
			description = template(Component.text("Enter a value between {0} and {1}", HE_MEDIUM_GRAY), 1, item.stock),
			backButtonHandler = { backButtonHandler.invoke() },
			inputValidator = IntegerValidator,
			componentTransformer = { Component.text(it) },
			successfulInputHandler = { _, (_, result) -> println("result: $result") }
		).open()
	}
}
