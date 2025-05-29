package net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.manage

import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import xyz.xenondevs.invui.window.Window

class SellOrderCreationMenu(viewer: Player) : InvUIWindowWrapper(viewer, async = true) {
	override fun buildWindow(): Window? {
		return null
	}

	override fun buildTitle(): Component {
		return GuiText("").build()
	}
}
