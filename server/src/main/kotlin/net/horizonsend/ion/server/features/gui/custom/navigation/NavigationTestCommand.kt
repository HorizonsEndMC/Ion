package net.horizonsend.ion.server.features.gui.custom.navigation

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.server.command.SLCommand
import org.bukkit.entity.Player
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.window.Window

@CommandAlias("navtest")
object NavigationTestCommand : SLCommand() {
	@Default
	@Suppress("unused")
	fun onTest(sender: Player) {
		NavigationSystemMapGui.openWindow(sender, sender.world)
	}
}
