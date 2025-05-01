package net.horizonsend.ion.server.gui.invui.bazaar.purchase

import net.horizonsend.ion.server.features.gui.GuiText

interface BazaarGui {
	val parent: BazaarPurchaseMenuParent
	fun modifyGuiText(guiText: GuiText)
}
