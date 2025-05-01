package net.horizonsend.ion.server.gui.invui

import xyz.xenondevs.invui.gui.Gui

interface InvUIGuiWrapper<G : Gui> {
	fun getGui(): G
}
