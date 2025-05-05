package net.horizonsend.ion.server.gui.invui

import net.horizonsend.ion.server.gui.CommonGuiWrapper
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.entity.Player
import xyz.xenondevs.invui.window.Window

abstract class InvUIWindowWrapper(val viewer: Player, val async: Boolean = false) : CommonGuiWrapper {
	protected var currentWindow: Window? = null

	abstract fun buildWindow(): Window

	override fun openGui() {
		if (async) {
			Tasks.async {
				currentWindow = buildWindow()
				Tasks.sync { currentWindow?.open() }
			}
		} else {
			Tasks.sync {
				currentWindow = buildWindow()
				currentWindow?.open()
			}
		}
	}

	fun getOpenWindow() = currentWindow
}
