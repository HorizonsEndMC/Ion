package net.horizonsend.ion.server.gui.invui.bazaar.orders.browse

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.economy.BazaarOrder
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import xyz.xenondevs.invui.window.Window

class BuyOrderFulfillmentMenu(viewer: Player, val item: Oid<BazaarOrder>) : InvUIWindowWrapper(viewer, async = true) {
	override fun buildWindow(): Window? {
		return null
	}

	override fun buildTitle(): Component {
		return GuiText("").build()
	}
}
