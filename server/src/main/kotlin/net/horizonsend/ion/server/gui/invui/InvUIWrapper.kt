package net.horizonsend.ion.server.gui.invui

import net.horizonsend.ion.server.gui.CommonGuiWrapper
import org.bukkit.entity.Player
import xyz.xenondevs.invui.window.Window

interface InvUIWrapper : CommonGuiWrapper {
	val viewer: Player

	fun buildWindow(): Window

	override fun openGui() {
		buildWindow().open()
	}
}
