package net.horizonsend.ion.server.gui.invui.bazaar

import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.BazaarPurchaseMenuParent

interface BazaarGui {
	val parent: BazaarPurchaseMenuParent
	fun modifyGuiText(guiText: GuiText)
	fun refresh() {}
}
