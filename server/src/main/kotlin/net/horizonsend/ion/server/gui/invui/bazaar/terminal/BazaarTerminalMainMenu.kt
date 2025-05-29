package net.horizonsend.ion.server.gui.invui.bazaar.terminal

import net.horizonsend.ion.server.features.multiblock.type.economy.BazaarTerminalMultiblock
import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.window.Window

class BazaarTerminalMainMenu(viewer: Player, val terminalMultiblockEntity: BazaarTerminalMultiblock.BazaarTerminalMultiblockEntity) : InvUIWindowWrapper(viewer) {
	override fun buildWindow(): Window {
		val gui = Gui.normal()
			.setStructure(". . . . . . . . . ")
			.setStructure(". . . . . . . . . ")
			.setStructure(". . . . . . . . . ")
			.setStructure(". . . . . . . . . ")
			.build()

		return normalWindow(gui)
	}

	override fun buildTitle(): Component {
		return Component.text("Bazaar Terminal Main Menu")
	}
}
