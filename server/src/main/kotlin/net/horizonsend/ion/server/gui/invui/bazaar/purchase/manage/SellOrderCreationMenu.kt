package net.horizonsend.ion.server.gui.invui.bazaar.purchase.manage

import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import xyz.xenondevs.invui.window.Window

class SellOrderCreationMenu(viewer: Player) : InvUIWindowWrapper(viewer, async = true) {
	// TODO
	override fun buildWindow(): Window? {
		return null
	}

	override fun buildTitle(): Component {
		return GuiText("").build()
	}
}
