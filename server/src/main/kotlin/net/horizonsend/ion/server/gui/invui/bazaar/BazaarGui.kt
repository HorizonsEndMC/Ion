package net.horizonsend.ion.server.gui.invui.bazaar

import net.horizonsend.ion.server.features.gui.GuiText

interface BazaarGui {
	fun modifyGuiText(guiText: GuiText)
	fun refresh() {}
}
