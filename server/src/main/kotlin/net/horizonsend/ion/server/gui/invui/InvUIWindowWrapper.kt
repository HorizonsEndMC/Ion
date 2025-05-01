package net.horizonsend.ion.server.gui.invui

import net.horizonsend.ion.server.gui.CommonGuiWrapper
import org.bukkit.entity.Player
import xyz.xenondevs.invui.window.Window

abstract class InvUIWindowWrapper(val viewer: Player) : CommonGuiWrapper {
	protected var currentWindow: Window? = null

	abstract fun buildWindow(): Window

	override fun openGui() {
		currentWindow = buildWindow()
		currentWindow?.open()
	}

	fun getOpenWindow() = currentWindow
}
